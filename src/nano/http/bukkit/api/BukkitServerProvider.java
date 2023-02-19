package nano.http.bukkit.api;

import nano.http.d2.core.Response;
import nano.http.d2.serve.ServeProvider;

import java.io.File;
import java.util.Properties;

public abstract class BukkitServerProvider implements ServeProvider {
    // This is the method that will be called when the plugin is enabled.
    // You can use this to register commands, hooks, etc.
    public abstract void onEnable(String name, File dir, String uri);

    // This is the method that will be called when the plugin is disabled.
    // You can use this to unregister commands, hooks, etc.
    // Don't forget to save your databases!
    public abstract void onDisable();

    // This is the method that will be called when a request is made directly to the plugin.
    // For example, if you have a plugin with the URI "/myplugin", and you make a request to "/myplugin/abc/hello",
    // this method will be called with the URI "/abc/hello".
    // If there is another plugin registered with the URI "/myplugin/abc", the request will be routed to it with the URI "/hello".
    // This is because the server will try to route the request to the plugin with the longest matching URI.
    // If you return null, the request will be passed to the fallback part, but not the next plugin.
    // In the previous example, the request will be passed to the fallback part with the URI "/myplugin/abc/hello".
    // And "/myplugin" will not receive the request anyway.
    public abstract Response serve(String uri, String method, Properties header, Properties parms, Properties files);

    // This is the method that will be called when a request is made to the server, but none of the plugins handle it.
    // Or the routed plugin returns null.
    // Use this to make your plugin act like a fallback for the server.
    // If you return null, the request will be passed to the next plugin.
    // If no plugin handles the request, the server will return a 404 error.
    public abstract Response fallback(String uri, String method, Properties header, Properties parms, Properties files);
}
