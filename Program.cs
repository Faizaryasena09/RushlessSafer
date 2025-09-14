
using System.Web;

namespace RushlessSafer
{
    internal static class Program
    {
        [STAThread]
        static void Main(string[] args)
        {
            ApplicationConfiguration.Initialize();

            string targetUrl = "https://google.com"; // Default URL
            string cookies = "";

            if (args.Length > 0)
            {
                // Argument comes in as "rushless-safer:?url=...&cookies=..."
                var fullArg = args[0];
                var uri = new Uri(fullArg);
                var query = HttpUtility.ParseQueryString(uri.Query);
                
                if (query.Get("url") is string urlValue)
                {
                    targetUrl = urlValue;
                }
                if (query.Get("cookies") is string cookieValue)
                {
                    cookies = cookieValue;
                }
            }

            Application.Run(new LockdownForm(targetUrl, cookies));
        }
    }
}
