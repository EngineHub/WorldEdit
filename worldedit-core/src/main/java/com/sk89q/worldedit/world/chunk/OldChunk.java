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

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import com.sk89q.worldedit.world.storage.InvalidFormatException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an Alpha chunk.
 */
public class OldChunk implements Chunk {

    private CompoundTag rootTag;
    private byte[] blocks;
    private byte[] data;
    private int rootX;
    private int rootZ;

    private Map<BlockVector, Map<String,Tag>> tileEntities;

    /**
     * Construct the chunk with a compound tag.
     *
     * @param world the world
     * @param tag the tag
     * @throws DataException
     */
    public OldChunk(World world, CompoundTag tag) throws DataException {
        rootTag = tag;
        
        blocks = NBTUtils.getChildTag(rootTag.getValue(), "Blocks", ByteArrayTag.class).getValue();
        data = NBTUtils.getChildTag(rootTag.getValue(), "Data", ByteArrayTag.class).getValue();
        rootX = NBTUtils.getChildTag(rootTag.getValue(), "xPos", IntTag.class).getValue();
        rootZ = NBTUtils.getChildTag(rootTag.getValue(), "zPos", IntTag.class).getValue();

        int size = 16 * 16 * 128;
        if (blocks.length != size) {
            throw new InvalidFormatException("Chunk blocks byte array expected "
                    + "to be " + size + " bytes; found " + blocks.length);
        }

        if (data.length != (size/2)) {
            throw new InvalidFormatException("Chunk block data byte array "
                    + "expected to be " + size + " bytes; found " + data.length);
        }
    }

    /**
     * Used to load the tile entities.
     *
     * @throws DataException
     */
    private void populateTileEntities() throws DataException {
        List<Tag> tags = NBTUtils.getChildTag(
                rootTag.getValue(), "TileEntities", ListTag.class)
                .getValue();

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
     * @return a tag
     * @throws DataException
     */
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
        if(position.getBlockY() >= 128) BlockTypes.AIR.getDefaultState().toBaseBlock();
        int id, dataVal;

        int x = position.getBlockX() - rootX * 16;
        int y = position.getBlockY();
        int z = position.getBlockZ() - rootZ * 16;
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
        CompoundTag tileEntity = getBlockTileEntity(position);

        return state.toBaseBlock(tileEntity);
    }

}
