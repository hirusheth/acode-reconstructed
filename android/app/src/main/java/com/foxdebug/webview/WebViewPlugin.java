package com.foxdebug.webview;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.webkit.CookieManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.silkimen.http.HttpRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class WebViewPlugin extends CordovaPlugin {
    private static final String TAG = "AcodeWebView";
    private static WebViewPlugin instance;
    private final HashMap<String, WebViewInstance> instances = new HashMap<>();
    private CallbackContext messageCallback;

    protected void pluginInitialize() {
        instance = this;
    }

    public static WebViewPlugin getInstance() {
        return instance;
    }

    /* JADX WARN: Code duplicated, block: B:36:0x0073  */
    public boolean execute(String str, JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        try {
            switch (str) {
                case "setMessageCallback":
                    this.messageCallback = callbackContext;
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
                    pluginResult.setKeepCallback(true);
                    callbackContext.sendPluginResult(pluginResult);
                    break;
                case "create":
                    create(jSONArray.getJSONObject(0), callbackContext);
                    break;
                case "loadURL":
                    loadURL(jSONArray.getString(0), jSONArray.getString(1), callbackContext);
                    break;
                case "loadHTML":
                    loadHTML(jSONArray.getString(0), jSONArray.getString(1), callbackContext);
                    break;
                case "evaluate":
                    evaluate(jSONArray.getString(0), jSONArray.getString(1), callbackContext);
                    break;
                case "postMessage":
                    postMessage(jSONArray.getString(0), jSONArray.getString(1), callbackContext);
                    break;
                case "show":
                    show(jSONArray.getString(0), callbackContext);
                    break;
                case "hide":
                    hide(jSONArray.getString(0), callbackContext);
                    break;
                case "reload":
                    reload(jSONArray.getString(0), callbackContext);
                    break;
                case "destroy":
                    destroy(jSONArray.getString(0), callbackContext);
                    break;
                default:
                    callbackContext.error("Unknown action: " + str);
                    break;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error: " + str, e);
            callbackContext.error(e.getMessage());
            return true;
        }
    }

    private String generateId() {
        return "wv_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private void create(JSONObject jSONObject, final CallbackContext callbackContext) throws JSONException {
        final String strGenerateId = generateId();
        final String strOptString = jSONObject.optString("mode", "hidden");
        final String strOptString2 = jSONObject.optString("title", "");
        final boolean zOptBoolean = jSONObject.optBoolean("allowNavigation", true);
        final boolean zOptBoolean2 = jSONObject.optBoolean("allowDownloads", false);
        final boolean zOptBoolean3 = jSONObject.optBoolean("visible", true);
        this.cordova.getActivity().runOnUiThread(new Runnable() { // from class: com.foxdebug.webview.WebViewPlugin.1
            @Override // java.lang.Runnable
            public void run() {
                WebViewInstance webViewInstance = new WebViewInstance(strGenerateId, strOptString, strOptString2, zOptBoolean, zOptBoolean2, WebViewPlugin.this);
                WebViewPlugin.this.instances.put(strGenerateId, webViewInstance);
                try {
                    if (webViewInstance.isFullscreen()) {
                        if (zOptBoolean3) {
                            WebViewPlugin.this.showFullscreenActivity(strGenerateId);
                        }
                    } else {
                        webViewInstance.createWebView(WebViewPlugin.this.cordova.getActivity());
                    }
                    callbackContext.success(strGenerateId);
                } catch (Exception e) {
                    WebViewPlugin.this.instances.remove(strGenerateId);
                    try {
                        webViewInstance.destroy();
                    } catch (Exception unused) {
                    }
                    Log.e(WebViewPlugin.TAG, "Create error: " + e.getMessage(), e);
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    void showFullscreenActivity(String str) {
        Intent intent = new Intent(this.cordova.getActivity(), (Class<?>) WebViewActivity.class);
        intent.putExtra("webviewId", str);
        WebViewInstance webViewPlugin = getInstance(str);
        WebViewActivity hostingActivity = webViewPlugin != null ? webViewPlugin.getHostingActivity() : null;
        if (hostingActivity != null && !hostingActivity.isFinishing() && !hostingActivity.isDestroyed()) {
            intent.addFlags(131072);
        }
        this.cordova.getActivity().startActivity(intent);
    }

    public WebViewInstance getInstance(String str) {
        return this.instances.get(str);
    }

    public void removeInstance(String str) {
        this.instances.remove(str);
    }

    private void loadURL(String str, String str2, CallbackContext callbackContext) {
        WebViewInstance webViewPlugin = getInstance(str);
        if (webViewPlugin == null) {
            callbackContext.error("WebView not found: " + str);
        } else {
            webViewPlugin.loadURL(str2, callbackContext);
        }
    }

    private void loadHTML(String str, String str2, CallbackContext callbackContext) {
        WebViewInstance webViewPlugin = getInstance(str);
        if (webViewPlugin == null) {
            callbackContext.error("WebView not found: " + str);
        } else {
            webViewPlugin.loadHTML(str2, callbackContext);
        }
    }

    private void evaluate(String str, String str2, CallbackContext callbackContext) {
        WebViewInstance webViewPlugin = getInstance(str);
        if (webViewPlugin == null) {
            callbackContext.error("WebView not found: " + str);
        } else {
            webViewPlugin.evaluate(str2, callbackContext);
        }
    }

    private void postMessage(String str, String str2, CallbackContext callbackContext) {
        WebViewInstance webViewPlugin = getInstance(str);
        if (webViewPlugin == null) {
            callbackContext.error("WebView not found: " + str);
        } else {
            webViewPlugin.postMessage(str2, callbackContext);
        }
    }

    private void show(String str, CallbackContext callbackContext) {
        WebViewInstance webViewPlugin = getInstance(str);
        if (webViewPlugin == null) {
            callbackContext.error("WebView not found: " + str);
        } else {
            webViewPlugin.show(callbackContext);
        }
    }

    private void hide(String str, CallbackContext callbackContext) {
        WebViewInstance webViewPlugin = getInstance(str);
        if (webViewPlugin == null) {
            callbackContext.error("WebView not found: " + str);
        } else {
            webViewPlugin.hide(callbackContext);
        }
    }

    private void reload(String str, CallbackContext callbackContext) {
        WebViewInstance webViewPlugin = getInstance(str);
        if (webViewPlugin == null) {
            callbackContext.error("WebView not found: " + str);
        } else {
            webViewPlugin.reload(callbackContext);
        }
    }

    private void destroy(String str, final CallbackContext callbackContext) {
        final WebViewInstance webViewInstanceRemove = this.instances.remove(str);
        if (webViewInstanceRemove == null) {
            callbackContext.error("WebView not found: " + str);
        } else {
            this.cordova.getActivity().runOnUiThread(new Runnable() { // from class: com.foxdebug.webview.WebViewPlugin.2
                @Override // java.lang.Runnable
                public void run() {
                    webViewInstanceRemove.destroy();
                    callbackContext.success();
                }
            });
        }
    }

    void download(String str, String str2, String str3, String str4) {
        AppCompatActivity activity = this.cordova.getActivity();
        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(str));
            request.setMimeType(str3);
            request.addRequestHeader(HttpRequest.HEADER_USER_AGENT, str2);
            String cookie = CookieManager.getInstance().getCookie(str);
            if (cookie != null && !cookie.isEmpty()) {
                request.addRequestHeader("Cookie", cookie);
            }
            request.setDescription("Downloading file...");
            request.setTitle(str4);
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(1);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, str4);
            DownloadManager downloadManager = (DownloadManager) activity.getSystemService("download");
            if (downloadManager == null) {
                throw new IllegalStateException("DownloadManager unavailable");
            }
            downloadManager.enqueue(request);
            Toast.makeText(activity, "Download started...", 0).show();
        } catch (Exception e) {
            Log.e(TAG, "Download failed", e);
            Toast.makeText(activity, "Download failed", 0).show();
        }
    }

    public void sendMessageToCordova(String str, String str2) {
        if (this.messageCallback == null) {
            return;
        }
        try {
            JSONObject jSONObject = new JSONObject();
            jSONObject.put("id", str);
            jSONObject.put("message", str2);
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jSONObject);
            pluginResult.setKeepCallback(true);
            this.messageCallback.sendPluginResult(pluginResult);
        } catch (JSONException e) {
            Log.e(TAG, "Error building message payload", e);
        }
    }

    public void sendEventToCordova(String str, String str2, JSONObject jSONObject) {
        if (this.messageCallback == null) {
            return;
        }
        try {
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("id", str);
            jSONObject2.put(NotificationCompat.CATEGORY_EVENT, str2);
            if (jSONObject != null) {
                jSONObject2.put("data", jSONObject);
            }
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, jSONObject2);
            pluginResult.setKeepCallback(true);
            this.messageCallback.sendPluginResult(pluginResult);
        } catch (JSONException e) {
            Log.e(TAG, "Error building event payload", e);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        Iterator it = new ArrayList(this.instances.values()).iterator();
        while (it.hasNext()) {
            try {
                ((WebViewInstance) it.next()).destroy();
            } catch (Exception unused) {
            }
        }
        this.instances.clear();
        instance = null;
    }
}