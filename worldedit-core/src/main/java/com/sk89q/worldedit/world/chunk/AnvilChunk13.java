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
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.nbt.BinaryTag;
import com.sk89q.worldedit.util.nbt.BinaryTagTypes;
import com.sk89q.worldedit.util.nbt.CompoundBinaryTag;
import com.sk89q.worldedit.util.nbt.IntBinaryTag;
import com.sk89q.worldedit.util.nbt.ListBinaryTag;
import com.sk89q.worldedit.util.nbt.NbtUtils;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.storage.InvalidFormatException;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * The chunk format for Minecraft 1.13 to 1.15
 */
public class AnvilChunk13 implements Chunk {

    private final CompoundBinaryTag rootTag;
    private final BlockState[][] blocks;
    private final int rootX;
    private final int rootZ;

    private Map<BlockVector3, CompoundBinaryTag> tileEntities;


    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag the tag to read
     * @throws DataException on a data error
     * @deprecated Use {@link #AnvilChunk13(CompoundBinaryTag)}
     */
    @Deprecated
    public AnvilChunk13(CompoundTag tag) throws DataException {
        this(tag.asBinaryTag());
    }

    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag the tag to read
     * @throws DataException on a data error
     */
    public AnvilChunk13(CompoundBinaryTag tag) throws DataException {
        rootTag = tag;

        rootX = NbtUtils.getChildTag(rootTag, "xPos", BinaryTagTypes.INT).value();
        rootZ = NbtUtils.getChildTag(rootTag, "zPos", BinaryTagTypes.INT).value();

        blocks = new BlockState[16][];

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

            // parse palette
            ListBinaryTag paletteEntries = sectionTag.getList("Palette", BinaryTagTypes.COMPOUND);
            int paletteSize = paletteEntries.size();
            if (paletteSize == 0) {
                continue;
            }
            BlockState[] palette = new BlockState[paletteSize];
            for (int paletteEntryId = 0; paletteEntryId < paletteSize; paletteEntryId++) {
                CompoundBinaryTag paletteEntry = (CompoundBinaryTag) paletteEntries.get(paletteEntryId);
                BlockType type = BlockTypes.get(paletteEntry.getString("Name"));
                if (type == null) {
                    throw new InvalidFormatException("Invalid block type: " + paletteEntry.getString("Name"));
                }
                BlockState blockState = type.getDefaultState();
                if (paletteEntry.get("Properties") != null) {
                    CompoundBinaryTag properties = NbtUtils.getChildTag(paletteEntry, "Properties", BinaryTagTypes.COMPOUND);
                    for (Property<?> property : blockState.getStates().keySet()) {
                        if (properties.get(property.getName()) != null) {
                            String value = properties.getString(property.getName());
                            try {
                                blockState = getBlockStateWith(blockState, property, value);
                            } catch (IllegalArgumentException e) {
                                throw new InvalidFormatException("Invalid block state for " + blockState.getBlockType().getId() + ", " + property.getName() + ": " + value);
                            }
                        }
                    }
                }
                palette[paletteEntryId] = blockState;
            }

            // parse block states
            long[] blockStatesSerialized = NbtUtils.getChildTag(sectionTag, "BlockStates", BinaryTagTypes.LONG_ARRAY).value();

            BlockState[] chunkSectionBlocks = new BlockState[16 * 16 * 16];
            blocks[y] = chunkSectionBlocks;

            readBlockStates(palette, blockStatesSerialized, chunkSectionBlocks);
        }
    }

    protected void readBlockStates(BlockState[] palette, long[] blockStatesSerialized, BlockState[] chunkSectionBlocks) throws InvalidFormatException {
        int paletteBits = 4;
        while ((1 << paletteBits) < palette.length) {
            ++paletteBits;
        }
        int paletteMask = (1 << paletteBits) - 1;

        long currentSerializedValue = 0;
        int nextSerializedItem = 0;
        int remainingBits = 0;
        for (int blockPos = 0; blockPos < chunkSectionBlocks.length; blockPos++) {
            int localBlockId;
            if (remainingBits < paletteBits) {
                int bitsNextLong = paletteBits - remainingBits;
                localBlockId = (int) currentSerializedValue;
                if (nextSerializedItem >= blockStatesSerialized.length) {
                    throw new InvalidFormatException("Too short block state table");
                }
                currentSerializedValue = blockStatesSerialized[nextSerializedItem++];
                localBlockId |= (currentSerializedValue & ((1 << bitsNextLong) - 1)) << remainingBits;
                currentSerializedValue >>>= bitsNextLong;
                remainingBits = 64 - bitsNextLong;
            } else {
                localBlockId = (int) (currentSerializedValue & paletteMask);
                currentSerializedValue >>>= paletteBits;
                remainingBits -= paletteBits;
            }
            if (localBlockId >= palette.length) {
                throw new InvalidFormatException("Invalid block state table entry: " + localBlockId);
            }
            chunkSectionBlocks[blockPos] = palette[localBlockId];
        }
    }

    private <T> BlockState getBlockStateWith(BlockState source, Property<T> property, String value) {
        return source.with(property, property.getValueFor(value));
    }

    /**
     * Used to load the tile entities.
     */
    private void populateTileEntities() throws DataException {
        tileEntities = new HashMap<>();
        if (rootTag.get("TileEntities") == null) {
            return;
        }
        ListBinaryTag tags = NbtUtils.getChildTag(rootTag, "TileEntities", BinaryTagTypes.LIST);

        for (BinaryTag tag : tags) {
            if (!(tag instanceof CompoundBinaryTag)) {
                throw new InvalidFormatException("CompoundTag expected in TileEntities");
            }

            CompoundBinaryTag t = (CompoundBinaryTag) tag;

            int x = ((IntBinaryTag) t.get("x")).value();
            int y = ((IntBinaryTag) t.get("y")).value();
            int z = ((IntBinaryTag) t.get("z")).value();

            BlockVector3 vec = BlockVector3.at(x, y, z);
            tileEntities.put(vec, t);
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
        int x = position.getX() - rootX * 16;
        int y = position.getY();
        int z = position.getZ() - rootZ * 16;

        int section = y >> 4;
        int yIndex = y & 0x0F;

        if (section < 0 || section >= blocks.length) {
            throw new DataException("Chunk does not contain position " + position);
        }

        BlockState[] sectionBlocks = blocks[section];
        BlockState state = sectionBlocks != null ? sectionBlocks[(yIndex << 8) | (z << 4) | x] : BlockTypes.AIR.getDefaultState();

        CompoundBinaryTag tileEntity = getBlockTileEntity(position);

        if (tileEntity != null) {
            return state.toBaseBlock(tileEntity);
        }

        return state.toBaseBlock();
    }

}
