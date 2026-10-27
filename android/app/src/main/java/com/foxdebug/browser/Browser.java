package com.foxdebug.browser;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.foxdebug.system.Ui;
import com.silkimen.http.HttpRequest;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class Browser extends LinearLayout {
    public int FILE_SELECT_CODE;
    final int REQUEST_SELECT_FILE;
    public boolean console;
    public Context context;
    public boolean desktopMode;
    private Emulator deviceEmulator;
    public boolean emulator;
    private ImageView favicon;
    ValueCallback<Uri[]> filePathCallback;
    private int fontSize;
    private int imageSize;
    private ProgressBar loading;
    private LinearLayout main;
    public Menu menu;
    private boolean onlyConsole;
    private int padding;
    private Ui.Theme theme;
    private String title;
    private int titleHeight;
    private TextView titleText;
    private int titleTextHeight;
    private String url;
    private TextView urlText;
    public WebView webView;

    public Browser(Context context, Ui.Theme theme, Boolean bool) {
        super(context);
        this.FILE_SELECT_CODE = 1;
        this.emulator = false;
        this.console = false;
        this.desktopMode = false;
        this.url = "";
        this.title = "Browser";
        this.REQUEST_SELECT_FILE = 1;
        this.theme = theme;
        this.context = context;
        this.onlyConsole = bool.booleanValue();
        this.padding = Ui.dpToPixels(context, 5);
        this.fontSize = Ui.dpToPixels(context, 5);
        this.imageSize = Ui.dpToPixels(context, 35);
        this.titleHeight = Ui.dpToPixels(context, 45);
        this.titleTextHeight = Ui.dpToPixels(context, 35);
        init();
    }

    public void init() {
        this.favicon = createIcon(Ui.Icons.LOGO);
        ImageButton imageButtonCreateIconButton = createIconButton(Ui.Icons.MORE_VERT);
        imageButtonCreateIconButton.setOnClickListener(new View.OnClickListener() { // from class: com.foxdebug.browser.Browser.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Browser.this.menu.show(view);
            }
        });
        ImageButton imageButtonCreateIconButton2 = createIconButton(Ui.Icons.REFRESH);
        imageButtonCreateIconButton2.setOnClickListener(new View.OnClickListener() { // from class: com.foxdebug.browser.Browser.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Browser.this.webView.reload();
            }
        });
        ProgressBar progressBar = new ProgressBar(this.context, null, R.attr.progressBarStyle);
        this.loading = progressBar;
        int i = this.imageSize;
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(i, i, 0.0f));
        FrameLayout frameLayout = new FrameLayout(this.context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-2, -2);
        layoutParams.gravity = 16;
        frameLayout.setLayoutParams(layoutParams);
        frameLayout.addView(this.favicon);
        frameLayout.addView(this.loading);
        LinearLayout linearLayoutCreateTile = createTile();
        linearLayoutCreateTile.addView(frameLayout);
        TextView textViewCreateTextView = this.onlyConsole ? createTextView(this.title) : createEditText(this.title);
        this.titleText = textViewCreateTextView;
        linearLayoutCreateTile.addView(textViewCreateTextView);
        if (!this.onlyConsole) {
            linearLayoutCreateTile.addView(imageButtonCreateIconButton2);
            linearLayoutCreateTile.addView(imageButtonCreateIconButton);
        }
        WebView webView = new WebView(this.context);
        this.webView = webView;
        webView.setFocusable(true);
        this.webView.setFocusableInTouchMode(true);
        this.webView.setBackgroundColor(-1);
        this.webView.setDownloadListener(new AnonymousClass3());
        fitWebViewTo(0, 0, 1.0f);
        this.webView.setWebChromeClient(new BrowserChromeClient(this));
        this.webView.setWebViewClient(new BrowserWebViewClient(this));
        WebSettings settings = this.webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setDisplayZoomControls(false);
        settings.setDomStorageEnabled(true);
        LinearLayout linearLayout = new LinearLayout(this.context);
        linearLayout.setGravity(17);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, -1, 1.0f));
        linearLayout.setBackgroundColor(this.theme.get("primaryColor"));
        linearLayout.addView(this.webView);
        setOrientation(1);
        setFocusableInTouchMode(true);
        setFocusable(true);
        createMenu();
        addView(linearLayoutCreateTile);
        addView(linearLayout);
    }

    /* JADX INFO: renamed from: com.foxdebug.browser.Browser$3, reason: invalid class name */
    class AnonymousClass3 implements DownloadListener {
        AnonymousClass3() {
        }

        @Override // android.webkit.DownloadListener
        public void onDownloadStart(final String str, final String str2, String str3, final String str4, long j) {
            final String strGuessFileName = URLUtil.guessFileName(str, str3, str4);
            new Handler(Looper.getMainLooper()).post(new Runnable() { // from class: com.foxdebug.browser.Browser$3$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m150lambda$onDownloadStart$1$comfoxdebugbrowserBrowser$3(strGuessFileName, str, str4, str2);
                }
            });
        }

        /* JADX INFO: renamed from: lambda$onDownloadStart$1$com-foxdebug-browser-Browser$3, reason: not valid java name */
        /* synthetic */ void m150lambda$onDownloadStart$1$comfoxdebugbrowserBrowser$3(final String str, final String str2, final String str3, final String str4) {
            new AlertDialog.Builder(Browser.this.getContext()).setTitle("Download file").setMessage("Do you want to download \"" + str + "\"?").setPositiveButton("Yes", new DialogInterface.OnClickListener() { // from class: com.foxdebug.browser.Browser$3$$ExternalSyntheticLambda1
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    this.f$0.m149lambda$onDownloadStart$0$comfoxdebugbrowserBrowser$3(str2, str3, str4, str, dialogInterface, i);
                }
            }).setNegativeButton("Cancel", (DialogInterface.OnClickListener) null).show();
        }

        /* JADX INFO: renamed from: lambda$onDownloadStart$0$com-foxdebug-browser-Browser$3, reason: not valid java name */
        /* synthetic */ void m149lambda$onDownloadStart$0$comfoxdebugbrowserBrowser$3(String str, String str2, String str3, String str4, DialogInterface dialogInterface, int i) {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(str));
            request.setMimeType(str2);
            request.addRequestHeader(HttpRequest.HEADER_USER_AGENT, str3);
            request.setDescription("Downloading file...");
            request.setTitle(str4);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(1);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, str4);
            ((DownloadManager) Browser.this.getContext().getSystemService("download")).enqueue(request);
            Toast.makeText(Browser.this.getContext(), "Download started...", 0).show();
        }
    }

    private void createMenu() {
        Menu menu = new Menu(this.context, this.theme);
        this.menu = menu;
        menu.addItem(Ui.Icons.DEVICES, "Devices", false);
        this.menu.addItem(Ui.Icons.NO_CACHE, "Disable Cache", false);
        this.menu.addItem(Ui.Icons.TERMINAL, "Console", false);
        this.menu.addItem(Ui.Icons.OPEN_IN_BROWSER, "Open in Browser");
        this.menu.addItem(Ui.Icons.EXIT, "Exit");
        this.menu.setCallback(new Menu.Callback() { // from class: com.foxdebug.browser.Browser.4
            @Override // com.foxdebug.browser.Menu.Callback
            public void onSelect(String str, Boolean bool) {
                str.hashCode();
                switch (str) {
                    case "Console":
                        Browser.this.setConsoleVisible(bool.booleanValue());
                        break;
                    case "Disable Cache":
                        Browser.this.webView.getSettings().setCacheMode(bool.booleanValue() ? 2 : -1);
                        break;
                    case "Devices":
                        if (Browser.this.deviceEmulator == null) {
                            Browser.this.createDeviceEmulatorLayout();
                        }
                        Browser.this.emulator = bool.booleanValue();
                        if (bool.booleanValue()) {
                            Browser.this.setDesktopMode(true);
                            Browser.this.setConsoleVisible(false);
                            Browser.this.menu.setChecked("Console", false);
                            Browser.this.menu.setVisible("Console", false);
                            Browser browser = Browser.this;
                            browser.addView(browser.deviceEmulator);
                            Browser browser2 = Browser.this;
                            browser2.fitWebViewTo(browser2.deviceEmulator.getWidthProgress(), Browser.this.deviceEmulator.getHeightProgress(), Browser.this.deviceEmulator.getScaleProgress());
                            break;
                        } else {
                            Browser.this.menu.setVisible("Console", true);
                            Browser browser3 = Browser.this;
                            browser3.removeView(browser3.deviceEmulator);
                            Browser.this.fitWebViewTo(0, 0, 1.0f);
                            Browser.this.webView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() { // from class: com.foxdebug.browser.Browser.4.1
                                @Override // android.view.ViewTreeObserver.OnGlobalLayoutListener
                                public void onGlobalLayout() {
                                    Browser.this.webView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                                    Browser.this.setDesktopMode(false);
                                }
                            });
                            break;
                        }
                        break;
                    case "Open in Browser":
                        Browser.this.context.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(Browser.this.url)));
                        Browser.this.exit();
                        break;
                    case "Exit":
                        Browser.this.exit();
                        break;
                }
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void createDeviceEmulatorLayout() {
        Emulator emulator = new Emulator(this.context, this.theme);
        this.deviceEmulator = emulator;
        emulator.setReference(this.webView);
        this.deviceEmulator.setChangeListener(new Emulator.Callback() { // from class: com.foxdebug.browser.Browser.5
            @Override // com.foxdebug.browser.Emulator.Callback
            public void onChange(int i, int i2, float f) {
                Browser.this.fitWebViewTo(i, i2, f);
            }
        });
    }

    private void setTextViewProperties(TextView textView, int i) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, i, 1.0f);
        layoutParams.gravity = 16;
        textView.setMaxLines(1);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setSingleLine(true);
        textView.setHorizontallyScrolling(true);
        textView.setTextColor(this.theme.get("primaryTextColor"));
        textView.setLayoutParams(layoutParams);
        textView.setGravity(16);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setDesktopMode(boolean z) {
        WebSettings settings = this.webView.getSettings();
        this.desktopMode = z;
        settings.setUserAgentString(z ? "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36" : null);
        if (z) {
            this.webView.getMeasuredWidth();
            this.webView.getMeasuredHeight();
        }
        settings.setLoadWithOverviewMode(z);
        settings.setUseWideViewPort(z);
        settings.setLoadWithOverviewMode(z);
        settings.setSupportZoom(z);
        settings.setBuiltInZoomControls(z);
        this.webView.reload();
    }

    public void setDesktopMode() {
        if (this.desktopMode) {
            updateViewportDimension(this.webView.getMeasuredWidth(), this.webView.getMeasuredHeight());
        }
    }

    public void setUrl(String str) {
        this.url = str;
        setTitle(str);
        setProgressBarVisible(true);
        this.webView.loadUrl(str);
    }

    public void setTitle(String str) {
        this.title = str;
        this.titleText.setText(str);
    }

    public void setFavicon(Bitmap bitmap) {
        this.favicon.setImageBitmap(bitmap);
    }

    public void setConsoleVisible(boolean z) {
        this.console = z;
        this.webView.evaluateJavascript(String.format("document.dispatchEvent(new CustomEvent('%sconsole'))", z ? "show" : "hide"), null);
    }

    public void setProgressBarVisible(boolean z) {
        this.loading.setVisibility(z ? 0 : 8);
    }

    private void updateViewportDimension(int i, int i2) {
        String strValueOf;
        String strValueOf2;
        String str;
        if (i <= 0) {
            strValueOf = "device-width";
            strValueOf2 = "device-height";
            str = "1";
        } else {
            strValueOf = String.valueOf(i);
            str = "0.1";
            strValueOf2 = "";
        }
        if (i2 > 0) {
            strValueOf2 = String.valueOf(i2);
        }
        this.webView.evaluateJavascript(String.format("!function(){var e=document.head;if(e){e.querySelectorAll(\"meta[name=viewport]\").forEach(function(e){e.remove()});var t=document.createElement(\"meta\");t.name=\"viewport\",t.content=\"width=%s, height=%s, initial-scale=%s\",e.append(t)}}();", strValueOf, strValueOf2, str), null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void fitWebViewTo(int i, int i2, float f) {
        this.webView.setScaleX(f);
        this.webView.setScaleY(f);
        this.webView.setLayoutParams(new LinearLayout.LayoutParams(i == 0 ? -1 : i, i2 != 0 ? i2 : -1));
        if (i <= 0 || i2 <= 0) {
            return;
        }
        updateViewportDimension(i, i2);
    }

    private void styleIcon(ImageView imageView) {
        int iDpToPixels = Ui.dpToPixels(this.context, 7);
        int i = this.imageSize;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(i, i);
        layoutParams.gravity = 16;
        imageView.setBackgroundDrawable(null);
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        imageView.setPadding(iDpToPixels, iDpToPixels, iDpToPixels, iDpToPixels);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void keyboardVisible(boolean z) {
        if (z) {
            ((InputMethodManager) this.context.getSystemService("input_method")).toggleSoftInput(2, 0);
        } else {
            ((InputMethodManager) this.context.getSystemService("input_method")).hideSoftInputFromWindow(this.webView.getWindowToken(), 0);
        }
    }

    public boolean goBack() {
        if (this.console) {
            this.menu.setChecked("Console", false);
            setConsoleVisible(false);
            return true;
        }
        if (!this.webView.canGoBack()) {
            return false;
        }
        this.webView.goBack();
        this.url = this.webView.getOriginalUrl();
        return true;
    }

    private TextView createTextView(String str) {
        TextView textView = new TextView(this.context);
        setTextViewProperties(textView, this.titleHeight);
        textView.setText(str);
        return textView;
    }

    private EditText createEditText(String str) {
        final EditText editText = new EditText(this.context);
        GradientDrawable gradientDrawable = new GradientDrawable();
        String type = this.theme.getType();
        int i = this.titleTextHeight / 2;
        gradientDrawable.setCornerRadius(i);
        gradientDrawable.setColor(type.equals("light") ? 285212672 : 301989887);
        editText.setBackground(gradientDrawable);
        setTextViewProperties(editText, this.titleTextHeight);
        editText.setText(str);
        editText.setPadding(i, 0, i, 0);
        int i2 = this.fontSize;
        editText.setTextSize(i2 < 10 ? 10.0f : i2);
        editText.setInputType(16);
        editText.setImeOptions(2);
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() { // from class: com.foxdebug.browser.Browser.6
            @Override // android.view.View.OnFocusChangeListener
            public void onFocusChange(View view, boolean z) {
                if (z) {
                    Browser.this.titleText.setText(Browser.this.url);
                    Browser.this.keyboardVisible(true);
                } else {
                    Browser.this.titleText.setText(Browser.this.title);
                    Browser.this.keyboardVisible(false);
                }
            }
        });
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() { // from class: com.foxdebug.browser.Browser.7
            @Override // android.widget.TextView.OnEditorActionListener
            public boolean onEditorAction(TextView textView, int i3, KeyEvent keyEvent) {
                if (i3 != 2) {
                    return false;
                }
                String string = textView.getText().toString();
                if (!string.startsWith("http://") && !string.startsWith("https://")) {
                    string = "http://" + string;
                }
                Browser.this.title = string;
                Browser.this.setUrl(string);
                editText.clearFocus();
                Browser.this.keyboardVisible(false);
                return true;
            }
        });
        return editText;
    }

    private LinearLayout createTile() {
        return createTile(this.titleHeight);
    }

    private LinearLayout createTile(int i) {
        LinearLayout linearLayout = new LinearLayout(this.context);
        linearLayout.setOrientation(0);
        linearLayout.setBackgroundColor(this.theme.get("primaryColor"));
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(-1, i));
        linearLayout.setHorizontalGravity(3);
        linearLayout.setVerticalGravity(48);
        return linearLayout;
    }

    private ImageButton createIconButton(String str) {
        return createIconButton(Ui.Icons.get(this.context, str, this.theme.get("primaryTextColor")));
    }

    private ImageButton createIconButton(Bitmap bitmap) {
        ImageButton imageButton = new ImageButton(this.context);
        imageButton.setImageBitmap(bitmap);
        styleIcon(imageButton);
        return imageButton;
    }

    private ImageView createIcon(String str) {
        Bitmap bitmap = Ui.Icons.get(this.context, str, this.theme.get("primaryTextColor"));
        ImageView imageView = new ImageView(this.context);
        imageView.setImageBitmap(bitmap);
        styleIcon(imageView);
        return imageView;
    }

    public void exit() {
        if (this.console) {
            setConsoleVisible(false);
        } else if (this.webView.canGoBack()) {
            this.webView.goBack();
        } else {
            this.webView.destroy();
            ((Activity) this.context).finish();
        }
    }
}