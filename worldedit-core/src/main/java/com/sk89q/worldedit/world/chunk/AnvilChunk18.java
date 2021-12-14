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
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongArrayTag;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.storage.InvalidFormatException;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * The chunk format for Minecraft 1.18 and newer
 */
public class AnvilChunk18 implements Chunk {

    private final CompoundTag rootTag;
    private final Int2ObjectOpenHashMap<BlockState[]> blocks;
    private final int rootX;
    private final int rootZ;

    private Map<BlockVector3, Map<String, Tag>> tileEntities;

    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag the tag to read
     * @throws DataException on a data error
     */
    public AnvilChunk18(CompoundTag tag) throws DataException {
        rootTag = tag;

        rootX = NBTUtils.getChildTag(rootTag.getValue(), "xPos", IntTag.class).getValue();
        rootZ = NBTUtils.getChildTag(rootTag.getValue(), "zPos", IntTag.class).getValue();

        List<Tag> sections = NBTUtils.getChildTag(rootTag.getValue(), "sections", ListTag.class).getValue();
        blocks = new Int2ObjectOpenHashMap<>(sections.size());

        for (Tag rawSectionTag : sections) {
            if (!(rawSectionTag instanceof CompoundTag)) {
                continue;
            }

            CompoundTag sectionTag = (CompoundTag) rawSectionTag;
            Object yValue = sectionTag.getValue().get("Y").getValue(); // sometimes a byte, sometimes an int
            if (!(yValue instanceof Number)) {
                throw new InvalidFormatException("Y is not numeric: " + yValue);
            }
            int y = ((Number) yValue).intValue();

            Tag rawBlockStatesTag = sectionTag.getValue().get("block_states"); // null for sections outside of the world limits
            if (rawBlockStatesTag instanceof CompoundTag) {
                CompoundTag blockStatesTag = (CompoundTag) rawBlockStatesTag;

                // parse palette
                List<CompoundTag> paletteEntries = blockStatesTag.getList("palette", CompoundTag.class);
                int paletteSize = paletteEntries.size();
                if (paletteSize == 0) {
                    continue;
                }
                BlockState[] palette = new BlockState[paletteSize];
                for (int paletteEntryId = 0; paletteEntryId < paletteSize; paletteEntryId++) {
                    CompoundTag paletteEntry = paletteEntries.get(paletteEntryId);
                    BlockType type = BlockTypes.get(paletteEntry.getString("Name"));
                    if (type == null) {
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
                if (paletteSize == 1) {
                    // the same block everywhere
                    blocks.put(y, palette);
                    continue;
                }

                // parse block states
                long[] blockStatesSerialized = NBTUtils.getChildTag(blockStatesTag.getValue(), "data", LongArrayTag.class).getValue();

                BlockState[] chunkSectionBlocks = new BlockState[16 * 16 * 16];
                blocks.put(y, chunkSectionBlocks);

                readBlockStates(palette, blockStatesSerialized, chunkSectionBlocks);
            }
        }
    }

    protected void readBlockStates(BlockState[] palette, long[] blockStatesSerialized, BlockState[] chunkSectionBlocks) throws InvalidFormatException {
        PackedIntArrayReader reader = new PackedIntArrayReader(blockStatesSerialized);
        for (int blockPos = 0; blockPos < chunkSectionBlocks.length; blockPos++) {
            int index = reader.get(blockPos);
            if (index >= palette.length) {
                throw new InvalidFormatException("Invalid block state table entry: " + index);
            }
            chunkSectionBlocks[blockPos] = palette[index];
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
        if (!rootTag.getValue().containsKey("block_entities")) {
            return;
        }
        List<Tag> tags = NBTUtils.getChildTag(rootTag.getValue(),
                "block_entities", ListTag.class).getValue();

        for (Tag tag : tags) {
            if (!(tag instanceof CompoundTag)) {
                throw new InvalidFormatException("CompoundTag expected in block_entities");
            }

            CompoundTag t = (CompoundTag) tag;

            Map<String, Tag> values = new HashMap<>(t.getValue());
            int x = ((IntTag) values.get("x")).getValue();
            int y = ((IntTag) values.get("y")).getValue();
            int z = ((IntTag) values.get("z")).getValue();

            BlockVector3 vec = BlockVector3.at(x, y, z);
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
    private CompoundTag getBlockTileEntity(BlockVector3 position) throws DataException {
        if (tileEntities == null) {
            populateTileEntities();
        }

        Map<String, Tag> values = tileEntities.get(position);
        if (values == null) {
            return null;
        }

        return new CompoundTag(values);
    }

    @Override
    public BaseBlock getBlock(BlockVector3 position) throws DataException {
        int x = position.getX() - rootX * 16;
        int y = position.getY();
        int z = position.getZ() - rootZ * 16;

        int section = y >> 4;
        int yIndex = y & 0x0F;

        BlockState[] sectionBlocks = blocks.get(section);
        if (sectionBlocks == null) {
            return BlockTypes.AIR.getDefaultState().toBaseBlock();
        }
        BlockState state = sectionBlocks[sectionBlocks.length == 1 ? 0 : ((yIndex << 8) | (z << 4) | x)];

        CompoundTag tileEntity = getBlockTileEntity(position);

        if (tileEntity != null) {
            return state.toBaseBlock(tileEntity);
        }

        return state.toBaseBlock();
    }

}
