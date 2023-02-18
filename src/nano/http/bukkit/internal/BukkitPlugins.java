package nano.http.bukkit.internal;

import nano.http.bukkit.Main;
import nano.http.d2.console.Logger;

public class BukkitPlugins implements Runnable {
    @Override
    public void run() {
        StringBuilder sb = new StringBuilder("NanoCore, ");
        for (Bukkit_Node node : Main.router.nodes) {
            sb.append(node.name).append(", ");
        }
        Logger.info("Plugins: " + sb.substring(0, sb.length() - 2));
    }
}
