package org.apache.cordova;

import android.webkit.HttpAuthHandler;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
public class CordovaHttpAuthHandler implements ICordovaHttpAuthHandler {
    private final HttpAuthHandler handler;

    public CordovaHttpAuthHandler(HttpAuthHandler httpAuthHandler) {
        this.handler = httpAuthHandler;
    }

    @Override // org.apache.cordova.ICordovaHttpAuthHandler
    public void cancel() {
        this.handler.cancel();
    }

    @Override // org.apache.cordova.ICordovaHttpAuthHandler
    public void proceed(String str, String str2) {
        this.handler.proceed(str, str2);
    }
}