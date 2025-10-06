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
        Object obj = SerlImpl.readObject(iis, ctx);
        long tail = SerlImpl.readLong(iis);
        if (tail != 0x0d000721) {
            throw new IOException("Data corrupted!");
        }
        iis.close();
        return new SerlBridgeResult(obj, ctx.isDirty);
    }


}
