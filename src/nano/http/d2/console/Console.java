package nano.http.d2.console;

import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Console {
    private static final AtomicBoolean awaiting = new AtomicBoolean(false);
    private static final ConcurrentHashMap<String, Runnable> commands = new ConcurrentHashMap<>();
    private static String result;
    private static int Tid = 0;
    public static boolean magic = true;

    static {
        Thread t = new Thread(() -> {
            try {
                Thread.sleep(2000L);
            } catch (Exception ignored) {
            }
            if (!magic) {
                Logger.info("Console is disabled D:");
                return;
            }
            Logger.info("Console start-up finished.");
            Scanner s = new Scanner(System.in);
            try {
                while (true) {
                    handle(s.nextLine());
                }
            } catch (Exception e) {
                Logger.error("Console error", e);
                Logger.warning("The console has been disabled due to an error.");
                Logger.warning("Please restart the server to re-enable the console.");
            }
        });
        t.setName("Console-Thread-Scanner");
        t.setDaemon(true);
        t.start();
        commands.put("help", () -> {
            Logger.info("Available commands:");
            StringBuilder sb = new StringBuilder();
            for (String s : commands.keySet()) {
                if (s.charAt(0) != '_') {
                    sb.append(s).append(", ");
                }
            }
            sb.delete(sb.length() - 2, sb.length());
            Logger.info(sb.toString());
        });
        // Commands that are not meant to be used by the user should be prefixed with an underscore.
        commands.put("_help", () -> {
            Logger.info("Available commands (!):");
            StringBuilder sb = new StringBuilder();
            for (String s : commands.keySet()) {
                sb.append(s).append(", ");
            }
            sb.delete(sb.length() - 2, sb.length());
            Logger.info(sb.toString());
        });
    }

    private static void handle(String s) {
        if (awaiting.get()) {
            result = s;
            synchronized (awaiting) {
                awaiting.notify();
            }
            return;
        }
        if (commands.containsKey(s)) {
            Thread t = new Thread(commands.get(s));
            t.setName("Console-Command-" + s + "-" + Tid);
            Tid++;
            t.start();
        } else {
            Logger.warning("Unknown command: " + s + " (type 'help' for a list of commands)");
        }
    }

    public static String await() {
        if (awaiting.get()) {
            throw new RuntimeException("Already awaiting!");
        }
        awaiting.set(true);
        try {
            synchronized (awaiting) {
                awaiting.wait();
            }
        } catch (InterruptedException e) {
            return null;
        }
        awaiting.set(false);
        return result;
    }

    public static void register(String command, Runnable r) {
        if (!commands.containsKey(command)) {
            commands.put(command, r);
            return;
        }
        Logger.error("Can not register command : " + command, new UnsupportedOperationException("Duplicate command registration"));
    }

    public static void unregister(String command) {
        commands.remove(command);
    }
}
