package nano.http.d2.core.thread.internal;

import nano.http.d2.console.Logger;
import nano.http.d2.core.HTTPSession;
import nano.http.d2.core.thread.NanoPool;

import java.lang.reflect.Field;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class NanoAbortPolicy implements RejectedExecutionHandler {
    private static final Field callableField;
    private static final Field runnerField;

    static {
        try {
            callableField = FutureTask.class.getDeclaredField("callable");
            callableField.setAccessible(true);
            runnerField = Class.forName("java.util.concurrent.Executors$RunnableAdapter").getDeclaredField("task");
            runnerField.setAccessible(true);
        } catch (Throwable t) {
            Logger.error("Can not set-up the backup thread pool.", t);
            throw new RuntimeException("Fuck JUC!");
        }
    }

    private static Runnable getRunnable(FutureTask<?> t) throws Throwable {
        // Since we're not modifying its value, it shouldn't alter any problem.
        return (Runnable) runnerField.get(callableField.get(t));
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        try {
            FutureTask<?> ft = (FutureTask<?>) r;
            HTTPSession session = (HTTPSession) getRunnable(ft);
            if (!session.isHighDemand) {
                session.isHighDemand = true;
                NanoPool.submitError(session);
            }
        } catch (Throwable ignored) {
        }
    }
}
