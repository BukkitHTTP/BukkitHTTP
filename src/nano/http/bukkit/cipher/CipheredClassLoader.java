package nano.http.bukkit.cipher;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CipheredClassLoader extends ClassLoader {
    byte[] key;
    JarFile jar;
    Enumeration<JarEntry> entries;

    public CipheredClassLoader(String key, File xar) throws IOException {
        super(CipheredClassLoader.class.getClassLoader());
        String tmpKey = KeyGen.getKey(key);
        if (tmpKey.equals("X")) {
            throw new RuntimeException("Unable to validate License");
        }
        this.key = tmpKey.getBytes(StandardCharsets.UTF_8);
        jar = new JarFile(xar);
        entries = jar.entries();
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

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            String path = name.replace('.', '/') + ".class";
            path = process(path);
            JarEntry entry = jar.getJarEntry(path);
            if (entry != null) {
                byte[] bytes = readAllBytes(jar.getInputStream(entry));
                if (bytes[0] != (byte) 0xCA || bytes[1] != (byte) 0xFE || bytes[2] != (byte) 0xBA || bytes[3] != (byte) 0xBE) {
                    decrypt(bytes, key);
                }
                return defineClass(name, bytes, 0, bytes.length);
            }
            return getParent().loadClass(name);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        InputStream is = super.getResourceAsStream(name);
        if (is != null) {
            return is;
        }
        if (name.startsWith("/")) {
            name = name.substring(1);
        }
        JarEntry entry = jar.getJarEntry(name);
        if (entry != null) {
            try {
                byte[] bytes = readAllBytes(jar.getInputStream(entry));
                decrypt(bytes, key);
                return new ByteArrayInputStream(bytes);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    public void close() throws IOException {
        jar.close();
    }
}
