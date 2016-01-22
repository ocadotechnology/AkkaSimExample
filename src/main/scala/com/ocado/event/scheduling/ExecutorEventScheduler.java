package com.ocado.event.scheduling;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ocado.event.Event;
import com.ocado.time.TimeProvider;

public class ExecutorEventScheduler implements EventScheduler {
    private final TimeProvider timeProvider;

    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);

    private final AtomicBoolean failed = new AtomicBoolean(false);

    public ExecutorEventScheduler(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public void schedule(Event event) {
        try {
            executor.schedule(() -> executeEvent(event), (long) (event.time - timeProvider.getTime()), TimeUnit.MILLISECONDS);
        } catch (RejectedExecutionException e) {
            if (failed.compareAndSet(false, true)) {
                System.out.println("Failed to schedule event [" + event + "]");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        executor.shutdown();
    }

    @Override
    public TimeProvider getTimeProvider() {
        return timeProvider;
    }

    private void executeEvent(Event event) {
        try {
            event.setTimeProvider(timeProvider);
            event.setScheduler(this);
            event.action();
        } catch (Throwable t) {
            System.out.println("Simulation failed at " + event.time);
            t.printStackTrace();
            stop();
        }
    }
}
