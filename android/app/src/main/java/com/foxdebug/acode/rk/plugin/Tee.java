package com.foxdebug.acode.rk.plugin;

import android.content.Context;
import com.foxdebug.acode.rk.auth.EncryptedPreferenceManager;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class Tee extends CordovaPlugin {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private Context context;
    private final Map<String, String> tokenStore = new ConcurrentHashMap();
    private final Map<String, List<String>> permissionStore = new ConcurrentHashMap();
    private volatile String sessionId = null;

    public void initialize(CordovaInterface cordovaInterface, CordovaWebView cordovaWebView) {
        super.initialize(cordovaInterface, cordovaWebView);
        this.context = cordovaInterface.getContext();
    }

    public void onReset() {
        this.sessionId = null;
    }

    public boolean execute(String str, JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        if ("establishConnection".equals(str)) {
            synchronized (this) {
                if (this.sessionId != null) {
                    callbackContext.error("CONNECTION_ALREADY_ESTABLISHED");
                } else {
                    this.sessionId = generateSecret();
                    callbackContext.success(this.sessionId);
                }
            }
            return true;
        }
        if ("requestToken".equals(str)) {
            String string = jSONArray.getString(0);
            String string2 = jSONArray.getString(1);
            String string3 = jSONArray.getString(2);
            if (!isValidSession(string)) {
                callbackContext.error("INVALID_SESSION");
                return true;
            }
            handleTokenRequest(string2, string3, callbackContext);
            return true;
        }
        if ("get_secret".equals(str)) {
            String string4 = jSONArray.getString(0);
            String string5 = jSONArray.getString(1);
            String string6 = jSONArray.getString(2);
            String pluginIdFromToken = getPluginIdFromToken(string4);
            if (pluginIdFromToken == null) {
                callbackContext.error("INVALID_TOKEN");
                return true;
            }
            callbackContext.success(new EncryptedPreferenceManager(this.context, pluginIdFromToken).getString(string5, string6));
            return true;
        }
        if ("set_secret".equals(str)) {
            String string7 = jSONArray.getString(0);
            String string8 = jSONArray.getString(1);
            String string9 = jSONArray.getString(2);
            String pluginIdFromToken2 = getPluginIdFromToken(string7);
            if (pluginIdFromToken2 == null) {
                callbackContext.error("INVALID_TOKEN");
                return true;
            }
            new EncryptedPreferenceManager(this.context, pluginIdFromToken2).setString(string8, string9);
            callbackContext.success();
            return true;
        }
        if ("delete_secret".equals(str)) {
            String string10 = jSONArray.getString(0);
            String string11 = jSONArray.getString(1);
            String pluginIdFromToken3 = getPluginIdFromToken(string10);
            if (pluginIdFromToken3 == null) {
                callbackContext.error("INVALID_TOKEN");
                return true;
            }
            new EncryptedPreferenceManager(this.context, pluginIdFromToken3).remove(string11);
            callbackContext.success();
            return true;
        }
        if ("clear_all_secrets".equals(str)) {
            String pluginIdFromToken4 = getPluginIdFromToken(jSONArray.getString(0));
            if (pluginIdFromToken4 == null) {
                callbackContext.error("INVALID_TOKEN");
                return true;
            }
            new EncryptedPreferenceManager(this.context, pluginIdFromToken4).clear();
            callbackContext.success();
            return true;
        }
        if ("invalidate".equals(str)) {
            String string12 = jSONArray.getString(0);
            String pluginIdFromToken5 = getPluginIdFromToken(string12);
            if (pluginIdFromToken5 == null) {
                callbackContext.error("INVALID_TOKEN");
                return true;
            }
            invalidateToken(string12, pluginIdFromToken5);
            callbackContext.success();
            return true;
        }
        if ("grantedPermission".equals(str)) {
            String string13 = jSONArray.getString(0);
            String string14 = jSONArray.getString(1);
            if (!this.permissionStore.containsKey(string13)) {
                callbackContext.error("INVALID_TOKEN");
                return true;
            }
            callbackContext.success(grantedPermission(string13, string14) ? 1 : 0);
            return true;
        }
        if (!"listAllPermissions".equals(str)) {
            return false;
        }
        String string15 = jSONArray.getString(0);
        if (!this.permissionStore.containsKey(string15)) {
            callbackContext.error("INVALID_TOKEN");
            return true;
        }
        callbackContext.success(new JSONArray((Collection) listAllPermissions(string15)));
        return true;
    }

    private String getPluginIdFromToken(String str) {
        for (Map.Entry<String, String> entry : this.tokenStore.entrySet()) {
            if (constantTimeEquals(str, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void invalidateToken(String str, String str2) {
        String str3 = this.tokenStore.get(str2);
        if (str3 != null && constantTimeEquals(str, str3)) {
            this.tokenStore.remove(str2);
        }
        this.permissionStore.remove(str);
    }

    private boolean isValidSession(String str) {
        String str2 = this.sessionId;
        return str2 != null && constantTimeEquals(str, str2);
    }

    public boolean isTokenValid(String str, String str2) {
        String str3 = this.tokenStore.get(str2);
        return str3 != null && constantTimeEquals(str, str3);
    }

    public boolean grantedPermission(String str, String str2) {
        List<String> list = this.permissionStore.get(str);
        return list != null && list.contains(str2);
    }

    public List<String> listAllPermissions(String str) {
        List<String> list = this.permissionStore.get(str);
        if (list == null) {
            return new ArrayList();
        }
        return new ArrayList(list);
    }

    private synchronized void handleTokenRequest(String str, String str2, CallbackContext callbackContext) {
        String strGenerateSecret = this.tokenStore.get(str);
        if (strGenerateSecret == null) {
            strGenerateSecret = generateSecret();
            this.tokenStore.put(str, strGenerateSecret);
        }
        try {
            JSONArray jSONArrayOptJSONArray = new JSONObject(str2).optJSONArray("permissions");
            ArrayList arrayList = new ArrayList();
            if (jSONArrayOptJSONArray != null) {
                for (int i = 0; i < jSONArrayOptJSONArray.length(); i++) {
                    arrayList.add(jSONArrayOptJSONArray.getString(i));
                }
            }
            this.permissionStore.put(strGenerateSecret, arrayList);
            callbackContext.success(strGenerateSecret);
        } catch (JSONException unused) {
            callbackContext.error("INVALID_PLUGIN_JSON");
        }
    }

    private static String generateSecret() {
        byte[] bArr = new byte[32];
        SECURE_RANDOM.nextBytes(bArr);
        return toHex(bArr);
    }

    private static String toHex(byte[] bArr) {
        StringBuilder sb = new StringBuilder(bArr.length * 2);
        for (byte b : bArr) {
            sb.append(Character.forDigit((b >> 4) & 15, 16));
            sb.append(Character.forDigit(b & 15, 16));
        }
        return sb.toString();
    }

    private static boolean constantTimeEquals(String str, String str2) {
        if (str == null || str2 == null) {
            return false;
        }
        return MessageDigest.isEqual(str.getBytes(StandardCharsets.UTF_8), str2.getBytes(StandardCharsets.UTF_8));
    }
}