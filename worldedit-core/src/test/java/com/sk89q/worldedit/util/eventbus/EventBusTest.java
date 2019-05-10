package com.sk89q.worldedit.util.eventbus;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class EventBusTest {

    private static final class Event {

    }

    private static final class Subscriber {

        private final List<Event> events = new ArrayList<>();

        @Subscribe
        public void onEvent(Event event) {
            events.add(event);
        }

    }

    private EventBus eventBus = new EventBus();

    @Test
    public void testRegister() {
        Subscriber subscriber = new Subscriber();
        eventBus.register(subscriber);
        Event e1 = new Event();
        eventBus.post(e1);
        Event e2 = new Event();
        eventBus.post(e2);
        assertEquals(asList(e1, e2), subscriber.events);
    }

    @Test
    public void testUnregister() {
        Subscriber subscriber = new Subscriber();
        eventBus.register(subscriber);
        Event e1 = new Event();
        eventBus.post(e1);
        eventBus.unregister(subscriber);
        Event e2 = new Event();
        eventBus.post(e2);
        assertEquals(singletonList(e1), subscriber.events);
    }
}
