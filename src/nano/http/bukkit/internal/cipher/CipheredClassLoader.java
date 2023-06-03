package nano.http.bukkit.internal.cipher;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.jar.JarFile;

public class CipheredClassLoader extends ClassLoader {
    byte[] key;
    JarFile jar;

    public CipheredClassLoader(String key, File xar) throws IOException {
        super(CipheredClassLoader.class.getClassLoader());
        String tmpKey = KeyGen.getKey(key);
        if (tmpKey.equals("X")) {
            throw new RuntimeException("Unable to validate License");
        }
        this.key = tmpKey.getBytes(StandardCharsets.UTF_8);
        jar = new JarFile(xar);
    }

    public static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    public static void decrypt(byte[] bytes, byte[] key) {
        for (int i = 0; i < bytes.length; i++) {
            if (i % 101 == 0) {
                bytes[i] ^= (byte) 0x2B;
            }
            if (i % 2 == 0) {
                bytes[i] ^= (byte) 0x78;
            }
            // No need to be polite here.
            bytes[i] ^= key[i % key.length];
        }
    }

    public static String process(String s) {
        int h = s.hashCode();
        String ans = h > 0 ? "Nano" : "Guard";
        h = Math.abs(h);
        h %= 89999999;
        h += 10000000;
        ans += h;
        ans += "$.class";
        return ans;
    }


    public void close() throws IOException {
        jar.close();
    }
}
