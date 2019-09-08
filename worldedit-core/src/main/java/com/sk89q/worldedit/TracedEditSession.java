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

package com.sk89q.worldedit;

import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.World;

public class TracedEditSession extends EditSession {

    TracedEditSession(EventBus eventBus, World world, int maxBlocks, BlockBag blockBag, EditSessionEvent event) {
        super(eventBus, world, maxBlocks, blockBag, event);
    }

    private final Throwable stacktrace = new Throwable("Creation trace.");

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        if (commitRequired()) {
            WorldEdit.logger.warn("####### LEFTOVER BUFFER BLOCKS DETECTED #######");
            WorldEdit.logger.warn("This means that some code did not flush their EditSession.");
            WorldEdit.logger.warn("Here is a stacktrace from the creation of this EditSession:", stacktrace);
        }
    }
}
