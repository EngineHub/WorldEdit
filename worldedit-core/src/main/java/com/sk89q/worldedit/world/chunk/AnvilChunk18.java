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
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.storage.InvalidFormatException;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinTagType;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * The chunk format for Minecraft 1.18 and newer
 */
public class AnvilChunk18 implements Chunk {

    private final LinCompoundTag rootTag;
    private final Int2ObjectOpenHashMap<BlockState[]> blocks;
    private final int rootX;
    private final int rootZ;

    private Map<BlockVector3, LinCompoundTag> tileEntities;

    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag the tag to read
     * @throws DataException on a data error
     * @deprecated Use {@link #AnvilChunk18(LinCompoundTag)}
     */
    @Deprecated
    public AnvilChunk18(CompoundTag tag) throws DataException {
        this(tag.toLinTag());
    }

    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag the tag to read
     * @throws DataException on a data error
     */
    public AnvilChunk18(LinCompoundTag tag) throws DataException {
        rootTag = tag;

        rootX = rootTag.getTag("xPos", LinTagType.intTag()).valueAsInt();
        rootZ = rootTag.getTag("zPos", LinTagType.intTag()).valueAsInt();

        var sections = rootTag.getListTag("sections", LinTagType.compoundTag()).value();
        blocks = new Int2ObjectOpenHashMap<>(sections.size());

        for (LinCompoundTag sectionTag : sections) {
            Object yValue = sectionTag.value().get("Y").value(); // sometimes a byte, sometimes an int
            if (!(yValue instanceof Number yNumber)) {
                throw new InvalidFormatException("Y is not numeric: " + yValue);
            }
            int y = yNumber.intValue();

            var blockStatesTag = sectionTag.findTag("block_states", LinTagType.compoundTag());
            if (blockStatesTag == null) {
                // null for sections outside the world limits
                continue;
            }
            // parse palette
            var paletteEntries = blockStatesTag.getListTag("palette", LinTagType.compoundTag()).value();
            int paletteSize = paletteEntries.size();
            if (paletteSize == 0) {
                continue;
            }
            BlockState[] palette = new BlockState[paletteSize];
            for (int paletteEntryId = 0; paletteEntryId < paletteSize; paletteEntryId++) {
                LinCompoundTag paletteEntry = paletteEntries.get(paletteEntryId);
                String typeString = paletteEntry.getTag("Name", LinTagType.stringTag()).value();
                BlockType type = BlockTypes.get(typeString);
                if (type == null) {
                    throw new InvalidFormatException("Invalid block type: " + typeString);
                }
                BlockState blockState = type.getDefaultState();
                var properties = paletteEntry.findTag("Properties", LinTagType.compoundTag());
                if (properties != null) {
                    for (Property<?> property : blockState.getStates().keySet()) {
                        var name = properties.findTag(property.getName(), LinTagType.stringTag());
                        if (name != null) {
                            String value = name.value();
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
            long[] blockStatesSerialized = blockStatesTag.getTag("data", LinTagType.longArrayTag()).value();

            BlockState[] chunkSectionBlocks = new BlockState[16 * 16 * 16];
            blocks.put(y, chunkSectionBlocks);

            readBlockStates(palette, blockStatesSerialized, chunkSectionBlocks);
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
        var tags = rootTag.findListTag("block_entities", LinTagType.compoundTag());
        if (tags == null) {
            return;
        }

        for (LinCompoundTag tag : tags.value()) {
            int x = tag.getTag("x", LinTagType.intTag()).valueAsInt();
            int y = tag.getTag("y", LinTagType.intTag()).valueAsInt();
            int z = tag.getTag("z", LinTagType.intTag()).valueAsInt();

            BlockVector3 vec = BlockVector3.at(x, y, z);
            tileEntities.put(vec, tag);
        }
    }

    /**
     * Get the compound tag for a block's tile entity data. May
     * return null if there is no tile entity data. Not public yet because
     * what this function returns isn't ideal for usage.
     *
     * @param position the position
     * @return the compound tag for that position, which may be null
     * @throws DataException thrown if there is a data error
     */
    @Nullable
    private LinCompoundTag getBlockTileEntity(BlockVector3 position) throws DataException {
        if (tileEntities == null) {
            populateTileEntities();
        }

        return tileEntities.get(position);
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

        LinCompoundTag tileEntity = getBlockTileEntity(position);

        if (tileEntity != null) {
            return state.toBaseBlock(tileEntity);
        }

        return state.toBaseBlock();
    }

}
