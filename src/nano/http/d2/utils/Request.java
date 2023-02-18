package nano.http.d2.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@SuppressWarnings("unused")
public class Request {
    public static String get(String dest, Properties header) {
        try {
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
        } catch (Exception e) {
            return null;
        }
    }

    public static String post(String dest, String data, Properties header) {
        try {
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
        } catch (Exception e) {
            return null;
        }
    }

    public static String jsonPost(String dest, String data, Properties header) {
        try {
            URL url = new URL(dest);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            if (header != null) {
                for (String s : header.stringPropertyNames()) {
                    con.setRequestProperty(s, header.getProperty(s));
                }
            }
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            byte[] requestBodyBytes = data.getBytes(StandardCharsets.UTF_8);
            con.setRequestProperty("Content-Length", Integer.toString(requestBodyBytes.length));
            con.getOutputStream().write(requestBodyBytes);
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder sbf = new StringBuilder();
            String temp;
            while ((temp = br.readLine()) != null) {
                sbf.append(temp);
                sbf.append("\r\n");
            }
            return sbf.toString();
        } catch (Exception e) {
            return null;
        }
    }
}
