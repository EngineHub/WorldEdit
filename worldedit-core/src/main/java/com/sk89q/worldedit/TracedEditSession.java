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

package com.sk89q.worldedit;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.util.ErrorReporting;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.World;

import java.lang.ref.Cleaner;
import javax.annotation.Nullable;

/**
 * Internal use only.
 */
class TracedEditSession extends EditSession {

    private static final Cleaner cleaner = Cleaner.create();

    private static final class TraceRecord implements Runnable {
        private final Throwable stacktrace = new Throwable("An EditSession was not closed.");
        private final Actor actor;

        private volatile boolean committed = false;

        private TraceRecord(Actor actor) {
            this.actor = actor;
        }

        @Override
        public void run() {
            if (!committed) {
                WorldEdit.logger.warn("####### EDIT SESSION NOT CLOSED #######");
                WorldEdit.logger.warn("This means that some code did not close their EditSession.");
                WorldEdit.logger.warn("Here is a stacktrace from the creation of this EditSession:", stacktrace);
                ErrorReporting.trigger(actor, stacktrace);
            }
        }
    }

    private final TraceRecord record;
    private final Cleaner.Cleanable cleanable;

    TracedEditSession(EventBus eventBus, @Nullable World world, int maxBlocks, @Nullable BlockBag blockBag,
                      @Nullable Actor actor,
                      boolean tracing) {
        super(eventBus, world, maxBlocks, blockBag, actor, tracing);
        this.record = new TraceRecord(actor);
        this.cleanable = cleaner.register(this, record);
    }

    @Override
    public void close() {
        try {
            super.close();
        } finally {
            this.record.committed = true;
            cleanable.clean();
        }
    }
}
