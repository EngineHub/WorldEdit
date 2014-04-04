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

package com.sk89q.worldedit.schematic;

import com.sk89q.jnbt.*;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.data.DataException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

/**
 * @author zml2008
 */
public class MCEditSchematicFormat extends SchematicFormat {
    private static final int MAX_SIZE = Short.MAX_VALUE - Short.MIN_VALUE;

    protected MCEditSchematicFormat() {
        super("MCEdit", "mcedit", "mce");
    }

    @Override
    public CuboidClipboard load(File file) throws IOException, DataException {
        FileInputStream stream = new FileInputStream(file);
        NBTInputStream nbtStream = new NBTInputStream(
                new GZIPInputStream(stream));

        Vector origin = new Vector();
        Vector offset = new Vector();

        // Schematic tag
        CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
        nbtStream.close();
        if (!schematicTag.getName().equals("Schematic")) {
            throw new DataException("Tag \"Schematic\" does not exist or is not first");
        }

        // Check
        Map<String, Tag> schematic = schematicTag.getValue();
        if (!schematic.containsKey("Blocks")) {
            throw new DataException("Schematic file is missing a \"Blocks\" tag");
        }

        // Get information
        short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
        short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
        short height = getChildTag(schematic, "Height", ShortTag.class).getValue();

        try {
            int originX = getChildTag(schematic, "WEOriginX", IntTag.class).getValue();
            int originY = getChildTag(schematic, "WEOriginY", IntTag.class).getValue();
            int originZ = getChildTag(schematic, "WEOriginZ", IntTag.class).getValue();
            origin = new Vector(originX, originY, originZ);
        } catch (DataException e) {
            // No origin data
        }

        try {
            int offsetX = getChildTag(schematic, "WEOffsetX", IntTag.class).getValue();
            int offsetY = getChildTag(schematic, "WEOffsetY", IntTag.class).getValue();
            int offsetZ = getChildTag(schematic, "WEOffsetZ", IntTag.class).getValue();
            offset = new Vector(offsetX, offsetY, offsetZ);
        } catch (DataException e) {
            // No offset data
        }

        // Check type of Schematic
        String materials = getChildTag(schematic, "Materials", StringTag.class).getValue();
        if (!materials.equals("Alpha")) {
            throw new DataException("Schematic file is not an Alpha schematic");
        }

        // Get blocks
        byte[] blockId = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
        byte[] blockData = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
        byte[] addId = new byte[0];
        short[] blocks = new short[blockId.length]; // Have to later combine IDs

        // We support 4096 block IDs using the same method as vanilla Minecraft, where
        // the highest 4 bits are stored in a separate byte array.
        if (schematic.containsKey("AddBlocks")) {
            addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
        }

        // Combine the AddBlocks data with the first 8-bit block ID
        for (int index = 0; index < blockId.length; index++) {
            if ((index >> 1) >= addId.length) { // No corresponding AddBlocks index
                blocks[index] = (short) (blockId[index] & 0xFF);
            } else {
                if ((index & 1) == 0) {
                    blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                } else {
                    blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
                }
            }
        }

        // Need to pull out tile entities
        List<Tag> tileEntities = getChildTag(schematic, "TileEntities", ListTag.class)
                .getValue();
        Map<BlockVector, Map<String, Tag>> tileEntitiesMap =
                new HashMap<BlockVector, Map<String, Tag>>();

        for (Tag tag : tileEntities) {
            if (!(tag instanceof CompoundTag)) continue;
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
            tileEntitiesMap.put(vec, values);
        }

        Vector size = new Vector(width, height, length);
        CuboidClipboard clipboard = new CuboidClipboard(size);
        clipboard.setOrigin(origin);
        clipboard.setOffset(offset);

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    BlockVector pt = new BlockVector(x, y, z);
                    BaseBlock block = getBlockForId(blocks[index], blockData[index]);

                    if (block instanceof TileEntityBlock && tileEntitiesMap.containsKey(pt)) {
                        try {
                            ((TileEntityBlock) block).setNbtData(new CompoundTag("", tileEntitiesMap.get(pt)));
                        } catch (com.sk89q.worldedit.world.DataException e) {
                            throw new DataException(e.getMessage());
                        }
                    }
                    clipboard.setBlock(pt, block);
                }
            }
        }

        return clipboard;
    }

    @Override
    public void save(CuboidClipboard clipboard, File file) throws IOException, DataException {
        int width = clipboard.getWidth();
        int height = clipboard.getHeight();
        int length = clipboard.getLength();

        if (width > MAX_SIZE) {
            throw new DataException("Width of region too large for a .schematic");
        }
        if (height > MAX_SIZE) {
            throw new DataException("Height of region too large for a .schematic");
        }
        if (length > MAX_SIZE) {
            throw new DataException("Length of region too large for a .schematic");
        }

        HashMap<String, Tag> schematic = new HashMap<String, Tag>();
        schematic.put("Width", new ShortTag("Width", (short) width));
        schematic.put("Length", new ShortTag("Length", (short) length));
        schematic.put("Height", new ShortTag("Height", (short) height));
        schematic.put("Materials", new StringTag("Materials", "Alpha"));
        schematic.put("WEOriginX", new IntTag("WEOriginX", clipboard.getOrigin().getBlockX()));
        schematic.put("WEOriginY", new IntTag("WEOriginY", clipboard.getOrigin().getBlockY()));
        schematic.put("WEOriginZ", new IntTag("WEOriginZ", clipboard.getOrigin().getBlockZ()));
        schematic.put("WEOffsetX", new IntTag("WEOffsetX", clipboard.getOffset().getBlockX()));
        schematic.put("WEOffsetY", new IntTag("WEOffsetY", clipboard.getOffset().getBlockY()));
        schematic.put("WEOffsetZ", new IntTag("WEOffsetZ", clipboard.getOffset().getBlockZ()));

        // Copy
        byte[] blocks = new byte[width * height * length];
        byte[] addBlocks = null;
        byte[] blockData = new byte[width * height * length];
        ArrayList<Tag> tileEntities = new ArrayList<Tag>();

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    BaseBlock block = clipboard.getPoint(new BlockVector(x, y, z));

                    // Save 4096 IDs in an AddBlocks section
                    if (block.getType() > 255) {
                        if (addBlocks == null) { // Lazily create section
                            addBlocks = new byte[(blocks.length >> 1) + 1];
                        }

                        addBlocks[index >> 1] = (byte) (((index & 1) == 0) ?
                                addBlocks[index >> 1] & 0xF0 | (block.getType() >> 8) & 0xF
                                : addBlocks[index >> 1] & 0xF | ((block.getType() >> 8) & 0xF) << 4);
                    }

                    blocks[index] = (byte) block.getType();
                    blockData[index] = (byte) block.getData();

                    // Store TileEntity data
                    if (block instanceof TileEntityBlock) {
                        TileEntityBlock tileEntityBlock = block;

                        // Get the list of key/values from the block
                        CompoundTag rawTag = tileEntityBlock.getNbtData();
                        if (rawTag != null) {
                            Map<String, Tag> values = new HashMap<String, Tag>();
                            for (Entry<String, Tag> entry : rawTag.getValue().entrySet()) {
                                values.put(entry.getKey(), entry.getValue());
                            }
                            
                            values.put("id", new StringTag("id", tileEntityBlock.getNbtId()));
                            values.put("x", new IntTag("x", x));
                            values.put("y", new IntTag("y", y));
                            values.put("z", new IntTag("z", z));
                            
                            CompoundTag tileEntityTag = new CompoundTag("TileEntity", values);
                            tileEntities.add(tileEntityTag);
                        }
                    }
                }
            }
        }

        schematic.put("Blocks", new ByteArrayTag("Blocks", blocks));
        schematic.put("Data", new ByteArrayTag("Data", blockData));
        schematic.put("Entities", new ListTag("Entities", CompoundTag.class, new ArrayList<Tag>()));
        schematic.put("TileEntities", new ListTag("TileEntities", CompoundTag.class, tileEntities));
        if (addBlocks != null) {
            schematic.put("AddBlocks", new ByteArrayTag("AddBlocks", addBlocks));
        }

        // Build and output
        CompoundTag schematicTag = new CompoundTag("Schematic", schematic);
        NBTOutputStream stream = new NBTOutputStream(new FileOutputStream(file));
        stream.writeTag(schematicTag);
        stream.close();
    }

    @Override
    public boolean isOfFormat(File file) {
        DataInputStream str = null;
        try {
            str = new DataInputStream(new GZIPInputStream(new FileInputStream(file)));
            if ((str.readByte() & 0xFF) != NBTConstants.TYPE_COMPOUND) {
                return false;
            }
            byte[] nameBytes = new byte[str.readShort() & 0xFFFF];
            str.readFully(nameBytes);
            String name = new String(nameBytes, NBTConstants.CHARSET);
            return name.equals("Schematic");
        } catch (IOException e) {
            return false;
        } finally {
            if (str != null) {
                try {
                    str.close();
                } catch (IOException ignore) {
                    // blargh
                }
            }
        }
    }

    /**
     * Get child tag of a NBT structure.
     *
     * @param items The parent tag map
     * @param key The name of the tag to get
     * @param expected The expected type of the tag
     * @return child tag casted to the expected type
     * @throws DataException if the tag does not exist or the tag is not of the expected type
     */
    private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key,
                                                 Class<T> expected) throws DataException {

        if (!items.containsKey(key)) {
            throw new DataException("Schematic file is missing a \"" + key + "\" tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new DataException(
                    key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }
}