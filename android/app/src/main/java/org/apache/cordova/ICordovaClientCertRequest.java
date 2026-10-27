package org.apache.cordova;

import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/* JADX INFO: loaded from: /tmp/rea-extracted-bfa67709-b073-4945-86a1-757c1b395bbe/classes2.dex */
public interface ICordovaClientCertRequest {
    void cancel();

    String getHost();

    String[] getKeyTypes();

    int getPort();

    Principal[] getPrincipals();

    void ignore();

    void proceed(PrivateKey privateKey, X509Certificate[] x509CertificateArr);
}