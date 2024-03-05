package nano.http.d2.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@SuppressWarnings("unused")
public class Request {
    public static String get(String dest, Properties header) throws Exception {
        URL url = new URL(dest);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        if (header != null) {
            for (String s : header.stringPropertyNames()) {
                con.setRequestProperty(s, header.getProperty(s));
            }
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sbf = new StringBuilder();
        String temp;
        while ((temp = br.readLine()) != null) {
            sbf.append(temp);
            sbf.append("\r\n");
        }
        return sbf.toString();
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
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = con.getInputStream().read(buffer)) != -1) {
            baos.write(buffer, 0, len);
        }
        return baos.toByteArray();
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
        BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder sbf = new StringBuilder();
        String temp;
        while ((temp = br.readLine()) != null) {
            sbf.append(temp);
            sbf.append("\r\n");
        }
        return sbf.toString();
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
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            br = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
        }
        StringBuilder sbf = new StringBuilder();
        String temp;
        while ((temp = br.readLine()) != null) {
            sbf.append(temp);
            sbf.append("\r\n");
        }
        return sbf.toString();
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
        BufferedReader br;
        try {
            br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            br = new BufferedReader(new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8));
        }
        StringBuilder sbf = new StringBuilder();
        String temp;
        while ((temp = br.readLine()) != null) {
            sbf.append(temp);
            sbf.append("\r\n");
        }
        return sbf.toString();
    }
}
