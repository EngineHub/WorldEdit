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

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OperationFuture implements Future<Operation> {
    private static final int NANOS_MILLIS_FACTOR = 1000000;

    private final Object LOCK = new Object();
    private final Operation originalOperation;
    private Operation operation;
    private Throwable thrown;
    private boolean started = false;
    private boolean done = false;
    private boolean cancelled = false;

    public OperationFuture(Operation op) {
        this.operation = this.originalOperation = op;
    }

    /**
     * Get the original Operation object this OperationFuture was started with.
     *
     * @return original Operation object
     */
    public Operation getOriginalOperation() {
        return originalOperation;
    }

    /**
     * Check if the Operation completed successfully, with no errors, and was
     * not cancelled.
     * <p/>
     * If this method returns true, {@link #get()} will return the final
     * Operation, without blocking and without throwing.
     *
     * @return true if complete and was successful
     */
    public boolean isSuccess() {
        return done && !cancelled && thrown == null;
    }

    /**
     * Access the error thrown by the Operation, if present.
     * <p/>
     * This method is an exception-free alternative to catching the
     * ExecutionException from calling {@link #get()}.
     *
     * @return error, if complete and error was thrown (null otherwise)
     */
    public Throwable getThrown() {
        return thrown;
    }

    @Override
    public boolean cancel(boolean mayInterrupt) {
        synchronized (LOCK) {
            if (started && !mayInterrupt) {
                return false;
            }
            if (isDone()) {
                return false;
            }
            if (!Operations.SlowCompletionWorker.cancel(this)) {
                return false;
            }

            cancelled = true;
            LOCK.notifyAll();
            return true;
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Returns true if this task completed. Completion may be due to normal
     * termination, an exception, or cancellation -- in all of these cases,
     * this method will return true.
     * <p/>
     * See the documentation for {@link #get()} for details about how these
     * methods are related.
     *
     * @see #get()
     * @return true if this task completed
     */
    @Override
    public boolean isDone() {
        return done || cancelled || thrown != null;
    }

    /**
     * Wait for this OperationFuture to complete, and get the result.
     * <p/>
     * In addition to the normal semantics of {@link Future#get()}, this method
     * will throw an IllegalThreadStateException if:
     *  <ul><li> The OperationFuture is not done - isDone() returns false; and
     * </li><li> The get() call is made on the main Minecraft Server thread.
     * </li></ul>
     * <p/>
     * The purpose of this is twofold: firstly, to prevent accidentally creating
     * a single-threaded deadlock - the Operation is eventually run on the main
     * server thread, so blocking the main server thread would mean that it
     * could never execute; but at the same time allow for some main-code thread
     * to get the results of an OperationFuture.
     * <p/>
     * Here's an example of how to do this:
     * <pre>
     *  final com.sk89q.worldedit.entity.Player player = ....;
     *  final OperationFuture future = Operations.completeSlowly(new RegionVisitor(region, func));
     *  Bukkit.getScheduler().runTaskTimer(plugin, new BukkitRunnable() {
     *    public void run() {
     *      if (future.isDone()) {
     *        if (future.isSuccess()) {
     *          RegionVisitor result = (RegionVisitor) future.get();
     *          player.print(result.getAffected() + " blocks changed.");
     *        } else if (future.isCancelled()) {
     *          player.print("Command cancelled!");
     *        } else {
     *          player.print("Command failed! " + future.getThrown().getClass().getName());
     *          future.getThrown().printStackTrace();
     *        }
     *        this.cancel();
     *        return;
     *      }
     *    }
     *  }, 2, 2);
     * </pre>
     *
     * @return the final Operation in the chain
     * @throws IllegalThreadStateException - if would call wait() on main server
     *                                     thread
     * @throws CancellationException       - if the computation was cancelled
     * @throws ExecutionException          - if the computation threw an
     *                                     exception
     * @throws InterruptedException        - if the current thread was
     *                                     interrupted while waiting
     */
    @Override
    public Operation get() throws InterruptedException, ExecutionException {
        // Try the fast-track
        // This bypasses the main-thread check (see javadoc).
        synchronized (LOCK) {
            if (isDone()) {
                return getFinalResult();
            }
        }

        if (Operations.getSchedulingPlatform().isPrimaryThread()) {
            throw new IllegalThreadStateException("May not call get() on a non-done OperationFuture on the main thread");
        }

        // Wait loop
        synchronized (LOCK) {
            while (!isDone()) {
                LOCK.wait();
            }
            return getFinalResult();
        }
    }

    /**
     * See the documentation for {@link #get()}.
     *
     * @param duration How long to wait, using the TimeUnit API
     * @param timeUnit Unit of time that the duration is in
     * @return the final Operation in the chain
     * @throws IllegalStateException if would call wait() on main server thread
     * @throws CancellationException if the computation was cancelled
     * @throws ExecutionException    if the computation threw an exception
     * @throws InterruptedException  if the current thread was interrupted while waiting
     * @throws TimeoutException      if the wait timed out
     * @see #get()
     */
    @Override
    public Operation get(long duration, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        // Try the fast-track
        // This bypasses the main-thread check (see javadoc).
        synchronized (LOCK) {
            if (isDone()) {
                return getFinalResult();
            }
        }

        if (Operations.getSchedulingPlatform().isPrimaryThread()) {
            throw new IllegalStateException("May not call get() on a non-done OperationFuture on the main thread");
        }

        // Do wait-loop
        long finishTime = System.nanoTime() + timeUnit.toNanos(duration);
        synchronized (LOCK) {
            while (!isDone()) {
                long waitTime = finishTime - System.nanoTime();
                if (waitTime <= 0) {
                    throw new TimeoutException("Timed out waiting for the OperationFuture to complete - waited for " + duration + " " + timeUnit.toString());
                } else {
                    LOCK.wait(waitTime / NANOS_MILLIS_FACTOR, (int) (waitTime % NANOS_MILLIS_FACTOR));
                }
            }
            return getFinalResult();
        }
    }

    // Internal-use methods

    /**
     * Get the result of this OperationFuture, whether it is an object or
     * exception. If the result is an exception, it is thrown.
     * <p/>
     * Internal use only.
     *
     * @return result - final Operation if successful
     * @throws ExecutionException if the Operation failed
     * @throws CancellationException if the Future was cancelled
     */
    private Operation getFinalResult() throws ExecutionException {
        if (thrown != null) {
            throw new ExecutionException("Error while executing WorldEdit operation " + operation.getClass().getSimpleName(), thrown);
        } else if (cancelled) {
            throw new CancellationException("WorldEdit operation " + operation.getClass().getSimpleName() + " was cancelled.");
        } else if (done) {
            return operation;
        } else {
            throw new AssertionError("Inconsistent code: a getFinalResult() call was made on an OperationFuture that was not complete");
        }
    }

    protected Operation getOperation() {
        return operation;
    }

    protected void replaceOperation(Operation replacement) {
        if (replacement != null) {
            operation = replacement;
        }
    }

    protected void setStarted() {
        synchronized (LOCK) {
            started = true;
            LOCK.notifyAll();
        }
    }

    protected void complete() {
        synchronized (LOCK) {
            done = true;
            LOCK.notifyAll();
        }
    }

    protected void throwing(Throwable thrown) {
        synchronized (LOCK) {
            this.thrown = thrown;
            LOCK.notifyAll();
        }
    }
}
