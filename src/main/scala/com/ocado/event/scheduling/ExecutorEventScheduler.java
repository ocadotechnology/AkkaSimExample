package com.ocado.event.scheduling;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.ocado.event.Event;
import com.ocado.time.TimeProvider;

public class ExecutorEventScheduler implements EventScheduler {
    private final TimeProvider timeProvider;

    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);

    private final Map<Event, ScheduledFuture<?>> eventsMap = new ConcurrentHashMap<>();

    private final AtomicBoolean failed = new AtomicBoolean(false);

    public ExecutorEventScheduler(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public void schedule(Event event) {
        try {
            long delay = (long) (event.time - timeProvider.getTime());
            ScheduledFuture<?> future = executor.schedule(() -> executeEvent(event), delay, TimeUnit.MILLISECONDS);
            eventsMap.put(event, future);
        } catch (RejectedExecutionException e) {
            if (failed.compareAndSet(false, true)) {
                System.out.println("Failed to schedule event [" + event + "]");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void cancel(Event e) {
        ScheduledFuture<?> future = eventsMap.get(e);
        if (future != null) {
            future.cancel(false);
            eventsMap.remove(e);
        }
    }

    @Override
    public void stop() {
        executor.shutdown();
        eventsMap.clear();
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
            eventsMap.remove(event);
        } catch (Throwable t) {
            System.out.println("Simulation failed at " + event.time);
            t.printStackTrace();
            stop();
        }
    }
}
