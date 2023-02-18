package nano.http.bukkit.internal;

import nano.http.bukkit.Main;
import nano.http.d2.console.Console;
import nano.http.d2.console.Logger;

public class BukkitUnload implements Runnable {
    @Override
    public void run() {
        Logger.info("Input the name of the plugin you want to unload.");
        String name = Console.await();
        if (Main.router.removeNode(name)) {
            Logger.info("Plugin unloaded successfully.");
        } else {
            Logger.info("Plugin unloaded unsuccessfully.");
        }
    }
}
