package nano.http.d2.core.thread.internal;

import nano.http.d2.console.Logger;
import nano.http.d2.core.HTTPSession;
import nano.http.d2.core.thread.NanoPool;

import java.lang.reflect.Field;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public class NanoAbortPolicy implements RejectedExecutionHandler {
    private static Field callableField = null;
    private static Field runnerField = null;

    static {
        try {
            callableField = FutureTask.class.getDeclaredField("callable");
            callableField.setAccessible(true);
            runnerField = Class.forName("java.util.concurrent.Executors$RunnableAdapter").getDeclaredField("task");
            runnerField.setAccessible(true);
        } catch (Throwable t) {
            Logger.warning("Java version is not SE8, err handler will not work! (Expected Java8)");
            Logger.warning("Consider setting core pool size to negative value to change err handler to CallerRunsPolicy");
        }
    }

    private static Runnable getRunnable(FutureTask<?> t) throws Throwable {
        // Since we're not modifying its value, it shouldn't alter any problem.
        return (Runnable) runnerField.get(callableField.get(t));
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        if (callableField == null || runnerField == null) {
            return;
        }
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
