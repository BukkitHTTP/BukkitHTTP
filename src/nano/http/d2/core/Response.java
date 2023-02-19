package nano.http.d2.core;

import nano.http.d2.hooks.HookManager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * HTTP response.
 * Return one of these from serve().
 */
public class Response {
    /**
     * HTTP status code after processing, e.g. "200 OK", HTTP_OK
     */
    public final String status;
    /**
     * MIME type of content, e.g. "text/html"
     */
    public final String mimeType;
    /**
     * Data of the response, may be null.
     */
    public final InputStream data;
    /**
     * Headers for the HTTP response. Use addHeader()
     * to add lines.
     */
    public final Properties header = new Properties();

    /**
     * Basic constructor.
     */
    public Response(String status, String mimeType, InputStream data) {
        this.status = status;
        this.mimeType = mimeType;
        this.data = data;
        HookManager.headerHook.process(this);
    }

    /**
     * Convenience method that makes an InputStream out of
     * given text.
     */
    public Response(String status, String mimeType, String txt) {
        this(status, mimeType, new ByteArrayInputStream(txt.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Adds given line to the header.
     */
    public void addHeader(String name, String value) {
        header.put(name, value);
    }
}
