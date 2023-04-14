package nano.http.bukkit.internal;

import com.sun.management.OperatingSystemMXBean;
import nano.http.d2.console.Logger;

import java.io.PrintWriter;
import java.lang.management.*;
import java.util.Date;
import java.util.Map;

public class WatchDog implements Runnable {
    @Override
    public void run() {
        OperatingSystemMXBean os_mxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        ThreadMXBean threadMxBean = ManagementFactory.getThreadMXBean();
        int count = 0;
        while (true) {
            double cpuLoad = os_mxb.getSystemCpuLoad();
            if (cpuLoad > 0.7) {
                count++;
                if (count >= 60) {
                    Logger.warning("[WatchDog] CPU Load is too high! (" + ((int) (cpuLoad * 100)) + ")");
                    Logger.warning("[WatchDog] Dumping Threads...");
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
                            writer.println("\nThread " + thread.getName() + " (id=" + thread.getId() + ") status: " + thread.getState() + " CPU time: " + threadMxBean.getThreadCpuTime(thread.getId()) + " ns");
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
                        writer.close();
                    } catch (Exception e) {
                        Logger.error("[WatchDog] Error writing thread dump to file.", e);
                    }
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
