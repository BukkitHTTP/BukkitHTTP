package nano.http.bukkit.internal;

import nano.http.bukkit.api.BukkitServerProvider;
import nano.http.d2.console.Logger;
import nano.http.d2.consts.Mime;
import nano.http.d2.consts.Status;
import nano.http.d2.core.Response;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Properties;

public class Bukkit_Node {
    final String uri;
    final BukkitServerProvider serverProvider;
    final ClassLoader classLoader;
    final Method onEnable;
    final Method onDisable;
    final Method serve;
    final Method fallback;
    final String name;

    public Bukkit_Node(String uri, ClassLoader classLoader, String classPath, String name) throws Exception {
        this.uri = uri;
        this.classLoader = classLoader;
        this.serverProvider = (BukkitServerProvider) classLoader.loadClass(classPath).getConstructor().newInstance();
        this.onEnable = serverProvider.getClass().getMethod("onEnable", String.class, File.class, String.class);
        this.onDisable = serverProvider.getClass().getMethod("onDisable");
        this.serve = serverProvider.getClass().getMethod("serve", String.class, String.class, Properties.class, Properties.class, Properties.class);
        this.fallback = serverProvider.getClass().getMethod("fallback", String.class, String.class, Properties.class, Properties.class, Properties.class);
        this.name = name;
    }

    public void onEnable(String name, File dir, String uri) throws Exception {
        onEnable.invoke(serverProvider, name, dir, uri);
    }

    public void onDisable() throws Exception {
        onDisable.invoke(serverProvider);
    }

    public Response serve(String uri, String method, Properties header, Properties parms, Properties files) {
        try {
            return (Response) serve.invoke(serverProvider, uri, method, header, parms, files);
        } catch (Exception e) {
            Logger.error("Error while serving request.", e);
            return new Response(Status.HTTP_INTERNALERROR, Mime.MIME_PLAINTEXT, "Error: " + e);
        }
    }

    public Response fallback(String uri, String method, Properties header, Properties parms, Properties files) {
        try {
            return (Response) fallback.invoke(serverProvider, uri, method, header, parms, files);
        } catch (Exception e) {
            Logger.error("Error while serving fallback request.", e);
            return null;
        }
    }
}
