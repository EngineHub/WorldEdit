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

import com.google.common.collect.ImmutableMap;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.storage.InvalidFormatException;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

import java.util.Map;
import javax.annotation.Nullable;

/**
 * The chunk format for Minecraft 1.13 to 1.15
 */
public class AnvilChunk13 implements Chunk {

    private final LinCompoundTag rootTag;
    private final BlockState[][] blocks;
    private final int rootX;
    private final int rootZ;

    private Map<BlockVector3, LinCompoundTag> tileEntities;


    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag the tag to read
     * @throws DataException on a data error
     * @deprecated Use {@link #AnvilChunk13(LinCompoundTag)}
     */
    @Deprecated
    public AnvilChunk13(CompoundTag tag) throws DataException {
        this(tag.toLinTag());
    }

    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag the tag to read
     * @throws DataException on a data error
     */
    public AnvilChunk13(LinCompoundTag tag) throws DataException {
        rootTag = tag;

        rootX = rootTag.getTag("xPos", LinTagType.intTag()).valueAsInt();
        rootZ = rootTag.getTag("zPos", LinTagType.intTag()).valueAsInt();

        blocks = new BlockState[16][];

        LinListTag<LinTag<?>> sections = rootTag.getTag("Sections", LinTagType.listTag());

        for (LinTag<?> rawSectionTag : sections.value()) {
            if (!(rawSectionTag instanceof LinCompoundTag sectionTag)) {
                continue;
            }

            var sectionYTag = sectionTag.findTag("Y", LinTagType.byteTag());
            if (sectionYTag == null) {
                continue; // Empty section.
            }

            int y = sectionYTag.value();
            if (y < 0 || y >= 16) {
                continue;
            }

            // parse palette
            LinListTag<LinCompoundTag> paletteEntries = sectionTag.getTag(
                "Palette", LinTagType.listTag()
            ).asTypeChecked(LinTagType.compoundTag());
            int paletteSize = paletteEntries.value().size();
            if (paletteSize == 0) {
                continue;
            }
            BlockState[] palette = new BlockState[paletteSize];
            for (int paletteEntryId = 0; paletteEntryId < paletteSize; paletteEntryId++) {
                LinCompoundTag paletteEntry = paletteEntries.get(paletteEntryId);
                String blockType = paletteEntry.getTag("Name", LinTagType.stringTag()).value();
                BlockType type = BlockTypes.get(blockType);
                if (type == null) {
                    throw new InvalidFormatException("Invalid block type: " + blockType);
                }
                BlockState blockState = type.getDefaultState();
                var propertiesTag = paletteEntry.findTag("Properties", LinTagType.compoundTag());
                if (propertiesTag != null) {
                    for (Property<?> property : blockState.getStates().keySet()) {
                        var propertyName = propertiesTag.findTag(property.getName(), LinTagType.stringTag());
                        if (propertyName != null) {
                            String value = propertyName.value();
                            try {
                                blockState = getBlockStateWith(blockState, property, value);
                            } catch (IllegalArgumentException e) {
                                throw new InvalidFormatException("Invalid block state for " + blockState.getBlockType().id() + ", " + property.getName() + ": " + value);
                            }
                        }
                    }
                }
                palette[paletteEntryId] = blockState;
            }

            // parse block states
            long[] blockStatesSerialized = sectionTag.getTag("BlockStates", LinTagType.longArrayTag()).value();

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
                localBlockId |= (int) (currentSerializedValue & ((1L << bitsNextLong) - 1)) << remainingBits;
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
    private Map<BlockVector3, LinCompoundTag> populateTileEntities() {
        LinListTag<LinCompoundTag> tags = rootTag.findListTag(
            "TileEntities", LinTagType.compoundTag()
        );
        if (tags == null) {
            return ImmutableMap.of();
        }
        var tileEntities = ImmutableMap.<BlockVector3, LinCompoundTag>builderWithExpectedSize(tags.value().size());
        for (LinCompoundTag tag : tags.value()) {
            int x = tag.getTag("x", LinTagType.intTag()).valueAsInt();
            int y = tag.getTag("y", LinTagType.intTag()).valueAsInt();
            int z = tag.getTag("z", LinTagType.intTag()).valueAsInt();

            BlockVector3 vec = BlockVector3.at(x, y, z);
            tileEntities.put(vec, tag);
        }
        return tileEntities.build();
    }

    /**
     * Get the map of tags keyed to strings for a block's tile entity data. May
     * return null if there is no tile entity data. Not public yet because
     * what this function returns isn't ideal for usage.
     *
     * @param position the position
     * @return the compound tag for that position, which may be null
     */
    @Nullable
    private LinCompoundTag getBlockTileEntity(BlockVector3 position) {
        if (tileEntities == null) {
            this.tileEntities = populateTileEntities();
        }

        return tileEntities.get(position);
    }

    @Override
    public BaseBlock getBlock(BlockVector3 position) throws DataException {
        int x = position.x() - rootX * 16;
        int y = position.y();
        int z = position.z() - rootZ * 16;

        int section = y >> 4;
        int yIndex = y & 0x0F;

        if (section < 0 || section >= blocks.length) {
            throw new DataException("Chunk does not contain position " + position);
        }

        BlockState[] sectionBlocks = blocks[section];
        BlockState state = sectionBlocks != null ? sectionBlocks[(yIndex << 8) | (z << 4) | x] : BlockTypes.AIR.getDefaultState();

        LinCompoundTag tileEntity = getBlockTileEntity(position);

        return state.toBaseBlock(tileEntity);
    }

}
