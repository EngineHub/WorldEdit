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

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.World;

/**
 * A legacy abstract implementation of {@link World}. New implementations
 * should use {@link AbstractWorld} when possible.
 *
 * @deprecated Replace with {@link World} wherever appropriate
 */
@Deprecated
public abstract class LocalWorld extends AbstractWorld {

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        return getBlock(position);
    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        switch (type) {
            case BIG_TREE:
                return generateBigTree(editSession, pt);
            case BIRCH:
                return generateBirchTree(editSession, pt);
            case REDWOOD:
                return generateRedwoodTree(editSession, pt);
            case TALL_REDWOOD:
                return generateTallRedwoodTree(editSession, pt);
            default:
            case TREE:
                return generateTree(editSession, pt);
        }
    }

    @Override
    public boolean generateTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        return false;
    }

    @Override
    public boolean generateBigTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        return false;
    }

    @Override
    public boolean generateBirchTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        return false;
    }

    @Override
    public boolean generateRedwoodTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        return false;
    }

    @Override
    public boolean generateTallRedwoodTree(EditSession editSession, Vector pt) throws MaxChangedBlocksException {
        return false;
    }

}
