package com.foxdebug.webview;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class WebViewActivity extends Activity {
    private WebView webView;
    private String webviewId;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.webviewId = getIntent().getStringExtra("webviewId");
        WebViewPlugin webViewPlugin = WebViewPlugin.getInstance();
        WebViewInstance webViewPlugin2 = webViewPlugin != null ? webViewPlugin.getInstance(this.webviewId) : null;
        if (webViewPlugin2 == null) {
            finish();
            return;
        }
        webViewPlugin2.createWebView(this);
        WebView webView = webViewPlugin2.getWebView();
        this.webView = webView;
        if (webView == null) {
            finish();
            return;
        }
        webViewPlugin2.setHostingActivity(this);
        String title = webViewPlugin2.getTitle();
        if (title != null && !title.isEmpty()) {
            setTitle(title);
        }
        if (this.webView.getParent() != null) {
            ((ViewGroup) this.webView.getParent()).removeView(this.webView);
        }
        FrameLayout frameLayout = new FrameLayout(this);
        WebViewInstance.applySystemBarInsets(frameLayout);
        frameLayout.addView(this.webView, new FrameLayout.LayoutParams(-1, -1));
        setContentView(frameLayout);
        if (Build.VERSION.SDK_INT >= 30) {
            getWindow().setDecorFitsSystemWindows(false);
        }
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        WebView webView = this.webView;
        if (webView != null && webView.canGoBack()) {
            this.webView.goBack();
        } else {
            finish();
        }
    }

    @Override // android.app.Activity
    protected void onDestroy() {
        String str;
        super.onDestroy();
        WebViewPlugin webViewPlugin = WebViewPlugin.getInstance();
        if (webViewPlugin != null && (str = this.webviewId) != null) {
            WebViewInstance webViewPlugin2 = webViewPlugin.getInstance(str);
            if (webViewPlugin2 != null) {
                webViewPlugin2.clearHostingActivity(this);
                webViewPlugin2.destroy();
                webViewPlugin.removeInstance(this.webviewId);
            }
            webViewPlugin.sendEventToCordova(this.webviewId, "closed", null);
        }
        this.webView = null;
    }
}