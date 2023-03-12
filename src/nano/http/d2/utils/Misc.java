package nano.http.d2.utils;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Attention !
 * This file is designed to be used internally by NanoHTTPD.
 * To make FileServer work efficiently, this file is not written in a modular way.
 * It's not recommended to use this file in your own project.
 * Use other files in this package as alternatives.
 */

public class Misc {
    public static final ConcurrentHashMap<String, String> theMimeTypes = new ConcurrentHashMap<>();
    public static final SimpleDateFormat gmtFrmt;

    public static final String BOM = "\ufeff";

    static {
        theMimeTypes.put("css", "text/css");
        theMimeTypes.put("java", "text/java");
        theMimeTypes.put("js", "text/javascript");
        theMimeTypes.put("htm", "text/html");
        theMimeTypes.put("html", "text/html");
        theMimeTypes.put("txt", "text/plain");
        theMimeTypes.put("asc", "text/plain");
        theMimeTypes.put("gif", "image/gif");
        theMimeTypes.put("jpg", "image/jpeg");
        theMimeTypes.put("jpeg", "image/jpeg");
        theMimeTypes.put("png", "image/png");
        theMimeTypes.put("bmp", "image/bmp");
        theMimeTypes.put("mp3", "audio/mpeg");
        theMimeTypes.put("m3u", "audio/mpeg-url");
        theMimeTypes.put("pdf", "application/pdf");
        theMimeTypes.put("doc", "application/msword");
        theMimeTypes.put("ogg", "application/x-ogg");
        theMimeTypes.put("zip", "application/octet-stream");
        theMimeTypes.put("exe", "application/octet-stream");
        theMimeTypes.put("class", "application/octet-stream");
    }

    static {
        gmtFrmt = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static String encodeUri(String uri) {
        StringBuilder newUri = new StringBuilder();
        StringTokenizer st = new StringTokenizer(uri, "/ ", true);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if ("/".equals(tok)) {
                newUri.append("/");
            } else if (" ".equals(tok)) {
                newUri.append("%20");
            } else {
                try {
                    newUri.append(URLEncoder.encode(tok, "UTF-8"));
                } catch (Exception ignored) {
                }
            }
        }
        return newUri.toString();
    }

    public static String chinese(String text) {
        return "<html>\n" +
                "<head>\n" +
                "    <meta content=\"text/html; charset=UTF-8\" http-equiv=\"Content-Type\">\n" +
                "</head>\n" +
                "<body>\n" +
                text +
                "</body>\n" +
                "</html>";
    }
}
