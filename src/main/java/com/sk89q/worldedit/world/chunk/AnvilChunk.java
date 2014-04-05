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

import com.sk89q.jnbt.*;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.world.DataException;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.storage.InvalidFormatException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnvilChunk implements Chunk {

    private CompoundTag rootTag;
    private byte[][] blocks;
    private byte[][] blocksAdd;
    private byte[][] data;
    private int rootX;
    private int rootZ;

    private Map<BlockVector, Map<String,Tag>> tileEntities;
    @SuppressWarnings("unused")
    private World world; // TODO: remove if stays unused.

    /**
     * Construct the chunk with a compound tag.
     * 
     * @param world the world to construct the chunk for
     * @param tag the tag to read
     * @throws DataException on a data error
     */
    public AnvilChunk(World world, CompoundTag tag) throws DataException {
        rootTag = tag;
        this.world = world;

        rootX = NBTUtils.getChildTag(rootTag.getValue(), "xPos", IntTag.class).getValue();
        rootZ = NBTUtils.getChildTag(rootTag.getValue(), "zPos", IntTag.class).getValue();

        blocks = new byte[16][16 * 16 * 16];
        blocksAdd = new byte[16][16 * 16 * 8];
        data = new byte[16][16 * 16 * 8];
        
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

            blocks[y] = NBTUtils.getChildTag(sectionTag.getValue(),
                    "Blocks", ByteArrayTag.class).getValue();
            data[y] = NBTUtils.getChildTag(sectionTag.getValue(), "Data",
                    ByteArrayTag.class).getValue();

            // 4096 ID block support
            if (sectionTag.getValue().containsKey("Add")) {
                blocksAdd[y] = NBTUtils.getChildTag(sectionTag.getValue(),
                        "Add", ByteArrayTag.class).getValue();
            }
        }

        int sectionsize = 16 * 16 * 16;
        for (int i = 0; i < blocks.length; i++) {
            if (blocks[i].length != sectionsize) {
                throw new InvalidFormatException(
                        "Chunk blocks byte array expected " + "to be "
                                + sectionsize + " bytes; found "
                                + blocks[i].length);
            }
        }

        for (int i = 0; i < data.length; i++) {
            if (data[i].length != (sectionsize / 2)) {
                throw new InvalidFormatException("Chunk block data byte array "
                        + "expected to be " + sectionsize + " bytes; found "
                        + data[i].length);
            }
        }
    }
    
    @Override
    public int getBlockID(Vector pos) throws DataException {
        int x = pos.getBlockX() - rootX * 16;
        int y = pos.getBlockY();
        int z = pos.getBlockZ() - rootZ * 16;

        int section = y >> 4;
        if (section < 0 || section >= blocks.length) {
            throw new DataException("Chunk does not contain position " + pos);
        }
        
        int yindex = y & 0x0F;
        if (yindex < 0 || yindex >= 16) {
            throw new DataException("Chunk does not contain position " + pos);
        }

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
            throw new DataException("Chunk does not contain position " + pos);
        }
    }

    @Override
    public int getBlockData(Vector pos) throws DataException {
        int x = pos.getBlockX() - rootX * 16;
        int y = pos.getBlockY();
        int z = pos.getBlockZ() - rootZ * 16;

        int section = y >> 4;
        int yIndex = y & 0x0F;
        
        if (section < 0 || section >= blocks.length) {
            throw new DataException("Chunk does not contain position " + pos);
        }
        
        if (yIndex < 0 || yIndex >= 16) {
            throw new DataException("Chunk does not contain position " + pos);
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
            throw new DataException("Chunk does not contain position " + pos);
        }
    }

    /**
     * Used to load the tile entities.
     *
     * @throws DataException
     */
    private void populateTileEntities() throws DataException {
        List<Tag> tags = NBTUtils.getChildTag(rootTag.getValue(),
                "TileEntities", ListTag.class).getValue();

        tileEntities = new HashMap<BlockVector, Map<String, Tag>>();

        for (Tag tag : tags) {
            if (!(tag instanceof CompoundTag)) {
                throw new InvalidFormatException(
                        "CompoundTag expected in TileEntities");
            }

            CompoundTag t = (CompoundTag) tag;

            int x = 0;
            int y = 0;
            int z = 0;

            Map<String, Tag> values = new HashMap<String, Tag>();

            for (Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
                if (entry.getKey().equals("x")) {
                    if (entry.getValue() instanceof IntTag) {
                        x = ((IntTag) entry.getValue()).getValue();
                    }
                } else if (entry.getKey().equals("y")) {
                    if (entry.getValue() instanceof IntTag) {
                        y = ((IntTag) entry.getValue()).getValue();
                    }
                } else if (entry.getKey().equals("z")) {
                    if (entry.getValue() instanceof IntTag) {
                        z = ((IntTag) entry.getValue()).getValue();
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
    private CompoundTag getBlockTileEntity(Vector pos) throws DataException {
        if (tileEntities == null) {
            populateTileEntities();
        }

        Map<String, Tag> values = tileEntities.get(new BlockVector(pos));
        if (values == null) {
            return null;
        }
        return new CompoundTag("", values);
    }

    @Override
    public BaseBlock getBlock(Vector pos) throws DataException {
        int id = getBlockID(pos);
        int data = getBlockData(pos);
        BaseBlock block;

        /*if (id == BlockID.WALL_SIGN || id == BlockID.SIGN_POST) {
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
        } else {*/
            block = new BaseBlock(id, data);
        //}

        if (block instanceof TileEntityBlock) {
            CompoundTag tileEntity = getBlockTileEntity(pos);
            if (tileEntity != null) {
                ((TileEntityBlock) block).setNbtData(tileEntity);
            }
        }

        return block;
    }

}
