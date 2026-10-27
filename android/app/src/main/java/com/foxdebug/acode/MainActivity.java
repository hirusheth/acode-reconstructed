package com.foxdebug.acode;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import java.lang.ref.WeakReference;
import org.apache.cordova.CordovaActivity;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class MainActivity extends CordovaActivity {
    private static WeakReference<Context> weakContext;

    public static Context getContext() {
        WeakReference<Context> weakReference = weakContext;
        if (weakReference != null) {
            return weakReference.get();
        }
        return null;
    }

    private static boolean hasBrokenRealmeSplashTransfer() {
        if (Build.VERSION.SDK_INT == 33) {
            return "realme".equalsIgnoreCase(Build.MANUFACTURER) || "realme".equalsIgnoreCase(Build.BRAND);
        }
        return false;
    }

    protected void loadConfig() {
        super.loadConfig();
        if (hasBrokenRealmeSplashTransfer()) {
            this.preferences.set("FadeSplashScreen", false);
        }
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        weakContext = new WeakReference<>(this);
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.getBoolean("cdvStartInBackground", false)) {
            moveTaskToBack(true);
        }
        loadUrl(this.launchUrl);
    }
}