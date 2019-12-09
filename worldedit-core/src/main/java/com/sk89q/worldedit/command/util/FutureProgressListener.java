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

package com.sk89q.worldedit.command.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;

import java.util.Timer;

public class FutureProgressListener implements Runnable {

    private static final Timer timer = new Timer();
    private static final int MESSAGE_DELAY = 1000;

    private final MessageTimerTask task;

    @Deprecated
    public FutureProgressListener(Actor sender, String message) {
        this(sender, TextComponent.of(message));
    }

    public FutureProgressListener(Actor sender, Component message) {
        checkNotNull(sender);
        checkNotNull(message);

        task = new MessageTimerTask(sender, message);
        timer.schedule(task, MESSAGE_DELAY);
    }

    @Override
    public void run() {
        task.cancel();
    }

    @Deprecated
    public static void addProgressListener(ListenableFuture<?> future, Actor sender, String message) {
        future.addListener(new FutureProgressListener(sender, message), MoreExecutors.directExecutor());
    }

    public static void addProgressListener(ListenableFuture<?> future, Actor sender, Component message) {
        future.addListener(new FutureProgressListener(sender, message), MoreExecutors.directExecutor());
    }

}
