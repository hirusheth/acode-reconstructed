package com.foxdebug.crashhandler;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class CrashActivity extends Activity {
    private int colorBorder;
    private int colorButtonPrimaryBg;
    private int colorButtonPrimaryText;
    private int colorButtonSecondaryBg;
    private int colorButtonSecondaryText;
    private int colorLinkText;
    private int colorMetaLabel;
    private int colorMetaValue;
    private int colorPrimaryBg;
    private int colorPrimaryText;
    private int colorSecondaryBg;
    private int colorSecondaryText;
    private int colorTraceBg;
    private String errorMessage;
    private String errorType;
    private String fullReport;
    private boolean isDarkTheme;
    private String stackTrace;

    @Override // android.app.Activity
    protected void onCreate(Bundle bundle) {
        String str;
        String strValueOf = "Unknown";
        super.onCreate(bundle);
        loadThemeColors();
        applySystemBarColors();
        Intent intent = getIntent();
        String stringExtra = intent.getStringExtra("error_type");
        this.errorType = stringExtra;
        if (stringExtra == null) {
            this.errorType = "Unexpected Crash";
        }
        String stringExtra2 = intent.getStringExtra("error_message");
        this.errorMessage = stringExtra2;
        if (stringExtra2 == null) {
            this.errorMessage = "No error message provided";
        }
        String stringExtra3 = intent.getStringExtra("stack_trace");
        this.stackTrace = stringExtra3;
        if (stringExtra3 == null) {
            this.stackTrace = "No stack trace details available.";
        }
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            str = packageInfo.versionName;
            try {
                if (Build.VERSION.SDK_INT >= 28) {
                    strValueOf = String.valueOf(packageInfo.getLongVersionCode());
                } else {
                    strValueOf = String.valueOf(packageInfo.versionCode);
                }
            } catch (Exception unused) {
            }
        } catch (Exception unused2) {
            str = "Unknown";
        }
        String str2 = Build.MANUFACTURER + " " + Build.MODEL;
        String str3 = Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")";
        String webViewVersion = getWebViewVersion();
        String appLanguage = getAppLanguage();
        this.fullReport = "Acode Crash Report\n==================\nWebView Version: " + webViewVersion + "\nApp Language: " + appLanguage + "\nError Message: " + this.errorMessage + "\nApp Version: " + str + " (" + strValueOf + ")\nDevice: " + str2 + "\nAndroid Version: " + str3 + "\n\nStack Trace:\n" + this.stackTrace;
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        scrollView.setBackgroundColor(this.colorPrimaryBg);
        scrollView.setFillViewport(true);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        ViewGroup.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        int iDp = dp(20.0f);
        linearLayout.setPadding(iDp, iDp, iDp, iDp);
        linearLayout.setLayoutParams(layoutParams);
        TextView textView = new TextView(this);
        textView.setText("Acode Crashed");
        textView.setTextSize(24.0f);
        textView.setTextColor(this.colorPrimaryText);
        textView.setTypeface(Typeface.create("sans-serif-medium", 1));
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams2.setMargins(0, dp(10.0f), 0, dp(8.0f));
        textView.setLayoutParams(layoutParams2);
        linearLayout.addView(textView);
        TextView textView2 = new TextView(this);
        textView2.setText("An unrecoverable exception occurred in Acode's native system. The application details and exception logs have been recorded below.");
        textView2.setTextSize(14.0f);
        textView2.setTextColor(this.colorSecondaryText);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams3.setMargins(0, 0, 0, dp(20.0f));
        textView2.setLayoutParams(layoutParams3);
        linearLayout.addView(textView2);
        TextView textView3 = new TextView(this);
        textView3.setText("DEVICE & APP INFO");
        textView3.setTextSize(11.0f);
        textView3.setTextColor(this.colorLinkText);
        textView3.setTypeface(Typeface.create("sans-serif", 1));
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams4.setMargins(0, 0, 0, dp(6.0f));
        textView3.setLayoutParams(layoutParams4);
        linearLayout.addView(textView3);
        LinearLayout linearLayout2 = new LinearLayout(this);
        linearLayout2.setOrientation(1);
        linearLayout2.setPadding(dp(16.0f), dp(16.0f), dp(16.0f), dp(16.0f));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(this.colorSecondaryBg);
        gradientDrawable.setCornerRadius(dp(4.0f));
        gradientDrawable.setStroke(dp(1.0f), this.colorBorder);
        linearLayout2.setBackground(gradientDrawable);
        LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams5.setMargins(0, 0, 0, dp(20.0f));
        linearLayout2.setLayoutParams(layoutParams5);
        linearLayout2.addView(createMetaRow("App Version", str + " (" + strValueOf + ")"));
        linearLayout2.addView(createMetaRow("Device", str2));
        linearLayout2.addView(createMetaRow("Android OS", str3));
        linearLayout2.addView(createMetaRow("WebView", webViewVersion));
        linearLayout2.addView(createMetaRow("App Language", appLanguage));
        linearLayout.addView(linearLayout2);
        TextView textView4 = new TextView(this);
        textView4.setText("STACK TRACE");
        textView4.setTextSize(11.0f);
        textView4.setTextColor(this.colorLinkText);
        textView4.setTypeface(Typeface.create("sans-serif", 1));
        LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams6.setMargins(0, 0, 0, dp(6.0f));
        textView4.setLayoutParams(layoutParams6);
        linearLayout.addView(textView4);
        LinearLayout linearLayout3 = new LinearLayout(this);
        linearLayout3.setOrientation(1);
        linearLayout3.setPadding(dp(12.0f), dp(12.0f), dp(12.0f), dp(12.0f));
        GradientDrawable gradientDrawable2 = new GradientDrawable();
        gradientDrawable2.setColor(this.colorTraceBg);
        gradientDrawable2.setCornerRadius(dp(4.0f));
        gradientDrawable2.setStroke(dp(1.0f), this.colorBorder);
        linearLayout3.setBackground(gradientDrawable2);
        LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(-1, dp(280.0f));
        layoutParams7.setMargins(0, 0, 0, dp(24.0f));
        linearLayout3.setLayoutParams(layoutParams7);
        ScrollView scrollView2 = new ScrollView(this);
        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
        TextView textView5 = new TextView(this);
        textView5.setText(this.stackTrace);
        textView5.setTextSize(12.0f);
        textView5.setTextColor(this.colorSecondaryText);
        textView5.setTypeface(Typeface.MONOSPACE);
        textView5.setHorizontallyScrolling(true);
        textView5.setTextIsSelectable(true);
        horizontalScrollView.addView(textView5);
        scrollView2.addView(horizontalScrollView);
        linearLayout3.addView(scrollView2);
        linearLayout.addView(linearLayout3);
        LinearLayout linearLayout4 = new LinearLayout(this);
        linearLayout4.setOrientation(1);
        linearLayout4.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        TextView textViewCreateButton = createButton("Restart Acode", this.colorButtonPrimaryText, this.colorButtonPrimaryBg, false);
        textViewCreateButton.setOnClickListener(new View.OnClickListener() { // from class: com.foxdebug.crashhandler.CrashActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                Intent launchIntentForPackage = CrashActivity.this.getPackageManager().getLaunchIntentForPackage(CrashActivity.this.getPackageName());
                if (launchIntentForPackage != null) {
                    launchIntentForPackage.addFlags(268468224);
                    CrashActivity.this.startActivity(launchIntentForPackage);
                }
                CrashActivity.this.finish();
                System.exit(0);
            }
        });
        linearLayout4.addView(textViewCreateButton);
        TextView textViewCreateButton2 = createButton("Copy Error Details", this.colorButtonSecondaryText, this.colorButtonSecondaryBg, true);
        textViewCreateButton2.setOnClickListener(new View.OnClickListener() { // from class: com.foxdebug.crashhandler.CrashActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                ((ClipboardManager) CrashActivity.this.getSystemService("clipboard")).setPrimaryClip(ClipData.newPlainText("Acode Crash Log", CrashActivity.this.fullReport));
                Toast.makeText(CrashActivity.this, "Copied report to clipboard!", 0).show();
            }
        });
        linearLayout4.addView(textViewCreateButton2);
        TextView textViewCreateButton3 = createButton("Close", this.colorButtonSecondaryText, this.colorButtonSecondaryBg, true);
        textViewCreateButton3.setOnClickListener(new View.OnClickListener() { // from class: com.foxdebug.crashhandler.CrashActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                CrashActivity.this.finish();
                System.exit(0);
            }
        });
        linearLayout4.addView(textViewCreateButton3);
        linearLayout.addView(linearLayout4);
        scrollView.addView(linearLayout);
        setContentView(scrollView);
    }

    private void loadThemeColors() {
        SharedPreferences sharedPreferences;
        try {
            sharedPreferences = getApplicationContext().getSharedPreferences("acode_theme", 0);
        } catch (Exception unused) {
            sharedPreferences = null;
        }
        this.colorPrimaryBg = getThemeColor(sharedPreferences, "primaryColor", "#23272a");
        this.colorSecondaryBg = getThemeColor(sharedPreferences, "secondaryColor", "#2d3134");
        this.colorPrimaryText = getThemeColor(sharedPreferences, "primaryTextColor", "#f5f5f5");
        this.colorSecondaryText = getThemeColor(sharedPreferences, "secondaryTextColor", "#e4e4e4");
        this.colorLinkText = getThemeColor(sharedPreferences, "linkTextColor", "#8ab4f8");
        this.colorButtonPrimaryBg = getThemeColor(sharedPreferences, "activeColor", "#4285f4");
        this.colorButtonPrimaryText = getThemeColor(sharedPreferences, "buttonTextColor", "#ffffff");
        this.colorButtonSecondaryBg = this.colorSecondaryBg;
        this.colorButtonSecondaryText = this.colorSecondaryText;
        String string = "dark";
        if (sharedPreferences != null) {
            try {
                string = sharedPreferences.getString("type", "dark");
            } catch (Exception unused2) {
            }
        }
        this.isDarkTheme = !"light".equals(string);
        this.colorBorder = deriveBorderColor(this.colorPrimaryBg, this.colorSecondaryBg);
        this.colorTraceBg = deriveTraceBg(this.colorPrimaryBg);
        this.colorMetaLabel = deriveMetaLabelColor(this.colorSecondaryText);
        this.colorMetaValue = this.colorPrimaryText;
    }

    private int getThemeColor(SharedPreferences sharedPreferences, String str, String str2) {
        if (sharedPreferences == null) {
            return safeParseColor(str2);
        }
        try {
            String string = sharedPreferences.getString(str, null);
            if (string != null && !string.isEmpty()) {
                return safeParseColor(string, str2);
            }
        } catch (Exception unused) {
        }
        return safeParseColor(str2);
    }

    private int safeParseColor(String str) {
        return safeParseColor(str, "#000000");
    }

    private int safeParseColor(String str, String str2) {
        try {
            try {
                return Color.parseColor(str);
            } catch (Exception unused) {
                return -16777216;
            }
        } catch (Exception unused2) {
            return Color.parseColor(str2);
        }
    }

    private int deriveBorderColor(int i, int i2) {
        int iMax;
        int iMax2;
        int iMax3;
        int iRed = (Color.red(i) + Color.red(i2)) / 2;
        int iGreen = (Color.green(i) + Color.green(i2)) / 2;
        int iBlue = (Color.blue(i) + Color.blue(i2)) / 2;
        if (this.isDarkTheme) {
            iMax = Math.min(255, iRed + 20);
            iMax2 = Math.min(255, iGreen + 20);
            iMax3 = Math.min(255, iBlue + 20);
        } else {
            iMax = Math.max(0, iRed - 20);
            iMax2 = Math.max(0, iGreen - 20);
            iMax3 = Math.max(0, iBlue - 20);
        }
        return Color.rgb(iMax, iMax2, iMax3);
    }

    private int deriveTraceBg(int i) {
        if (this.isDarkTheme) {
            return Color.rgb(Math.max(0, Color.red(i) - 12), Math.max(0, Color.green(i) - 12), Math.max(0, Color.blue(i) - 12));
        }
        return Color.rgb(Math.min(255, Color.red(i) + 8), Math.min(255, Color.green(i) + 8), Math.min(255, Color.blue(i) + 8));
    }

    private int deriveMetaLabelColor(int i) {
        return Color.argb(180, Color.red(i), Color.green(i), Color.blue(i));
    }

    private void applySystemBarColors() {
        try {
            Window window = getWindow();
            window.setStatusBarColor(this.colorPrimaryBg);
            window.setNavigationBarColor(this.colorPrimaryBg);
            if (this.isDarkTheme) {
                return;
            }
            View decorView = window.getDecorView();
            decorView.setSystemUiVisibility(decorView.getSystemUiVisibility() | 8192);
        } catch (Exception unused) {
        }
    }

    private View createMetaRow(String str, String str2) {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, 0, 0, dp(6.0f));
        linearLayout.setLayoutParams(layoutParams);
        TextView textView = new TextView(this);
        textView.setText(str + ": ");
        textView.setTextSize(13.0f);
        textView.setTextColor(this.colorMetaLabel);
        textView.setTypeface(Typeface.DEFAULT_BOLD);
        textView.setLayoutParams(new LinearLayout.LayoutParams(dp(100.0f), -2));
        TextView textView2 = new TextView(this);
        textView2.setText(str2);
        textView2.setTextSize(13.0f);
        textView2.setTextColor(this.colorMetaValue);
        textView2.setLayoutParams(new LinearLayout.LayoutParams(-1, -2));
        linearLayout.addView(textView);
        linearLayout.addView(textView2);
        return linearLayout;
    }

    private TextView createButton(String str, int i, int i2, boolean z) {
        final TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(15.0f);
        textView.setTextColor(i);
        textView.setGravity(17);
        textView.setPadding(dp(16.0f), dp(14.0f), dp(16.0f), dp(14.0f));
        textView.setTypeface(Typeface.create("sans-serif-medium", 1));
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i2);
        gradientDrawable.setCornerRadius(dp(4.0f));
        if (z) {
            gradientDrawable.setStroke(dp(1.0f), this.colorBorder);
        }
        textView.setBackground(gradientDrawable);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -2);
        layoutParams.setMargins(0, 0, 0, dp(12.0f));
        textView.setLayoutParams(layoutParams);
        textView.setOnTouchListener(new View.OnTouchListener() { // from class: com.foxdebug.crashhandler.CrashActivity.4
            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == 0) {
                    textView.setAlpha(0.7f);
                    return false;
                }
                if (motionEvent.getAction() != 1 && motionEvent.getAction() != 3) {
                    return false;
                }
                textView.setAlpha(1.0f);
                return false;
            }
        });
        return textView;
    }

    private String getWebViewVersion() {
        try {
            PackageInfo currentWebViewPackage = WebView.getCurrentWebViewPackage();
            if (currentWebViewPackage != null) {
                return currentWebViewPackage.versionName;
            }
        } catch (Exception unused) {
        }
        int i = 3;
        try {
            String[] strArr = {"com.google.android.webview", "com.android.chrome", "com.android.webview"};
            for (int i2 = 0; i2 < i; i2++) {
                try {
                    PackageInfo packageInfo = getPackageManager().getPackageInfo(strArr[i2], 0);
                    if (packageInfo != null) {
                        return packageInfo.versionName;
                    }
                    continue;
                } catch (Exception unused2) {
                }
            }
        } catch (Exception unused3) {
        }
        try {
            return System.getProperty("http.agent");
        } catch (Exception unused4) {
            return "Unknown";
        }
    }

    private String getAppLanguage() {
        try {
            File file = new File(getExternalFilesDir(null), "settings.json");
            if (!file.exists()) {
                file = new File(getFilesDir(), "settings.json");
            }
            if (file.exists()) {
                FileInputStream fileInputStream = new FileInputStream(file);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                while (true) {
                    String line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    sb.append(line);
                }
                bufferedReader.close();
                fileInputStream.close();
                JSONObject jSONObject = new JSONObject(sb.toString());
                if (jSONObject.has("lang")) {
                    return jSONObject.getString("lang");
                }
                return "en-us";
            }
            return "en-us";
        } catch (Exception unused) {
            return "en-us";
        }
    }

    private int dp(float f) {
        return (int) TypedValue.applyDimension(1, f, getResources().getDisplayMetrics());
    }
}