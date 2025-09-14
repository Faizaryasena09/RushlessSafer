using Microsoft.Web.WebView2.Core;
using System.Diagnostics;
using System.Text.Json;
using System.Windows.Forms;

using Microsoft.Web.WebView2.WinForms;

namespace RushlessSafer
{
    public partial class LockdownForm : Form
    {
        private readonly string _initialUrl;
        private readonly string _cookies;
        private KeyboardHook _keyboardHook;

        public LockdownForm(string url, string cookies)
        {
            _initialUrl = url;
            _cookies = cookies;
            InitializeComponent();
            InitializeSecurityFeatures();
            InitializeWebView();
            InitializeFooter(); // Add this call
        }

        private async void InitializeFooter()
        {
            // Create footer panel
            Panel footerPanel = new Panel();
            footerPanel.BackColor = Color.FromArgb(240, 240, 240);
            footerPanel.Dock = DockStyle.Bottom;
            footerPanel.Height = 40;
            this.Controls.Add(footerPanel);

            // Create logo WebView
            var logoWebView = new WebView2();
            logoWebView.Dock = DockStyle.Left;
            logoWebView.Width = 100;
            logoWebView.DefaultBackgroundColor = Color.Transparent;
            footerPanel.Controls.Add(logoWebView);
            await logoWebView.EnsureCoreWebView2Async(null);

            // Create a stylized 'R' SVG logo
            string svgLogo = """
                <svg width=\"100\" height=\"40\" viewBox=\"0 0 100 40\" xmlns=\"http://www.w3.org/2000/svg\">
                    <style>
                        path {
                            stroke: black;
                            stroke-width: 5;
                            fill: none;
                            stroke-linecap: round;
                            stroke-linejoin: round;
                        }
                    </style>
                    <path d=\"M 20 35 V 5 h 20 a 15 15 0 0 1 0 30 H 20 L 45 35\"/>
                </svg>
            """;

            logoWebView.NavigateToString(svgLogo);


            // Move existing controls to footer
            this.Controls.Remove(lblBattery);
            this.Controls.Remove(btnWifi);

            // Add to footer and adjust properties
            lblBattery.Dock = DockStyle.Right;
            lblBattery.AutoSize = false; // Set to false to manually control size
            lblBattery.TextAlign = ContentAlignment.MiddleCenter;
            lblBattery.BackColor = Color.Transparent;
            lblBattery.ForeColor = Color.Black;
            lblBattery.Font = new Font("Segoe UI", 9F, FontStyle.Regular, GraphicsUnit.Point);
            lblBattery.Padding = new Padding(5);


            btnWifi.Dock = DockStyle.Right;
            btnWifi.BackColor = Color.Transparent;
            btnWifi.FlatAppearance.BorderSize = 0;
            btnWifi.FlatStyle = FlatStyle.Flat;
            btnWifi.ForeColor = Color.Black;
            btnWifi.Font = new Font("Segoe UI", 9F, FontStyle.Regular, GraphicsUnit.Point);
            btnWifi.Width = 100;


            footerPanel.Controls.Add(btnWifi);
            footerPanel.Controls.Add(lblBattery);


            // Adjust WebView to not overlap with the footer
            webView.Dock = DockStyle.None;
            webView.Anchor = AnchorStyles.Top | AnchorStyles.Bottom | AnchorStyles.Left | AnchorStyles.Right;
            webView.Height = this.ClientSize.Height - footerPanel.Height;
            webView.Width = this.ClientSize.Width;

            this.Resize += (sender, e) => {
                webView.Height = this.ClientSize.Height - footerPanel.Height;
                webView.Width = this.ClientSize.Width;
            };
        }



        private void InitializeSecurityFeatures()
        {
            // 1. Install low-level keyboard hook
            _keyboardHook = new KeyboardHook();

            // 2. Bring form to front and make fullscreen
            this.FormBorderStyle = FormBorderStyle.None;
            this.WindowState = FormWindowState.Maximized;
            this.TopMost = true;
        }

        private async void InitializeWebView()
        {
            await webView.EnsureCoreWebView2Async(null);

            // Handle messages from web content
            webView.CoreWebView2.WebMessageReceived += HandleWebMessage;

            // Set cookies
            var cookieManager = webView.CoreWebView2.CookieManager;
            var domain = new Uri(_initialUrl).Host;
            if (!string.IsNullOrEmpty(_cookies))
            {
                var cookiePairs = _cookies.Split(';');
                foreach (var cookiePair in cookiePairs)
                {
                    var parts = cookiePair.Trim().Split('=');
                    if (parts.Length == 2)
                    {
                        var cookie = cookieManager.CreateCookie(parts[0], parts[1], domain, "/");
                        cookieManager.AddOrUpdateCookie(cookie);
                    }
                }
            }

            // Navigate to the target URL
            webView.CoreWebView2.Navigate(_initialUrl);
        }

        private bool _isExiting = false;

        private void CleanupAndExit()
        {
            if (_isExiting) return;
            _isExiting = true;

            // Restart Explorer Shell
            Process.Start("explorer.exe");

            // Dispose keyboard hook to re-enable normal keyboard function
            _keyboardHook.Dispose();

            // Use Application.Exit() for a graceful shutdown
            Application.Exit();
        }

        private void HandleWebMessage(object? sender, CoreWebView2WebMessageReceivedEventArgs args)
        {
            var jsonMessage = args.WebMessageAsJson;
            if (string.IsNullOrEmpty(jsonMessage)) return;

            try
            {
                var message = JsonDocument.Parse(jsonMessage);
                var root = message.RootElement;
                if (root.TryGetProperty("type", out var typeElement))
                {
                    string type = typeElement.GetString() ?? "";
                    if (string.Equals(type, "unlock", StringComparison.OrdinalIgnoreCase))
                    {
                        // Use Invoke to ensure cleanup runs on the UI thread
                        this.Invoke(new Action(CleanupAndExit));
                    }
                    else if (string.Equals(type, "redirect", StringComparison.OrdinalIgnoreCase))
                    {
                        if (root.TryGetProperty("url", out var urlElement))
                        {
                            string redirectUrl = urlElement.GetString() ?? "";
                            if (!string.IsNullOrEmpty(redirectUrl))
                            {
                                Process.Start(new ProcessStartInfo(redirectUrl) { UseShellExecute = true });
                                this.Invoke(new Action(CleanupAndExit));
                            }
                        }
                    }
                }
            }
            catch (JsonException) { /* Ignore invalid JSON */ }
        }

        private void LockdownForm_Load(object sender, EventArgs e)
        {
            // Kill Explorer Shell
            Process.Start(new ProcessStartInfo("taskkill", "/f /im explorer.exe") { CreateNoWindow = true });

            // Start battery timer
            batteryTimer.Start();
            batteryTimer_Tick(sender, e); // Initial tick
        }

        private void LockdownForm_FormClosing(object sender, FormClosingEventArgs e)
        {
            CleanupAndExit();
        }

        private void batteryTimer_Tick(object sender, EventArgs e)
        { 
            var powerStatus = SystemInformation.PowerStatus;
            lblBattery.Visible = powerStatus.BatteryChargeStatus != BatteryChargeStatus.NoSystemBattery;
            if (!lblBattery.Visible) return;

            string status = "";
            switch (powerStatus.PowerLineStatus)
            {
                case PowerLineStatus.Online:
                    status = "(Mengisi Daya)";
                    break;
                case PowerLineStatus.Offline:
                    status = "";
                    break;
                case PowerLineStatus.Unknown:
                    status = "(Status Tidak Diketahui)";
                    break;
            }
            float percent = powerStatus.BatteryLifePercent * 100;
            lblBattery.Text = $"Baterai: {percent:F0}% {status}";
        }

        private void btnWifi_Click(object sender, EventArgs e)
        {
            // Open Windows' available networks flyout
            Process.Start(new ProcessStartInfo("explorer.exe", "ms-availablenetworks:") { UseShellExecute = true });
        }
    }
}