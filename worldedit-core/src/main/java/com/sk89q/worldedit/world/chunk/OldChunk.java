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

import com.google.errorprone.annotations.InlineMe;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import com.sk89q.worldedit.world.storage.InvalidFormatException;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinIntTag;
import org.enginehub.linbus.tree.LinListTag;
import org.enginehub.linbus.tree.LinTagType;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an Alpha chunk.
 */
public class OldChunk implements Chunk {

    private final LinCompoundTag rootTag;
    private final byte[] blocks;
    private final byte[] data;
    private final int rootX;
    private final int rootZ;

    private Map<BlockVector3, LinCompoundTag> tileEntities;


    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag the tag
     * @throws DataException if there is an error getting the chunk data
     * @deprecated Use {@link #OldChunk(LinCompoundTag)}
     */
    @InlineMe(replacement = "this(tag.toLinTag())")
    @Deprecated
    public OldChunk(CompoundTag tag) throws DataException {
        this(tag.toLinTag());
    }

    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag the tag
     * @throws DataException if there is an error getting the chunk data
     */
    public OldChunk(LinCompoundTag tag) throws DataException {
        rootTag = tag;

        blocks = rootTag.getTag("Blocks", LinTagType.byteArrayTag()).value();
        data = rootTag.getTag("Data", LinTagType.byteArrayTag()).value();
        rootX = rootTag.getTag("xPos", LinTagType.intTag()).value();
        rootZ = rootTag.getTag("zPos", LinTagType.intTag()).value();

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
        LinListTag<LinCompoundTag> tags = rootTag.getTag("TileEntities", LinTagType.listTag())
            .asTypeChecked(LinTagType.compoundTag());

        tileEntities = new HashMap<>();

        for (LinCompoundTag t : tags.value()) {
            int x = 0;
            int y = 0;
            int z = 0;

            LinCompoundTag.Builder values = LinCompoundTag.builder();

            for (String key : t.value().keySet()) {
                var value = t.value().get(key);
                switch (key) {
                    case "x" -> {
                        if (value instanceof LinIntTag v) {
                            x = v.valueAsInt();
                        }
                    }
                    case "y" -> {
                        if (value instanceof LinIntTag v) {
                            y = v.valueAsInt();
                        }
                    }
                    case "z" -> {
                        if (value instanceof LinIntTag v) {
                            z = v.valueAsInt();
                        }
                    }
                    default -> {
                        // Do nothing.
                    }
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
    private LinCompoundTag getBlockTileEntity(BlockVector3 position) throws DataException {
        if (tileEntities == null) {
            populateTileEntities();
        }

        return tileEntities.get(position);
    }

    @Override
    public BaseBlock getBlock(BlockVector3 position) throws DataException {
        if (position.y() >= 128) {
            return BlockTypes.VOID_AIR.getDefaultState().toBaseBlock();
        }
        int id;
        int dataVal;

        int x = position.x() - rootX * 16;
        int y = position.y();
        int z = position.z() - rootZ * 16;
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

        LinCompoundTag tileEntity = getBlockTileEntity(position);

        return state.toBaseBlock(tileEntity);
    }

}
