package nano.http.d2.core.thread;

import nano.http.d2.core.thread.internal.NanoExecutor;

public class NanoPool {
    private static NanoExecutor executor = null;

    private static int CORE_SIZE = 20;

    private static int ERROR_SIZE = 3;

    public static void setCoreSize(int coreSize) throws IllegalArgumentException {
        if (executor != null) throw new IllegalStateException("Cannot change core size after initialization");
        CORE_SIZE = coreSize;
    }

    public static void setErrorSize(int errorSize) {
        if (executor != null) throw new IllegalStateException("Cannot change error size after initialization");
        ERROR_SIZE = errorSize;
    }

    public static boolean isInitialized() {
        return executor != null;
    }

    private static void init() {
        if (executor == null) {
            executor = new NanoExecutor(CORE_SIZE, ERROR_SIZE);
        }
    }

    public static void submit(Runnable r) {
        init();
        executor.executorService.submit(r);
    }

    public static void submitError(Runnable r) {
        init();
        executor.errorExecutorService.submit(r);
    }
}
