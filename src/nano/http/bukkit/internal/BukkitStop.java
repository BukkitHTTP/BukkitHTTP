package nano.http.bukkit.internal;

import nano.http.bukkit.Main;
import nano.http.d2.console.Logger;

public class BukkitStop implements Runnable {
    public static boolean isStopping = false;

    public static void doStop() {
        if (isStopping) {
            return;
        }
        isStopping = true;
        Main.server.stop();
        Logger.info("Stopping BukkitHTTP Server...");
        int count = Main.router.nodes.size();
        int now = 1;
        for (Bukkit_Node node : Main.router.nodes) {
            try {
                node.onDisable();
                Logger.info("[ " + now++ + " / " + count + " ]");
            } catch (Throwable e) {
                Logger.error("Error while disabling plugin " + node.name + " , ignoring!", e);
            }
        }
    }

    @Override
    public void run() {
        System.exit(0);
    }
}
