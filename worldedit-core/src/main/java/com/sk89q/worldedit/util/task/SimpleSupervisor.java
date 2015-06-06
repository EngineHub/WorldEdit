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

package com.sk89q.worldedit.util.task;

import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.worldedit.extension.platform.Actor;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An implementation of a {@code Supervisor} that informs owners that are
 * {@link Actor}s of progress on tasks.
 */
public class SimpleSupervisor implements Supervisor {

    private static final Timer timer = new Timer();
    private static final int QUEUE_MESSAGE_DELAY = 1000;

    private final List<Task<?>> monitored = new ArrayList<Task<?>>();
    private final Object lock = new Object();

    @Override
    public List<Task<?>> getTasks() {
        synchronized (lock) {
            return new ArrayList<Task<?>>(monitored);
        }
    }

    @Override
    public void monitor(final Task<?> task) {
        checkNotNull(task);

        Object owner = task.getOwner();
        final TimerTask delayedMessage;
        if (owner instanceof Actor) {
            delayedMessage = new ActorMessageTimerTask((Actor) task.getOwner(), "Please wait. Your task has been queued.");
            timer.schedule(delayedMessage, QUEUE_MESSAGE_DELAY);
        } else {
            delayedMessage = null;
        }

        synchronized (lock) {
            monitored.add(task);
        }

        task.addListener(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    monitored.remove(task);
                }

                if (delayedMessage != null) {
                    delayedMessage.cancel();
                }
            }
        }, MoreExecutors.sameThreadExecutor());
    }

}
