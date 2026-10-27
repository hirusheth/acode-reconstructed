package com.foxdebug.websocket;

import android.util.Base64;
import android.util.Log;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class WebSocketInstance extends WebSocketListener {
    private static final int DEFAULT_CLOSE_CODE = 1000;
    private static final String DEFAULT_CLOSE_REASON = "Normal closure";
    private static final String TAG = "WebSocketInstance";
    private String binaryType;
    private CallbackContext callbackContext;
    private final CordovaInterface cordova;
    private final String instanceId;
    private WebSocket webSocket;
    private String extensions = "";
    private String protocol = "";
    private int readyState = 0;

    public WebSocketInstance(String str, JSONArray jSONArray, JSONObject jSONObject, String str2, OkHttpClient okHttpClient, CordovaInterface cordovaInterface, String str3) {
        this.cordova = cordovaInterface;
        this.instanceId = str3;
        this.binaryType = str2;
        OkHttpClient okHttpClientBuild = okHttpClient.newBuilder().connectTimeout(10L, TimeUnit.SECONDS).build();
        Request.Builder builderUrl = new Request.Builder().url(str);
        if (jSONObject != null) {
            Iterator<String> itKeys = jSONObject.keys();
            while (itKeys.hasNext()) {
                String next = itKeys.next();
                builderUrl.addHeader(next, jSONObject.optString(next));
            }
        }
        if (jSONArray != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < jSONArray.length(); i++) {
                sb.append(jSONArray.optString(i)).append(",");
            }
            if (sb.length() > 0) {
                sb.setLength(sb.length() - 1);
                builderUrl.addHeader("Sec-WebSocket-Protocol", sb.toString());
            }
        }
        okHttpClientBuild.newWebSocket(builderUrl.build(), this);
    }

    public void setCallback(CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }

    public void send(String str, boolean z) {
        if (this.webSocket != null) {
            Log.d(TAG, "websocket instanceId=" + this.instanceId + " received send(..., isBinary=" + z + ") action call, sending message=" + str);
            if (z) {
                sendBinary(str);
                return;
            } else {
                this.webSocket.send(str);
                return;
            }
        }
        Log.d(TAG, "websocket instanceId=" + this.instanceId + " received send(..., isBinary=" + z + ")  ignoring... as webSocket is null (not present/connected)");
    }

    private void sendBinary(String str) {
        this.webSocket.send(ByteString.of(Base64.decode(str, 0)));
    }

    public String close(int i, String str) {
        WebSocket webSocket = this.webSocket;
        if (webSocket != null) {
            this.readyState = 2;
            try {
                Log.d(TAG, "websocket instanceId=" + this.instanceId + " received close() action call, code=" + i + " reason=" + str + " close method result: " + webSocket.close(i, str));
                return null;
            } catch (Exception e) {
                return e.getMessage();
            }
        }
        Log.d(TAG, "websocket instanceId=" + this.instanceId + " received close() action call, ignoring... as webSocket is null (not present)");
        return "";
    }

    public String close() {
        Log.d(TAG, "WebSocket instanceId=" + this.instanceId + " close() called with no arguments. Using defaults.");
        return close(1000, DEFAULT_CLOSE_REASON);
    }

    public void onOpen(WebSocket webSocket, Response response) {
        this.webSocket = webSocket;
        this.readyState = 1;
        this.extensions = response.headers("Sec-WebSocket-Extensions").toString();
        this.protocol = response.header("Sec-WebSocket-Protocol");
        Log.i(TAG, "websocket instanceId=" + this.instanceId + " Openedreceived extensions=" + this.extensions);
        sendEvent("open", null, false, false);
    }

    public void onMessage(WebSocket webSocket, String str) {
        Log.d(TAG, "websocket instanceId=" + this.instanceId + " Received message: " + str);
        sendEvent("message", str, false, false);
    }

    public void onMessage(WebSocket webSocket, ByteString byteString) {
        Log.d(TAG, "websocket instanceId=" + this.instanceId + " Received message(bytes/binary payload): " + byteString.toString());
        try {
            if ("arraybuffer".equals(this.binaryType)) {
                sendEvent("message", byteString.base64(), true, false);
            } else {
                sendEvent("message", byteString.utf8(), true, true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
        }
    }

    public void onClosing(WebSocket webSocket, int i, String str) {
        this.readyState = 2;
        Log.i(TAG, "websocket instanceId=" + this.instanceId + " is Closing code: " + i + " reason: " + str);
        this.webSocket.close(i, str);
    }

    public void onClosed(WebSocket webSocket, int i, String str) {
        this.readyState = 3;
        Log.i(TAG, "websocket instanceId=" + this.instanceId + " Closed code: " + i + " reason: " + str);
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("code", i);
            jSONObject.put("reason", str);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating close event", e);
        }
        sendEvent("close", jSONObject.toString(), false, false);
        WebSocketPlugin.removeInstance(this.instanceId);
    }

    public void onFailure(WebSocket webSocket, Throwable th, Response response) {
        this.readyState = 3;
        sendEvent("error", th.getMessage(), false, false);
        Log.e(TAG, "websocket instanceId=" + this.instanceId + " Error: " + th.getMessage());
    }

    public void setBinaryType(String str) {
        this.binaryType = str;
    }

    private void sendEvent(String str, String str2, boolean z, boolean z2) {
        if (this.callbackContext != null) {
            try {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("type", str);
                jSONObject.put("extensions", this.extensions);
                jSONObject.put("readyState", this.readyState);
                jSONObject.put("isBinary", z);
                jSONObject.put("parseAsText", z2);
                if (str2 != null) {
                    jSONObject.put("data", str2);
                }
                Log.d(TAG, "sending event: " + str + " eventObj " + jSONObject.toString());
                PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jSONObject);
                pluginResult.setKeepCallback(true);
                this.callbackContext.sendPluginResult(pluginResult);
            } catch (Exception e) {
                Log.e(TAG, "Error sending event", e);
            }
        }
    }
}