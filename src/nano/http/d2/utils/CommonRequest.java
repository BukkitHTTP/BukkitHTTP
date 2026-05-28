package nano.http.d2.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@SuppressWarnings("unused")
public class CommonRequest {
    public static int connTimeout = 0;

    static {
        // https://stackoverflow.com/questions/8335501/does-httpurlconnection-censor-some-headers-notably-origin
        // Oracle fuck you
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
    }

    public static String get(String dest, Properties header) throws Exception {
        return get(dest, header, Proxy.NO_PROXY);
    }

    public static String get(String dest, Properties header, Proxy p) throws Exception {
        URL url = new URL(dest);
        HttpURLConnection con = (HttpURLConnection) url.openConnection(p);
        try {
            if (connTimeout > 0) {
                con.setConnectTimeout(connTimeout);
                con.setReadTimeout(connTimeout);
            }
            con.setRequestMethod("GET");

            if (header != null) {
                for (String s : header.stringPropertyNames()) {
                    con.setRequestProperty(s, header.getProperty(s));
                }
            }
            try (InputStream in = con.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } finally {
            con.disconnect();
        }
    }

    public static byte[] getBytes(String dest, Properties header) throws Exception {
        return getBytes(dest, header, Proxy.NO_PROXY);
    }

    public static byte[] getBytes(String dest, Properties header, Proxy p) throws Exception {
        URL url = new URL(dest);
        HttpURLConnection con = (HttpURLConnection) url.openConnection(p);
        try {
            if (connTimeout > 0) {
                con.setConnectTimeout(connTimeout);
                con.setReadTimeout(connTimeout);
            }
            con.setRequestMethod("GET");
            if (header != null) {
                for (String s : header.stringPropertyNames()) {
                    con.setRequestProperty(s, header.getProperty(s));
                }
            }
            try (InputStream in = con.getInputStream()) {
                return in.readAllBytes();
            }
        } finally {
            con.disconnect();
        }
    }

    public static String post(String dest, String data, Properties header) throws Exception {
        return post(dest, data, header, Proxy.NO_PROXY);
    }

    public static String post(String dest, String data, Properties header, Proxy p) throws Exception {
        URL url = new URL(dest);
        HttpURLConnection con = (HttpURLConnection) url.openConnection(p);
        try {
            if (connTimeout > 0) {
                con.setConnectTimeout(connTimeout);
                con.setReadTimeout(connTimeout);
            }
            con.setRequestMethod("POST");
            if (header != null) {
                for (String s : header.stringPropertyNames()) {
                    con.setRequestProperty(s, header.getProperty(s));
                }
            }
            con.setDoOutput(true);
            try (OutputStream out = con.getOutputStream()) {
                out.write(data.getBytes(StandardCharsets.UTF_8));
            }
            try (InputStream in = con.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } finally {
            con.disconnect();
        }
    }

    public static String parmPost(String dest, Properties params, Properties header) throws Exception {
        return parmPost(dest, params, header, Proxy.NO_PROXY);
    }

    public static String parmPost(String dest, Properties params, Properties header, Proxy p) throws Exception {
        StringBuilder paramStr = new StringBuilder();
        if (params != null) {
            for (String s : params.stringPropertyNames()) {
                if (!paramStr.isEmpty()) {
                    paramStr.append("&");
                }
                paramStr.append(s).append("=").append(params.getProperty(s));
            }
        }
        URL url = new URL(dest);
        HttpURLConnection con = (HttpURLConnection) url.openConnection(p);
        try {
            if (connTimeout > 0) {
                con.setConnectTimeout(connTimeout);
                con.setReadTimeout(connTimeout);
            }
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            if (header != null) {
                for (String s : header.stringPropertyNames()) {
                    con.setRequestProperty(s, header.getProperty(s));
                }
            }
            con.setDoOutput(true);
            byte[] requestBodyBytes = paramStr.toString().getBytes(StandardCharsets.UTF_8);
            con.setRequestProperty("Content-Length", Integer.toString(requestBodyBytes.length));
            try (OutputStream out = con.getOutputStream()) {
                out.write(requestBodyBytes);
            }
            try (InputStream in = con.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } finally {
            con.disconnect();
        }
    }

    public static String jsonPost(String dest, String data, Properties header) throws Exception {
        return jsonPost(dest, data, header, Proxy.NO_PROXY);
    }

    public static String jsonPost(String dest, String data, Properties header, Proxy p) throws Exception {
        URL url = new URL(dest);
        HttpURLConnection con = (HttpURLConnection) url.openConnection(p);
        try {
            if (connTimeout > 0) {
                con.setConnectTimeout(connTimeout);
                con.setReadTimeout(connTimeout);
            }
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            if (header != null) {
                for (String s : header.stringPropertyNames()) {
                    con.setRequestProperty(s, header.getProperty(s));
                }
            }
            con.setDoOutput(true);
            byte[] requestBodyBytes = data.getBytes(StandardCharsets.UTF_8);
            con.setRequestProperty("Content-Length", Integer.toString(requestBodyBytes.length));
            try (OutputStream out = con.getOutputStream()) {
                out.write(requestBodyBytes);
            }
            try (InputStream in = con.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } finally {
            con.disconnect();
        }
    }

    public static String filePost(String dest, String filename, String mime, byte[] data, Properties header) throws Exception {
        return filePost(dest, filename, mime, data, header, Proxy.NO_PROXY);
    }

    public static String filePost(String dest, String filename, String mime, byte[] data, Properties header, Proxy p) throws Exception {
        String boundary = "HsBoUnDaRy";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
        baos.write(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n").getBytes(StandardCharsets.UTF_8));
        baos.write(("Content-Type: " + mime + "\r\n").getBytes(StandardCharsets.UTF_8));
        baos.write(("\r\n").getBytes(StandardCharsets.UTF_8));
        baos.write(data);
        baos.write(("\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        byte[] requestBodyBytes = baos.toByteArray();
        baos.close();
        URL url = new URL(dest);
        HttpURLConnection con = (HttpURLConnection) url.openConnection(p);
        try {
            if (connTimeout > 0) {
                con.setConnectTimeout(connTimeout);
                con.setReadTimeout(connTimeout);
            }
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
            if (header != null) {
                for (String s : header.stringPropertyNames()) {
                    con.setRequestProperty(s, header.getProperty(s));
                }
            }
            con.setRequestProperty("Content-Length", String.valueOf(requestBodyBytes.length));
            con.setDoOutput(true);
            try (OutputStream out = con.getOutputStream()) {
                out.write(requestBodyBytes);
            }
            try (InputStream in = con.getInputStream()) {
                return new String(in.readAllBytes(), StandardCharsets.UTF_8);
            }
        } finally {
            con.disconnect();
        }
    }
}
