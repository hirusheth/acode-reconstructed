package com.foxdebug.websocket;

import android.util.Log;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import okhttp3.OkHttpClient;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class WebSocketPlugin extends CordovaPlugin {
    private static final ConcurrentHashMap<String, WebSocketInstance> instances = new ConcurrentHashMap<>();
    public OkHttpClient okHttpMainClient = null;

    protected void pluginInitialize() {
        this.okHttpMainClient = new OkHttpClient();
    }

    public boolean execute(final String str, final JSONArray jSONArray, final CallbackContext callbackContext) throws JSONException {
        this.cordova.getThreadPool().execute(new Runnable() { // from class: com.foxdebug.websocket.WebSocketPlugin.1
            @Override // java.lang.Runnable
            public void run() {
                String str2 = str;
                str2.hashCode();
                switch (str2) {
                    case "setBinaryType":
                        String strOptString = jSONArray.optString(0);
                        String strOptString2 = jSONArray.optString(1);
                        WebSocketInstance webSocketInstance = (WebSocketInstance) WebSocketPlugin.instances.get(strOptString);
                        if (webSocketInstance != null) {
                            webSocketInstance.setBinaryType(strOptString2);
                            break;
                        } else {
                            Log.d("WebSocketPlugin", "setBinaryType called for instanceId=" + strOptString + " but It's not found. ignoring....");
                            break;
                        }
                        break;
                    case "send":
                        String strOptString3 = jSONArray.optString(0);
                        String strOptString4 = jSONArray.optString(1);
                        boolean zOptBoolean = jSONArray.optBoolean(2, false);
                        WebSocketInstance webSocketInstance2 = (WebSocketInstance) WebSocketPlugin.instances.get(strOptString3);
                        Log.d("WebSocketPlugin", "send called");
                        if (webSocketInstance2 != null) {
                            webSocketInstance2.send(strOptString4, zOptBoolean);
                            callbackContext.success();
                            break;
                        } else {
                            callbackContext.error("Invalid instance ID");
                            break;
                        }
                        break;
                    case "close":
                        String strOptString5 = jSONArray.optString(0);
                        int iOptInt = jSONArray.optInt(1, 1000);
                        String strOptString6 = jSONArray.optString(2, "Normal closure");
                        WebSocketInstance webSocketInstance3 = (WebSocketInstance) WebSocketPlugin.instances.get(strOptString5);
                        if (webSocketInstance3 != null) {
                            String strClose = webSocketInstance3.close(iOptInt, strOptString6);
                            if (strClose == null) {
                                callbackContext.success();
                            } else if (!strClose.isEmpty()) {
                                callbackContext.error(strClose);
                            }
                            break;
                        } else {
                            callbackContext.error("Invalid instance ID");
                            break;
                        }
                        break;
                    case "connect":
                        String strOptString7 = jSONArray.optString(0);
                        JSONArray jSONArrayOptJSONArray = jSONArray.optJSONArray(1);
                        JSONObject jSONObjectOptJSONObject = jSONArray.optJSONObject(2);
                        String strOptString8 = jSONArray.optString(3, null);
                        String string = UUID.randomUUID().toString();
                        WebSocketPlugin.instances.put(string, new WebSocketInstance(strOptString7, jSONArrayOptJSONArray, jSONObjectOptJSONObject, strOptString8, WebSocketPlugin.this.okHttpMainClient, WebSocketPlugin.this.cordova, string));
                        callbackContext.success(string);
                        break;
                    case "registerListener":
                        WebSocketInstance webSocketInstance4 = (WebSocketInstance) WebSocketPlugin.instances.get(jSONArray.optString(0));
                        if (webSocketInstance4 != null) {
                            webSocketInstance4.setCallback(callbackContext);
                            break;
                        } else {
                            callbackContext.error("Invalid instance ID");
                            break;
                        }
                        break;
                    case "listClients":
                        JSONArray jSONArray2 = new JSONArray();
                        Iterator it = WebSocketPlugin.instances.keySet().iterator();
                        while (it.hasNext()) {
                            jSONArray2.put((String) it.next());
                        }
                        callbackContext.success(jSONArray2);
                        break;
                }
            }
        });
        return true;
    }

    public void onDestroy() {
        Iterator<WebSocketInstance> it = instances.values().iterator();
        while (it.hasNext()) {
            it.next().close();
        }
        instances.clear();
        this.okHttpMainClient.dispatcher().executorService().shutdown();
        Log.i("WebSocketPlugin", "cleaned up... on destroy");
    }

    public static void removeInstance(String str) {
        instances.remove(str);
    }
}