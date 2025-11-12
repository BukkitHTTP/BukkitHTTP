package nano.http.d2.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@SuppressWarnings("unused")
public class Encoding {
    private static final ThreadLocal<MessageDigest> md5Local = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            return null;
        }
    });

    public static String enBase64(String origin) {
        return java.util.Base64.getEncoder().encodeToString(origin.getBytes(StandardCharsets.UTF_8));
    }

    public static String deBase64(String origin) {
        return new String(java.util.Base64.getDecoder().decode(origin.replace(" ", "+").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    public static String deURL(String origin) {
        boolean needToChange = false;
        int numChars = origin.length();
        StringBuilder sb = new StringBuilder(numChars > 500 ? numChars / 2 : numChars);
        int i = 0;
        char c;
        byte[] bytes = null;
        outer:
        while (i < numChars) {
            c = origin.charAt(i);
            switch (c) {
                case '+':
                    sb.append(' ');
                    i++;
                    needToChange = true;
                    break;
                case '%':
                    try {
                        if (bytes == null) bytes = new byte[(numChars - i) / 3];
                        int pos = 0;
                        while (((i + 2) < numChars) && (c == '%')) {
                            int v = Integer.parseInt(origin, i + 1, i + 3, 16);
                            if (v < 0) throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape " + "(%) pattern - negative value");
                            bytes[pos++] = (byte) v;
                            i += 3;
                            if (i < numChars) c = origin.charAt(i);
                        }
                        if ((i < numChars) && (c == '%')) {
                            sb.append('%');
                            i++;
                            sb.append(origin.substring(i));
                            break outer;
                        } else {
                            sb.append(new String(bytes, 0, pos, StandardCharsets.UTF_8));
                        }

                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("URLDecoder: Illegal hex characters in escape (%) pattern - " + e.getMessage());
                    }
                    needToChange = true;
                    break;
                default:
                    sb.append(c);
                    i++;
                    break;
            }
        }
        return (needToChange ? sb.toString() : origin);
    }

    public static String enURL(String origin) {
        return URLEncoder.encode(origin, StandardCharsets.UTF_8);
    }

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
