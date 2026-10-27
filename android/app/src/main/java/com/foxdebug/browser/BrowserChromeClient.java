package com.foxdebug.browser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

/* JADX INFO: compiled from: Browser.java */
/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
class BrowserChromeClient extends WebChromeClient {
    Browser browser;

    public BrowserChromeClient(Browser browser) {
        this.browser = browser;
    }

    @Override // android.webkit.WebChromeClient
    public void onReceivedTitle(WebView webView, String str) {
        super.onReceivedTitle(webView, str);
        this.browser.setTitle(str);
    }

    @Override // android.webkit.WebChromeClient
    public void onReceivedIcon(WebView webView, Bitmap bitmap) {
        super.onReceivedIcon(webView, bitmap);
        this.browser.setFavicon(bitmap);
    }

    @Override // android.webkit.WebChromeClient
    public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> valueCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        String str;
        if (this.browser.filePathCallback != null) {
            this.browser.filePathCallback.onReceiveValue(null);
        }
        this.browser.filePathCallback = valueCallback;
        String[] acceptTypes = fileChooserParams.getAcceptTypes();
        if (acceptTypes == null || acceptTypes.length <= 0 || (str = acceptTypes[0]) == null || str.trim().isEmpty()) {
            str = "*/*";
        }
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType(str);
        if (fileChooserParams.getMode() == 1) {
            intent.putExtra("android.intent.extra.ALLOW_MULTIPLE", true);
        }
        ((Activity) webView.getContext()).startActivityForResult(Intent.createChooser(intent, "Select File"), this.browser.FILE_SELECT_CODE);
        return true;
    }
}