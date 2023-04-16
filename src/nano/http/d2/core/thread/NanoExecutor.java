package nano.http.d2.core.thread;

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
    public static ExecutorService executorService = new ThreadPoolExecutor(2, 20, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(1), new NanoThreadFactory("Worker"), new NanoAbortPolicy());
    public static ExecutorService errorExecutorService = new ThreadPoolExecutor(1, 3, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(40), new NanoThreadFactory("ErrorHandler"), new NanoAbortPolicy());
}
