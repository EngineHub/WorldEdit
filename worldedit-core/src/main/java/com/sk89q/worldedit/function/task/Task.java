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

package com.sk89q.worldedit.function.task;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A task to be run by the task manager. Includes a future
 */
public class Task<T> {

    private final Deque<Operation> queue = new ArrayDeque<>();
    private Operation current;
    private Supplier<T> supplier;
    private Consumer<T> consumer;
    private Consumer<WorldEditException> exceptionConsumer;
    private Consumer<String> statusConsumer;

    private List<String> statusMessageList = new ArrayList<>();

    /**
     * Create a new queue with operations from the given collection.
     *
     * @param operations a collection of operations
     */
    public Task(Collection<Operation> operations) {
        checkNotNull(operations);
        for (Operation operation : operations) {
            queue.addLast(operation);
        }
    }

    /**
     * Create a new queue with operations from the given array.
     *
     * @param operation an array of operations
     */
    public Task(Operation... operation) {
        checkNotNull(operation);
        for (Operation o : operation) {
            queue.addLast(o);
        }
    }

    /**
     * Include a supplier for futures to retrieve information from.
     *
     * @param supplier The supplier
     * @return The current task, for chaining
     */
    public Task<T> withSupplier(Supplier<T> supplier) {
        checkNotNull(supplier);
        this.supplier = supplier;
        return this;
    }

    /**
     * Adds a consumer to receive information from the supplier.
     *
     * @param consumer The consumer
     * @return The current task, for chaining
     */
    public Task<T> withConsumer(Consumer<T> consumer) {
        checkNotNull(this.supplier, "A supplier must be provided before a consumer!");
        checkNotNull(consumer);
        this.consumer = consumer;
        return this;
    }

    public Task<T> onExcept(Consumer<WorldEditException> exceptionConsumer) {
        checkNotNull(exceptionConsumer);
        this.exceptionConsumer = exceptionConsumer;
        return this;
    }

    /**
     * Adds a consumer for status messages of tasks.
     *
     * @param statusConsumer The consumer
     * @return The current task, for chaining
     */
    public Task<T> withStatusConsumer(Consumer<String> statusConsumer) {
        checkNotNull(statusConsumer);
        this.statusConsumer = statusConsumer;
        return this;
    }

    /**
     * Adds exception and status consumers to this actor.
     *
     * @param actor The actor
     * @return The task, for chaining
     */
    public Task<T> addActorConsumers(Actor actor) {
        checkNotNull(actor);
        onExcept(e -> actor.printError(e.getMessage()));
        withStatusConsumer(actor::print);
        return this;
    }

    /**
     * Resume the task with a given context.
     *
     * @param run The context
     * @return If the task is completed.
     */
    public boolean resumeTask(RunContext run) {
        if (current == null && !queue.isEmpty()) {
            current = queue.poll();
        }

        if (current != null) {
            if (statusConsumer != null) {
                statusMessageList.clear();
                current.addStatusMessages(statusMessageList);
                for (String message : statusMessageList) {
                    statusConsumer.accept(message);
                }
            }

            try {
                current = current.resume(run);
            } catch (WorldEditException e) {
                if (exceptionConsumer != null) {
                    exceptionConsumer.accept(e);
                    return true;
                }
            }

            if (current == null) {
                current = queue.poll();
            }
        }

        if (current == null && consumer != null) {
            consumer.accept(supplier.get());
        }

        return current == null;
    }

    public void cancel() {
        for (Operation operation : queue) {
            operation.cancel();
        }
        queue.clear();
    }
}
