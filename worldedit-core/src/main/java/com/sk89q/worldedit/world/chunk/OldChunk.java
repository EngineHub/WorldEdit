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

package com.sk89q.worldedit.world.chunk;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.nbt.BinaryTag;
import com.sk89q.worldedit.util.nbt.BinaryTagTypes;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.util.nbt.IntBinaryTag;
import com.sk89q.worldedit.util.nbt.ListBinaryTag;
import com.sk89q.worldedit.util.nbt.NbtUtils;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import com.sk89q.worldedit.world.storage.InvalidFormatException;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an Alpha chunk.
 */
public class OldChunk implements Chunk {

    private final CompoundBinaryTag rootTag;
    private final byte[] blocks;
    private final byte[] data;
    private final int rootX;
    private final int rootZ;

    private Map<BlockVector3, CompoundBinaryTag> tileEntities;


    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag the tag
     * @throws DataException if there is an error getting the chunk data
     * @deprecated Use {@link #OldChunk(CompoundBinaryTag)}
     */
    @Deprecated
    public OldChunk(CompoundTag tag) throws DataException {
        this(tag.asBinaryTag());
    }

    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag the tag
     * @throws DataException if there is an error getting the chunk data
     */
    public OldChunk(CompoundBinaryTag tag) throws DataException {
        rootTag = tag;

        blocks = NbtUtils.getChildTag(rootTag, "Blocks", BinaryTagTypes.BYTE_ARRAY).value();
        data = NbtUtils.getChildTag(rootTag, "Data", BinaryTagTypes.BYTE_ARRAY).value();
        rootX = NbtUtils.getChildTag(rootTag, "xPos", BinaryTagTypes.INT).value();
        rootZ = NbtUtils.getChildTag(rootTag, "zPos", BinaryTagTypes.INT).value();

        int size = 16 * 16 * 128;
        if (blocks.length != size) {
            throw new InvalidFormatException("Chunk blocks byte array expected "
                    + "to be " + size + " bytes; found " + blocks.length);
        }

        if (data.length != (size / 2)) {
            throw new InvalidFormatException("Chunk block data byte array "
                    + "expected to be " + size + " bytes; found " + data.length);
        }
    }

    /**
     * Used to load the tile entities.
     *
     * @throws DataException if there is an error getting the chunk data
     */
    private void populateTileEntities() throws DataException {
        ListBinaryTag tags = NbtUtils.getChildTag(rootTag, "TileEntities", BinaryTagTypes.LIST);

        tileEntities = new HashMap<>();

        for (BinaryTag tag : tags) {
            if (!(tag instanceof CompoundBinaryTag)) {
                throw new InvalidFormatException("CompoundTag expected in TileEntities");
            }

            CompoundBinaryTag t = (CompoundBinaryTag) tag;

            int x = 0;
            int y = 0;
            int z = 0;

            CompoundBinaryTag.Builder values = CompoundBinaryTag.builder();

            for (String key : t.keySet()) {
                BinaryTag value = t.get(key);
                switch (key) {
                    case "x":
                        if (value instanceof IntBinaryTag) {
                            x = ((IntBinaryTag) value).value();
                        }
                        break;
                    case "y":
                        if (value instanceof IntBinaryTag) {
                            y = ((IntBinaryTag) value).value();
                        }
                        break;
                    case "z":
                        if (value instanceof IntBinaryTag) {
                            z = ((IntBinaryTag) value).value();
                        }
                        break;
                    default:
                        break;
                }

                values.put(key, value);
            }

            BlockVector3 vec = BlockVector3.at(x, y, z);
            tileEntities.put(vec, values.build());
        }
    }

    /**
     * Get the map of tags keyed to strings for a block's tile entity data. May
     * return null if there is no tile entity data. Not public yet because
     * what this function returns isn't ideal for usage.
     *
     * @param position the position
     * @return a tag
     * @throws DataException if there is an error getting the chunk data
     */
    private CompoundBinaryTag getBlockTileEntity(BlockVector3 position) throws DataException {
        if (tileEntities == null) {
            populateTileEntities();
        }

        CompoundBinaryTag values = tileEntities.get(position);
        if (values == null) {
            return null;
        }
        return values;
    }

    @Override
    public BaseBlock getBlock(BlockVector3 position) throws DataException {
        if (position.getY() >= 128) {
            return BlockTypes.VOID_AIR.getDefaultState().toBaseBlock();
        }
        int id;
        int dataVal;

        int x = position.getX() - rootX * 16;
        int y = position.getY();
        int z = position.getZ() - rootZ * 16;
        int index = y + (z * 128 + (x * 128 * 16));
        try {
            id = blocks[index];
        } catch (IndexOutOfBoundsException e) {
            throw new DataException("Chunk does not contain position " + position);
        }

        boolean shift = index % 2 == 0;
        index /= 2;

        try {
            if (!shift) {
                dataVal = (data[index] & 0xF0) >> 4;
            } else {
                dataVal = data[index] & 0xF;
            }
        } catch (IndexOutOfBoundsException e) {
            throw new DataException("Chunk does not contain position " + position);
        }

        BlockState state = LegacyMapper.getInstance().getBlockFromLegacy(id, dataVal);
        if (state == null) {
            WorldEdit.logger.warn("Unknown legacy block " + id + ":" + dataVal + " found when loading legacy anvil chunk.");
            return BlockTypes.AIR.getDefaultState().toBaseBlock();
        }

        CompoundBinaryTag tileEntity = getBlockTileEntity(position);

        if (tileEntity != null) {
            return state.toBaseBlock(tileEntity);
        }

        return state.toBaseBlock();
    }

}
