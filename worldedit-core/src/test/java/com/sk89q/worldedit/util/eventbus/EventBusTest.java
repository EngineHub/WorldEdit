/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.util.eventbus;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
