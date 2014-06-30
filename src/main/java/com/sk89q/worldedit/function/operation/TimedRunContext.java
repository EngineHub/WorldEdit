package com.sk89q.worldedit.function.operation;

import java.util.concurrent.TimeUnit;

/**
 * Allows an Operation to run until a time limit is reached, as measured by
 * {@link System#nanoTime()}.
 */
public class TimedRunContext implements RunContext {
    private final long stopTimeNanos;

    /**
     * Create a TimedRunContext with a known stop time, as measured by System.nanoTime().
     *
     * @param stopTimeNanos nanoTime at which to stop the operation
     */
    public TimedRunContext(long stopTimeNanos) {
        this.stopTimeNanos = stopTimeNanos;
    }

    /**
     * Create a TimedRunContext stopping some time from now, using the Java
     * TimeUnit API.
     *
     * @param sourceDuration duration from now, in source units
     * @param sourceUnit the source unit
     */
    public TimedRunContext(long sourceDuration, TimeUnit sourceUnit) {
        stopTimeNanos = System.nanoTime() + sourceUnit.toNanos(sourceDuration);
    }

    @Override
    public boolean shouldContinue() {
        return System.nanoTime() < stopTimeNanos;
    }
}
