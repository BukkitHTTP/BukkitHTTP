package nano.http.d2.database.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class SerlBridge {
    public SerlBridge(String phase) {
        if (!phase.equals("LET ME IN")) {
            throw new IllegalArgumentException("Do NOT access me, unless you really know what you are doing!");
        }
    }

    public static final long MAX_INFLATED_BYTES = 1024L * 1024 * 1024; // 1GB

    public byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DeflaterOutputStream dos = new DeflaterOutputStream(baos);
        SerlImpl.writeObject(obj, dos, new SerlCtx());
        SerlImpl.writeLong(0x0d000721, dos);
        dos.finish();
        return baos.toByteArray();
    }

    public SerlBridgeResult deserialize(byte[] data) throws IOException {
        return deserialize(data, Thread.currentThread().getContextClassLoader());
    }

    public SerlBridgeResult deserialize(byte[] data, ClassLoader cl) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        InflaterInputStream iis = new InflaterInputStream(bais);
        DeSerlCtx ctx = new DeSerlCtx(cl);
        // Inflate fully (capped) BEFORE parsing, so the parser can validate
        // every length field against the bytes actually present. Without this,
        // a crafted ~30-byte file could make readString() request a 2GB
        // allocation, and OutOfMemoryError would blow straight through every
        // catch(Exception) on the way up.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        long total = 0;
        int r;
        while ((r = iis.read(buf)) > 0) {
            total += r;
            if (total > MAX_INFLATED_BYTES) {
                throw new IOException("Database inflates beyond the limit: " + MAX_INFLATED_BYTES + " bytes");
            }
            baos.write(buf, 0, r);
        }
        ByteArrayInputStream in = new ByteArrayInputStream(baos.toByteArray());
        ctx.source = in;
        Object obj = SerlImpl.readObject(in, ctx);
        long tail = SerlImpl.readLong(in);
        if (tail != 0x0d000721) {
            throw new IOException("Data corrupted!");
        }
        iis.close();
        return new SerlBridgeResult(obj, ctx.isDirty);
    }
}
