package nano.http.bukkit.cipher;

import java.io.*;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class CipheredClassLoader extends ClassLoader {
    byte[] key;
    JarFile jar;
    Enumeration<JarEntry> entries;

    public CipheredClassLoader(byte[] key, File xar) throws IOException {
        super(CipheredClassLoader.class.getClassLoader());
        this.key = key;
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
            if (i % 10 == 0) {
                bytes[i] ^= (byte) 0x05;
                // Or anything else. Just don't leave it unmodified. Or see cracks fly. 
                // What? Why I leave it undocumented? Why don't you open your source?
            }
            bytes[i] ^= key[i % key.length];
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            String path = name.replace('.', '/') + ".class";
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
        JarEntry entry = jar.getJarEntry(name);
        if (entry != null) {
            try {
                byte[] bytes = readAllBytes(jar.getInputStream(entry));
                decrypt(bytes, key);
                return new ByteArrayInputStream(bytes);
            } catch (IOException e) {
                return null;
            }
        } else {
            return super.getResourceAsStream(name);
        }
    }

    public void close() throws IOException {
        jar.close();
    }
}
