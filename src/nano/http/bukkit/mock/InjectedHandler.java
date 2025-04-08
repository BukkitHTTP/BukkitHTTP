package nano.http.bukkit.mock;


import nano.http.bukkit.mock.dirty.MakeAccessible;
import nano.http.bukkit.mock.impl.ModifiedCon;
import nano.http.d2.core.ParmsDecoder;
import nano.http.d2.serve.ServeProvider;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Properties;

public class InjectedHandler extends URLStreamHandler {
    public final String protocol;

    public InjectedHandler(String protocol) {
        this.protocol = protocol;
    }

    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        ServeProvider provider = InjectRegistry.get(u.getHost());
        if (provider == null) {
            return byReal(u);
        }

        Properties query = new Properties();
        ParmsDecoder.decodeParms(u.getQuery(), query);
        return new ModifiedCon(u, Proxy.NO_PROXY, query, provider, u.getPath());
    }

    @Override
    protected URLConnection openConnection(URL u, Proxy p) throws IOException {
        ServeProvider provider = InjectRegistry.get(u.getHost());
        if (provider == null) {
            return byReal(u, p);
        }

        Properties query = new Properties();
        ParmsDecoder.decodeParms(u.getQuery(), query);
        return new ModifiedCon(u, p, query, provider, u.getPath());
    }

    private URLConnection byReal(URL u, Proxy p) throws IOException {
        try {
            URLStreamHandler realHandler = getRealHandler();
            Method openConnection = realHandler.getClass().getDeclaredMethod("openConnection", URL.class, Proxy.class);
            MakeAccessible.makeAccessible(openConnection);
            openConnection.invoke(realHandler, u, p);
            return (URLConnection) openConnection.invoke(realHandler, u, p);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private URLConnection byReal(URL u) throws IOException {
        try {
            URLStreamHandler realHandler = getRealHandler();
            Method openConnection = realHandler.getClass().getDeclaredMethod("openConnection", URL.class);
            MakeAccessible.makeAccessible(openConnection);
            return (URLConnection) openConnection.invoke(realHandler, u);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private URLStreamHandler getRealHandler() {
        try {
            Class<?> clazz = Class.forName("sun.net.www.protocol." + protocol + ".Handler");
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            MakeAccessible.makeAccessible(constructor);
            return (URLStreamHandler) constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
