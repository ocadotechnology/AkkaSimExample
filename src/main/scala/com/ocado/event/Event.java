package com.ocado.event;

import java.util.concurrent.atomic.AtomicLong;

import com.ocado.event.scheduling.EventScheduler;
import com.ocado.time.TimeProvider;

public abstract class Event {
    private static AtomicLong idGenerator = new AtomicLong(0);
    public final long id;
    public final double time;
    public final String description;

    protected EventScheduler scheduler;
    protected TimeProvider timeProvider;

    public Event(double time, String description) {
        this.id = idGenerator.getAndIncrement();
        this.time = time;
        this.description = description;
    }

    public static EventBuilder at(double time, String description) {
        return new EventBuilder(time, description);
    }

    public final void action() {
        System.out.println("Executing " + this + " at " + timeProvider.getTime());
        execute();
    }

    protected abstract void execute();

    public void setScheduler(EventScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public void setTimeProvider(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        Event other = (Event) obj;
        return id == other.id;
    }

    @Override
    public String toString() {
        return "Event(id=" + id + ", time=" + time + ", desc=" + description + ")";
    }

    public static class EventBuilder {
        private final double time;
        private final String description;

        EventBuilder(double time, String description) {
            this.time = time;
            this.description = description;
        }

        public Event run(Runnable r) {
            return new Event(time, description) {
                @Override
                public void execute() {
                    r.run();
                }
            };
        }
    }
}
