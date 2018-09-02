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

package com.sk89q.worldedit.world.chunk;

import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongArrayTag;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.storage.InvalidFormatException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * The chunk format for Minecraft 1.13 and newer
 */
public class AnvilChunk13 implements Chunk {

    private CompoundTag rootTag;
    private BlockState[][] blocks;
    private int rootX;
    private int rootZ;

    private Map<BlockVector, Map<String,Tag>> tileEntities;

    /**
     * Construct the chunk with a compound tag.
     * 
     * @param tag the tag to read
     * @throws DataException on a data error
     */
    public AnvilChunk13(CompoundTag tag) throws DataException {
        rootTag = tag;

        rootX = NBTUtils.getChildTag(rootTag.getValue(), "xPos", IntTag.class).getValue();
        rootZ = NBTUtils.getChildTag(rootTag.getValue(), "zPos", IntTag.class).getValue();

        blocks = new BlockState[16][];

        List<Tag> sections = NBTUtils.getChildTag(rootTag.getValue(), "Sections", ListTag.class).getValue();

        for (Tag rawSectionTag : sections) {
            if (!(rawSectionTag instanceof CompoundTag)) {
                continue;
            }

            CompoundTag sectionTag = (CompoundTag) rawSectionTag;
            if (!sectionTag.getValue().containsKey("Y")) {
                continue; // Empty section.
            }

            int y = NBTUtils.getChildTag(sectionTag.getValue(), "Y", ByteTag.class).getValue();
            if (y < 0 || y >= 16) {
                continue;
            }

            int blocksPerChunkSection = 16 * 16 * 16;
            BlockState[] chunkSectionBlocks = new BlockState[blocksPerChunkSection];
            blocks[y] = chunkSectionBlocks;

            // parse palette
            List<CompoundTag> paletteEntries = sectionTag.getList("Palette", CompoundTag.class);
            int paletteSize = paletteEntries.size();
            BlockState[] palette = new BlockState[paletteSize];
            for (int paletteEntryId = 0; paletteEntryId < paletteSize; paletteEntryId++) {
                CompoundTag paletteEntry = paletteEntries.get(paletteEntryId);
                BlockType type = BlockTypes.get(paletteEntry.getString("Name"));
                if(type == null) {
                    throw new InvalidFormatException("Invalid block type: " + paletteEntry.getString("Name"));
                }
                BlockState blockState = type.getDefaultState();
                if (paletteEntry.containsKey("Properties")) {
                    CompoundTag properties = NBTUtils.getChildTag(paletteEntry.getValue(), "Properties", CompoundTag.class);
                    for (Property<?> property : blockState.getStates().keySet()) {
                        if (properties.containsKey(property.getName())) {
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
            int paletteBits = 4;
            while ((1 << paletteBits) < paletteSize) {
                ++paletteBits;
            }
            int paletteMask = (1 << paletteBits) - 1;

            // parse block states
            long[] blockStatesSerialized = NBTUtils.getChildTag(sectionTag.getValue(), "BlockStates", LongArrayTag.class).getValue();
            long currentSerializedValue = 0;
            int nextSerializedItem = 0;
            int remainingBits = 0;
            for (int blockPos = 0; blockPos < blocksPerChunkSection; blockPos++) {
                int localBlockId = 0;
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
    }

    private <T> BlockState getBlockStateWith(BlockState source, Property<T> property, String value) {
        return source.with(property, property.getValueFor(value));
    }

    /**
     * Used to load the tile entities.
     *
     * @throws DataException
     */
    private void populateTileEntities() throws DataException {
        List<Tag> tags = NBTUtils.getChildTag(rootTag.getValue(),
                "TileEntities", ListTag.class).getValue();

        tileEntities = new HashMap<>();

        for (Tag tag : tags) {
            if (!(tag instanceof CompoundTag)) {
                throw new InvalidFormatException("CompoundTag expected in TileEntities");
            }

            CompoundTag t = (CompoundTag) tag;

            int x = 0;
            int y = 0;
            int z = 0;

            Map<String, Tag> values = new HashMap<>();

            for (Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
                switch (entry.getKey()) {
                    case "x":
                        if (entry.getValue() instanceof IntTag) {
                            x = ((IntTag) entry.getValue()).getValue();
                        }
                        break;
                    case "y":
                        if (entry.getValue() instanceof IntTag) {
                            y = ((IntTag) entry.getValue()).getValue();
                        }
                        break;
                    case "z":
                        if (entry.getValue() instanceof IntTag) {
                            z = ((IntTag) entry.getValue()).getValue();
                        }
                        break;
                }

                values.put(entry.getKey(), entry.getValue());
            }

            BlockVector vec = new BlockVector(x, y, z);
            tileEntities.put(vec, values);
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
    private CompoundTag getBlockTileEntity(Vector position) throws DataException {
        if (tileEntities == null) {
            populateTileEntities();
        }

        Map<String, Tag> values = tileEntities.get(new BlockVector(position));
        if (values == null) {
            return null;
        }

        return new CompoundTag(values);
    }

    @Override
    public BaseBlock getBlock(Vector position) throws DataException {
        int x = position.getBlockX() - rootX * 16;
        int y = position.getBlockY();
        int z = position.getBlockZ() - rootZ * 16;

        int section = y >> 4;
        int yIndex = y & 0x0F;

        if (section < 0 || section >= blocks.length) {
            throw new DataException("Chunk does not contain position " + position);
        }

        BlockState[] sectionBlocks = blocks[section];
        BlockState state = sectionBlocks != null ? sectionBlocks[(yIndex << 8) | (z << 4) | x] : BlockTypes.AIR.getDefaultState();

        CompoundTag tileEntity = getBlockTileEntity(position);

        return state.toBaseBlock(tileEntity);
    }

}
