package nano.http.bukkit.mock;

import nano.http.bukkit.mock.dirty.MakeAccessible;
import nano.http.d2.console.Logger;

import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class InjectURI implements URLStreamHandlerFactory {
    public final URLStreamHandlerFactory realFactory;

    public InjectURI(URLStreamHandlerFactory realFactory) {
        this.realFactory = realFactory;

    }

    public static void inject() {
        try {
            Field field = URL.class.getDeclaredField("factory");
            MakeAccessible.makeAccessible(field);
            URLStreamHandlerFactory factory = (URLStreamHandlerFactory) field.get(null);
            if (factory instanceof InjectURI) {
                return;
            }
            field.set(null, new InjectURI(factory));
        } catch (Exception ex) {
            Logger.error("Failed to inject URLStreamHandlerFactory", ex);
        }
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals("http") || protocol.equals("https")) {
            return new InjectedHandler(protocol);
        }
        if (realFactory != null) {
            return realFactory.createURLStreamHandler(protocol);
        }
        try {
            Class<?> clazz = Class.forName("sun.net.www.protocol." + protocol + ".Handler");
            return (URLStreamHandler) clazz.newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
