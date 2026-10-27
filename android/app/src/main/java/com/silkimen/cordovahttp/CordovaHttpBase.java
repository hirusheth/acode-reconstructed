package com.silkimen.cordovahttp;

import android.util.Base64;
import android.util.Log;
import com.silkimen.http.HttpBodyDecoder;
import com.silkimen.http.HttpRequest;
import com.silkimen.http.JsonUtils;
import com.silkimen.http.TLSConfiguration;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import javax.net.ssl.SSLException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
abstract class CordovaHttpBase implements Runnable {
    protected static final String TAG = "Cordova-Plugin-HTTP";
    protected CordovaObservableCallbackContext callbackContext;
    protected int connectTimeout;
    protected Object data;
    protected boolean followRedirects;
    protected JSONObject headers;
    protected String method;
    protected int readTimeout;
    protected String responseType;
    protected String serializer;
    protected TLSConfiguration tlsConfiguration;
    protected String url;

    public CordovaHttpBase(String str, String str2, String str3, Object obj, JSONObject jSONObject, int i, int i2, boolean z, String str4, TLSConfiguration tLSConfiguration, CordovaObservableCallbackContext cordovaObservableCallbackContext) {
        this.method = str;
        this.url = str2;
        this.serializer = str3;
        this.data = obj;
        this.headers = jSONObject;
        this.connectTimeout = i;
        this.readTimeout = i2;
        this.followRedirects = z;
        this.responseType = str4;
        this.tlsConfiguration = tLSConfiguration;
        this.callbackContext = cordovaObservableCallbackContext;
    }

    public CordovaHttpBase(String str, String str2, JSONObject jSONObject, int i, int i2, boolean z, String str3, TLSConfiguration tLSConfiguration, CordovaObservableCallbackContext cordovaObservableCallbackContext) {
        this.serializer = "none";
        this.method = str;
        this.url = str2;
        this.headers = jSONObject;
        this.connectTimeout = i;
        this.readTimeout = i2;
        this.followRedirects = z;
        this.responseType = str3;
        this.tlsConfiguration = tLSConfiguration;
        this.callbackContext = cordovaObservableCallbackContext;
    }

    @Override // java.lang.Runnable
    public void run() {
        CordovaHttpResponse cordovaHttpResponse = new CordovaHttpResponse();
        HttpRequest httpRequestCreateRequest = null;
        try {
            httpRequestCreateRequest = createRequest();
            prepareRequest(httpRequestCreateRequest);
            sendBody(httpRequestCreateRequest);
            processResponse(httpRequestCreateRequest, cordovaHttpResponse);
            httpRequestCreateRequest.disconnect();
        } catch (HttpRequest.HttpRequestException e) {
            IOException cause = e.getCause();
            String message = cause.getMessage();
            if (cause instanceof SSLException) {
                cordovaHttpResponse.setStatus(-2);
                cordovaHttpResponse.setErrorMessage("TLS connection could not be established: " + e.getMessage());
                Log.w(TAG, "TLS connection could not be established", e);
            } else if (cause instanceof UnknownHostException) {
                cordovaHttpResponse.setStatus(-3);
                cordovaHttpResponse.setErrorMessage("Host could not be resolved: " + e.getMessage());
                Log.w(TAG, "Host could not be resolved", e);
            } else if (cause instanceof SocketTimeoutException) {
                cordovaHttpResponse.setStatus(-4);
                cordovaHttpResponse.setErrorMessage("Request timed out: " + e.getMessage());
                Log.w(TAG, "Request timed out", e);
            } else if ((cause instanceof InterruptedIOException) && "thread interrupted".equals(message.toLowerCase())) {
                setAborted(httpRequestCreateRequest, cordovaHttpResponse);
            } else {
                cordovaHttpResponse.setStatus(-1);
                cordovaHttpResponse.setErrorMessage("There was an error with the request: " + message);
                Log.w(TAG, "Generic request error", e);
            }
        } catch (InterruptedException unused) {
            setAborted(httpRequestCreateRequest, cordovaHttpResponse);
        } catch (Exception e2) {
            cordovaHttpResponse.setStatus(-1);
            cordovaHttpResponse.setErrorMessage(e2.getMessage());
            Log.e(TAG, "An unexpected error occured", e2);
        }
        try {
            if (cordovaHttpResponse.hasFailed()) {
                this.callbackContext.error(cordovaHttpResponse.toJSON());
            } else {
                this.callbackContext.success(cordovaHttpResponse.toJSON());
            }
        } catch (JSONException e3) {
            Log.e(TAG, "An unexpected error occured while creating HTTP response object", e3);
        }
    }

    protected HttpRequest createRequest() throws JSONException {
        return new HttpRequest(this.url, this.method);
    }

    protected void prepareRequest(HttpRequest httpRequest) throws JSONException, IOException {
        httpRequest.followRedirects(this.followRedirects);
        httpRequest.connectTimeout(this.connectTimeout);
        httpRequest.readTimeout(this.readTimeout);
        httpRequest.acceptCharset("UTF-8");
        httpRequest.uncompress(true);
        if (this.tlsConfiguration.getHostnameVerifier() != null) {
            httpRequest.setHostnameVerifier(this.tlsConfiguration.getHostnameVerifier());
        }
        httpRequest.setSSLSocketFactory(this.tlsConfiguration.getTLSSocketFactory());
        setContentType(httpRequest);
        httpRequest.headers(JsonUtils.getStringMap(this.headers));
    }

    protected void setContentType(HttpRequest httpRequest) {
        if ("json".equals(this.serializer)) {
            httpRequest.contentType(HttpRequest.CONTENT_TYPE_JSON, "UTF-8");
            return;
        }
        if ("utf8".equals(this.serializer)) {
            httpRequest.contentType("text/plain", "UTF-8");
        } else if ("raw".equals(this.serializer)) {
            httpRequest.contentType("application/octet-stream");
        } else {
            if ("urlencoded".equals(this.serializer)) {
                return;
            }
            "multipart".equals(this.serializer);
        }
    }

    protected void sendBody(HttpRequest httpRequest) throws Exception {
        if (this.data == null) {
            return;
        }
        if ("json".equals(this.serializer)) {
            httpRequest.send(this.data.toString());
            return;
        }
        if ("utf8".equals(this.serializer)) {
            httpRequest.send(((JSONObject) this.data).getString("text"));
            return;
        }
        if ("raw".equals(this.serializer)) {
            httpRequest.send(Base64.decode((String) this.data, 0));
            return;
        }
        if ("urlencoded".equals(this.serializer)) {
            httpRequest.form(JsonUtils.getObjectMap((JSONObject) this.data));
            return;
        }
        if ("multipart".equals(this.serializer)) {
            JSONArray jSONArray = ((JSONObject) this.data).getJSONArray("buffers");
            JSONArray jSONArray2 = ((JSONObject) this.data).getJSONArray("names");
            JSONArray jSONArray3 = ((JSONObject) this.data).getJSONArray("fileNames");
            JSONArray jSONArray4 = ((JSONObject) this.data).getJSONArray("types");
            for (int i = 0; i < jSONArray.length(); i++) {
                byte[] bArrDecode = Base64.decode(jSONArray.getString(i), 0);
                String string = jSONArray2.getString(i);
                if (jSONArray3.isNull(i)) {
                    httpRequest.part(string, new String(bArrDecode, "UTF-8"));
                } else {
                    httpRequest.part(string, jSONArray3.getString(i), jSONArray4.getString(i), new ByteArrayInputStream(bArrDecode));
                }
            }
            if (jSONArray.length() == 0) {
                httpRequest.contentType("multipart/form-data; boundary=00content0boundary00");
                httpRequest.send("\r\n--00content0boundary00--\r\n");
            }
        }
    }

    protected void processResponse(HttpRequest httpRequest, CordovaHttpResponse cordovaHttpResponse) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        httpRequest.receive(byteArrayOutputStream);
        cordovaHttpResponse.setStatus(httpRequest.code());
        cordovaHttpResponse.setUrl(httpRequest.url().toString());
        cordovaHttpResponse.setHeaders(httpRequest.headers());
        if (httpRequest.code() >= 200 && httpRequest.code() < 300) {
            if ("text".equals(this.responseType) || "json".equals(this.responseType)) {
                cordovaHttpResponse.setBody(HttpBodyDecoder.decodeBody(byteArrayOutputStream.toByteArray(), httpRequest.charset()));
                return;
            } else {
                cordovaHttpResponse.setData(byteArrayOutputStream.toByteArray());
                return;
            }
        }
        cordovaHttpResponse.setErrorMessage(HttpBodyDecoder.decodeBody(byteArrayOutputStream.toByteArray(), httpRequest.charset()));
    }

    protected void setAborted(HttpRequest httpRequest, CordovaHttpResponse cordovaHttpResponse) {
        cordovaHttpResponse.setStatus(-8);
        cordovaHttpResponse.setErrorMessage("Request was aborted");
        if (httpRequest != null) {
            try {
                httpRequest.disconnect();
            } catch (Exception e) {
                Log.w(TAG, "Failed to close aborted request", e);
            }
        }
        Log.i(TAG, "Request was aborted");
    }
}