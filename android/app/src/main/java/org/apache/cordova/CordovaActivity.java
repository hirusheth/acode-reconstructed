package org.apache.cordova;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.ArrayList;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
public class CordovaActivity extends AppCompatActivity {
    private static int ACTIVITY_EXITING = 2;
    private static int ACTIVITY_RUNNING = 1;
    private static int ACTIVITY_STARTING = 0;
    public static String TAG = "CordovaActivity";
    protected CordovaWebView appView;
    protected CordovaInterfaceImpl cordovaInterface;
    protected boolean immersiveMode;
    protected String launchUrl;
    protected ArrayList<PluginEntry> pluginEntries;
    protected CordovaPreferences preferences;
    private SplashScreen splashScreen;
    protected boolean keepRunning = true;
    private boolean canEdgeToEdge = false;
    private boolean isFullScreen = false;

    protected boolean showInitialSplashScreen() {
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void onCreate(Bundle bundle) {
        if (showInitialSplashScreen()) {
            this.splashScreen = SplashScreen.installSplashScreen(this);
        }
        loadConfig();
        this.canEdgeToEdge = this.preferences.getBoolean("AndroidEdgeToEdge", false);
        LOG.setLogLevel(this.preferences.getString("loglevel", "ERROR"));
        LOG.i(TAG, "Apache Cordova native platform version 15.1.0 is starting");
        LOG.d(TAG, "CordovaActivity.onCreate()");
        if (!this.preferences.getBoolean("ShowTitle", false)) {
            getWindow().requestFeature(1);
        }
        if (this.preferences.getBoolean("SetFullscreen", false)) {
            LOG.d(TAG, "The SetFullscreen configuration is deprecated in favor of Fullscreen, and will be removed in a future version.");
            this.preferences.set("Fullscreen", true);
        }
        boolean z = this.preferences.getBoolean("Fullscreen", false);
        this.isFullScreen = z;
        if (z) {
            if (!this.preferences.getBoolean("FullscreenNotImmersive", false)) {
                this.immersiveMode = true;
                setImmersiveUiVisibility();
            } else {
                getWindow().setFlags(1024, 1024);
            }
        } else {
            getWindow().setFlags(2048, 2048);
        }
        super.onCreate(bundle);
        CordovaInterfaceImpl cordovaInterfaceImplMakeCordovaInterface = makeCordovaInterface();
        this.cordovaInterface = cordovaInterfaceImplMakeCordovaInterface;
        if (bundle != null) {
            cordovaInterfaceImplMakeCordovaInterface.restoreInstanceState(bundle);
        }
    }

    protected void init() {
        this.appView = makeWebView();
        createViews();
        if (!this.appView.isInitialized()) {
            this.appView.init(this.cordovaInterface, this.pluginEntries, this.preferences);
        }
        this.cordovaInterface.onCordovaInit(this.appView.getPluginManager());
        if (showInitialSplashScreen()) {
            this.cordovaInterface.pluginManager.postMessage("setupSplashScreen", this.splashScreen);
        }
        if ("media".equals(this.preferences.getString("DefaultVolumeStream", "").toLowerCase(Locale.ENGLISH))) {
            setVolumeControlStream(3);
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected void loadConfig() {
        ConfigXmlParser configXmlParser = new ConfigXmlParser();
        configXmlParser.parse((Context) this);
        CordovaPreferences preferences = configXmlParser.getPreferences();
        this.preferences = preferences;
        preferences.setPreferencesBundle(getIntent().getExtras());
        this.launchUrl = configXmlParser.getLaunchUrl();
        this.pluginEntries = configXmlParser.getPluginEntries();
        Config.parser = configXmlParser;
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected void createViews() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        final FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        final View view = this.appView.getView();
        view.setLayoutParams(new FrameLayout.LayoutParams(-1, -1));
        final View view2 = new View(this);
        view2.setTag("statusBarView");
        ViewCompat.setOnApplyWindowInsetsListener(frameLayout, new OnApplyWindowInsetsListener() { // from class: org.apache.cordova.CordovaActivity$$ExternalSyntheticLambda0
            public final WindowInsetsCompat onApplyWindowInsets(View view3, WindowInsetsCompat windowInsetsCompat) {
                return this.f$0.m613lambda$createViews$0$orgapachecordovaCordovaActivity(view2, view, view3, windowInsetsCompat);
            }
        });
        frameLayout.addView(view);
        frameLayout.addView(view2);
        setContentView(frameLayout);
        frameLayout.post(new Runnable() { // from class: org.apache.cordova.CordovaActivity$$ExternalSyntheticLambda1
            @Override // java.lang.Runnable
            public final void run() {
                ViewCompat.requestApplyInsets(frameLayout);
            }
        });
        view.requestFocusFromTouch();
    }

    /* JADX INFO: renamed from: lambda$createViews$0$org-apache-cordova-CordovaActivity, reason: not valid java name */
    /* synthetic */ WindowInsetsCompat m613lambda$createViews$0$orgapachecordovaCordovaActivity(View view, View view2, View view3, WindowInsetsCompat windowInsetsCompat) {
        Insets insets = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
        int iMax = 0;
        int i = (view.getVisibility() == 8 || this.canEdgeToEdge || this.isFullScreen) ? 0 : insets.top;
        int i2 = (this.canEdgeToEdge || this.isFullScreen) ? 0 : insets.left;
        int i3 = (this.canEdgeToEdge || this.isFullScreen) ? 0 : insets.right;
        Insets insets2 = windowInsetsCompat.getInsets(WindowInsetsCompat.Type.ime());
        if (!this.isFullScreen) {
            iMax = this.canEdgeToEdge ? insets2.bottom : Math.max(insets.bottom, insets2.bottom);
        }
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view2.getLayoutParams();
        if (layoutParams.leftMargin != i2 || layoutParams.topMargin != i || layoutParams.rightMargin != i3 || layoutParams.bottomMargin != iMax) {
            layoutParams.setMargins(i2, i, i3, iMax);
            view2.setLayoutParams(layoutParams);
        }
        view.setLayoutParams(new FrameLayout.LayoutParams(-1, i, 48));
        return windowInsetsCompat;
    }

    protected CordovaWebView makeWebView() {
        return new CordovaWebViewImpl(makeWebViewEngine());
    }

    /* JADX WARN: Multi-variable type inference failed */
    protected CordovaWebViewEngine makeWebViewEngine() {
        return CordovaWebViewImpl.createEngine(this, this.preferences);
    }

    protected CordovaInterfaceImpl makeCordovaInterface() {
        return new CordovaInterfaceImpl(this) { // from class: org.apache.cordova.CordovaActivity.1
            @Override // org.apache.cordova.CordovaInterfaceImpl, org.apache.cordova.CordovaInterface
            public Object onMessage(String str, Object obj) {
                return CordovaActivity.this.onMessage(str, obj);
            }
        };
    }

    public void loadUrl(String str) {
        if (this.appView == null) {
            init();
        }
        this.keepRunning = this.preferences.getBoolean("KeepRunning", true);
        this.appView.loadUrlIntoView(str, true);
    }

    protected void onPause() {
        super.onPause();
        LOG.d(TAG, "Paused the activity.");
        if (this.appView != null) {
            this.appView.handlePause(this.keepRunning || this.cordovaInterface.activityResultCallback != null);
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        CordovaWebView cordovaWebView = this.appView;
        if (cordovaWebView != null) {
            cordovaWebView.onNewIntent(intent);
        }
    }

    protected void onResume() {
        super.onResume();
        LOG.d(TAG, "Resumed the activity.");
        if (this.appView == null) {
            return;
        }
        if (!getWindow().getDecorView().hasFocus()) {
            getWindow().getDecorView().requestFocus();
        }
        this.appView.handleResume(this.keepRunning);
    }

    protected void onStop() {
        super.onStop();
        LOG.d(TAG, "Stopped the activity.");
        CordovaWebView cordovaWebView = this.appView;
        if (cordovaWebView == null) {
            return;
        }
        cordovaWebView.handleStop();
    }

    protected void onStart() {
        super.onStart();
        LOG.d(TAG, "Started the activity.");
        CordovaWebView cordovaWebView = this.appView;
        if (cordovaWebView == null) {
            return;
        }
        cordovaWebView.handleStart();
    }

    public void onDestroy() {
        LOG.d(TAG, "CordovaActivity.onDestroy()");
        super.onDestroy();
        CordovaWebView cordovaWebView = this.appView;
        if (cordovaWebView != null) {
            cordovaWebView.handleDestroy();
        }
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (z && this.immersiveMode) {
            setImmersiveUiVisibility();
        }
    }

    protected void setImmersiveUiVisibility() {
        getWindow().getDecorView().setSystemUiVisibility(5894);
    }

    public void startActivityForResult(Intent intent, int i, Bundle bundle) {
        this.cordovaInterface.setActivityResultRequestCode(i);
        super.startActivityForResult(intent, i, bundle);
    }

    protected void onActivityResult(int i, int i2, Intent intent) {
        LOG.d(TAG, "Incoming Result. Request code = " + i);
        super.onActivityResult(i, i2, intent);
        this.cordovaInterface.onActivityResult(i, i2, intent);
    }

    public void onReceivedError(int i, final String str, final String str2) {
        final String string = this.preferences.getString("errorUrl", null);
        if (string != null && !str2.equals(string) && this.appView != null) {
            runOnUiThread(new Runnable() { // from class: org.apache.cordova.CordovaActivity.2
                @Override // java.lang.Runnable
                public void run() {
                    this.appView.showWebPage(string, false, true, null);
                }
            });
        } else {
            final boolean z = i != -2;
            runOnUiThread(new Runnable() { // from class: org.apache.cordova.CordovaActivity.3
                @Override // java.lang.Runnable
                public void run() {
                    if (z) {
                        this.appView.getView().setVisibility(8);
                        this.displayError("Application Error", str + " (" + str2 + ")", "OK", z);
                    }
                }
            });
        }
    }

    public void displayError(final String str, final String str2, final String str3, final boolean z) {
        runOnUiThread(new Runnable() { // from class: org.apache.cordova.CordovaActivity.4
            @Override // java.lang.Runnable
            public void run() {
                try {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(str2);
                    builder.setTitle(str);
                    builder.setCancelable(false);
                    builder.setPositiveButton(str3, new DialogInterface.OnClickListener() { // from class: org.apache.cordova.CordovaActivity.4.1
                        @Override // android.content.DialogInterface.OnClickListener
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            if (z) {
                                CordovaActivity.this.finish();
                            }
                        }
                    });
                    builder.create();
                    builder.show();
                } catch (Exception unused) {
                    CordovaActivity.this.finish();
                }
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        CordovaWebView cordovaWebView = this.appView;
        if (cordovaWebView != null) {
            cordovaWebView.getPluginManager().postMessage("onCreateOptionsMenu", menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        CordovaWebView cordovaWebView = this.appView;
        if (cordovaWebView == null) {
            return true;
        }
        cordovaWebView.getPluginManager().postMessage("onPrepareOptionsMenu", menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        CordovaWebView cordovaWebView = this.appView;
        if (cordovaWebView == null) {
            return true;
        }
        cordovaWebView.getPluginManager().postMessage("onOptionsItemSelected", menuItem);
        return true;
    }

    public Object onMessage(String str, Object obj) {
        if ("onReceivedError".equals(str)) {
            JSONObject jSONObject = (JSONObject) obj;
            try {
                onReceivedError(jSONObject.getInt("errorCode"), jSONObject.getString("description"), jSONObject.getString("url"));
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        if (!"exit".equals(str)) {
            return null;
        }
        finish();
        return null;
    }

    protected void onSaveInstanceState(Bundle bundle) {
        this.cordovaInterface.onSaveInstanceState(bundle);
        super.onSaveInstanceState(bundle);
    }

    public void onConfigurationChanged(Configuration configuration) {
        PluginManager pluginManager;
        super.onConfigurationChanged(configuration);
        CordovaWebView cordovaWebView = this.appView;
        if (cordovaWebView == null || (pluginManager = cordovaWebView.getPluginManager()) == null) {
            return;
        }
        pluginManager.onConfigurationChanged(configuration);
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        try {
            this.cordovaInterface.onRequestPermissionsResult(i, strArr, iArr);
        } catch (JSONException e) {
            LOG.d(TAG, "JSONException: Parameters fed into the method are not valid");
            e.printStackTrace();
        }
    }
}