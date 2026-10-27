package org.apache.cordova;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
public interface ICordovaCookieManager {
    void clearCookies();

    void flush();

    String getCookie(String str);

    void setCookie(String str, String str2);

    void setCookiesEnabled(boolean z);
}