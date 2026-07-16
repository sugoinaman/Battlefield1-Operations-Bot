package rotation;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Production scheduler retaining the original one-second initial delay and four-second polling interval.
 */
public final class ExecutorRotationScheduler implements RotationScheduler {

    private ScheduledExecutorService executor;

    @Override
    public synchronized void start(Runnable task) {
        if (isRunning()) {
            stop();
        }
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(task, 1, 4, TimeUnit.SECONDS);
    }

    @Override
    public synchronized void stop() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
        executor = null;
    }

    @Override
    public synchronized boolean isRunning() {
        return executor != null && !executor.isShutdown();
    }
}
