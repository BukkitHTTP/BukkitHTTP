package nano.http.d2.core.thread.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class NanoExecutor {
    // FAQ:
    // Q: Why is the core pool size 2?
    // A: Because most of the time, the server is idle, and the core pool size is 2, which is enough to handle the request.
    // Q: Why is the pool a static, single instance?
    // A: While NanoHttpd is instantiated, the pool will not affect the behavior of the server.
    // But, if the pool is not static, idle threads will not be used, causing lag.
    // If your server has a lot of requests, you can increase the maximum pool size, but not instantiating multiple thread-pools.

    public final ExecutorService executorService;
    public final ExecutorService errorExecutorService;

    public NanoExecutor(int coreSize, int errorSize) {
        if (coreSize < 2) throw new IllegalArgumentException("Core pool size must be at least 2");
        if (errorSize < 1) throw new IllegalArgumentException("Error pool size must be at least 1");
        executorService = new ThreadPoolExecutor(2, coreSize, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1), new NanoThreadFactory("Worker"), new NanoAbortPolicy());
        errorExecutorService = new ThreadPoolExecutor(1, errorSize, 30L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(40), new NanoThreadFactory("ErrorHandler"), new NanoAbortPolicy());
    }
}
