package nano.http.d2.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Cache {
    private final byte[] data;

    public Cache(InputStream is) {
        try {
            data = is.readAllBytes();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public byte[] getData() {
        return data;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(data);
    }
}
