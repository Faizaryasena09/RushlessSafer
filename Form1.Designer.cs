namespace RushlessSafer
{
    partial class LockdownForm
    {
        private System.ComponentModel.IContainer components = null;
        private Microsoft.Web.WebView2.WinForms.WebView2 webView;
        private System.Windows.Forms.Timer batteryTimer;
        private System.Windows.Forms.Label lblBattery;
        private System.Windows.Forms.Button btnWifi;

        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        private void InitializeComponent()
        {
            this.components = new System.ComponentModel.Container();
            this.webView = new Microsoft.Web.WebView2.WinForms.WebView2();
            this.batteryTimer = new System.Windows.Forms.Timer(this.components);
            this.lblBattery = new System.Windows.Forms.Label();
            this.btnWifi = new System.Windows.Forms.Button();
            ((System.ComponentModel.ISupportInitialize)(this.webView)).BeginInit();
            this.SuspendLayout();
            // 
            // webView
            // 
            this.webView.AllowExternalDrop = true;
            this.webView.CreationProperties = null;
            this.webView.DefaultBackgroundColor = System.Drawing.Color.White;
            this.webView.Dock = System.Windows.Forms.DockStyle.Fill;
            this.webView.Location = new System.Drawing.Point(0, 0);
            this.webView.Name = "webView";
            this.webView.Size = new System.Drawing.Size(800, 450);
            this.webView.TabIndex = 0;
            this.webView.ZoomFactor = 1D;
            // 
            // batteryTimer
            // 
            this.batteryTimer.Interval = 5000;
            this.batteryTimer.Tick += new System.EventHandler(this.batteryTimer_Tick);
            // 
            // lblBattery
            // 
            this.lblBattery.Name = "lblBattery";
            this.lblBattery.Size = new System.Drawing.Size(108, 30);
            this.lblBattery.TabIndex = 1;
            this.lblBattery.Text = "Baterai: 100%";
            // 
            // btnWifi
            // 
            this.btnWifi.Name = "btnWifi";
            this.btnWifi.Size = new System.Drawing.Size(130, 30);
            this.btnWifi.TabIndex = 2;
            this.btnWifi.Text = "Ganti Wi-Fi";
            this.btnWifi.UseVisualStyleBackColor = false;
            this.btnWifi.Click += new System.EventHandler(this.btnWifi_Click);
            // 
            // LockdownForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 20F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(800, 450);
            this.Controls.Add(this.webView);
            this.Name = "LockdownForm";
            this.Text = "Rushless Safer";
            this.Load += new System.EventHandler(this.LockdownForm_Load);
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.LockdownForm_FormClosing);
            ((System.ComponentModel.ISupportInitialize)(this.webView)).EndInit();
            this.ResumeLayout(false);
            this.PerformLayout();
        }
    }
}