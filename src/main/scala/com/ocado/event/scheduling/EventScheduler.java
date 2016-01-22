package com.ocado.event.scheduling;

import java.util.Comparator;

import com.ocado.event.Event;
import com.ocado.time.TimeProvider;

public interface EventScheduler {
    Comparator<Event> EVENT_COMPARATOR = (o1, o2) -> {
        if (o1.time < o2.time) {
            return -1;
        }
        if (o1.time > o2.time) {
            return 1;
        }
        return Long.compare(o1.id, o2.id);
    };

    void schedule(Event e);

    void stop();

    TimeProvider getTimeProvider();

    default void doAt(double time, Runnable r, String description) {
        schedule(Event.at(time, description).run(r));
    }
}
