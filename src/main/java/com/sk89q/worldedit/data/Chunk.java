// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/


package com.sk89q.worldedit.data;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import com.sk89q.jnbt.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.*;

/**
 * Represents a chunk.
 *
 * @author sk89q
 */
public class Chunk {
    private CompoundTag rootTag;
    private byte[] blocks;
    private byte[] data;
    private int rootX;
    private int rootZ;
    private Map<BlockVector,Map<String,Tag>> tileEntities;

    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag
     * @throws DataException
     */
    public Chunk(CompoundTag tag) throws DataException {
        rootTag = tag;

        blocks = ((ByteArrayTag)getChildTag(
                rootTag.getValue(), "Blocks", ByteArrayTag.class)).getValue();
        data = ((ByteArrayTag)getChildTag(
                rootTag.getValue(), "Data", ByteArrayTag.class)).getValue();
        rootX = ((IntTag)getChildTag(
                rootTag.getValue(), "xPos", IntTag.class)).getValue();
        rootZ = ((IntTag)getChildTag(
                rootTag.getValue(), "zPos", IntTag.class)).getValue();

        if (blocks.length != 32768) {
            throw new InvalidFormatException("Chunk blocks byte array expected "
                    + "to be 32,768 bytes; found " + blocks.length);
        }

        if (data.length != 16384) {
            throw new InvalidFormatException("Chunk block data byte array "
                    + "expected to be 16,384 bytes; found " + data.length);
        }
    }

    /**
     * Get the block ID of a block.
     *
     * @param pos
     * @return
     * @throws DataException
     */
    public int getBlockID(Vector pos) throws DataException {
        int x = pos.getBlockX() - rootX * 16;
        int y = pos.getBlockY();
        int z = pos.getBlockZ() - rootZ * 16;
        int index = y + (z * 128 + (x * 128 * 16));

        try {
            return blocks[index];
        } catch (IndexOutOfBoundsException e) {
            throw new DataException("Chunk does not contain position " + pos);
        }
    }

    /**
     * Get the block data of a block.
     *
     * @param pos
     * @return
     * @throws DataException
     */
    public int getBlockData(Vector pos) throws DataException {
        int x = pos.getBlockX() - rootX * 16;
        int y = pos.getBlockY();
        int z = pos.getBlockZ() - rootZ * 16;
        int index = y + (z * 128 + (x * 128 * 16));
        boolean shift = index % 2 == 0;
        index /= 2;

        try {
            if (!shift) {
                return (data[index] & 0xF0) >> 4;
            } else {
                return data[index] & 0xF;
            }
        } catch (IndexOutOfBoundsException e) {
            throw new DataException("Chunk does not contain position " + pos);
        }
    }

    /**
     * Used to load the tile entities.
     *
     * @throws DataException
     */
    private void populateTileEntities() throws DataException {
        List<Tag> tags = (List<Tag>)((ListTag)getChildTag(
                rootTag.getValue(), "TileEntities", ListTag.class))
                .getValue();

        tileEntities = new HashMap<BlockVector,Map<String,Tag>>();

        for (Tag tag : tags) {
            if (!(tag instanceof CompoundTag)) {
                throw new InvalidFormatException("CompoundTag expected in TileEntities");
            }

            CompoundTag t = (CompoundTag)tag;

            int x = 0;
            int y = 0;
            int z = 0;

            Map<String,Tag> values = new HashMap<String,Tag>();

            for (Map.Entry<String,Tag> entry : t.getValue().entrySet()) {
                if (entry.getKey().equals("x")) {
                    if (entry.getValue() instanceof IntTag) {
                        x = ((IntTag)entry.getValue()).getValue();
                    }
                } else if (entry.getKey().equals("y")) {
                    if (entry.getValue() instanceof IntTag) {
                        y = ((IntTag)entry.getValue()).getValue();
                    }
                } else if (entry.getKey().equals("z")) {
                    if (entry.getValue() instanceof IntTag) {
                        z = ((IntTag)entry.getValue()).getValue();
                    }
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
     * @param pos
     * @return
     * @throws DataException
     */
    private Map<String,Tag> getBlockTileEntity(Vector pos) throws DataException {
        if (tileEntities == null)
            populateTileEntities();

        return tileEntities.get(new BlockVector(pos));
    }

    /**
     * Get a block;
     *
     * @param pos
     * @return block
     * @throws DataException
     */
    public BaseBlock getBlock(Vector pos) throws DataException {
        int id = getBlockID(pos);
        int data = getBlockData(pos);
        BaseBlock block;

        if (id == BlockID.WALL_SIGN || id == BlockID.SIGN_POST) {
            block = new SignBlock(id, data);
        } else if (id == BlockID.CHEST) {
            block = new ChestBlock(data);
        } else if (id == BlockID.FURNACE || id == BlockID.BURNING_FURNACE) {
            block = new FurnaceBlock(id, data);
        } else if (id == BlockID.DISPENSER) {
            block = new DispenserBlock(data);
        } else if (id == BlockID.MOB_SPAWNER) {
            block = new MobSpawnerBlock(data);
        } else if (id == BlockID.NOTE_BLOCK) {
            block = new NoteBlock(data);
        } else {
            block = new BaseBlock(id, data);
        }

        if (block instanceof TileEntityBlock) {
            Map<String,Tag> tileEntity = getBlockTileEntity(pos);
            ((TileEntityBlock)block).fromTileEntityNBT(tileEntity);
        }

        return block;
    }

    /**
     * Get child tag of a NBT structure.
     *
     * @param items
     * @param key
     * @param expected
     * @return child tag
     * @throws InvalidFormatException
     */
    public static Tag getChildTag(Map<String,Tag> items, String key,
            Class<? extends Tag> expected)
            throws InvalidFormatException {
        if (!items.containsKey(key)) {
            throw new InvalidFormatException("Missing a \"" + key + "\" tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new InvalidFormatException(
                key + " tag is not of tag type " + expected.getName());
        }
        return tag;
    }
}
