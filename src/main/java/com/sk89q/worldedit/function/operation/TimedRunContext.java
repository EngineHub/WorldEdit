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

package com.sk89q.worldedit.function.operation;

import java.util.concurrent.TimeUnit;

/**
 * Allows an Operation to run until a time limit is reached, as measured by
 * {@link System#nanoTime()}.
 */
public class TimedRunContext implements RunContext {

    private final long stopTimeNanos;
    private final boolean cancelled;

    /**
     * Create a TimedRunContext with a known stop time, as measured by System.nanoTime().
     *
     * @param stopTimeNanos nanoTime at which to stop the operation
     * @param cancelled whether the cancel flag has been set
     */
    public TimedRunContext(long stopTimeNanos, boolean cancelled) {
        this.stopTimeNanos = stopTimeNanos;
        this.cancelled = cancelled;
    }

    /**
     * Create a TimedRunContext stopping some time from now, using the Java
     * TimeUnit API.
     *
     * @param sourceDuration duration from now, in source units
     * @param sourceUnit the source unit
     * @param cancelled whether the cancel flag has been set
     */
    public TimedRunContext(long sourceDuration, TimeUnit sourceUnit, boolean cancelled) {
        stopTimeNanos = System.nanoTime() + sourceUnit.toNanos(sourceDuration);
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean shouldContinue() {
        return System.nanoTime() < stopTimeNanos;
    }

}
