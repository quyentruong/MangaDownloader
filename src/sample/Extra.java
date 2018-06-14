package sample;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import org.apache.commons.io.FileUtils;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;

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

    boolean isValidPNG(String fileName) {
        File png = new File(fileName);
        try {
            byte[] data = FileUtils.readFileToByteArray(png);
            byte[] first8Byte = Arrays.copyOf(data, 8);
            String hex = HexBin.encode(first8Byte);
            return hex.equalsIgnoreCase("89504E470D0A1A0A");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    boolean isValidJPG(String fileName) {
        File jpg = new File(fileName);
        try {
            byte[] data = FileUtils.readFileToByteArray(jpg);
            byte[] first2Byte = Arrays.copyOf(data, 2);
            byte[] last2byte = Arrays.copyOfRange(data, data.length - 2, data.length);
            String first2Byte_hex = HexBin.encode(first2Byte);
            String last2byte_hex = HexBin.encode(last2byte);
            return first2Byte_hex.equalsIgnoreCase("FFD8") && last2byte_hex.equalsIgnoreCase("FFD9");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    boolean isValidGIF(String fileName) {
        File gif = new File(fileName);
        try {
            byte[] data = FileUtils.readFileToByteArray(gif);
            byte[] first4Byte = Arrays.copyOf(data, 4);
            String hex = HexBin.encode(first4Byte);
            return hex.equalsIgnoreCase("47494638");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    URL parseURL(String src) {
        if (src.contains("url=")) src = src.split("url=")[1];
        if (src.contains("?imgmax=0")) src = src.replace("?imgmax=0", "").trim();
        String temp = null;
        try {
            temp = URLDecoder.decode(src, "UTF-8");
            temp = URLEncoder.encode(temp, "UTF-8");
            if (src.equalsIgnoreCase(temp)) src = URLDecoder.decode(src, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        if (!src.contains("http")) src = "https:" + src;
        try {
            return new URL(src);
        } catch (MalformedURLException e) {
            return null;
        }
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
