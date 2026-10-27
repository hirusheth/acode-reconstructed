package com.foxdebug.server;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.webkit.ProxyConfig;
import com.silkimen.http.HttpRequest;
import com.sshtools.common.files.nio.AbstractFilePath;
import fi.iki.elonen.NanoHTTPD;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class NanoHTTPDWebserver extends NanoHTTPD {
    Context context;
    public CallbackContext onRequestCallbackContext;
    public HashMap<String, Object> responses;

    public NanoHTTPDWebserver(int i, Context context) {
        super(i);
        this.context = context;
        this.responses = new HashMap<>();
    }

    private String getBodyText(NanoHTTPD.IHTTPSession iHTTPSession) {
        HashMap map = new HashMap();
        NanoHTTPD.Method method = iHTTPSession.getMethod();
        if (NanoHTTPD.Method.PUT.equals(method) || NanoHTTPD.Method.POST.equals(method)) {
            try {
                iHTTPSession.parseBody(map);
            } catch (NanoHTTPD.ResponseException | IOException unused) {
                return "{}";
            }
        }
        return map.get("postData");
    }

    private JSONObject createJSONRequest(String str, NanoHTTPD.IHTTPSession iHTTPSession) throws JSONException {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("requestId", str);
        jSONObject.put("body", getBodyText(iHTTPSession));
        jSONObject.put("headers", iHTTPSession.getHeaders());
        jSONObject.put("method", iHTTPSession.getMethod());
        jSONObject.put("path", iHTTPSession.getUri());
        jSONObject.put("query", iHTTPSession.getQueryParameterString());
        return jSONObject;
    }

    private String getContentType(JSONObject jSONObject) throws JSONException {
        if (jSONObject.has("headers") && jSONObject.getJSONObject("headers").has(HttpRequest.HEADER_CONTENT_TYPE)) {
            return jSONObject.getJSONObject("headers").getString(HttpRequest.HEADER_CONTENT_TYPE);
        }
        return "text/plain";
    }

    private NanoHTTPD.Response newFixedFileResponse(DocumentFile documentFile, String str) throws IOException {
        NanoHTTPD.Response responseNewFixedLengthResponse = newFixedLengthResponse(NanoHTTPD.Response.Status.OK, str, getInputStream(documentFile), (int) documentFile.length());
        responseNewFixedLengthResponse.addHeader("Accept-Ranges", "bytes");
        return responseNewFixedLengthResponse;
    }

    NanoHTTPD.Response serveFile(Map<String, String> map, DocumentFile documentFile, String str) {
        long j;
        NanoHTTPD.Response responseNewFixedLengthResponse;
        long j2;
        try {
            long length = documentFile.length();
            String hexString = Integer.toHexString((documentFile.getUri().toString() + documentFile.lastModified() + "" + length).hashCode());
            String strSubstring = map.get("range");
            long j3 = -1;
            if (strSubstring == null || !strSubstring.startsWith("bytes=")) {
                j = 0;
            } else {
                strSubstring = strSubstring.substring("bytes=".length());
                int iIndexOf = strSubstring.indexOf(45);
                if (iIndexOf > 0) {
                    try {
                        j2 = Long.parseLong(strSubstring.substring(0, iIndexOf));
                        try {
                            j3 = Long.parseLong(strSubstring.substring(iIndexOf + 1));
                        } catch (NumberFormatException e) {
                            e = e;
                            Log.w("NanoHTTPDWebserver", "Invalid range header: " + strSubstring, e);
                        }
                    } catch (NumberFormatException e2) {
                        e = e2;
                        j2 = 0;
                    }
                } else {
                    j2 = 0;
                }
                j = j2;
            }
            String str2 = map.get("if-range");
            boolean z = str2 == null || hexString.equals(str2);
            String str3 = map.get("if-none-match");
            boolean z2 = str3 != null && (ProxyConfig.MATCH_ALL_SCHEMES.equals(str3) || str3.equals(hexString));
            boolean z3 = z;
            if (!z3 || strSubstring == null || j < 0 || j >= length) {
                if (!z3 || strSubstring == null || j < length) {
                    if (strSubstring == null && z2) {
                        NanoHTTPD.Response responseNewFixedLengthResponse2 = newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_MODIFIED, str, "");
                        responseNewFixedLengthResponse2.addHeader(HttpRequest.HEADER_ETAG, hexString);
                        return responseNewFixedLengthResponse2;
                    }
                    if (!z3 && z2) {
                        NanoHTTPD.Response responseNewFixedLengthResponse3 = newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_MODIFIED, str, "");
                        responseNewFixedLengthResponse3.addHeader(HttpRequest.HEADER_ETAG, hexString);
                        return responseNewFixedLengthResponse3;
                    }
                    NanoHTTPD.Response responseNewFixedFileResponse = newFixedFileResponse(documentFile, str);
                    responseNewFixedFileResponse.addHeader(HttpRequest.HEADER_CONTENT_LENGTH, "" + length);
                    responseNewFixedFileResponse.addHeader(HttpRequest.HEADER_ETAG, hexString);
                    return responseNewFixedFileResponse;
                }
                responseNewFixedLengthResponse = newFixedLengthResponse(NanoHTTPD.Response.Status.RANGE_NOT_SATISFIABLE, "text/plain", "");
                responseNewFixedLengthResponse.addHeader("Content-Range", "bytes */" + length);
                responseNewFixedLengthResponse.addHeader(HttpRequest.HEADER_ETAG, hexString);
            } else {
                if (z2) {
                    NanoHTTPD.Response responseNewFixedLengthResponse4 = newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_MODIFIED, str, "");
                    responseNewFixedLengthResponse4.addHeader(HttpRequest.HEADER_ETAG, hexString);
                    return responseNewFixedLengthResponse4;
                }
                if (j3 < 0) {
                    j3 = length - 1;
                }
                long j4 = j3;
                long j5 = (j4 - j) + 1;
                long j6 = j5 < 0 ? 0L : j5;
                InputStream inputStream = getInputStream(documentFile);
                inputStream.skip(j);
                responseNewFixedLengthResponse = newFixedLengthResponse(NanoHTTPD.Response.Status.PARTIAL_CONTENT, str, inputStream, j6);
                responseNewFixedLengthResponse.addHeader("Accept-Ranges", "bytes");
                responseNewFixedLengthResponse.addHeader(HttpRequest.HEADER_CONTENT_LENGTH, "" + j6);
                responseNewFixedLengthResponse.addHeader("Content-Range", "bytes " + j + "-" + j4 + AbstractFilePath.SEPARATOR + length);
                responseNewFixedLengthResponse.addHeader(HttpRequest.HEADER_ETAG, hexString);
            }
            return responseNewFixedLengthResponse;
        } catch (Exception e3) {
            Log.d("ServeFileError", e3.getMessage());
            return e3.getClass().getName().equals("FileNotFoundException") ? newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND, "text/plain", e3.getMessage()) : newFixedLengthResponse(NanoHTTPD.Response.Status.FORBIDDEN, "text/plain", e3.getMessage());
        }
    }

    @Override // fi.iki.elonen.NanoHTTPD
    public NanoHTTPD.Response serve(NanoHTTPD.IHTTPSession iHTTPSession) {
        PluginResult pluginResult;
        String string = UUID.randomUUID().toString();
        NanoHTTPD.Response responseNewFixedLengthResponse = null;
        try {
            pluginResult = new PluginResult(PluginResult.Status.OK, createJSONRequest(string, iHTTPSession));
        } catch (JSONException e) {
            e.printStackTrace();
            pluginResult = null;
        }
        pluginResult.setKeepCallback(true);
        this.onRequestCallbackContext.sendPluginResult(pluginResult);
        while (!this.responses.containsKey(string)) {
            try {
                Thread.sleep(1L);
            } catch (InterruptedException e2) {
                e2.printStackTrace();
            }
        }
        JSONObject jSONObject = (JSONObject) this.responses.get(string);
        if (jSONObject.has("path")) {
            try {
                String string2 = jSONObject.getString("path");
                NanoHTTPD.Response responseServeFile = serveFile(iHTTPSession.getHeaders(), getFile(string2), URLConnection.guessContentTypeFromName(string2));
                JSONObject jSONObject2 = getJSONObject(jSONObject, "headers");
                if (jSONObject2 != null) {
                    Iterator<String> itKeys = jSONObject2.keys();
                    while (itKeys.hasNext()) {
                        String next = itKeys.next();
                        responseServeFile.addHeader(next, jSONObject.getJSONObject("headers").getString(next));
                    }
                }
                return responseServeFile;
            } catch (JSONException e3) {
                e3.printStackTrace();
                return null;
            }
        }
        try {
            responseNewFixedLengthResponse = newFixedLengthResponse(NanoHTTPD.Response.Status.lookup(jSONObject.getInt(NotificationCompat.CATEGORY_STATUS)), getContentType(jSONObject), jSONObject.getString("body"));
            Iterator<String> itKeys2 = jSONObject.getJSONObject("headers").keys();
            while (itKeys2.hasNext()) {
                String next2 = itKeys2.next();
                responseNewFixedLengthResponse.addHeader(next2, jSONObject.getJSONObject("headers").getString(next2));
            }
        } catch (JSONException e4) {
            e4.printStackTrace();
        }
        return responseNewFixedLengthResponse;
    }

    private DocumentFile getFile(String str) {
        Uri uri = Uri.parse(str);
        if (str.matches("file:///(.*)")) {
            return DocumentFile.fromFile(new File(uri.getPath()));
        }
        return DocumentFile.fromSingleUri(this.context, Uri.parse(str));
    }

    private InputStream getInputStream(DocumentFile documentFile) throws IOException {
        return this.context.getContentResolver().openInputStream(documentFile.getUri());
    }

    private JSONObject getJSONObject(JSONObject jSONObject, String str) {
        try {
            return jSONObject.getJSONObject(str);
        } catch (JSONException e) {
            Log.w("NanoHTTPDWebserver", "Missing or invalid JSON object for key: " + str, e);
            return null;
        }
    }
}