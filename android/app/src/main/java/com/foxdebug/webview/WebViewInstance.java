package com.foxdebug.webview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import androidx.webkit.ProxyConfig;
import com.sshtools.common.sftp.SftpFileAttributes;
import fi.iki.elonen.NanoHTTPD;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class WebViewInstance {
    private static final String BRIDGE_JS = "(function(){if(window.webview&&window.webview.__acodeBridge){return;}var callbacks=[];window.webview={__acodeBridge:true,onMessage:function(cb){if(typeof cb==='function'){callbacks.push(cb);}},offMessage:function(cb){callbacks=callbacks.filter(function(c){return c!==cb;});},postMessage:function(msg){var data=(typeof msg==='string')?msg:JSON.stringify(msg);window.AcodeWebViewNative.postMessage(String(data));},_dispatch:function(msg){callbacks.slice().forEach(function(cb){try{cb(msg);}catch(e){console.error(e);}});}};})();";
    private static final Pattern SCHEME_PATTERN = Pattern.compile("^([a-zA-Z][a-zA-Z0-9+\\-.]*)://");
    private static final String TAG = "WebViewInstance";
    final boolean allowDownloads;
    final boolean allowNavigation;
    final String id;
    final String mode;
    final WebViewPlugin plugin;
    final String title;
    private WebView webView;
    private WebViewActivity hostingActivity = null;
    private boolean isDestroyed = false;
    private String pendingUrl = null;
    private String pendingHtml = null;

    WebViewInstance(String str, String str2, String str3, boolean z, boolean z2, WebViewPlugin webViewPlugin) {
        this.id = str;
        this.mode = str2;
        this.title = str3;
        this.allowNavigation = z;
        this.allowDownloads = z2;
        this.plugin = webViewPlugin;
    }

    public WebView getWebView() {
        return this.webView;
    }

    String getTitle() {
        return this.title;
    }

    boolean isFullscreen() {
        return "fullscreen".equals(this.mode);
    }

    WebViewActivity getHostingActivity() {
        return this.hostingActivity;
    }

    void setHostingActivity(WebViewActivity webViewActivity) {
        this.hostingActivity = webViewActivity;
    }

    void clearHostingActivity(WebViewActivity webViewActivity) {
        if (this.hostingActivity == webViewActivity) {
            this.hostingActivity = null;
        }
    }

    void createWebView(Activity activity) {
        if (this.webView != null) {
            return;
        }
        WebView webView = new WebView(activity);
        this.webView = webView;
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        setFileUrlAccessFlags(settings);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        this.webView.setWebViewClient(new InstanceWebViewClient());
        this.webView.setWebChromeClient(new InstanceWebChromeClient());
        this.webView.setFocusable(true);
        this.webView.setFocusableInTouchMode(true);
        this.webView.addJavascriptInterface(new JsBridge(), "AcodeWebViewNative");
        if (this.allowDownloads) {
            this.webView.setDownloadListener(new InstanceDownloadListener(activity));
        }
        injectBridge(this.webView);
        String str = this.pendingUrl;
        if (str != null) {
            this.webView.loadUrl(str);
            this.pendingUrl = null;
            this.pendingHtml = null;
        } else {
            String str2 = this.pendingHtml;
            if (str2 != null) {
                this.webView.loadDataWithBaseURL(null, str2, NanoHTTPD.MIME_HTML, "UTF-8", null);
                this.pendingHtml = null;
            }
        }
    }

    private static void setFileUrlAccessFlags(WebSettings webSettings) {
        webSettings.setAllowFileAccessFromFileURLs(false);
        webSettings.setAllowUniversalAccessFromFileURLs(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void injectBridge(WebView webView) {
        webView.evaluateJavascript(BRIDGE_JS, null);
    }

    static void applySystemBarInsets(View view) {
        view.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: com.foxdebug.webview.WebViewInstance.1
            @Override // android.view.View.OnApplyWindowInsetsListener
            public WindowInsets onApplyWindowInsets(View view2, WindowInsets windowInsets) {
                int systemWindowInsetLeft;
                int systemWindowInsetTop;
                int systemWindowInsetRight;
                int systemWindowInsetBottom;
                if (Build.VERSION.SDK_INT >= 30) {
                    Insets insets = windowInsets.getInsets(WindowInsets.Type.systemBars());
                    systemWindowInsetLeft = insets.left;
                    systemWindowInsetTop = insets.top;
                    systemWindowInsetRight = insets.right;
                    systemWindowInsetBottom = insets.bottom;
                } else {
                    systemWindowInsetLeft = windowInsets.getSystemWindowInsetLeft();
                    systemWindowInsetTop = windowInsets.getSystemWindowInsetTop();
                    systemWindowInsetRight = windowInsets.getSystemWindowInsetRight();
                    systemWindowInsetBottom = windowInsets.getSystemWindowInsetBottom();
                }
                view2.setPadding(systemWindowInsetLeft, systemWindowInsetTop, systemWindowInsetRight, systemWindowInsetBottom);
                return windowInsets;
            }
        });
    }

    private static String sanitizeUrl(String str) {
        if (str == null) {
            return null;
        }
        String strTrim = str.trim();
        if (strTrim.isEmpty()) {
            return null;
        }
        Matcher matcher = SCHEME_PATTERN.matcher(strTrim);
        if (matcher.find()) {
            String strGroup = matcher.group(1);
            if (strGroup.equalsIgnoreCase(ProxyConfig.MATCH_HTTP) || strGroup.equalsIgnoreCase(ProxyConfig.MATCH_HTTPS)) {
                return strTrim;
            }
            return null;
        }
        return "https://" + strTrim;
    }

    void loadURL(String str, final CallbackContext callbackContext) {
        if (this.isDestroyed) {
            callbackContext.error("WebView has been destroyed");
            return;
        }
        final String strSanitizeUrl = sanitizeUrl(str);
        if (strSanitizeUrl == null) {
            callbackContext.error("Blocked URL: only http:// and https:// URLs are allowed");
        } else {
            if (this.webView == null) {
                this.pendingUrl = strSanitizeUrl;
                this.pendingHtml = null;
                callbackContext.success();
                return;
            }
            runOnUiThread(new Runnable() { // from class: com.foxdebug.webview.WebViewInstance.2
                @Override // java.lang.Runnable
                public void run() {
                    WebViewInstance.this.webView.loadUrl(strSanitizeUrl);
                    callbackContext.success();
                }
            });
        }
    }

    void loadHTML(final String str, final CallbackContext callbackContext) {
        if (this.isDestroyed) {
            callbackContext.error("WebView has been destroyed");
        } else {
            if (this.webView == null) {
                this.pendingHtml = str;
                this.pendingUrl = null;
                callbackContext.success();
                return;
            }
            runOnUiThread(new Runnable() { // from class: com.foxdebug.webview.WebViewInstance.3
                @Override // java.lang.Runnable
                public void run() {
                    WebViewInstance.this.webView.loadDataWithBaseURL(null, str, NanoHTTPD.MIME_HTML, "UTF-8", null);
                    callbackContext.success();
                }
            });
        }
    }

    void evaluate(final String str, final CallbackContext callbackContext) {
        if (this.isDestroyed) {
            callbackContext.error("WebView has been destroyed");
        } else if (this.webView == null) {
            callbackContext.error("WebView is not ready");
        } else {
            runOnUiThread(new Runnable() { // from class: com.foxdebug.webview.WebViewInstance.4
                @Override // java.lang.Runnable
                public void run() {
                    WebViewInstance.this.webView.evaluateJavascript(str, new ValueCallback<String>() { // from class: com.foxdebug.webview.WebViewInstance.4.1
                        @Override // android.webkit.ValueCallback
                        public void onReceiveValue(String str2) {
                            callbackContext.success(WebViewInstance.decodeJsResult(str2));
                        }
                    });
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String decodeJsResult(String str) {
        if (str == null) {
            return null;
        }
        try {
            Object objNextValue = new JSONTokener(str).nextValue();
            if (objNextValue == JSONObject.NULL) {
                return null;
            }
            return String.valueOf(objNextValue);
        } catch (JSONException unused) {
            return str;
        }
    }

    void postMessage(String str, final CallbackContext callbackContext) {
        if (this.isDestroyed) {
            callbackContext.error("WebView has been destroyed");
        } else if (this.webView == null) {
            callbackContext.error("WebView is not ready");
        } else {
            final String str2 = "(function(){var raw=" + JSONObject.quote(str) + ";var msg;try{msg=JSON.parse(raw);}catch(e){msg=raw;}if(window.webview&&window.webview._dispatch){window.webview._dispatch(msg);}})();";
            runOnUiThread(new Runnable() { // from class: com.foxdebug.webview.WebViewInstance.5
                @Override // java.lang.Runnable
                public void run() {
                    WebViewInstance.this.webView.evaluateJavascript(str2, null);
                    callbackContext.success();
                }
            });
        }
    }

    void show(CallbackContext callbackContext) {
        if (this.isDestroyed) {
            callbackContext.error("WebView has been destroyed");
        } else if (!isFullscreen()) {
            callbackContext.error("Hidden WebViews cannot be shown; use mode \"fullscreen\" to display content");
        } else {
            runOnUiThread(new Runnable() { // from class: com.foxdebug.webview.WebViewInstance.6
                @Override // java.lang.Runnable
                public void run() {
                    WebViewInstance.this.plugin.showFullscreenActivity(WebViewInstance.this.id);
                }
            });
            callbackContext.success();
        }
    }

    void hide(CallbackContext callbackContext) {
        if (this.isDestroyed) {
            callbackContext.error("WebView has been destroyed");
        } else if (!isFullscreen()) {
            callbackContext.success();
        } else {
            hideFullscreen(callbackContext);
        }
    }

    private void hideFullscreen(CallbackContext callbackContext) {
        if (this.isDestroyed) {
            callbackContext.error("WebView has been destroyed");
        } else {
            runOnUiThread(new Runnable() { // from class: com.foxdebug.webview.WebViewInstance.7
                @Override // java.lang.Runnable
                public void run() {
                    if (WebViewInstance.this.hostingActivity == null || WebViewInstance.this.hostingActivity.isFinishing() || WebViewInstance.this.hostingActivity.isDestroyed()) {
                        return;
                    }
                    WebViewInstance.this.hostingActivity.moveTaskToBack(true);
                }
            });
            callbackContext.success();
        }
    }

    void reload(final CallbackContext callbackContext) {
        if (this.isDestroyed) {
            callbackContext.error("WebView has been destroyed");
        } else if (this.webView == null) {
            callbackContext.error("WebView is not ready");
        } else {
            runOnUiThread(new Runnable() { // from class: com.foxdebug.webview.WebViewInstance.8
                @Override // java.lang.Runnable
                public void run() {
                    WebViewInstance.this.webView.reload();
                    callbackContext.success();
                }
            });
        }
    }

    void destroy() {
        if (this.isDestroyed) {
            return;
        }
        this.isDestroyed = true;
        WebViewActivity webViewActivity = this.hostingActivity;
        this.hostingActivity = null;
        if (webViewActivity != null && !webViewActivity.isFinishing()) {
            webViewActivity.finish();
        }
        runOnUiThread(new Runnable() { // from class: com.foxdebug.webview.WebViewInstance.9
            @Override // java.lang.Runnable
            public void run() {
                if (WebViewInstance.this.webView != null) {
                    if (WebViewInstance.this.webView.getParent() != null) {
                        ((ViewGroup) WebViewInstance.this.webView.getParent()).removeView(WebViewInstance.this.webView);
                    }
                    WebViewInstance.this.webView.removeJavascriptInterface("AcodeWebViewNative");
                    WebViewInstance.this.webView.setDownloadListener(null);
                    WebViewInstance.this.webView.setWebChromeClient(null);
                    WebViewInstance.this.webView.setWebViewClient(null);
                    WebViewInstance.this.webView.loadUrl("about:blank");
                    WebViewInstance.this.webView.destroy();
                }
                WebViewInstance.this.webView = null;
            }
        });
    }

    void onPageFinished(WebView webView) {
        injectBridge(webView);
        try {
            JSONObject jSONObject = new JSONObject();
            String url = webView.getUrl();
            String title = webView.getTitle();
            if (url == null) {
                url = "";
            }
            jSONObject.put("url", url);
            if (title == null) {
                title = "";
            }
            jSONObject.put("title", title);
            this.plugin.sendEventToCordova(this.id, "pageFinished", jSONObject);
        } catch (JSONException e) {
            Log.e(TAG, "onPageFinished error", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static void runOnUiThread(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    private class InstanceWebViewClient extends WebViewClient {
        private InstanceWebViewClient() {
        }

        @Override // android.webkit.WebViewClient
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
            return shouldBlockNavigation(webResourceRequest.getUrl());
        }

        @Override // android.webkit.WebViewClient
        public boolean shouldOverrideUrlLoading(WebView webView, String str) {
            return shouldBlockNavigation(Uri.parse(str));
        }

        private boolean shouldBlockNavigation(Uri uri) {
            String scheme;
            return (WebViewInstance.this.allowNavigation && (scheme = uri.getScheme()) != null && (scheme.equalsIgnoreCase(ProxyConfig.MATCH_HTTP) || scheme.equalsIgnoreCase(ProxyConfig.MATCH_HTTPS))) ? false : true;
        }

        @Override // android.webkit.WebViewClient
        public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
            super.onPageStarted(webView, str, bitmap);
            WebViewInstance.injectBridge(webView);
        }

        @Override // android.webkit.WebViewClient
        public void onPageFinished(WebView webView, String str) {
            super.onPageFinished(webView, str);
            WebViewInstance.this.onPageFinished(webView);
        }
    }

    private class InstanceWebChromeClient extends WebChromeClient {
        private InstanceWebChromeClient() {
        }

        @Override // android.webkit.WebChromeClient
        public void onReceivedTitle(WebView webView, String str) {
            super.onReceivedTitle(webView, str);
            try {
                JSONObject jSONObject = new JSONObject();
                if (str == null) {
                    str = "";
                }
                jSONObject.put("title", str);
                WebViewInstance.this.plugin.sendEventToCordova(WebViewInstance.this.id, "titleChanged", jSONObject);
            } catch (JSONException e) {
                Log.e(WebViewInstance.TAG, "onReceivedTitle error", e);
            }
        }
    }

    private class InstanceDownloadListener implements DownloadListener {
        private final Context context;

        InstanceDownloadListener(Context context) {
            this.context = context;
        }

        @Override // android.webkit.DownloadListener
        public void onDownloadStart(final String str, final String str2, String str3, final String str4, long j) {
            String str5;
            if (WebViewInstance.this.isDestroyed) {
                return;
            }
            Context context = this.context;
            if ((context instanceof Activity) && ((Activity) context).isFinishing()) {
                return;
            }
            String scheme = Uri.parse(str).getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase(ProxyConfig.MATCH_HTTP) && !scheme.equalsIgnoreCase(ProxyConfig.MATCH_HTTPS))) {
                WebViewInstance.runOnUiThread(new Runnable() { // from class: com.foxdebug.webview.WebViewInstance.InstanceDownloadListener.1
                    @Override // java.lang.Runnable
                    public void run() {
                        Toast.makeText(InstanceDownloadListener.this.context, "This download type is not supported", 0).show();
                    }
                });
                return;
            }
            final String strGuessFileName = URLUtil.guessFileName(str, str3, str4);
            String size = WebViewInstance.formatSize(j);
            if (size.isEmpty()) {
                str5 = "Do you want to download \"" + strGuessFileName + "\"?";
            } else {
                str5 = "Do you want to download \"" + strGuessFileName + "\" (" + size + ")?";
            }
            final String str6 = str5;
            WebViewInstance.runOnUiThread(new Runnable() { // from class: com.foxdebug.webview.WebViewInstance.InstanceDownloadListener.2
                @Override // java.lang.Runnable
                public void run() {
                    new AlertDialog.Builder(InstanceDownloadListener.this.context).setTitle("Download file").setMessage(str6).setPositiveButton("Download", new DialogInterface.OnClickListener() { // from class: com.foxdebug.webview.WebViewInstance.InstanceDownloadListener.2.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            WebViewInstance.this.plugin.download(str, str2, str4, strGuessFileName);
                        }
                    }).setNegativeButton("Cancel", (DialogInterface.OnClickListener) null).show();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String formatSize(long j) {
        if (j <= 0) {
            return "";
        }
        if (j < SftpFileAttributes.SSH_FILEXFER_ATTR_ALLOCATION_SIZE) {
            return j + " B";
        }
        if (j < 1048576) {
            return String.format(Locale.US, "%.1f KB", Double.valueOf(j / 1024.0d));
        }
        if (j < 1073741824) {
            return String.format(Locale.US, "%.1f MB", Double.valueOf(j / 1048576.0d));
        }
        return String.format(Locale.US, "%.1f GB", Double.valueOf(j / 1.073741824E9d));
    }

    public class JsBridge {
        public JsBridge() {
        }

        @JavascriptInterface
        public void postMessage(String str) {
            WebViewInstance.this.plugin.sendMessageToCordova(WebViewInstance.this.id, str);
        }
    }
}