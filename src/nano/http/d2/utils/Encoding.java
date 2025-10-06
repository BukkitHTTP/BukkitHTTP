package nano.http.d2.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@SuppressWarnings("unused")
public class Encoding {
    public static String enBase64(String origin) {
        return java.util.Base64.getEncoder().encodeToString(origin.getBytes(StandardCharsets.UTF_8));
    }

    public static String deBase64(String origin) {
        return new String(java.util.Base64.getDecoder().decode(origin.replace(" ", "+").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public static String deURL(String origin) {
        return URLDecoder.decode(origin, StandardCharsets.UTF_8);
    }

    public static String enURL(String origin) {
        return URLEncoder.encode(origin, StandardCharsets.UTF_8);
    }

    private static final ThreadLocal<MessageDigest> md5Local = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            return null;
        }
    });

    public static String enMd5(String origin) {
        try {
            MessageDigest md = md5Local.get();
            md.reset();
            byte[] bytes = md.digest(origin.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02X", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
