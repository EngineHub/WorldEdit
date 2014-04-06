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

import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.World;

import java.util.Random;

/**
 * A legacy abstract implementation of {@link World}. New implementations
 * should use {@link AbstractWorld} when possible.
 *
 * @deprecated Replace with {@link World} wherever appropriate
 */
@Deprecated
public abstract class LocalWorld extends AbstractWorld {

    /**
     * Named flags to use as parameters to {@link LocalWorld#killMobs(Vector, double, int)}
     */
    @SuppressWarnings("PointlessBitwiseExpression")
    public final class KillFlags {
        public static final int PETS = 1 << 0;
        public static final int NPCS = 1 << 1;
        public static final int ANIMALS = 1 << 2;
        public static final int GOLEMS = 1 << 3;
        public static final int AMBIENT = 1 << 4;
        public static final int FRIENDLY = PETS | NPCS | ANIMALS | GOLEMS | AMBIENT;
        public static final int WITH_LIGHTNING = 1 << 20;

        private KillFlags() {
        }
    }

    /**
     * @deprecated Don't use this anymore. It will be removed
     */
    @SuppressWarnings("ProtectedField")
    @Deprecated
    protected Random random = new Random();

    @Override
    public BaseBlock getBlock(Vector pt) {
        checkLoadedChunk(pt);

        @SuppressWarnings("deprecation") int type = getBlockType(pt);
        @SuppressWarnings("deprecation") int data = getBlockData(pt);

        switch (type) {
            case BlockID.WALL_SIGN:
            case BlockID.SIGN_POST: {
                SignBlock block = new SignBlock(type, data);
                copyFromWorld(pt, block);
                return block;
            }

            case BlockID.CHEST: {
                ChestBlock block = new ChestBlock(data);
                copyFromWorld(pt, block);
                return block;
            }

            case BlockID.FURNACE:
            case BlockID.BURNING_FURNACE: {
                FurnaceBlock block = new FurnaceBlock(type, data);
                copyFromWorld(pt, block);
                return block;
            }

            case BlockID.DISPENSER: {
                DispenserBlock block = new DispenserBlock(data);
                copyFromWorld(pt, block);
                return block;
            }

            case BlockID.MOB_SPAWNER: {
                MobSpawnerBlock block = new MobSpawnerBlock(data);
                copyFromWorld(pt, block);
                return block;
            }

            case BlockID.NOTE_BLOCK: {
                NoteBlock block = new NoteBlock(data);
                copyFromWorld(pt, block);
                return block;
            }

            case BlockID.HEAD: {
                SkullBlock block = new SkullBlock(data);
                copyFromWorld(pt, block);
                return block;
            }

            default:
                return new BaseBlock(type, data);
        }
    }

    /**
     * Given a block and a position, copy data from the world to the block
     * based on the type of block.
     * </p>
     * The provided {@link BaseBlock} should match that of the one in the
     * world.
     *
     * @param position the position
     * @param block the block
     * @return true if the copy operation succeeded, false otherwise
     */
    public abstract boolean copyFromWorld(Vector position, BaseBlock block);

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        return getBlock(position);
    }

    @Override
    public boolean setBlock(Vector pt, BaseBlock block, boolean notifyAdjacent) throws WorldEditException {
        boolean successful;

        // Default implementation will call the old deprecated methods
        if (notifyAdjacent) {
            successful = setTypeIdAndData(pt, block.getId(), block.getData());
        } else {
            successful = setTypeIdAndDataFast(pt, block.getId(), block.getData());
        }

        copyToWorld(pt, block);

        return successful;
    }

    /**
     * Given a block and a position, copy data to the world from the block
     * based on the type of block.
     * </p>
     * The provided {@link BaseBlock} should match that of the one in the
     * world.
     *
     * @param position the position
     * @param block the block
     * @return true if the copy operation succeeded, false otherwise
     */
    public abstract boolean copyToWorld(Vector position, BaseBlock block);

    @Override
    public boolean setBlock(Vector pt, BaseBlock block) throws WorldEditException {
        return setBlock(pt, block, true);
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
