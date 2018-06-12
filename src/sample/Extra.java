package sample;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

class Extra {
    private PrintStream ps_console;

    Extra() {
        ps_console = System.out;
    }

//    https://github.com/jhy/jsoup/issues/680
//    https://nanashi07.blogspot.com/2014/06/enable-ssl-connection-for-jsoup.html

    /**
     * Use this one before use Jsoup.connect to solve ssl problem
     */
    void enableSSLSocket() {
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        SSLContext context = null;
        try {
            context = SSLContext.getInstance("TLS");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            assert context != null;
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
    }

    @Deprecated
    void OutputToFile(String FileName) {
        File file = new File(FileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Create new print stream for file.
        assert fos != null;
        PrintStream ps = new PrintStream(fos);

        // Set file print stream.
        System.setOut(ps);
    }

    @Deprecated
    void OutputToConsole() {
        System.setOut(ps_console);
    }
}
