package nano.http.bukkit;

import nano.http.bukkit.internal.*;
import nano.http.bukkit.internal.cipher.BukkitCipher;
import nano.http.bukkit.internal.cipher.KeyGen;
import nano.http.d2.console.Console;
import nano.http.d2.console.Logger;
import nano.http.d2.core.NanoHTTPd;
import nano.http.d2.core.thread.NanoPool;
import nano.http.d2.hooks.HookManager;
import nano.http.d2.hooks.impls.EmptySock;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

public class Main {
    public static final String VERSION = "3.1.0 Alpha-1";
    public static final Bukkit_Router router = new Bukkit_Router();
    public static NanoHTTPd server;

    @SuppressWarnings("DataFlowIssue")
    public static void main(String[] args) throws Exception {
        // Log the start time
        long start = System.currentTimeMillis();
        // Print the version
        Logger.info("BukkitHTTP v" + VERSION + " (powered by NanoHTTPd)");
        //noinspection ConstantValue
        if (Main.VERSION.contains("Pro")) {
            // Enable the features of the Pro version
            Console.register("_cipher", new BukkitCipher());
            Console.register("_keygen", new KeyGen());
        }

        // Load the configuration
        File set = new File("server.properties");
        if (!set.exists()) {
            Properties pr = new Properties();
            pr.setProperty("port", "80");
            pr.setProperty("watchdog", "false");
            pr.setProperty("firewall", "false");
            pr.setProperty("threads", "20");
            pr.setProperty("errhandler-threads", "3");
            pr.store(new FileWriter(set), "BukkitHTTP Server Settings");
        }
        Properties pr = new Properties();
        pr.load(new FileReader(set));

        // Parse the configuration
        int port = Integer.parseInt(pr.getProperty("port"));

        // Load the plugins
        File plugin = new File("plugins");
        if (!plugin.exists()) {
            if (!plugin.mkdir()) {
                throw new Exception("Failed to create plugins folder");
            }
        }
        Logger.info("Loading plugins...");
        for (File f : plugin.listFiles()) {
            router.load(f);
        }

        // Register the core console commands
        Console.register("stop", new BukkitStop());
        Console.register("load", new BukkitLoad());
        Console.register("unload", new BukkitUnload());
        Console.register("pl", new BukkitPlugins());
        Console.register("dump", WatchDog::dump);

        // Start the watchdog, if enabled
        if (pr.getProperty("watchdog").equals("true")) {
            Logger.info("WatchDog is enabled.");
            Thread t = new Thread(new WatchDog());
            t.setName("BukkitHTTP-WatchDog");
            t.start();
        }

        String firewall = pr.getProperty("firewall");
        if (!firewall.equals("true")) {
            if (firewall.equals("cloudflare")) {
                Logger.warning("Enabling CloudFlare Only Mode!");
                HookManager.requestHook = new CloudflareHook(HookManager.requestHook);
            }
            Logger.warning("Firewall is disabled!");
            HookManager.socketHook = new EmptySock();
        } else {
            Logger.info("Firewall is enabled.");
        }
        // Some magic :P
        Runtime.getRuntime().addShutdownHook(new Thread(BukkitStop::doStop));
        HookManager.invoke();
        NanoPool.setCoreSize(Integer.parseInt(pr.getProperty("threads")));
        NanoPool.setErrorSize(Integer.parseInt(pr.getProperty("errhandler-threads")));

        // Start the server
        server = new NanoHTTPd(port, router);

        // Enjoy!
        Logger.info("Done! (" + (System.currentTimeMillis() - start) + "ms)" + " Listening on port " + port);

//        WebSocketServer.register("/debug", DebugWebSocket.class);
//        WebSocketClient wsc = new WebSocketClient("ws://localhost:80/debug");
//        wsc.sendTestFrame();
    }
}
