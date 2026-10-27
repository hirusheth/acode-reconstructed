package com.silkimen.cordovahttp;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Base64;
import android.util.Log;
import com.silkimen.http.TLSConfiguration;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Future;
import javax.net.ssl.TrustManagerFactory;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class CordovaHttpPlugin extends CordovaPlugin implements Observer {
    private static final String TAG = "Cordova-Plugin-HTTP";
    private HashMap<Integer, Future<?>> reqMap;
    private final Object reqMapLock = new Object();
    private TLSConfiguration tlsConfiguration;

    public void initialize(CordovaInterface cordovaInterface, CordovaWebView cordovaWebView) {
        super.initialize(cordovaInterface, cordovaWebView);
        this.tlsConfiguration = new TLSConfiguration();
        this.reqMap = new HashMap<>();
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidCAStore");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            keyStore.load(null);
            trustManagerFactory.init(keyStore);
            this.tlsConfiguration.setHostnameVerifier(null);
            this.tlsConfiguration.setTrustManagers(trustManagerFactory.getTrustManagers());
            if (this.preferences.contains("androidblacklistsecuresocketprotocols")) {
                this.tlsConfiguration.setBlacklistedProtocols(this.preferences.getString("androidblacklistsecuresocketprotocols", "").split(","));
            }
        } catch (Exception e) {
            Log.e(TAG, "An error occured while loading system's CA certificates", e);
        }
    }

    /* JADX WARN: Code duplicated, block: B:44:0x009a  */
    /* JADX WARN: Code duplicated, block: B:46:0x00a0  */
    /* JADX WARN: Code restructure failed: missing block: B:20:0x0033, code lost:
    
        r1 = r6.getString(0);
     */
    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:85:0x009a -> B:44:0x009a). Please report as a decompilation issue!!! */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public boolean execute(java.lang.String r5, org.json.JSONArray r6, org.apache.cordova.CallbackContext r7) throws org.json.JSONException {
        /*
            Method dump skipped, instruction units count: 301
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.silkimen.cordovahttp.CordovaHttpPlugin.execute(java.lang.String, org.json.JSONArray, org.apache.cordova.CallbackContext):boolean");
    }

    private boolean executeHttpRequestWithoutData(String str, JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        String string = jSONArray.getString(0);
        JSONObject jSONObject = jSONArray.getJSONObject(1);
        int i = jSONArray.getInt(2) * 1000;
        int i2 = jSONArray.getInt(3) * 1000;
        boolean z = jSONArray.getBoolean(4);
        String string2 = jSONArray.getString(5);
        Integer numValueOf = Integer.valueOf(jSONArray.getInt(6));
        CordovaObservableCallbackContext cordovaObservableCallbackContext = new CordovaObservableCallbackContext(callbackContext, numValueOf);
        startRequest(numValueOf, cordovaObservableCallbackContext, new CordovaHttpOperation(str.toUpperCase(), string, jSONObject, i, i2, z, string2, this.tlsConfiguration, cordovaObservableCallbackContext));
        return true;
    }

    private boolean executeHttpRequestWithData(String str, JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        String string = jSONArray.getString(0);
        Object obj = jSONArray.get(1);
        String string2 = jSONArray.getString(2);
        JSONObject jSONObject = jSONArray.getJSONObject(3);
        int i = jSONArray.getInt(4) * 1000;
        int i2 = jSONArray.getInt(5) * 1000;
        boolean z = jSONArray.getBoolean(6);
        String string3 = jSONArray.getString(7);
        Integer numValueOf = Integer.valueOf(jSONArray.getInt(8));
        CordovaObservableCallbackContext cordovaObservableCallbackContext = new CordovaObservableCallbackContext(callbackContext, numValueOf);
        startRequest(numValueOf, cordovaObservableCallbackContext, new CordovaHttpOperation(str.toUpperCase(), string, string2, obj, jSONObject, i, i2, z, string3, this.tlsConfiguration, cordovaObservableCallbackContext));
        return true;
    }

    private boolean uploadFiles(JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        String string = jSONArray.getString(0);
        JSONObject jSONObject = jSONArray.getJSONObject(1);
        JSONArray jSONArray2 = jSONArray.getJSONArray(2);
        JSONArray jSONArray3 = jSONArray.getJSONArray(3);
        int i = jSONArray.getInt(4) * 1000;
        int i2 = jSONArray.getInt(5) * 1000;
        boolean z = jSONArray.getBoolean(6);
        String string2 = jSONArray.getString(7);
        Integer numValueOf = Integer.valueOf(jSONArray.getInt(8));
        CordovaObservableCallbackContext cordovaObservableCallbackContext = new CordovaObservableCallbackContext(callbackContext, numValueOf);
        startRequest(numValueOf, cordovaObservableCallbackContext, new CordovaHttpUpload(string, jSONObject, jSONArray2, jSONArray3, i, i2, z, string2, this.tlsConfiguration, this.cordova.getActivity().getApplicationContext(), cordovaObservableCallbackContext));
        return true;
    }

    private boolean downloadFile(JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        String string = jSONArray.getString(0);
        JSONObject jSONObject = jSONArray.getJSONObject(1);
        String string2 = jSONArray.getString(2);
        int i = jSONArray.getInt(3) * 1000;
        int i2 = jSONArray.getInt(4) * 1000;
        boolean z = jSONArray.getBoolean(5);
        Integer numValueOf = Integer.valueOf(jSONArray.getInt(6));
        CordovaObservableCallbackContext cordovaObservableCallbackContext = new CordovaObservableCallbackContext(callbackContext, numValueOf);
        startRequest(numValueOf, cordovaObservableCallbackContext, new CordovaHttpDownload(string, jSONObject, string2, i, i2, z, this.tlsConfiguration, cordovaObservableCallbackContext));
        return true;
    }

    private void startRequest(Integer num, CordovaObservableCallbackContext cordovaObservableCallbackContext, CordovaHttpBase cordovaHttpBase) {
        synchronized (this.reqMapLock) {
            cordovaObservableCallbackContext.setObserver(this);
            addReq(num, this.cordova.getThreadPool().submit(cordovaHttpBase), cordovaObservableCallbackContext);
        }
    }

    private boolean setServerTrustMode(JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        this.cordova.getThreadPool().execute(new CordovaServerTrust(jSONArray.getString(0), this.cordova.getActivity(), this.tlsConfiguration, callbackContext));
        return true;
    }

    private boolean setClientAuthMode(JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        this.cordova.getThreadPool().execute(new CordovaClientAuth(jSONArray.getString(0), jSONArray.isNull(1) ? null : jSONArray.getString(1), jSONArray.isNull(2) ? null : Base64.decode(jSONArray.getString(2), 0), jSONArray.getString(3), this.cordova.getActivity(), this.cordova.getActivity().getApplicationContext(), this.tlsConfiguration, callbackContext));
        return true;
    }

    private boolean abort(JSONArray jSONArray, CallbackContext callbackContext) throws JSONException {
        boolean zCancel = false;
        Future<?> future = this.reqMap.get(Integer.valueOf(jSONArray.getInt(0)));
        if (future != null && !future.isDone()) {
            zCancel = future.cancel(true);
        }
        callbackContext.success(new JSONObject().put("aborted", zCancel));
        return true;
    }

    private void addReq(Integer num, Future<?> future, CordovaObservableCallbackContext cordovaObservableCallbackContext) {
        synchronized (this.reqMapLock) {
            if (!future.isDone()) {
                this.reqMap.put(num, future);
            }
        }
    }

    private void removeReq(Integer num) {
        synchronized (this.reqMapLock) {
            this.reqMap.remove(num);
        }
    }

    @Override // java.util.Observer
    public void update(Observable observable, Object obj) {
        synchronized (this.reqMapLock) {
            CordovaObservableCallbackContext cordovaObservableCallbackContext = (CordovaObservableCallbackContext) obj;
            if (cordovaObservableCallbackContext.getCallbackContext().isFinished()) {
                removeReq(cordovaObservableCallbackContext.getRequestId());
            }
        }
    }

    private boolean isNetworkAvailable() {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) this.cordova.getContext().getSystemService("connectivity")).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}