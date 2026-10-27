package com.silkimen.http;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes.dex */
public class TLSConfiguration {
    private SSLSocketFactory socketFactory;
    private TrustManager[] trustManagers = null;
    private KeyManager[] keyManagers = null;
    private HostnameVerifier hostnameVerifier = null;
    private String[] blacklistedProtocols = new String[0];

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.hostnameVerifier = hostnameVerifier;
    }

    public void setKeyManagers(KeyManager[] keyManagerArr) {
        this.keyManagers = keyManagerArr;
        this.socketFactory = null;
    }

    public void setTrustManagers(TrustManager[] trustManagerArr) {
        this.trustManagers = trustManagerArr;
        this.socketFactory = null;
    }

    public void setBlacklistedProtocols(String[] strArr) {
        this.blacklistedProtocols = strArr;
        this.socketFactory = null;
    }

    public HostnameVerifier getHostnameVerifier() {
        return this.hostnameVerifier;
    }

    public SSLSocketFactory getTLSSocketFactory() throws IOException {
        SSLSocketFactory sSLSocketFactory = this.socketFactory;
        if (sSLSocketFactory != null) {
            return sSLSocketFactory;
        }
        try {
            SSLContext sSLContext = SSLContext.getInstance("TLS");
            sSLContext.init(this.keyManagers, this.trustManagers, new SecureRandom());
            TLSSocketFactory tLSSocketFactory = new TLSSocketFactory(sSLContext, this.blacklistedProtocols);
            this.socketFactory = tLSSocketFactory;
            return tLSSocketFactory;
        } catch (GeneralSecurityException e) {
            IOException iOException = new IOException("Security exception occured while configuring TLS context");
            iOException.initCause(e);
            throw iOException;
        }
    }
}