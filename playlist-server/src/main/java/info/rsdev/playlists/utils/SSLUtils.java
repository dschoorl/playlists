package info.rsdev.playlists.utils;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Utility to disable checking the client and server certificates. This makes us
 * vulnerable for a man-in-the-middle attack, but we are only scraping top40 web
 * site and expect a specific HTML structure. Therefore, I think this risk is
 * acceptable (not exploitable)
 */
public class SSLUtils {

    static class InsecureTrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
            // Do nothing (trust all clients)
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
            // Do nothing (trust all servers)
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0]; // No accepted issuers
        }
    }

    public static SSLContext createInsecureSSLContext() {
        // Use TLS (modern and secure; avoid SSLv3)
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            // Initialize with our trust manager (no key manager or secure random needed)
            sslContext.init(null, new TrustManager[] { new InsecureTrustManager() }, new java.security.SecureRandom());
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException("Cannot create SSLContext", e);
        }
    }

}