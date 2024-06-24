package nano.http.bukkit.internal;

import com.sun.management.OperatingSystemMXBean;
import nano.http.d2.console.Logger;

import java.io.PrintWriter;
import java.lang.management.*;
import java.util.Date;
import java.util.Map;

public class WatchDog implements Runnable {
    private static final String[] msg = {"Don't be sad, have a hug!", "Oops...But why?", "I'm sorry, I'm sorry, I'm sorry!", "That's not a bug, that's a feature!", "Happy debugging!", "Get a cup of coffee!"};
    private static final OperatingSystemMXBean os_mxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public static void dump() {
        Logger.info("[WatchDog] Dumping threads...");
        OperatingSystemMXBean os_mxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        double cpuLoad = os_mxb.getSystemCpuLoad();
        Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
        try {
            PrintWriter writer = new PrintWriter("dump" + System.currentTimeMillis() + ".txt");
            writer.println("Thread dump at " + new Date());
            writer.println("CPU Load: " + ((int) (cpuLoad * 100)) + "%");
            for (Map.Entry<Thread, StackTraceElement[]> entry : threads.entrySet()) {
                Thread thread = entry.getKey();
                StackTraceElement[] stackTrace = entry.getValue();
                long[] ids = new long[1];
                ids[0] = thread.getId();
                ThreadInfo[] infos = threadMxBean.getThreadInfo(ids, true, true);
                ThreadInfo info = infos[0];
                writer.println("\nThread [" + thread.getName() + "] (id=" + thread.getId() + ") status: " + thread.getState() + " CPU time: " + threadMxBean.getThreadCpuTime(thread.getId()) + " ns");
                for (StackTraceElement element : stackTrace) {
                    writer.println("  " + element.toString());
                }
                for (LockInfo lockInfo : info.getLockedSynchronizers()) {
                    writer.println("  locked " + lockInfo);
                }
                MonitorInfo[] monitorInfos = info.getLockedMonitors();
                if (monitorInfos.length > 0) {
                    writer.println("  locked monitors:");
                    for (MonitorInfo monitorInfo : monitorInfos) {
                        writer.println("    - " + monitorInfo.getLockedStackDepth() + " levels deep");
                        writer.println("      " + monitorInfo);
                    }
                }
            }
            writer.println("\nSystem Properties:");
            writer.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
            writer.println("Memory: " + (Runtime.getRuntime().totalMemory() / 1024 / 1024) + "MB / " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "MB");
            writer.println("CPU Cores: " + os_mxb.getAvailableProcessors());
            writer.println("Java: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor"));
            writer.println("Java VM: " + System.getProperty("java.vm.name") + " " + System.getProperty("java.vm.version") + " " + System.getProperty("java.vm.vendor"));
            writer.println("Java Specification Name: " + System.getProperty("java.specification.name"));
            writer.println("Java Specification Vendor: " + System.getProperty("java.specification.vendor"));
            writer.println("Java Specification Version: " + System.getProperty("java.specification.version"));
            writer.println("Java Home: " + System.getProperty("java.home"));
            writer.println("Java Class Path: " + System.getProperty("java.class.path"));
            writer.println("Java Library Path: " + System.getProperty("java.library.path"));
            writer.println("Java I/O Temp Dir: " + System.getProperty("java.io.tmpdir"));
            writer.println("Java Ext Dir: " + System.getProperty("java.ext.dirs"));
            writer.println("User: " + System.getProperty("user.name"));
            writer.println("User Home: " + System.getProperty("user.home"));
            writer.println("User Dir: " + System.getProperty("user.dir"));
            writer.println("User Country: " + System.getProperty("user.country"));
            writer.println("User Language: " + System.getProperty("user.language"));
            writer.println("User Timezone: " + System.getProperty("user.timezone"));
            writer.println("File Separator: " + System.getProperty("file.separator"));
            writer.println("Path Separator: " + System.getProperty("path.separator"));
            writer.println("File Encoding: " + System.getProperty("file.encoding"));
            writer.println("Default Charset: " + System.getProperty("sun.jnu.encoding"));

            writer.println("\n" + msg[(int) (Math.random() * msg.length)]);
            writer.close();
            Logger.info("[WatchDog] Thread-dump created!");
        } catch (Exception e) {
            Logger.error("[WatchDog] Error writing thread dump to file.", e);
        }
    }

    @Override
    public void run() {
        int count = 0;
        while (true) {
            double cpuLoad = os_mxb.getSystemCpuLoad();
            if (cpuLoad > 0.7) {
                count++;
                if (count >= 60) {
                    Logger.warning("[WatchDog] CPU Load is too high! (" + ((int) (cpuLoad * 100)) + ")");
                    Logger.warning("[WatchDog] Dumping Threads...");
                    dump();
                    Logger.warning("[WatchDog] Stopping server...");
                    System.exit(0);
                }
            } else {
                count = 0;
            }
            try {
                //noinspection BusyWait
                Thread.sleep(1000L);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
