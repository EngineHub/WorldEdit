/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


package com.sk89q.worldedit.function.operation;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.util.formatting.text.Component;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationsTest extends BaseWorldEditTest {

    /**
     * An operation that needs several calls to {@code resume} before it completes.
     */
    private static final class Steps implements Operation {
        private final String name;
        private final int total;
        private final List<String> log;
        private int done;
        private boolean cancelled;

        private Steps(String name, int total, List<String> log) {
            this.name = name;
            this.total = total;
            this.log = log;
        }

        @Override
        public Operation resume(RunContext run) {
            done++;
            log.add(name + done);
            return done >= total ? null : this;
        }

        @Override
        public void cancel() {
            cancelled = true;
        }

        @Override
        public Iterable<Component> getStatusMessages() {
            return ImmutableList.of();
        }
    }

    private static Operation failingWith(WorldEditException exception) {
        return new Operation() {
            @Override
            public Operation resume(RunContext run) throws WorldEditException {
                throw exception;
            }

            @Override
            public void cancel() {
            }

            @Override
            public Iterable<Component> getStatusMessages() {
                return ImmutableList.of();
            }
        };
    }

    @Test
    void queueRunsOperationsInOrderToCompletion() throws WorldEditException {
        List<String> log = new ArrayList<>();
        OperationQueue queue = new OperationQueue(new Steps("a", 2, log), new Steps("b", 1, log), new Steps("c", 3, log));

        Operations.complete(queue);

        assertEquals(List.of("a1", "a2", "b1", "c1", "c2", "c3"), log);
    }

    @Test
    void queueCanBeResumedAfterAPartialRun() throws WorldEditException {
        List<String> log = new ArrayList<>();
        OperationQueue queue = new OperationQueue(new Steps("a", 3, log), new Steps("b", 2, log));

        Operation next = queue.resume(new RunContext());
        assertSame(queue, next, "the queue has work left");
        assertEquals(List.of("a1"), log);

        // Hand the returned operation back, like the platform does between ticks
        next = next.resume(new RunContext());
        assertEquals(List.of("a1", "a2"), log);
        Operations.complete(next);
        assertEquals(List.of("a1", "a2", "a3", "b1", "b2"), log);
    }

    @Test
    void queueFinishesWithNullAndEmptyQueueFinishesImmediately() throws WorldEditException {
        assertNull(new OperationQueue().resume(new RunContext()));
        assertNull(new OperationQueue(List.of()).resume(new RunContext()));

        List<String> log = new ArrayList<>();
        OperationQueue queue = new OperationQueue(new Steps("a", 1, log));
        assertNull(queue.resume(new RunContext()));
        assertNull(queue.resume(new RunContext()), "resuming a finished queue does nothing");
        assertEquals(List.of("a1"), log);
    }

    @Test
    void offeredOperationsRunAfterExistingOnes() throws WorldEditException {
        List<String> log = new ArrayList<>();
        OperationQueue queue = new OperationQueue(new Steps("a", 2, log));
        queue.resume(new RunContext());
        queue.offer(new Steps("b", 1, log));

        Operations.complete(queue);

        assertEquals(List.of("a1", "a2", "b1"), log);
    }

    @Test
    void cancelStopsOperationsThatHaveNotStarted() throws WorldEditException {
        List<String> log = new ArrayList<>();
        Steps first = new Steps("a", 2, log);
        Steps second = new Steps("b", 2, log);
        OperationQueue queue = new OperationQueue(first, second);
        queue.resume(new RunContext());

        queue.cancel();

        assertTrue(second.cancelled);
        assertEquals(List.of("a1"), log);
        Operations.complete(queue);
        assertEquals(List.of("a1", "a2"), log, "queued operations were dropped");
    }

    @Test
    void completeRethrowsWorldEditExceptions() {
        MaxChangedBlocksException exception = new MaxChangedBlocksException(7);
        assertSame(exception, assertThrows(MaxChangedBlocksException.class,
            () -> Operations.complete(failingWith(exception))));
    }

    @Test
    void completeLegacyOnlyRethrowsMaxChangedBlocks() {
        MaxChangedBlocksException limit = new MaxChangedBlocksException(7);
        assertSame(limit, assertThrows(MaxChangedBlocksException.class,
            () -> Operations.completeLegacy(failingWith(limit))));

        WorldEditException other = new WorldEditException() {
        };
        RuntimeException wrapped = assertThrows(RuntimeException.class,
            () -> Operations.completeLegacy(failingWith(other)));
        assertSame(other, wrapped.getCause());
    }

    @Test
    void completeBlindlyWrapsEveryWorldEditException() {
        MaxChangedBlocksException limit = new MaxChangedBlocksException(7);
        RuntimeException wrapped = assertThrows(RuntimeException.class,
            () -> Operations.completeBlindly(failingWith(limit)));
        assertSame(limit, wrapped.getCause());
    }

    @Test
    void completeAcceptsNullAndRunsEveryVariantToTheEnd() throws WorldEditException {
        Operations.complete(null);
        Operations.completeLegacy(null);
        Operations.completeBlindly(null);

        List<String> log = new ArrayList<>();
        Operations.completeLegacy(new Steps("a", 2, log));
        Operations.completeBlindly(new Steps("b", 2, log));
        assertEquals(List.of("a1", "a2", "b1", "b2"), log);
    }
}
