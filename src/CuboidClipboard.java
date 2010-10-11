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
 *
 * @author Albert
 */
public class CuboidClipboard {
    private int[][][] data;
    private Point min;
    private Point max;
    private Point origin;

    /**
     * Constructs the region instance. The minimum and maximum points must be
     * the respective minimum and maximum numbers!
     * 
     * @param min
     * @param max
     * @param origin
     */
    public CuboidClipboard(Point min, Point max, Point origin) {
        this.min = min;
        this.max = max;
        this.origin = origin;
        data = new int[(int)((max.getX()) - min.getX() + 1)]
            [(int)(max.getY() - min.getY() + 1)]
            [(int)(max.getZ() - min.getZ() + 1)];
    }

    /**
     * Get the width (X-direction) of the clipboard.
     *
     * @return
     */
    public int getWidth() {
        return (int)(max.getX() - min.getX() + 1);
    }

    /**
     * Get the length (Z-direction) of the clipboard.
     *
     * @return
     */
    public int getLength() {
        return (int)(max.getZ() - min.getZ() + 1);
    }

    /**
     * Get the height (Y-direction) of the clipboard.
     *
     * @return
     */
    public int getHeight() {
        return (int)(max.getY() - min.getY() + 1);
    }

    /**
     * Copy to the clipboard.
     *
     * @param editSession
     */
    public void copy(EditSession editSession) {
        for (int x = (int)min.getX(); x <= (int)max.getX(); x++) {
            for (int y = (int)min.getY(); y <= (int)max.getY(); y++) {
                for (int z = (int)min.getZ(); z <= (int)max.getZ(); z++) {
                    data[x - (int)min.getX()][y - (int)min.getY()][z - (int)min.getZ()] =
                        editSession.getBlock(x, y, z);
                }
            }
        }
    }

    /**
     * Paste from the clipboard.
     *
     * @param editSession
     * @param origin Position to paste it from
     * @param noAir True to not paste air
     * @throws MaxChangedBlocksException
     */
    public void paste(EditSession editSession, Point newOrigin, boolean noAir)
            throws MaxChangedBlocksException {
        int offsetX = (int)(min.getX() - origin.getX() + newOrigin.getX());
        int offsetY = (int)(min.getY() - origin.getY() + newOrigin.getY());
        int offsetZ = (int)(min.getZ() - origin.getZ() + newOrigin.getZ());

        place(editSession, offsetX, offsetY, offsetZ, noAir);
    }

    /**
     * Places the blocks in a position from the minimum corner.
     * 
     * @param editSession
     * @param offsetX
     * @param offsetY
     * @param offsetZ
     * @param noAir
     * @throws MaxChangedBlocksException
     */
    public void place(EditSession editSession, int offsetX,
            int offsetY, int offsetZ, boolean noAir)
            throws MaxChangedBlocksException {
        int xs = getWidth();
        int ys = getHeight();
        int zs = getLength();

        for (int x = 0; x < xs; x++) {
            for (int y = 0; y < ys; y++) {
                for (int z = 0; z < zs; z++) {
                    if (noAir && data[x][y][z] == 0) { continue; }

                    editSession.setBlock(x + offsetX, y + offsetY, z + offsetZ,
                                         data[x][y][z]);
                }
            }
        }
    }

    /**
     * Stack the clipboard in a certain direction a certain number of
     * times.
     *
     * @param editSession
     * @param xm
     * @param ym
     * @param zm
     * @short count
     * @param noAir
     * @param moveOrigin move the origin
     * @throws MaxChangedBlocksException
     */
    public void stack(EditSession editSession, int xm, int ym, int zm, short count,
            boolean noAir, boolean moveOrigin) throws MaxChangedBlocksException {
        int xs = getWidth();
        int ys = getHeight();
        int zs = getLength();
        int offsetX = (int)min.getX();
        int offsetY = (int)min.getY();
        int offsetZ = (int)min.getZ();

        for (short i = 1; i <= count; i++) {
            place(editSession, offsetX + xm * xs, offsetY + ym * ys,
                    offsetZ + zm * zs, noAir);
        }

        if (moveOrigin) {
            min = new Point((int)offsetX + xm * count,
                            (int)offsetY + ym * count,
                            (int)offsetZ + zm * count);
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
        int xs = getWidth();
        int ys = getHeight();
        int zs = getLength();

        if (xs > 65535) {
            throw new SchematicException("Width of region too large for a .schematic");
        }
        if (ys > 65535) {
            throw new SchematicException("Height of region too large for a .schematic");
        }
        if (zs > 65535) {
            throw new SchematicException("Length of region too large for a .schematic");
        }

        HashMap<String,Tag> schematic = new HashMap<String,Tag>();
        schematic.put("Width", new ShortTag("Width", (short)xs));
        schematic.put("Length", new ShortTag("Length", (short)zs));
        schematic.put("Height", new ShortTag("Height", (short)ys));
        schematic.put("Materials", new StringTag("Materials", "Alpha"));

        // Copy blocks
        byte[] blocks = new byte[xs * ys * zs];
        for (int x = 0; x < xs; x++) {
            for (int y = 0; y < ys; y++) {
                for (int z = 0; z < zs; z++) {
                    int index = y * xs * zs + z * xs + x;
                    blocks[index] = (byte)data[x][y][z];
                }
            }
        }
        schematic.put("Blocks", new ByteArrayTag("Blocks", blocks));

        // Current data is not supported
        byte[] data = new byte[xs * ys * zs];
        schematic.put("Data", new ByteArrayTag("Data", data));

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
     * @return
     * @throws SchematicException
     * @throws IOException
     */
    public static CuboidClipboard loadSchematic(String path, Point origin)
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
        short xs = (Short)getChildTag(schematic, "Width", ShortTag.class).getValue();
        short zs = (Short)getChildTag(schematic, "Length", ShortTag.class).getValue();
        short ys = (Short)getChildTag(schematic, "Height", ShortTag.class).getValue();
        String materials = (String)getChildTag(schematic, "Materials", StringTag.class).getValue();
        if (!materials.equals("Alpha")) {
            throw new SchematicException("Schematic file is not an Alpha schematic");
        }
        byte[] blocks = (byte[])getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();

        Point min = origin;
        Point max = new Point(
                origin.getX() + xs - 1,
                origin.getY() + ys - 1,
                origin.getZ() + zs - 1
                );
        CuboidClipboard clipboard = new CuboidClipboard(min, max, origin);

        for (int x = 0; x < xs; x++) {
            for (int y = 0; y < ys; y++) {
                for (int z = 0; z < zs; z++) {
                    int index = y * xs * zs + z * xs + x;
                    clipboard.data[x][y][z] = blocks[index];
                }
            }
        }

        return clipboard;
    }

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
