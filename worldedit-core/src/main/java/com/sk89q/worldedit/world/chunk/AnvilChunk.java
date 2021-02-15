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
import javax.annotation.Nullable;

public class AnvilChunk implements Chunk {

    private final CompoundBinaryTag rootTag;
    private final byte[][] blocks;
    private final byte[][] blocksAdd;
    private final byte[][] data;
    private final int rootX;
    private final int rootZ;

    private Map<BlockVector3, CompoundBinaryTag> tileEntities;


    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag the tag to read
     * @throws DataException on a data error
     * @deprecated Use {@link #AnvilChunk(CompoundBinaryTag)}
     */
    public AnvilChunk(CompoundTag tag) throws DataException {
        this(tag.asBinaryTag());
    }

    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag the tag to read
     * @throws DataException on a data error
     */
    public AnvilChunk(CompoundBinaryTag tag) throws DataException {
        rootTag = tag;

        rootX = NbtUtils.getChildTag(rootTag, "xPos", BinaryTagTypes.INT).value();
        rootZ = NbtUtils.getChildTag(rootTag, "zPos", BinaryTagTypes.INT).value();

        blocks = new byte[16][16 * 16 * 16];
        blocksAdd = new byte[16][16 * 16 * 8];
        data = new byte[16][16 * 16 * 8];

        ListBinaryTag sections = NbtUtils.getChildTag(rootTag, "Sections", BinaryTagTypes.LIST);

        for (BinaryTag rawSectionTag : sections) {
            if (!(rawSectionTag instanceof CompoundBinaryTag)) {
                continue;
            }

            CompoundBinaryTag sectionTag = (CompoundBinaryTag) rawSectionTag;
            if (sectionTag.get("Y") == null) {
                continue; // Empty section.
            }

            int y = NbtUtils.getChildTag(sectionTag, "Y", BinaryTagTypes.BYTE).value();
            if (y < 0 || y >= 16) {
                continue;
            }

            blocks[y] = NbtUtils.getChildTag(sectionTag,
                    "Blocks", BinaryTagTypes.BYTE_ARRAY).value();
            data[y] = NbtUtils.getChildTag(sectionTag, "Data",
                BinaryTagTypes.BYTE_ARRAY).value();

            // 4096 ID block support
            if (sectionTag.get("Add") != null) {
                blocksAdd[y] = NbtUtils.getChildTag(sectionTag,
                        "Add", BinaryTagTypes.BYTE_ARRAY).value();
            }
        }

        int sectionsize = 16 * 16 * 16;
        for (byte[] block : blocks) {
            if (block.length != sectionsize) {
                throw new InvalidFormatException(
                        "Chunk blocks byte array expected " + "to be "
                                + sectionsize + " bytes; found "
                                + block.length);
            }
        }

        for (byte[] aData : data) {
            if (aData.length != (sectionsize / 2)) {
                throw new InvalidFormatException("Chunk block data byte array "
                        + "expected to be " + sectionsize + " bytes; found "
                        + aData.length);
            }
        }
    }

    private int getBlockID(BlockVector3 position) throws DataException {
        int x = position.getX() - rootX * 16;
        int y = position.getY();
        int z = position.getZ() - rootZ * 16;

        int section = y >> 4;
        if (section < 0 || section >= blocks.length) {
            throw new DataException("Chunk does not contain position " + position);
        }

        int yindex = y & 0x0F;

        int index = x + (z * 16 + (yindex * 16 * 16));

        try {
            int addId = 0;

            // The block ID is the combination of the Blocks byte array with the
            // Add byte array. 'Blocks' stores the lowest 8 bits of a block's ID, and
            // 'Add' stores the highest 4 bits of the ID. The first block is stored
            // in the lowest nibble in the Add byte array.
            if (index % 2 == 0) {
                addId = (blocksAdd[section][index >> 1] & 0x0F) << 8;
            } else {
                addId = (blocksAdd[section][index >> 1] & 0xF0) << 4;
            }

            return (blocks[section][index] & 0xFF) + addId;
        } catch (IndexOutOfBoundsException e) {
            throw new DataException("Chunk does not contain position " + position);
        }
    }

    private int getBlockData(BlockVector3 position) throws DataException {
        int x = position.getX() - rootX * 16;
        int y = position.getY();
        int z = position.getZ() - rootZ * 16;

        int section = y >> 4;
        int yIndex = y & 0x0F;

        if (section < 0 || section >= blocks.length) {
            throw new DataException("Chunk does not contain position " + position);
        }

        int index = x + (z * 16 + (yIndex * 16 * 16));
        boolean shift = index % 2 == 0;
        index /= 2;

        try {
            if (!shift) {
                return (data[section][index] & 0xF0) >> 4;
            } else {
                return data[section][index] & 0xF;
            }
        } catch (IndexOutOfBoundsException e) {
            throw new DataException("Chunk does not contain position " + position);
        }
    }

    /**
     * Used to load the tile entities.
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
     * @return the compound tag for that position, which may be null
     * @throws DataException thrown if there is a data error
     */
    @Nullable
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
        int id = getBlockID(position);
        int data = getBlockData(position);

        BlockState state = LegacyMapper.getInstance().getBlockFromLegacy(id, data);
        if (state == null) {
            WorldEdit.logger.warn("Unknown legacy block " + id + ":" + data + " found when loading legacy anvil chunk.");
            return BlockTypes.AIR.getDefaultState().toBaseBlock();
        }
        CompoundBinaryTag tileEntity = getBlockTileEntity(position);

        if (tileEntity != null) {
            return state.toBaseBlock(tileEntity);
        }

        return state.toBaseBlock();
    }

}
