package com.silkimen.cordovahttp;

import com.silkimen.http.TLSConfiguration;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
class CordovaHttpOperation extends CordovaHttpBase {
    public CordovaHttpOperation(String str, String str2, String str3, Object obj, JSONObject jSONObject, int i, int i2, boolean z, String str4, TLSConfiguration tLSConfiguration, CordovaObservableCallbackContext cordovaObservableCallbackContext) {
        super(str, str2, str3, obj, jSONObject, i, i2, z, str4, tLSConfiguration, cordovaObservableCallbackContext);
    }

    public CordovaHttpOperation(String str, String str2, JSONObject jSONObject, int i, int i2, boolean z, String str3, TLSConfiguration tLSConfiguration, CordovaObservableCallbackContext cordovaObservableCallbackContext) {
        super(str, str2, jSONObject, i, i2, z, str3, tLSConfiguration, cordovaObservableCallbackContext);
    }
}