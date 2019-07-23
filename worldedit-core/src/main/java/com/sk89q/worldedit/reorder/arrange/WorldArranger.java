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

package com.sk89q.worldedit.reorder.arrange;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.world.World;

/**
 * Applies the changes to a world.
 */
public class WorldArranger implements Arranger {

    private final World world;

    public WorldArranger(World world) {
        this.world = world;
    }

    @Override
    public void rearrange(ArrangerContext context) {
        for (int i = 0; i < context.getActionCount(); i++) {
            try {
                context.getAction(i).apply(world);
            } catch (WorldEditException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
