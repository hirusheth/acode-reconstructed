package com.foxdebug.acode.rk.customtabs;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class CustomTabsPlugin extends CordovaPlugin {
    public boolean execute(String str, JSONArray jSONArray, final CallbackContext callbackContext) {
        final JSONObject jSONObject;
        if (!"open".equals(str)) {
            return false;
        }
        try {
            final String string = jSONArray.getString(0);
            if (jSONArray.optJSONObject(1) != null) {
                jSONObject = jSONArray.optJSONObject(1);
            } else {
                jSONObject = new JSONObject();
            }
            this.cordova.getActivity().runOnUiThread(new Runnable() { // from class: com.foxdebug.acode.rk.customtabs.CustomTabsPlugin$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m111lambda$execute$0$comfoxdebugacoderkcustomtabsCustomTabsPlugin(string, jSONObject, callbackContext);
                }
            });
            return true;
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
            return true;
        }
    }

    /* JADX INFO: renamed from: lambda$execute$0$com-foxdebug-acode-rk-customtabs-CustomTabsPlugin, reason: not valid java name */
    /* synthetic */ void m111lambda$execute$0$comfoxdebugacoderkcustomtabsCustomTabsPlugin(String str, JSONObject jSONObject, CallbackContext callbackContext) {
        try {
            openCustomTab(str, jSONObject);
            callbackContext.success();
        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
    }

    private void openCustomTab(String str, JSONObject jSONObject) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        String strOptString = jSONObject.optString("toolbarColor", null);
        if (strOptString != null && !strOptString.isEmpty()) {
            builder.setToolbarColor(Color.parseColor(strOptString));
        }
        CustomTabsIntent customTabsIntentBuild = builder.build();
        if (jSONObject.optBoolean("showTitle", true)) {
            customTabsIntentBuild.intent.putExtra(CustomTabsIntent.EXTRA_TITLE_VISIBILITY_STATE, 1);
        }
        try {
            customTabsIntentBuild.launchUrl(this.cordova.getActivity(), Uri.parse(str));
        } catch (ActivityNotFoundException unused) {
            this.cordova.getActivity().startActivity(new Intent("android.intent.action.VIEW", Uri.parse(str)));
        }
    }
}