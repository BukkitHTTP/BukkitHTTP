package nano.http.bukkit.api.debug;

import nano.http.bukkit.internal.Bukkit_Node;
import nano.http.d2.console.Console;
import nano.http.d2.console.Logger;
import nano.http.d2.core.NanoHTTPd;

import java.io.File;

// This is a stub file used to test the plugin.
@SuppressWarnings("unused")
public class DebugMain {
    public static void debug(Class<?> plugin, String uri) throws Exception {
        Bukkit_Node node = new Bukkit_Node(uri, plugin.getClassLoader(), plugin.getName(), "Debug");
        DebugRouter router = new DebugRouter(node, uri);
        node.onEnable(plugin.getName(), new File("."), uri);
        new NanoHTTPd(80, router);
        Console.register("stop", () -> {
            try {
                node.onDisable();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.exit(0);
        });
        Logger.info("Debug server started on port 80.");
        Logger.info("Use command /stop to stop.");
    }
}
