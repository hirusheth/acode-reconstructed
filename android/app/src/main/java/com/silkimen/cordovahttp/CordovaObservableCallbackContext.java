package com.silkimen.cordovahttp;

import java.util.Observer;
import org.apache.cordova.CallbackContext;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class CordovaObservableCallbackContext {
    private CallbackContext callbackContext;
    private Observer observer;
    private Integer requestId;

    public CordovaObservableCallbackContext(CallbackContext callbackContext, Integer num) {
        this.callbackContext = callbackContext;
        this.requestId = num;
    }

    public void success(JSONObject jSONObject) {
        this.callbackContext.success(jSONObject);
        notifyObserver();
    }

    public void error(JSONObject jSONObject) {
        this.callbackContext.error(jSONObject);
        notifyObserver();
    }

    public Integer getRequestId() {
        return this.requestId;
    }

    public CallbackContext getCallbackContext() {
        return this.callbackContext;
    }

    public Observer getObserver() {
        return this.observer;
    }

    protected void notifyObserver() {
        Observer observer = this.observer;
        if (observer != null) {
            observer.update(null, this);
        }
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }
}