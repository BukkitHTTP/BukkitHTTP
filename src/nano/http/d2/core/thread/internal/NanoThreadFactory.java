package nano.http.d2.core.thread.internal;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NanoThreadFactory implements ThreadFactory {
    private final AtomicInteger threadIdx = new AtomicInteger(0);

    private final String name;

    public NanoThreadFactory(String name) {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r);
        thread.setName("NanoHTTP-" + name + "-" + threadIdx.getAndIncrement());
        return thread;
    }
}
