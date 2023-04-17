package nano.http.bukkit;

import nano.http.bukkit.cipher.BukkitCipher;
import nano.http.bukkit.cipher.KeyGen;
import nano.http.bukkit.internal.*;
import nano.http.d2.console.Console;
import nano.http.d2.console.Logger;
import nano.http.d2.core.NanoHTTPd;
import nano.http.d2.hooks.HookManager;
import nano.http.d2.hooks.impls.EmptySock;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

public class Main {
    public static final String VERSION = "1.0.6 Pro2";
    public static final Bukkit_Router router = new Bukkit_Router();
    public static NanoHTTPd server;

    @SuppressWarnings("DataFlowIssue")
    public static void main(String[] args) throws Exception {
        Logger.info("BukkitHTTP v" + VERSION + " (powered by NanoHTTPd)");
        //noinspection ConstantValue
        if (Main.VERSION.contains("Pro")) {
            Console.register("_cipher", new BukkitCipher());
            Console.register("_keygen", new KeyGen());
        }
        long start = System.currentTimeMillis();
        File set = new File("server.properties");
        if (!set.exists()) {
            Properties pr = new Properties();
            pr.setProperty("port", "80");
            pr.setProperty("watchdog", "true");
            pr.setProperty("firewall", "true");
            pr.store(new FileWriter(set), "BukkitHTTP Server Settings");
        }
        Properties pr = new Properties();
        pr.load(new FileReader(set));
        if (!pr.containsKey("port")) {
            throw new Exception("Port not found in server.properties");
        }
        int port = Integer.parseInt(pr.getProperty("port"));
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
        Logger.info("Done! (" + (System.currentTimeMillis() - start) + "ms)" + " Listening on port " + port);
        Console.register("stop", new BukkitStop());
        Console.register("load", new BukkitLoad());
        Console.register("unload", new BukkitUnload());
        Console.register("pl", new BukkitPlugins());
        Runtime.getRuntime().addShutdownHook(new Thread(BukkitStop::doStop));
        server = new NanoHTTPd(port, router);
        if (pr.getProperty("watchdog").equals("true")) {
            Logger.info("WatchDog is enabled.");
            Thread t = new Thread(new WatchDog());
            t.setName("BukkitHTTP-WatchDog");
            t.start();
            if (!pr.getProperty("firewall").equals("true")) {
                Logger.warning("Firewall is disabled!");
                HookManager.socketHook = new EmptySock();
            }
        }
        HookManager.invoke();
        Console.register("dump", WatchDog::dump);
    }
}
