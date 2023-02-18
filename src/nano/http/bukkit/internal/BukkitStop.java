package nano.http.bukkit.internal;

import nano.http.bukkit.Main;
import nano.http.d2.console.Logger;

public class BukkitStop implements Runnable {
    private static boolean isStopping = false;

    public static void doStop() {
        if (isStopping) {
            return;
        }
        isStopping = true;
        Main.server.stop();
        Logger.info("Stopping BukkitHTTP Server...");
        for (Bukkit_Node node : Main.router.nodes) {
            try {
                node.onDisable();
            } catch (Exception e) {
                Logger.error("Error while disabling plugin " + node.name + " , ignoring!", e);
            }
        }
    }

    @Override
    public void run() {
        doStop();
        System.exit(0);
    }
}
