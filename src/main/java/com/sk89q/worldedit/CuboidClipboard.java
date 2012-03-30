// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.worldedit;


import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.data.*;
import com.sk89q.worldedit.schematic.SchematicFormat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The clipboard remembers the state of a cuboid region.
 *
 * @author sk89q
 */
public class CuboidClipboard {
    /**
     * Flip direction.
     */
    public enum FlipDirection {
        NORTH_SOUTH,
        WEST_EAST,
        UP_DOWN
    }

    private BaseBlock[][][] data;
    private Vector offset;
    private Vector origin;
    private Vector size;
    private List<CopiedEntity> entities = new ArrayList<CopiedEntity>();

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
     * @param offset
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
        boolean reverse = angle < 0;
        int numRotations = Math.abs((int) Math.floor(angle / 90.0));

        int width = getWidth();
        int length = getLength();
        int height = getHeight();
        Vector sizeRotated = size.transform2D(angle, 0, 0, 0, 0);
        int shiftX = sizeRotated.getX() < 0 ? -sizeRotated.getBlockX() - 1 : 0;
        int shiftZ = sizeRotated.getZ() < 0 ? -sizeRotated.getBlockZ() - 1 : 0;

        BaseBlock newData[][][] = new BaseBlock
                [Math.abs(sizeRotated.getBlockX())]
                [Math.abs(sizeRotated.getBlockY())]
                [Math.abs(sizeRotated.getBlockZ())];

        for (int x = 0; x < width; ++x) {
            for (int z = 0; z < length; ++z) {
                Vector v = (new Vector(x, 0, z)).transform2D(angle, 0, 0, 0, 0);
                int newX = v.getBlockX();
                int newZ = v.getBlockZ();
                for (int y = 0; y < height; ++y) {
                    BaseBlock block = data[x][y][z];
                    newData[shiftX + newX][y][shiftZ + newZ] = block;

                    if (reverse) {
                        for (int i = 0; i < numRotations; ++i) {
                            block.rotate90Reverse();
                        }
                    } else {
                        for (int i = 0; i < numRotations; ++i) {
                            block.rotate90();
                        }
                    }
                }
            }
        }

        data = newData;
        size = new Vector(Math.abs(sizeRotated.getBlockX()),
                          Math.abs(sizeRotated.getBlockY()),
                          Math.abs(sizeRotated.getBlockZ()));
        offset = offset.transform2D(angle, 0, 0, 0, 0)
                .subtract(shiftX, 0, shiftZ);
    }

    /**
     * Flip the clipboard.
     *
     * @param dir direction to flip
     */
    public void flip(FlipDirection dir) {
        flip(dir, false);
    }

    /**
     * Flip the clipboard.
     *
     * @param dir direction to flip
     * @param aroundPlayer flip the offset around the player
     */
    public void flip(FlipDirection dir, boolean aroundPlayer) {
        final int width = getWidth();
        final int length = getLength();
        final int height = getHeight();

        switch (dir) {
        case NORTH_SOUTH:
            final int wid = (int) Math.ceil(width / 2.0f);
            for (int xs = 0; xs < wid; ++xs) {
                for (int z = 0; z < length; ++z) {
                    for (int y = 0; y < height; ++y) {
                        BaseBlock old = data[xs][y][z].flip(dir);
                        if (xs == width - xs - 1) continue;
                        data[xs][y][z] = data[width - xs - 1][y][z].flip(dir);
                        data[width - xs - 1][y][z] = old;
                    }
                }
            }

            if (aroundPlayer) {
                offset = offset.setX(1 - offset.getX() - width);
            }

            break;

        case WEST_EAST:
            final int len = (int) Math.ceil(length / 2.0f);
            for (int zs = 0; zs < len; ++zs) {
                for (int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        BaseBlock old = data[x][y][zs].flip(dir);
                        if (zs == length - zs - 1) continue;
                        data[x][y][zs] = data[x][y][length - zs - 1].flip(dir);
                        data[x][y][length - zs - 1] = old;
                    }
                }
            }

            if (aroundPlayer) {
                offset = offset.setZ(1 - offset.getZ() - length);
            }

            break;

        case UP_DOWN:
            final int hei = (int) Math.ceil(height / 2.0f);
            for (int ys = 0; ys < hei; ++ys) {
                for (int x = 0; x < width; ++x) {
                    for (int z = 0; z < length; ++z) {
                        BaseBlock old = data[x][ys][z].flip(dir);
                        if (ys == height - ys - 1) continue;
                        data[x][ys][z] = data[x][height - ys - 1][z].flip(dir);
                        data[x][height - ys - 1][z] = old;
                    }
                }
            }

            if (aroundPlayer) {
                offset = offset.setY(1 - offset.getY() - height);
            }

            break;
        }
    }

    /**
     * Copy to the clipboard.
     *
     * @param editSession
     */
    public void copy(EditSession editSession) {
        for (int x = 0; x < size.getBlockX(); ++x) {
            for (int y = 0; y < size.getBlockY(); ++y) {
                for (int z = 0; z < size.getBlockZ(); ++z) {
                    data[x][y][z] =
                            editSession.getBlock(new Vector(x, y, z).add(getOrigin()));
                }
            }
        }
    }

    public void paste(EditSession editSession, Vector newOrigin, boolean noAir)
            throws MaxChangedBlocksException {
        paste(editSession, newOrigin, noAir, false);
    }

    /**
     * Paste from the clipboard.
     *
     * @param editSession
     * @param newOrigin Position to paste it from
     * @param noAir True to not paste air
     * @throws MaxChangedBlocksException
     */
    public void paste(EditSession editSession, Vector newOrigin, boolean noAir, boolean entities)
            throws MaxChangedBlocksException {
        place(editSession, newOrigin.add(offset), noAir);
        if (entities) {
            pasteEntities(newOrigin.add(offset));
        }
    }

    /**
     * Places the blocks in a position from the minimum corner.
     *
     * @param editSession
     * @param pos
     * @param noAir
     * @throws MaxChangedBlocksException
     */
    public void place(EditSession editSession, Vector pos, boolean noAir) throws MaxChangedBlocksException {
        for (int x = 0; x < size.getBlockX(); ++x) {
            for (int y = 0; y < size.getBlockY(); ++y) {
                for (int z = 0; z < size.getBlockZ(); ++z) {
                    if (noAir && data[x][y][z].isAir()) {
                        continue;
                    }

                    editSession.setBlock(new Vector(x, y, z).add(pos), data[x][y][z]);
                }
            }
        }
    }

    public LocalEntity[] pasteEntities(Vector pos) {
        LocalEntity[] entities = new LocalEntity[this.entities.size()];
        for (int i = 0; i < this.entities.size(); ++i) {
            CopiedEntity copied = this.entities.get(i);
            if (copied.entity.spawn(copied.entity.getPosition().setPosition(copied.relativePosition.add(pos)))) {
                entities[i] = copied.entity;
            }
        }
        return entities;
    }

    public void storeEntity(LocalEntity entity) {
        this.entities.add(new CopiedEntity(entity));
    }

    /**
     * Get one point in the copy. The point is relative to the origin
     * of the copy (0, 0, 0) and not to the actual copy origin.
     *
     * @param pos
     * @return null
     * @throws ArrayIndexOutOfBoundsException
     */
    public BaseBlock getPoint(Vector pos) throws ArrayIndexOutOfBoundsException {
        return data[pos.getBlockX()][pos.getBlockY()][pos.getBlockZ()];
    }

    /**
     * Get one point in the copy. The point is relative to the origin
     * of the copy (0, 0, 0) and not to the actual copy origin.
     *
     * @param pos
     * @return null
     * @throws ArrayIndexOutOfBoundsException
     */
    public void setBlock(Vector pt, BaseBlock block) {
        data[pt.getBlockX()][pt.getBlockY()][pt.getBlockZ()] = block;
    }

    /**
     * Get the size of the copy.
     *
     * @return
     */
    public Vector getSize() {
        return size;
    }

    /**
     * Saves the clipboard data to a .schematic-format file.
     *
     * @param path
     * @throws IOException
     * @throws DataException
     */
    @Deprecated
    public void saveSchematic(File path) throws IOException, DataException {
        SchematicFormat.MCEDIT.save(this, path);
    }

    /**
     * Load a .schematic file into a clipboard.
     *
     * @param path
     * @return clipboard
     * @throws DataException
     * @throws IOException
     */
    @Deprecated
    public static CuboidClipboard loadSchematic(File path)
            throws DataException, IOException {
        return SchematicFormat.MCEDIT.load(path);
    }

    /**
     * @return the origin
     */
    public Vector getOrigin() {
        return origin;
    }

    /**
     * @param origin the origin to set
     */
    public void setOrigin(Vector origin) {
        this.origin = origin;
    }

    /**
     * @return the offset
     */
    public Vector getOffset() {
        return offset;
    }

    /**
     * @param offset
     */
    public void setOffset(Vector offset) {
        this.offset = offset;
    }

    private class CopiedEntity {
        private final LocalEntity entity;
        private final Vector relativePosition;

        public CopiedEntity(LocalEntity entity) {
            this.entity = entity;
            this.relativePosition = entity.getPosition().getPosition().subtract(getOrigin());
        }
    }
}
