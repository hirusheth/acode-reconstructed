package com.foxdebug.server;

import java.io.IOException;
import java.util.HashMap;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class Server extends CordovaPlugin {
    public HashMap<Integer, NanoHTTPDWebserver> servers;

    public void initialize(CordovaInterface cordovaInterface, CordovaWebView cordovaWebView) {
        super.initialize(cordovaInterface, cordovaWebView);
        this.servers = new HashMap<>();
    }

    public boolean execute(String str, JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        if ("start".equals(str)) {
            try {
                start(jSONArray, callbackContext);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        if ("setOnRequestHandler".equals(str)) {
            setOnRequestHandler(jSONArray, callbackContext);
            return true;
        }
        if ("stop".equals(str)) {
            stop(jSONArray, callbackContext);
            return true;
        }
        if (!"send".equals(str)) {
            return false;
        }
        send(jSONArray, callbackContext);
        return true;
    }

    private void start(JSONArray jSONArray, CallbackContext callbackContext) throws JSONException, IOException {
        Integer numValueOf = 8080;
        if (jSONArray.length() == 1) {
            numValueOf = Integer.valueOf(jSONArray.getInt(0));
        }
        if (this.servers.get(numValueOf) != null) {
            callbackContext.success("Server started on port " + numValueOf);
            return;
        }
        try {
            NanoHTTPDWebserver nanoHTTPDWebserver = new NanoHTTPDWebserver(numValueOf.intValue(), this.cordova.getContext());
            nanoHTTPDWebserver.start();
            this.servers.put(numValueOf, nanoHTTPDWebserver);
            callbackContext.success("Server started on port " + numValueOf);
        } catch (Exception e) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, e.getMessage()));
        }
    }

    private void setOnRequestHandler(JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        Integer numValueOf = Integer.valueOf(jSONArray.getInt(0));
        NanoHTTPDWebserver nanoHTTPDWebserver = this.servers.get(numValueOf);
        if (nanoHTTPDWebserver == null) {
            callbackContext.error("Server not started on port " + numValueOf);
        } else {
            nanoHTTPDWebserver.onRequestCallbackContext = callbackContext;
        }
    }

    private void stop(JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        Integer numValueOf = Integer.valueOf(jSONArray.getInt(0));
        NanoHTTPDWebserver nanoHTTPDWebserver = this.servers.get(numValueOf);
        if (nanoHTTPDWebserver == null) {
            callbackContext.error("Server not started on port " + numValueOf);
            return;
        }
        nanoHTTPDWebserver.stop();
        this.servers.remove(numValueOf);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
    }

    private void send(JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        NanoHTTPDWebserver nanoHTTPDWebserver = this.servers.get(Integer.valueOf(jSONArray.getInt(0)));
        if (nanoHTTPDWebserver == null) {
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Server not running"));
        } else {
            nanoHTTPDWebserver.responses.put(jSONArray.getString(1), jSONArray.get(2));
            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK));
        }
    }
}