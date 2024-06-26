package nano.http.bukkit.api.debug;

import nano.http.bukkit.internal.Bukkit_Node;
import nano.http.d2.console.Console;
import nano.http.d2.console.Logger;
import nano.http.d2.core.NanoHTTPd;
import nano.http.d2.hooks.HookManager;

import java.io.File;

// This is a stub file used to test the plugin.
@SuppressWarnings("unused")
public class DebugMain {
    public static void debug(Class<?> plugin, String uri, int port, boolean disableFirewall) throws Exception {
        Bukkit_Node node = new Bukkit_Node(uri, plugin.getClassLoader(), plugin.getName(), "Debug");
        DebugRouter router = new DebugRouter(node, uri);
        try {
            node.onEnable(plugin.getName(), new File("."), uri);
        } catch (Throwable e) {
            Logger.error("Error while enabling plugin.", e);
        }
        new NanoHTTPd(port, router);
        Console.register("stop", () -> {
            try {
                node.onDisable();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            System.exit(0);
        });
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                node.onDisable();
            } catch (Throwable e) {
                Logger.error("Error while disabling plugin.", e);
            }
        }));
        if (disableFirewall) {
            HookManager.socketHook = (socket) -> true;
        }
        Logger.info("Debug server started on port " + port + ".");
        Logger.info("Use command /stop to stop.");
    }

    public static void debug(Class<?> plugin, String uri) throws Exception {
        debug(plugin, uri, 80);
    }

    public static void debug(Class<?> plugin, String uri, int port) throws Exception {
        debug(plugin, uri, port, true);
    }
}
