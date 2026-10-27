package com.foxdebug.browser;

import android.content.Intent;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class Plugin extends CordovaPlugin {
    public boolean execute(String str, JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        if (!str.equals("open")) {
            return false;
        }
        String string = jSONArray.getString(0);
        JSONObject jSONObject = jSONArray.getJSONObject(1);
        boolean zOptBoolean = jSONArray.optBoolean(2, false);
        String string2 = jSONObject.toString();
        Intent intent = new Intent(this.cordova.getActivity(), (Class<?>) BrowserActivity.class);
        intent.putExtra("url", string);
        intent.putExtra("theme", string2);
        intent.putExtra("onlyConsole", zOptBoolean);
        this.cordova.getActivity().startActivity(intent);
        callbackContext.success("Opened browser");
        return true;
    }
}