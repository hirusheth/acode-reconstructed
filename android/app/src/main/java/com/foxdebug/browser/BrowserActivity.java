package com.foxdebug.browser;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.webkit.WebChromeClient;
import androidx.core.view.accessibility.AccessibilityEventCompat;
import com.foxdebug.system.Ui;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class BrowserActivity extends Activity {
    private Browser browser;
    private Ui.Theme theme;

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra("url");
        String stringExtra2 = intent.getStringExtra("theme");
        boolean booleanExtra = intent.getBooleanExtra("onlyConsole", false);
        try {
            this.theme = new Ui.Theme(new JSONObject(stringExtra2));
        } catch (Exception unused) {
            this.theme = new Ui.Theme(new JSONObject());
        }
        Browser browser = new Browser(this, this.theme, Boolean.valueOf(booleanExtra));
        this.browser = browser;
        browser.setUrl(stringExtra);
        setContentView(this.browser);
        getWindow().setSoftInputMode(16);
        if (Build.VERSION.SDK_INT >= 30) {
            if (booleanExtra) {
                getWindow().setDecorFitsSystemWindows(true);
            } else {
                getWindow().setDecorFitsSystemWindows(false);
                this.browser.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() { // from class: com.foxdebug.browser.BrowserActivity.1
                    @Override // android.view.View.OnApplyWindowInsetsListener
                    public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                        Insets insets = windowInsets.getInsets(WindowInsets.Type.systemBars());
                        view.setPadding(insets.left, insets.top, insets.right, Math.max(insets.bottom, windowInsets.getInsets(WindowInsets.Type.ime()).bottom));
                        return WindowInsets.CONSUMED;
                    }
                });
            }
        }
        setSystemTheme(this.theme.get("primaryColor"));
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        if (this.browser.goBack()) {
            return;
        }
        this.browser.exit();
    }

    /* JADX INFO: Removed unreachable split cross block B:23:0x008e */
    private void setSystemTheme(int i) {
        try {
            Ui.Icons.setSize(Ui.dpToPixels(this, 18));
            Window window = getWindow();
            window.clearFlags(AccessibilityEventCompat.TYPE_VIEW_TARGETED_BY_SCROLL);
            window.addFlags(Integer.MIN_VALUE);
            try {
                window.getClass().getMethod("setNavigationBarColor", Integer.TYPE).invoke(window, Integer.valueOf(i));
                window.getClass().getMethod("setStatusBarColor", Integer.TYPE).invoke(window, Integer.valueOf(i));
                if (Build.VERSION.SDK_INT < 30) {
                    setStatusBarStyle(window);
                    setNavigationBarStyle(window);
                } else {
                    String type = this.theme.getType();
                    WindowInsetsController insetsController = window.getInsetsController();
                    if (type.equals("light")) {
                        insetsController.setSystemBarsAppearance(24, 24);
                    } else {
                        insetsController.setSystemBarsAppearance(0, 24);
                    }
                }
            } catch (IllegalArgumentException e) {
                Log.w("BrowserActivity", "Invalid system bar color or appearance input.", e);
            } catch (Exception e2) {
                Log.w("BrowserActivity", "Failed applying system bar theme values.", e2);
            }
        } catch (Exception e3) {
            Log.e("BrowserActivity", "Failed to apply system theme.", e3);
        }
    }

    private void setStatusBarStyle(Window window) {
        View decorView = window.getDecorView();
        int systemUiVisibility = decorView.getSystemUiVisibility();
        if (this.theme.getType().equals("light")) {
            decorView.setSystemUiVisibility(systemUiVisibility | 8192);
        }
    }

    private void setNavigationBarStyle(Window window) {
        View decorView = window.getDecorView();
        int systemUiVisibility = decorView.getSystemUiVisibility();
        if (this.theme.getType().equals("light")) {
            decorView.setSystemUiVisibility(systemUiVisibility | (-2147483632));
        }
    }

    @Override // android.app.Activity
    protected void onActivityResult(int i, int i2, Intent intent) {
        super.onActivityResult(i, i2, intent);
        if (i != this.browser.FILE_SELECT_CODE || this.browser.filePathCallback == null) {
            return;
        }
        this.browser.filePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(i2, intent));
        this.browser.filePathCallback = null;
    }
}