package com.sk89q.worldedit.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.ChestBlock;
import com.sk89q.worldedit.blocks.DispenserBlock;
import com.sk89q.worldedit.blocks.FurnaceBlock;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.NoteBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;

public class AnvilChunk implements Chunk {

    private CompoundTag rootTag;
    private byte[][] blocks;
    private byte[][] data;
    private int rootX;
    private int rootZ;

    private Map<BlockVector, Map<String,Tag>> tileEntities;
    @SuppressWarnings("unused")
    private LocalWorld world; // TODO: remove if stays unused.

    /**
     * Construct the chunk with a compound tag.
     *
     * @param tag
     * @throws DataException
     */
    public AnvilChunk(LocalWorld world, CompoundTag tag) throws DataException {
        rootTag = tag;
        this.world = world;

        rootX = NBTUtils.getChildTag( rootTag.getValue(), "xPos", IntTag.class).getValue();
        rootZ = NBTUtils.getChildTag( rootTag.getValue(), "zPos", IntTag.class).getValue();

        blocks = new byte[16][16*16*16];
        data = new byte[16][16*16*8];
        List<Tag> sections = NBTUtils.getChildTag(rootTag.getValue(), "Sections", ListTag.class).getValue();
        for(Tag section : sections) {
            if(!(section instanceof CompoundTag)) continue;
            CompoundTag compoundsection = (CompoundTag) section;
            if(!compoundsection.getValue().containsKey("Y")) continue; // Empty section.
            int y = NBTUtils.getChildTag(compoundsection.getValue(), "Y", ByteTag.class).getValue();
            if(y < 0 || y >= 16) continue;
            blocks[y]  = NBTUtils.getChildTag(compoundsection.getValue(), "Blocks", ByteArrayTag.class).getValue();
            data[y]  = NBTUtils.getChildTag(compoundsection.getValue(), "Data", ByteArrayTag.class).getValue();
        }
        
        int sectionsize = 16*16*16;
        for(int i = 0; i < blocks.length; i++) {
            if (blocks[i].length != sectionsize) {
                throw new InvalidFormatException("Chunk blocks byte array expected "
                        + "to be " + sectionsize + " bytes; found " + blocks[i].length);
            }
        }

        for(int i = 0; i < data.length; i++) {
            if (data[i].length != (sectionsize/2)) {
                throw new InvalidFormatException("Chunk block data byte array "
                        + "expected to be " + sectionsize + " bytes; found " + data[i].length);
            }
        }
    }
    
    @Override
    public int getBlockID(Vector pos) throws DataException {
        int x = pos.getBlockX() - rootX * 16;
        int y = pos.getBlockY();
        int z = pos.getBlockZ() - rootZ * 16;
        
        int section = y >> 4;
        if(section < 0 || section >= blocks.length) throw new DataException("Chunk does not contain position " + pos);
        int yindex = y & 0x0F;
        if(yindex < 0 || yindex >= 16) throw new DataException("Chunk does not contain position " + pos);
        
        
        int index = x + (z * 16 + (yindex * 16 * 16));
        try {
            return blocks[section][index];
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
        if(section < 0 || section >= blocks.length) throw new DataException("Chunk does not contain position " + pos);
        int yindex = y & 0x0F;
        if(yindex < 0 || yindex >= 16) throw new DataException("Chunk does not contain position " + pos);
        
        
        int index = x + (z * 16 + (yindex * 16 * 16));
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
        List<Tag> tags = NBTUtils.getChildTag(
                rootTag.getValue(), "TileEntities", ListTag.class)
                .getValue();

        tileEntities = new HashMap<BlockVector, Map<String, Tag>>();

        for (Tag tag : tags) {
            if (!(tag instanceof CompoundTag)) {
                throw new InvalidFormatException("CompoundTag expected in TileEntities");
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
    private Map<String, Tag> getBlockTileEntity(Vector pos) throws DataException {
        if (tileEntities == null) {
            populateTileEntities();
        }

        return tileEntities.get(new BlockVector(pos));
    }

    @Override
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
            Map<String, Tag> tileEntity = getBlockTileEntity(pos);
            ((TileEntityBlock) block).fromTileEntityNBT(tileEntity);
        }

        return block;
    }

}
