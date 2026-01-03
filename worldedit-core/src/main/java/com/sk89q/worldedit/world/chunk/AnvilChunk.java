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
import org.enginehub.linbus.tree.LinTag;
import org.enginehub.linbus.tree.LinTagType;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class AnvilChunk implements Chunk {

    private final LinCompoundTag rootTag;
    private final byte[][] blocks;
    private final byte[][] blocksAdd;
    private final byte[][] data;
    private final int rootX;
    private final int rootZ;

    private Map<BlockVector3, LinCompoundTag> tileEntities;


    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag the tag to read
     * @throws DataException on a data error
     * @deprecated Use {@link #AnvilChunk(LinCompoundTag)}
     */
    @InlineMe(replacement = "this(tag.toLinTag())")
    @Deprecated
    public AnvilChunk(CompoundTag tag) throws DataException {
        this(tag.toLinTag());
    }

    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag the tag to read
     * @throws DataException on a data error
     */
    public AnvilChunk(LinCompoundTag tag) throws DataException {
        rootTag = tag;

        rootX = rootTag.getTag("xPos", LinTagType.intTag()).value();
        rootZ = rootTag.getTag("zPos", LinTagType.intTag()).value();

        blocks = new byte[16][16 * 16 * 16];
        blocksAdd = new byte[16][16 * 16 * 8];
        data = new byte[16][16 * 16 * 8];

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

            blocks[y] = sectionTag.getTag("Blocks", LinTagType.byteArrayTag()).value();
            data[y] = sectionTag.getTag("Data", LinTagType.byteArrayTag()).value();

            // 4096 ID block support
            var addTag = sectionTag.findTag("Add", LinTagType.byteArrayTag());
            if (addTag != null) {
                blocksAdd[y] = addTag.value();
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
        int x = position.x() - rootX * 16;
        int y = position.y();
        int z = position.z() - rootZ * 16;

        int section = y >> 4;
        if (section < 0 || section >= blocks.length) {
            throw new DataException("Chunk does not contain position " + position);
        }

        int yindex = y & 0x0F;

        int index = x + (z * 16 + (yindex * 16 * 16));

        try {
            // The block ID is the combination of the Blocks byte array with the
            // Add byte array. 'Blocks' stores the lowest 8 bits of a block's ID, and
            // 'Add' stores the highest 4 bits of the ID. The first block is stored
            // in the lowest nibble in the Add byte array.
            byte addByte = blocksAdd[section][index >> 1];
            int addId = (index & 1) == 0 ? (addByte & 0x0F) << 8 : (addByte & 0xF0) << 4;

            return (blocks[section][index] & 0xFF) + addId;
        } catch (IndexOutOfBoundsException e) {
            throw new DataException("Chunk does not contain position " + position);
        }
    }

    private int getBlockData(BlockVector3 position) throws DataException {
        int x = position.x() - rootX * 16;
        int y = position.y();
        int z = position.z() - rootZ * 16;

        int section = y >> 4;
        int yIndex = y & 0x0F;

        if (section < 0 || section >= blocks.length) {
            throw new DataException("Chunk does not contain position " + position);
        }

        int index = x + (z * 16 + (yIndex * 16 * 16));
        boolean shift = (index & 1) != 0;
        index >>= 2;

        try {
            byte dataByte = data[section][index];
            return shift ? (dataByte & 0xF0) >> 4 : dataByte & 0x0F;
        } catch (IndexOutOfBoundsException e) {
            throw new DataException("Chunk does not contain position " + position);
        }
    }

    /**
     * Used to load the tile entities.
     */
    private void populateTileEntities() {
        LinListTag<LinCompoundTag> tags = rootTag.getTag("TileEntities", LinTagType.listTag())
            .asTypeChecked(LinTagType.compoundTag());

        tileEntities = new HashMap<>(tags.value().size());

        for (LinCompoundTag t : tags.value()) {
            int x = 0;
            int y = 0;
            int z = 0;

            LinCompoundTag.Builder values = LinCompoundTag.builder();

            for (String key : t.value().keySet()) {
                LinTag<?> value = t.value().get(key);
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
        int id = getBlockID(position);
        int data = getBlockData(position);

        BlockState state = LegacyMapper.getInstance().getBlockFromLegacy(id, data);
        if (state == null) {
            WorldEdit.logger.warn("Unknown legacy block " + id + ":" + data + " found when loading legacy anvil chunk.");
            return BlockTypes.AIR.getDefaultState().toBaseBlock();
        }
        LinCompoundTag tileEntity = getBlockTileEntity(position);

        if (tileEntity != null) {
            return state.toBaseBlock(tileEntity);
        }

        return state.toBaseBlock();
    }

}
