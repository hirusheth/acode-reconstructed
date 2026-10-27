package com.foxdebug.acode.rk.auth;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.WebView;
import androidx.browser.customtabs.CustomTabsIntent;
import com.silkimen.http.HttpRequest;
import com.sshtools.common.ssh.components.jce.JCEAlgorithms;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class Authenticator extends CordovaPlugin {
    private static final int AUTH_CONNECT_TIMEOUT_MS = 15000;
    private static final int AUTH_READ_TIMEOUT_MS = 30000;
    private static final String KEY_MIGRATED_V2 = "migrated_host_to_domain_cookies";
    private static final String KEY_PENDING_BASE_URL = "pending_login_base_url";
    private static final String KEY_PENDING_STATE = "pending_login_state";
    private static final String KEY_PENDING_VERIFIER = "pending_login_verifier";
    private static final String KEY_TOKEN = "auth_token";
    private static final String PREFS_FILENAME = "acode_auth_secure";
    private static final String PRO_PURCHASED = "pro_purchased";
    private static final String TAG = "AcodeAuth";
    private volatile CallbackContext loginCallback;
    private final Object loginCallbackLock = new Object();
    private EncryptedPreferenceManager prefManager;
    private static final String[] API_ORIGINS = {"https://acode.app"};
    private static final String[] LEGACY_ORIGINS = {"https://acode.app", "https://dev.acode.app"};

    protected void pluginInitialize() {
        Log.d(TAG, "Initializing Authenticator Plugin...");
        this.prefManager = new EncryptedPreferenceManager(this.cordova.getContext(), PREFS_FILENAME);
        CookieManager.getInstance().setAcceptThirdPartyCookies((WebView) this.webView.getView(), true);
        if (!this.prefManager.getBoolean(KEY_MIGRATED_V2, false)) {
            Log.d(TAG, "Migrating: clearing legacy host-scoped cookies");
            clearLegacyCookies();
            this.prefManager.setBoolean(KEY_MIGRATED_V2, true);
        }
        String string = this.prefManager.getString(KEY_TOKEN, "");
        if (!string.isEmpty()) {
            m108lambda$handleAuthCallback$3$comfoxdebugacoderkauthAuthenticator(string);
        }
        handleAuthCallback(this.cordova.getActivity().getIntent());
    }

    public boolean execute(String str, JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "Native Action Called: " + str);
        str.hashCode();
        switch (str) {
            case "logout":
                this.prefManager.remove(KEY_TOKEN);
                this.cordova.getActivity().runOnUiThread(new Runnable() { // from class: com.foxdebug.acode.rk.auth.Authenticator$$ExternalSyntheticLambda2
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m106lambda$execute$0$comfoxdebugacoderkauthAuthenticator();
                    }
                });
                if (callbackContext != null) {
                    callbackContext.success();
                }
                return true;
            case "login":
                JSONObject jSONObjectOptJSONObject = jSONArray.optJSONObject(0);
                if (jSONObjectOptJSONObject == null) {
                    jSONObjectOptJSONObject = new JSONObject();
                }
                startLogin(jSONObjectOptJSONObject, callbackContext);
                return true;
            case "saveToken":
                final String string = jSONArray.getString(0);
                Log.d(TAG, "Saving new token...");
                this.prefManager.setString(KEY_TOKEN, string);
                this.cordova.getActivity().runOnUiThread(new Runnable() { // from class: com.foxdebug.acode.rk.auth.Authenticator$$ExternalSyntheticLambda3
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m107lambda$execute$1$comfoxdebugacoderkauthAuthenticator(string);
                    }
                });
                callbackContext.success();
                return true;
            default:
                Log.w(TAG, "Attempted to call unknown action: " + str);
                return false;
        }
    }

    public void onNewIntent(Intent intent) {
        if (handleAuthCallback(intent)) {
            return;
        }
        super.onNewIntent(intent);
    }

    private void startLogin(JSONObject jSONObject, final CallbackContext callbackContext) {
        String strOptString = jSONObject.optString("baseUrl", "https://acode.app");
        int iOptInt = jSONObject.optInt("appVersionCode", 0);
        String strRandomHex = randomHex(24);
        String strRandomHex2 = randomHex(32);
        String strSha256Hex = sha256Hex(strRandomHex2);
        this.prefManager.setString(KEY_PENDING_STATE, strRandomHex);
        this.prefManager.setString(KEY_PENDING_VERIFIER, strRandomHex2);
        this.prefManager.setString(KEY_PENDING_BASE_URL, strOptString);
        setLoginCallback(callbackContext);
        final Uri uriBuild = Uri.parse(strOptString).buildUpon().appendEncodedPath("login").appendQueryParameter("redirect", "app").appendQueryParameter("authFlow", "app-code").appendQueryParameter("state", strRandomHex).appendQueryParameter("challenge", strSha256Hex).appendQueryParameter("appVersionCode", String.valueOf(iOptInt)).build();
        this.cordova.getActivity().runOnUiThread(new Runnable() { // from class: com.foxdebug.acode.rk.auth.Authenticator$$ExternalSyntheticLambda0
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m110lambda$startLogin$2$comfoxdebugacoderkauthAuthenticator(uriBuild, callbackContext);
            }
        });
    }

    /* JADX INFO: renamed from: lambda$startLogin$2$com-foxdebug-acode-rk-auth-Authenticator, reason: not valid java name */
    /* synthetic */ void m110lambda$startLogin$2$comfoxdebugacoderkauthAuthenticator(Uri uri, CallbackContext callbackContext) {
        try {
            CustomTabsIntent customTabsIntentBuild = new CustomTabsIntent.Builder().build();
            customTabsIntentBuild.intent.putExtra(CustomTabsIntent.EXTRA_TITLE_VISIBILITY_STATE, 1);
            customTabsIntentBuild.launchUrl(this.cordova.getActivity(), uri);
            PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
            pluginResult.setKeepCallback(true);
            callbackContext.sendPluginResult(pluginResult);
        } catch (Exception unused) {
            try {
                this.cordova.getActivity().startActivity(new Intent("android.intent.action.VIEW", uri));
                PluginResult pluginResult2 = new PluginResult(PluginResult.Status.NO_RESULT);
                pluginResult2.setKeepCallback(true);
                callbackContext.sendPluginResult(pluginResult2);
            } catch (Exception e) {
                failLogin(e.getMessage());
            }
        }
    }

    private boolean handleAuthCallback(Intent intent) {
        Uri data = intent != null ? intent.getData() : null;
        if (data == null || !"acode".equals(data.getScheme()) || !"auth".equals(data.getHost()) || !"/callback".equals(data.getPath())) {
            return false;
        }
        final String queryParameter = data.getQueryParameter("code");
        final String queryParameter2 = data.getQueryParameter("state");
        String string = this.prefManager.getString(KEY_PENDING_STATE, "");
        final String string2 = this.prefManager.getString(KEY_PENDING_VERIFIER, "");
        final String string3 = this.prefManager.getString(KEY_PENDING_BASE_URL, "https://acode.app");
        if (queryParameter == null || queryParameter2 == null || string.isEmpty() || string2.isEmpty() || !string.equals(queryParameter2)) {
            failLogin("Invalid login callback");
            return true;
        }
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.acode.rk.auth.Authenticator$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m109lambda$handleAuthCallback$4$comfoxdebugacoderkauthAuthenticator(string3, queryParameter, queryParameter2, string2);
            }
        });
        return true;
    }

    /* JADX INFO: renamed from: lambda$handleAuthCallback$4$com-foxdebug-acode-rk-auth-Authenticator, reason: not valid java name */
    /* synthetic */ void m109lambda$handleAuthCallback$4$comfoxdebugacoderkauthAuthenticator(String str, String str2, String str3, String str4) {
        try {
            final String strExchangeCode = exchangeCode(str, str2, str3, str4);
            this.prefManager.setString(KEY_TOKEN, strExchangeCode);
            this.prefManager.remove(KEY_PENDING_STATE);
            this.prefManager.remove(KEY_PENDING_VERIFIER);
            this.prefManager.remove(KEY_PENDING_BASE_URL);
            this.cordova.getActivity().runOnUiThread(new Runnable() { // from class: com.foxdebug.acode.rk.auth.Authenticator$$ExternalSyntheticLambda1
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m108lambda$handleAuthCallback$3$comfoxdebugacoderkauthAuthenticator(strExchangeCode);
                }
            });
            CallbackContext callbackContextTakeLoginCallback = takeLoginCallback();
            if (callbackContextTakeLoginCallback != null) {
                callbackContextTakeLoginCallback.success();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to exchange app auth code", e);
            failLogin("Unable to complete login");
        }
    }

    private String exchangeCode(String str, String str2, String str3, String str4) throws Exception {
        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(str + "/api/user/app-token/exchange").openConnection();
        try {
            httpURLConnection.setConnectTimeout(AUTH_CONNECT_TIMEOUT_MS);
            httpURLConnection.setReadTimeout(AUTH_READ_TIMEOUT_MS);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty(HttpRequest.HEADER_CONTENT_TYPE, HttpRequest.CONTENT_TYPE_JSON);
            httpURLConnection.setDoOutput(true);
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("code", str2);
            jSONObject.put("state", str3);
            jSONObject.put("verifier", str4);
            OutputStream outputStream = httpURLConnection.getOutputStream();
            try {
                outputStream.write(jSONObject.toString().getBytes(StandardCharsets.UTF_8));
                if (outputStream != null) {
                    outputStream.close();
                }
                int responseCode = httpURLConnection.getResponseCode();
                String stream = readStream((responseCode < 200 || responseCode >= 300) ? httpURLConnection.getErrorStream() : httpURLConnection.getInputStream());
                if (responseCode < 200 || responseCode >= 300) {
                    throw new IllegalStateException(stream);
                }
                String strOptString = new JSONObject(stream).optString("token", "");
                if (strOptString.isEmpty()) {
                    throw new IllegalStateException("Missing token");
                }
                httpURLConnection.disconnect();
                return strOptString;
            } catch (Throwable th) {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        } catch (Throwable th3) {
            httpURLConnection.disconnect();
            throw th3;
        }
    }

    private String readStream(InputStream inputStream) throws Exception {
        if (inputStream == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        while (true) {
            try {
                String line = bufferedReader.readLine();
                if (line != null) {
                    sb.append(line);
                } else {
                    bufferedReader.close();
                    return sb.toString();
                }
            } catch (Throwable th) {
                try {
                    bufferedReader.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
                throw th;
            }
        }
    }

    private void failLogin(String str) {
        this.prefManager.remove(KEY_PENDING_STATE);
        this.prefManager.remove(KEY_PENDING_VERIFIER);
        this.prefManager.remove(KEY_PENDING_BASE_URL);
        CallbackContext callbackContextTakeLoginCallback = takeLoginCallback();
        if (callbackContextTakeLoginCallback != null) {
            callbackContextTakeLoginCallback.error(str);
        }
    }

    private void setLoginCallback(CallbackContext callbackContext) {
        CallbackContext callbackContext2;
        synchronized (this.loginCallbackLock) {
            callbackContext2 = this.loginCallback;
            this.loginCallback = callbackContext;
        }
        if (callbackContext2 != null) {
            callbackContext2.error("Login cancelled");
        }
    }

    private CallbackContext takeLoginCallback() {
        CallbackContext callbackContext;
        synchronized (this.loginCallbackLock) {
            callbackContext = this.loginCallback;
            this.loginCallback = null;
        }
        return callbackContext;
    }

    private String randomHex(int i) {
        byte[] bArr = new byte[i];
        new SecureRandom().nextBytes(bArr);
        StringBuilder sb = new StringBuilder(i * 2);
        for (int i2 = 0; i2 < i; i2++) {
            sb.append(String.format("%02x", Byte.valueOf(bArr[i2])));
        }
        return sb.toString();
    }

    private String sha256Hex(String str) {
        try {
            byte[] bArrDigest = MessageDigest.getInstance(JCEAlgorithms.JCE_SHA256).digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bArrDigest.length * 2);
            for (byte b : bArrDigest) {
                sb.append(String.format("%02x", Byte.valueOf(b)));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create auth challenge", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: setTokenCookie, reason: merged with bridge method [inline-methods] and merged with bridge method [inline-methods] */
    public void m108lambda$handleAuthCallback$3$comfoxdebugacoderkauthAuthenticator(String str) {
        CookieManager cookieManager = CookieManager.getInstance();
        for (String str2 : API_ORIGINS) {
            cookieManager.setCookie(str2, "token=" + str + "; Domain=.acode.app; Path=/; Secure; HttpOnly; SameSite=None");
        }
        cookieManager.flush();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX INFO: renamed from: clearTokenCookie, reason: merged with bridge method [inline-methods] */
    public void m106lambda$execute$0$comfoxdebugacoderkauthAuthenticator() {
        CookieManager cookieManager = CookieManager.getInstance();
        for (String str : API_ORIGINS) {
            cookieManager.setCookie(str, "token=; Domain=.acode.app; Path=/; Max-Age=0; Secure; HttpOnly; SameSite=None");
        }
        cookieManager.flush();
    }

    private void clearLegacyCookies() {
        CookieManager cookieManager = CookieManager.getInstance();
        for (String str : LEGACY_ORIGINS) {
            cookieManager.setCookie(str, "token=; Path=/; Max-Age=0; Secure; HttpOnly; SameSite=None");
        }
        cookieManager.flush();
    }
}