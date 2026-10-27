package org.apache.cordova;

import org.json.JSONException;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
public interface ExposedJsApi {
    String exec(int i, String str, String str2, String str3, String str4) throws JSONException, IllegalAccessException;

    String retrieveJsMessages(int i, boolean z) throws IllegalAccessException;

    void setNativeToJsBridgeMode(int i, int i2) throws IllegalAccessException;
}