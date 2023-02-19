package nano.http.d2.utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public class Encoding {
    public static String enBase64(String origin) {
        return java.util.Base64.getEncoder().encodeToString(origin.getBytes(StandardCharsets.UTF_8));
    }

    public static String deBase64(String origin) {
        return new String(java.util.Base64.getDecoder().decode(origin.replace(" ", "+").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public static String deURL(String origin) {
        try {
            return java.net.URLDecoder.decode(origin, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    public static String enURL(String origin) {
        try {
            return java.net.URLEncoder.encode(origin, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }
}
