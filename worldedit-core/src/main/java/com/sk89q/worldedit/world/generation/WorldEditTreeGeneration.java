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

package com.sk89q.worldedit.world.generation;

import com.google.common.collect.Iterables;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nullable;

public final class WorldEditTreeGeneration {

    private WorldEditTreeGeneration() {
    }

    @SuppressWarnings("deprecation")
    @Nullable
    public static Boolean handleWorldEditTrees(World world, TreeType type, EditSession editSession, BlockVector3 position)
            throws MaxChangedBlocksException {
        if (type == WorldEditTreeTypes.RANDOM) {
            Collection<TreeType> treeTypes = TreeType.REGISTRY.values();
            TreeType randomType = Iterables.get(treeTypes, ThreadLocalRandom.current().nextInt(treeTypes.size()));
            return world.generateTree(randomType, editSession, position);
        }

        if (type == WorldEditTreeTypes.PINE) {
            // TODO Move this into this file once the legacy system is stripped away
            return com.sk89q.worldedit.util.TreeGenerator.TreeType.PINE.generate(editSession, position);
        }

        return null;
    }

}
