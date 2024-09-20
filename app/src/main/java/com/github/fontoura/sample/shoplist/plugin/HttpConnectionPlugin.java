package com.github.fontoura.sample.shoplist.plugin;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.github.fontoura.sample.shoplist.R;
import com.github.fontoura.sample.shoplist.utils.HTTPRequestPreparer;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

public final class HttpConnectionPlugin implements ActivityPlugin {

    private static final String TAG = HttpConnectionPlugin.class.getName();

    private Activity activity;
    private boolean pinningEnabled = false;
    private boolean activityCreated = false;

    private HTTPRequestPreparer requestPreparer;

    public HttpConnectionPlugin(Activity activity) {
        this.activity = activity;
    }

    public HttpConnectionPlugin withPinningEnabled(boolean pinningEnabled) {
        this.setPinningEnabled(pinningEnabled);
        return this;
    }

    public boolean isPinningEnabled() {
        return pinningEnabled;
    }

    public void setPinningEnabled(boolean pinningEnabled) {
        this.pinningEnabled = pinningEnabled;
        if (activityCreated) {
            recreateHTTPRequestPreparer();
        }
    }

    public HTTPRequestPreparer getHTTPRequestPreparer() {
        return requestPreparer;
    }

    @Override
    public void onCreate(Bundle bundle) {
        activityCreated = true;
        recreateHTTPRequestPreparer();
    }

    @Override
    public void onDestroy() {
        activityCreated = false;
    }

    private void recreateHTTPRequestPreparer() {
        Log.i(TAG, "Creating HTTP request preparer");
        HostnameVerifier hostnameVerifier = (hostname, session) -> true;
        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");

            TrustManager[] trustManagers;
            if (pinningEnabled) {
                InputStream certInputStream = activity.getResources()
                        .openRawResource(R.raw.certificate);

                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(null, null);
                keyStore.setCertificateEntry(
                        "app_certificate",
                        CertificateFactory.getInstance("X.509")
                                .generateCertificate(certInputStream));

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                        TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);

                trustManagers = trustManagerFactory.getTrustManagers();
            } else {
                TrustManager trustAllCerts =  new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                        // no op
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                        // no op
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                };
                trustManagers = new TrustManager[] { trustAllCerts };
            }
            sslContext.init(null, trustManagers, new SecureRandom());
        } catch (CertificateException | KeyStoreException | IOException |
                 NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException(
                    "Failed to " + (pinningEnabled ? "enable" : "disable") + " SSL pinning", e);
        }

        requestPreparer = connection -> {
            connection.setConnectTimeout(5000);
            if (connection instanceof HttpsURLConnection) {
                HttpsURLConnection secureConnection = (HttpsURLConnection) connection;
                secureConnection.setSSLSocketFactory(sslContext.getSocketFactory());
                secureConnection.setHostnameVerifier(hostnameVerifier);
            }
        };
    }
}
