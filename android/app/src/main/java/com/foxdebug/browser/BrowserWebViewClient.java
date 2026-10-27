package com.foxdebug.browser;

import android.graphics.Bitmap;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/* JADX INFO: compiled from: Browser.java */
/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
class BrowserWebViewClient extends WebViewClient {
    private Browser browser;

    public BrowserWebViewClient(Browser browser) {
        this.browser = browser;
    }

    @Override // android.webkit.WebViewClient
    public boolean shouldOverrideUrlLoading(WebView webView, String str) {
        this.browser.setUrl(str);
        this.browser.setProgressBarVisible(true);
        return false;
    }

    @Override // android.webkit.WebViewClient
    public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
        super.onPageStarted(webView, str, bitmap);
        this.browser.setProgressBarVisible(true);
    }

    @Override // android.webkit.WebViewClient
    public void onPageFinished(WebView webView, String str) {
        super.onPageFinished(webView, str);
        this.browser.setProgressBarVisible(false);
        if (!str.startsWith("http://localhost")) {
            try {
                File file = new File(this.browser.context.getFilesDir(), "eruda.js");
                StringBuilder sb = new StringBuilder();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                    sb.append("\n");
                }
                bufferedReader.close();
                this.browser.webView.evaluateJavascript("if(!window.eruda){  var script = document.createElement('script');  script.textContent = `" + sb.toString() + "`;  document.head.appendChild(script);  eruda.init({    theme: 'dark'  });  eruda._shadowRoot.querySelector('.eruda-entry-btn').style.display = 'none';  sessionStorage.setItem('__console_available', true);  document.addEventListener('showconsole', function() { eruda.show(); });  document.addEventListener('hideconsole', function() { eruda.hide(); });}", null);
            } catch (IOException e) {
                e.printStackTrace();
                this.browser.webView.evaluateJavascript("if(!window.eruda){  var script = document.createElement('script');  script.src = 'https://cdn.jsdelivr.net/npm/eruda';  script.crossOrigin = 'anonymous';  script.onload = function() {    eruda.init({      theme: 'dark'    });    eruda._shadowRoot.querySelector('.eruda-entry-btn').style.display = 'none';    sessionStorage.setItem('__console_available', true);    document.addEventListener('showconsole', function() { eruda.show(); });    document.addEventListener('hideconsole', function() { eruda.hide(); });  };  document.head.appendChild(script);}", null);
            }
            this.browser.menu.setChecked("Console", false);
            return;
        }
        this.browser.webView.evaluateJavascript("sessionStorage.getItem('__console_available')", new ValueCallback<String>() { // from class: com.foxdebug.browser.BrowserWebViewClient.1
            @Override // android.webkit.ValueCallback
            public void onReceiveValue(String str2) {
                boolean zEquals = str2.equals("null");
                BrowserWebViewClient.this.browser.menu.setVisible("Console", Boolean.valueOf((zEquals || BrowserWebViewClient.this.browser.emulator) ? false : true));
                if (!BrowserWebViewClient.this.browser.console || zEquals) {
                    return;
                }
                BrowserWebViewClient.this.browser.setConsoleVisible(true);
                BrowserWebViewClient.this.browser.menu.setChecked("Console", true);
            }
        });
    }

    @Override // android.webkit.WebViewClient
    public void onLoadResource(WebView webView, String str) {
        this.browser.setDesktopMode();
    }
}