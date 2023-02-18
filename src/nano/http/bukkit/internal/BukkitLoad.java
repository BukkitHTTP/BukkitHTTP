package nano.http.bukkit.internal;

import nano.http.bukkit.Main;
import nano.http.d2.console.Console;
import nano.http.d2.console.Logger;

import java.io.File;

public class BukkitLoad implements Runnable {
    @SuppressWarnings("DataFlowIssue")
    @Override
    public void run() {
        Logger.info("Input the path of the plugin you want to unload.");
        String name = Console.await();
        Main.router.load(new File(new File("plugins"), name));
    }
}
