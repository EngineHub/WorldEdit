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

package com.sk89q.worldedit.extent;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * An {@code Extent} that is currently also being simulated.
 *
 * <p>For example, a world being modified that is also running in a Minecraft
 * server (in the same process) is considered simulated. A copy of the same
 * world but loaded offline is not simulated.</p>
 */
public interface SimulatedExtent extends Extent {

    /**
     * Similar to {@link Extent#setBlock(Vector, BaseBlock)} but a
     * {@code notifyAndLight} parameter indicates whether adjacent blocks
     * should be notified that changes have been made and lighting operations
     * should be executed.
     *
     * <p>If it's not possible to skip lighting, or if it's not possible to
     * avoid notifying adjacent blocks, then attempt to meet the
     * specification as best as possible.</p>
     *
     * @param position position of the block
     * @param block block to set
     * @param notifyAndLight true to to notify and light
     * @return true if the block was successfully set (return value may not be accurate)
     */
    boolean setBlock(Vector position, BaseBlock block, boolean notifyAndLight) throws WorldEditException;

    /**
     * Notifies the simulation that the block at the given location has
     * been changed and it must be re-lighted (and issue other events).
     *
     * @param position position of the block
     * @param previousId the type ID of the previous block that was there
     * @return true if the block was successfully notified
     */
    boolean notifyAndLightBlock(Vector position, int previousId) throws WorldEditException;

}
