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

import org.jnbt.*;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import com.sk89q.worldedit.*;

/**
 * The clipboard remembers the state of a cuboid region.
 *
 * @author sk89q
 */
public class CuboidClipboard {
    private BaseBlock[][][] data;
    private Vector offset;
    private Vector origin;
    private Vector size;

    /**
     * Constructs the clipboard.
     *
     * @param size
     */
    public CuboidClipboard(Vector size) {
        this.size = size;
        data = new BaseBlock[size.getBlockX()][size.getBlockY()][size.getBlockZ()];
        origin = new Vector();
        offset = new Vector();
    }

    /**
     * Constructs the clipboard.
     *
     * @param size
     * @param origin
     */
    public CuboidClipboard(Vector size, Vector origin) {
        this.size = size;
        data = new BaseBlock[size.getBlockX()][size.getBlockY()][size.getBlockZ()];
        this.origin = origin;
        offset = new Vector();
    }

    /**
     * Constructs the clipboard.
     *
     * @param size
     * @param origin
     */
    public CuboidClipboard(Vector size, Vector origin, Vector offset) {
        this.size = size;
        data = new BaseBlock[size.getBlockX()][size.getBlockY()][size.getBlockZ()];
        this.origin = origin;
        this.offset = offset;
    }

    /**
     * Get the width (X-direction) of the clipboard.
     *
     * @return width
     */
    public int getWidth() {
        return size.getBlockX();
    }

    /**
     * Get the length (Z-direction) of the clipboard.
     *
     * @return length
     */
    public int getLength() {
        return size.getBlockZ();
    }

    /**
     * Get the height (Y-direction) of the clipboard.
     *
     * @return height
     */
    public int getHeight() {
        return size.getBlockY();
    }

    /**
     * Rotate the clipboard in 2D. It can only rotate by angles divisible by 90.
     * 
     * @param angle in degrees
     */
    public void rotate2D(int angle) {
        angle = angle % 360;
        if (angle % 90 != 0) { // Can only rotate 90 degrees at the moment
            return;
        }

        int width = getWidth();
        int length = getLength();
        int height = getHeight();
        int newWidth = angle % 180 == 0 ? width : length;
        int newLength = angle % 180 == 0 ? length : width;
        Vector sizeRotated = size.transform2D(angle, 0, 0, 0, 0);
        int shiftX = sizeRotated.getX() < 0 ? newWidth - 1 : 0;
        int shiftZ = sizeRotated.getZ() < 0 ? newLength - 1: 0;

        BaseBlock newData[][][] = new BaseBlock[newWidth][getHeight()][newLength];

        for (int x = 0; x < width; x++) {
            for (int z = 0; z < length; z++) {
                int newX = (new Vector(x, 0, z)).transform2D(angle, 0, 0, 0, 0)
                        .getBlockX();
                int newZ = (new Vector(x, 0, z)).transform2D(angle, 0, 0, 0, 0)
                        .getBlockZ();
                for (int y = 0; y < height; y++) {
                    newData[shiftX + newX][y][shiftZ + newZ] = data[x][y][z];
                }
            }
        }

        data = newData;
        size = new Vector(newWidth, getHeight(), newLength);
        offset = offset.transform2D(angle, 0, 0, 0, 0)
                .subtract(shiftX, 0, shiftZ);
    }

    /**
     * Copy to the clipboard.
     *
     * @param editSession
     */
    public void copy(EditSession editSession) {
        for (int x = 0; x < size.getBlockX(); x++) {
            for (int y = 0; y < size.getBlockY(); y++) {
                for (int z = 0; z < size.getBlockZ(); z++) {
                    data[x][y][z] =
                        editSession.getBlock(new Vector(x, y, z).add(origin));
                }
            }
        }
    }

    /**
     * Paste from the clipboard.
     *
     * @param editSession
     * @param newOrigin Position to paste it from
     * @param noAir True to not paste air
     * @throws MaxChangedBlocksException
     */
    public void paste(EditSession editSession, Vector newOrigin, boolean noAir)
            throws MaxChangedBlocksException {
        place(editSession, newOrigin.add(offset), noAir);
    }

    /**
     * Places the blocks in a position from the minimum corner.
     * 
     * @param editSession
     * @param pos
     * @param noAir
     * @throws MaxChangedBlocksException
     */
    public void place(EditSession editSession, Vector pos, boolean noAir)
            throws MaxChangedBlocksException {
        for (int x = 0; x < size.getBlockX(); x++) {
            for (int y = 0; y < size.getBlockY(); y++) {
                for (int z = 0; z < size.getBlockZ(); z++) {
                    if (noAir && data[x][y][z].isAir())
                        continue;
                    
                    editSession.setBlock(new Vector(x, y, z).add(pos),
                            data[x][y][z]);
                }
            }
        }
    }

    /**
     * Saves the clipboard data to a .schematic-format file.
     *
     * @param path
     * @throws IOException
     * @throws SchematicException
     */
    public void saveSchematic(String path) throws IOException, SchematicException {
        int width = getWidth();
        int height = getHeight();
        int length = getLength();

        if (width > 65535) {
            throw new SchematicException("Width of region too large for a .schematic");
        }
        if (height > 65535) {
            throw new SchematicException("Height of region too large for a .schematic");
        }
        if (length > 65535) {
            throw new SchematicException("Length of region too large for a .schematic");
        }

        HashMap<String,Tag> schematic = new HashMap<String,Tag>();
        schematic.put("Width", new ShortTag("Width", (short)width));
        schematic.put("Length", new ShortTag("Length", (short)length));
        schematic.put("Height", new ShortTag("Height", (short)height));
        schematic.put("Materials", new StringTag("Materials", "Alpha"));

        // Copy blocks
        byte[] blocks = new byte[width * height * length];
        byte[] blockData = new byte[width * height * length];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int index = y * width * length + z * width + x;
                    blocks[index] = (byte)data[x][y][z].getType();
                    blockData[index] = (byte)data[x][y][z].getData();
                }
            }
        }
        schematic.put("Blocks", new ByteArrayTag("Blocks", blocks));
        schematic.put("Data", new ByteArrayTag("Data", blockData));

        // These are not stored either
        schematic.put("Entities", new ListTag("Entities", CompoundTag.class, new ArrayList<Tag>()));
        schematic.put("TileEntities", new ListTag("TileEntities", CompoundTag.class, new ArrayList<Tag>()));

        // Build and output
        CompoundTag schematicTag = new CompoundTag("Schematic", schematic);
        NBTOutputStream stream = new NBTOutputStream(new FileOutputStream(path));
        stream.writeTag(schematicTag);
        stream.close();
    }

    /**
     * Load a .schematic file into a clipboard.
     * 
     * @param path
     * @param origin
     * @return clipboard
     * @throws SchematicException
     * @throws IOException
     */
    public static CuboidClipboard loadSchematic(String path)
            throws SchematicException, IOException {
        FileInputStream stream = new FileInputStream(path);
        NBTInputStream nbtStream = new NBTInputStream(stream);
        CompoundTag schematicTag = (CompoundTag)nbtStream.readTag();
        if (!schematicTag.getName().equals("Schematic")) {
            throw new SchematicException("Tag \"Schematic\" does not exist or is not first");
        }
        Map<String,Tag> schematic = schematicTag.getValue();
        if (!schematic.containsKey("Blocks")) {
            throw new SchematicException("Schematic file is missing a \"Blocks\" tag");
        }
        short width = (Short)getChildTag(schematic, "Width", ShortTag.class).getValue();
        short length = (Short)getChildTag(schematic, "Length", ShortTag.class).getValue();
        short height = (Short)getChildTag(schematic, "Height", ShortTag.class).getValue();
        String materials = (String)getChildTag(schematic, "Materials", StringTag.class).getValue();
        if (!materials.equals("Alpha")) {
            throw new SchematicException("Schematic file is not an Alpha schematic");
        }
        byte[] blocks = (byte[])getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
        byte[] blockData = (byte[])getChildTag(schematic, "Data", ByteArrayTag.class).getValue();

        Vector size = new Vector(width, height, length);

        CuboidClipboard clipboard = new CuboidClipboard(size);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int index = y * width * length + z * width + x;
                    clipboard.data[x][y][z] =
                            new BaseBlock(blocks[index], blockData[index]);
                }
            }
        }

        return clipboard;
    }

    /**
     * Get child tag of a NBT structure.
     * 
     * @param items
     * @param key
     * @param expected
     * @return child tag
     * @throws SchematicException
     */
    private static Tag getChildTag(Map<String,Tag> items, String key, Class expected)
            throws SchematicException {
        if (!items.containsKey(key)) {
            throw new SchematicException("Schematic file is missing a \"" + key + "\" tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new SchematicException(
                key + " tag is not of tag type " + expected.getName());
        }
        return tag;
    }
}
