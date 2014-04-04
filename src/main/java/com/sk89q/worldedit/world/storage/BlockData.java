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

package com.sk89q.worldedit.world.storage;

import com.sk89q.worldedit.CuboidClipboard.FlipDirection;

/**
 * Block data related classes.
 *
 * @deprecated use {@link com.sk89q.worldedit.blocks.BlockData}
 * @author sk89q
 */
@Deprecated
public final class BlockData {
    private BlockData() {
    }

    /**
     * Rotate a block's data value 90 degrees (north->east->south->west->north);
     * 
     * @param type
     * @param data
     * @return
     * @deprecated use {@link com.sk89q.worldedit.blocks.BlockData#rotate90(int, int)}
     */
    @Deprecated
    public static int rotate90(int type, int data) {
        return com.sk89q.worldedit.blocks.BlockData.rotate90(type, data);
    }

    /**
     * Rotate a block's data value -90 degrees (north<-east<-south<-west<-north);
     * 
     * @param type
     * @param data
     * @return
     * @deprecated use {@link com.sk89q.worldedit.blocks.BlockData#rotate90Reverse(int, int)}
     */
    @Deprecated
    public static int rotate90Reverse(int type, int data) {
        return com.sk89q.worldedit.blocks.BlockData.rotate90Reverse(type, data);
    }

    /**
     * Flip a block's data value.
     * 
     * @param type
     * @param data
     * @return
     * @deprecated use return {@link com.sk89q.worldedit.blocks.BlockData#flip(int, int)}
     */
    @Deprecated
    public static int flip(int type, int data) {
        return rotate90(type, rotate90(type, data));
    }

    /**
     * Flip a block's data value.
     * 
     * @param type
     * @param data
     * @param direction
     * @return
     * @deprecated use {@link com.sk89q.worldedit.blocks.BlockData#flip(int, int, FlipDirection)}
     */
    @Deprecated
    public static int flip(int type, int data, FlipDirection direction) {
        return com.sk89q.worldedit.blocks.BlockData.flip(type, data, direction);
    }

    /**
     * Cycle a block's data value. This usually goes through some rotational pattern
     * depending on the block. If it returns -1, it means the id and data specified
     * do not have anything to cycle to.
     *
     * @param type block id to be cycled
     * @param data block data value that it starts at
     * @param increment whether to go forward (1) or backward (-1) in the cycle
     * @return the new data value for the block
     * @deprecated use {@link com.sk89q.worldedit.blocks.BlockData#cycle(int, int, int)}
     */
    @Deprecated
    public static int cycle(int type, int data, int increment) {
        return com.sk89q.worldedit.blocks.BlockData.cycle(type, data, increment);
    }

    /**
     * Returns the data value for the next color of cloth in the rainbow. This
     * should not be used if you want to just increment the data value.
     * @param data
     * @return
     * @deprecated use {@link com.sk89q.worldedit.blocks.BlockData#nextClothColor(int)}
     */
    @Deprecated
    public static int nextClothColor(int data) {
        return com.sk89q.worldedit.blocks.BlockData.nextClothColor(data);
    }

    /**
     * Returns the data value for the previous ext color of cloth in the rainbow.
     * This should not be used if you want to just increment the data value.
     * @param data
     * @return
     * @deprecated use {@link com.sk89q.worldedit.blocks.BlockData#prevClothColor(int)}
     */
    @Deprecated
    public static int prevClothColor(int data) {
        return com.sk89q.worldedit.blocks.BlockData.prevClothColor(data);
    }
}
