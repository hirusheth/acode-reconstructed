package org.apache.cordova;

import java.security.SecureRandom;
import org.json.JSONArray;
import org.json.JSONException;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
public class CordovaBridge {
    private static final String LOG_TAG = "CordovaBridge";
    private volatile int expectedBridgeSecret = -1;
    private NativeToJsMessageQueue jsMessageQueue;
    private PluginManager pluginManager;

    public CordovaBridge(PluginManager pluginManager, NativeToJsMessageQueue nativeToJsMessageQueue) {
        this.pluginManager = pluginManager;
        this.jsMessageQueue = nativeToJsMessageQueue;
    }

    public String jsExec(int i, String str, String str2, String str3, String str4) throws JSONException, IllegalAccessException {
        String strPopAndEncode;
        if (!verifySecret("exec()", i)) {
            return null;
        }
        if (str4 == null) {
            return "@Null arguments.";
        }
        this.jsMessageQueue.setPaused(true);
        try {
            CordovaResourceApi.jsThread = Thread.currentThread();
            this.pluginManager.exec(str, str2, str3, str4);
            strPopAndEncode = this.jsMessageQueue.popAndEncode(false);
        } catch (Throwable th) {
            try {
                th.printStackTrace();
                strPopAndEncode = "";
            } finally {
                this.jsMessageQueue.setPaused(false);
            }
        }
        return strPopAndEncode;
    }

    public void jsSetNativeToJsBridgeMode(int i, int i2) throws IllegalAccessException {
        if (verifySecret("setNativeToJsBridgeMode()", i)) {
            this.jsMessageQueue.setBridgeMode(i2);
        }
    }

    public String jsRetrieveJsMessages(int i, boolean z) throws IllegalAccessException {
        if (verifySecret("retrieveJsMessages()", i)) {
            return this.jsMessageQueue.popAndEncode(z);
        }
        return null;
    }

    private boolean verifySecret(String str, int i) throws IllegalAccessException {
        if (!this.jsMessageQueue.isBridgeEnabled()) {
            if (i == -1) {
                LOG.d(LOG_TAG, str + " call made before bridge was enabled.");
                return false;
            }
            LOG.d(LOG_TAG, "Ignoring " + str + " from previous page load.");
            return false;
        }
        if (this.expectedBridgeSecret >= 0 && i == this.expectedBridgeSecret) {
            return true;
        }
        LOG.e(LOG_TAG, "Bridge access attempt with wrong secret token, possibly from malicious code. Disabling exec() bridge!");
        clearBridgeSecret();
        throw new IllegalAccessException();
    }

    void clearBridgeSecret() {
        this.expectedBridgeSecret = -1;
    }

    public boolean isSecretEstablished() {
        return this.expectedBridgeSecret != -1;
    }

    int generateBridgeSecret() {
        this.expectedBridgeSecret = new SecureRandom().nextInt(Integer.MAX_VALUE);
        return this.expectedBridgeSecret;
    }

    public void reset() {
        this.jsMessageQueue.reset();
        clearBridgeSecret();
    }

    public String promptOnJsPrompt(String str, String str2, String str3) {
        if (str3 != null && str3.startsWith("gap:")) {
            try {
                JSONArray jSONArray = new JSONArray(str3.substring(4));
                try {
                    String strJsExec = jsExec(jSONArray.getInt(0), jSONArray.getString(1), jSONArray.getString(2), jSONArray.getString(3), str2);
                    return strJsExec == null ? "" : strJsExec;
                } catch (IllegalAccessException e) {
                    e = e;
                    e.printStackTrace();
                    return "";
                } catch (JSONException e2) {
                    e = e2;
                    e.printStackTrace();
                    return "";
                }
            } catch (IllegalAccessException e3) {
                e = e3;
            } catch (JSONException e4) {
                e = e4;
            }
        } else {
            if (str3 != null && str3.startsWith("gap_bridge_mode:")) {
                try {
                    jsSetNativeToJsBridgeMode(Integer.parseInt(str3.substring(16)), Integer.parseInt(str2));
                } catch (IllegalAccessException e5) {
                    e5.printStackTrace();
                } catch (NumberFormatException e6) {
                    e6.printStackTrace();
                }
                return "";
            }
            if (str3 != null && str3.startsWith("gap_poll:")) {
                try {
                    String strJsRetrieveJsMessages = jsRetrieveJsMessages(Integer.parseInt(str3.substring(9)), "1".equals(str2));
                    return strJsRetrieveJsMessages == null ? "" : strJsRetrieveJsMessages;
                } catch (IllegalAccessException e7) {
                    e7.printStackTrace();
                    return "";
                }
            }
            if (str3 == null || !str3.startsWith("gap_init:")) {
                return null;
            }
            if (this.pluginManager.shouldAllowBridgeAccess(str)) {
                this.jsMessageQueue.setBridgeMode(Integer.parseInt(str3.substring(9)));
                return "" + generateBridgeSecret();
            }
            LOG.e(LOG_TAG, "gap_init called from restricted origin: " + str);
            return "";
        }
    }
}