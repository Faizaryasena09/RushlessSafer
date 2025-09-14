// tes.js

function openRushlessApp() {
    // The target URL you want to open in the app's WebView
    const targetUrl = "https://www.google.com"; // Example URL

    // The session token or unique ID from your web application
    const sessionToken = "user_session_abc123xyz"; // Example token

    // Encode the parameters to make them URL-safe
    const encodedUrl = encodeURIComponent(targetUrl);
    const encodedToken = encodeURIComponent(sessionToken);

    // Construct the deep link
    // Format: rushless://open?url=<ENCODED_URL>&token=<ENCODED_TOKEN>
    const deepLink = `rushless://open?url=${encodedUrl}&token=${encodedToken}`;

    // Log to console for debugging
    console.log("Redirecting to:", deepLink);

    // Redirect the browser to the deep link, which will trigger the app to open
    window.location.href = deepLink;
}

// You can create a button in your HTML to call this function, for example:
// <button onclick="openRushlessApp()">Open in Rushless Safer App</button>
