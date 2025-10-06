package nano.http.d2.utils;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@SuppressWarnings("unused")
public class CommonRequest {
    public static String get(String dest, Properties header) throws Exception {
        URL url = new URL(dest);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        if (header != null) {
            for (String s : header.stringPropertyNames()) {
                con.setRequestProperty(s, header.getProperty(s));
            }
        }
        return new String(con.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static byte[] getBytes(String dest, Properties header) throws Exception {
        URL url = new URL(dest);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        if (header != null) {
            for (String s : header.stringPropertyNames()) {
                con.setRequestProperty(s, header.getProperty(s));
            }
        }
        return con.getInputStream().readAllBytes();
    }

    public static String post(String dest, String data, Properties header) throws Exception {
        URL url = new URL(dest);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        if (header != null) {
            for (String s : header.stringPropertyNames()) {
                con.setRequestProperty(s, header.getProperty(s));
            }
        }
        con.setDoOutput(true);
        con.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
        return new String(con.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static String parmPost(String dest, Properties params, Properties header) throws Exception {
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
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
        con.getOutputStream().write(requestBodyBytes);
        return new String(con.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static String jsonPost(String dest, String data, Properties header) throws Exception {
        URL url = new URL(dest);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
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
        con.getOutputStream().write(requestBodyBytes);
        return new String(con.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    public static String filePost(String dest, String filename, String mime, byte[] data, Properties header) throws Exception {
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
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        if (header != null) {
            for (String s : header.stringPropertyNames()) {
                con.setRequestProperty(s, header.getProperty(s));
            }
        }
        con.setRequestProperty("Content-Length", String.valueOf(requestBodyBytes.length));
        con.setDoOutput(true);
        con.getOutputStream().write(requestBodyBytes);
        return new String(con.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
}
