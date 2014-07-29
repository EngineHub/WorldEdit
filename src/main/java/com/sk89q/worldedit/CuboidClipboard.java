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

package com.sk89q.worldedit;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.command.ClipboardCommands;
import com.sk89q.worldedit.command.SchematicCommands;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.world.DataException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The clipboard remembers the state of a cuboid region.
 *
 * @deprecated This is slowly being replaced with {@link Clipboard}, which is
 *             far more versatile. Transforms are supported using affine
 *             transformations and full entity support is provided because
 *             the clipboard properly implements {@link Extent}. However,
 *             the new clipboard class is only available in WorldEdit 6.x and
 *             beyond. We intend on keeping this deprecated class in WorldEdit
 *             for an extended amount of time so there is no rush to
 *             switch (but new features will not be supported). To copy between
 *             a clipboard and a world (or between any two {@code Extent}s),
 *             one can use {@link ForwardExtentCopy}. See
 *             {@link ClipboardCommands} and {@link SchematicCommands} for
 *             more information.
 */
@Deprecated
public class CuboidClipboard {

    /**
     * An enum of possible flip directions.
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
     * @param size the dimensions of the clipboard (should be at least 1 on every dimension)
     */
    public CuboidClipboard(Vector size) {
        checkNotNull(size);

        this.size = size;
        data = new BaseBlock[size.getBlockX()][size.getBlockY()][size.getBlockZ()];
        origin = new Vector();
        offset = new Vector();
    }

    /**
     * Constructs the clipboard.
     *
     * @param size the dimensions of the clipboard (should be at least 1 on every dimension)
     * @param origin the origin point where the copy was made, which must be the
     *               {@link CuboidRegion#getMinimumPoint()} relative to the copy
     */
    public CuboidClipboard(Vector size, Vector origin) {
        checkNotNull(size);
        checkNotNull(origin);

        this.size = size;
        data = new BaseBlock[size.getBlockX()][size.getBlockY()][size.getBlockZ()];
        this.origin = origin;
        offset = new Vector();
    }

    /**
     * Constructs the clipboard.
     *
     * @param size the dimensions of the clipboard (should be at least 1 on every dimension)
     * @param origin the origin point where the copy was made, which must be the
     *               {@link CuboidRegion#getMinimumPoint()} relative to the copy
     * @param offset the offset from the minimum point of the copy where the user was
     */
    public CuboidClipboard(Vector size, Vector origin, Vector offset) {
        checkNotNull(size);
        checkNotNull(origin);
        checkNotNull(offset);

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
    @SuppressWarnings("deprecation")
    public void rotate2D(int angle) {
        angle = angle % 360;
        if (angle % 90 != 0) { // Can only rotate 90 degrees at the moment
            return;
        }
        final boolean reverse = angle < 0;
        final int numRotations = Math.abs((int) Math.floor(angle / 90.0));

        final int width = getWidth();
        final int length = getLength();
        final int height = getHeight();
        final Vector sizeRotated = size.transform2D(angle, 0, 0, 0, 0);
        final int shiftX = sizeRotated.getX() < 0 ? -sizeRotated.getBlockX() - 1 : 0;
        final int shiftZ = sizeRotated.getZ() < 0 ? -sizeRotated.getBlockZ() - 1 : 0;

        final BaseBlock[][][] newData = new BaseBlock
                [Math.abs(sizeRotated.getBlockX())]
                [Math.abs(sizeRotated.getBlockY())]
                [Math.abs(sizeRotated.getBlockZ())];

        for (int x = 0; x < width; ++x) {
            for (int z = 0; z < length; ++z) {
                final Vector2D v = new Vector2D(x, z).transform2D(angle, 0, 0, shiftX, shiftZ);
                final int newX = v.getBlockX();
                final int newZ = v.getBlockZ();
                for (int y = 0; y < height; ++y) {
                    final BaseBlock block = data[x][y][z];
                    newData[newX][y][newZ] = block;

                    if (block == null) {
                        continue;
                    }

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
    @SuppressWarnings("deprecation")
    public void flip(FlipDirection dir, boolean aroundPlayer) {
        checkNotNull(dir);

        final int width = getWidth();
        final int length = getLength();
        final int height = getHeight();

        switch (dir) {
        case WEST_EAST:
            final int wid = (int) Math.ceil(width / 2.0f);
            for (int xs = 0; xs < wid; ++xs) {
                for (int z = 0; z < length; ++z) {
                    for (int y = 0; y < height; ++y) {
                        final BaseBlock block1 = data[xs][y][z];
                        if (block1 != null) {
                            block1.flip(dir);
                        }

                        // Skip the center plane
                        if (xs == width - xs - 1) {
                            continue;
                        }

                        final BaseBlock block2 = data[width - xs - 1][y][z];
                        if (block2 != null) {
                            block2.flip(dir);
                        }

                        data[xs][y][z] = block2;
                        data[width - xs - 1][y][z] = block1;
                    }
                }
            }

            if (aroundPlayer) {
                offset = offset.setX(1 - offset.getX() - width);
            }

            break;

        case NORTH_SOUTH:
            final int len = (int) Math.ceil(length / 2.0f);
            for (int zs = 0; zs < len; ++zs) {
                for (int x = 0; x < width; ++x) {
                    for (int y = 0; y < height; ++y) {
                        final BaseBlock block1 = data[x][y][zs];
                        if (block1 != null) {
                            block1.flip(dir);
                        }

                        // Skip the center plane
                        if (zs == length - zs - 1) {
                            continue;
                        }

                        final BaseBlock block2 = data[x][y][length - zs - 1];
                        if (block2 != null) {
                            block2.flip(dir);
                        }

                        data[x][y][zs] = block2;
                        data[x][y][length - zs - 1] = block1;
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
                        final BaseBlock block1 = data[x][ys][z];
                        if (block1 != null) {
                            block1.flip(dir);
                        }

                        // Skip the center plane
                        if (ys == height - ys - 1) {
                            continue;
                        }

                        final BaseBlock block2 = data[x][height - ys - 1][z];
                        if (block2 != null) {
                            block2.flip(dir);
                        }

                        data[x][ys][z] = block2;
                        data[x][height - ys - 1][z] = block1;
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
     * Copies blocks to the clipboard.
     *
     * @param editSession the EditSession from which to take the blocks
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

    /**
     * Copies blocks to the clipboard.
     *
     * @param editSession The EditSession from which to take the blocks
     * @param region A region that further constrains which blocks to take.
     */
    public void copy(EditSession editSession, Region region) {
        for (int x = 0; x < size.getBlockX(); ++x) {
            for (int y = 0; y < size.getBlockY(); ++y) {
                for (int z = 0; z < size.getBlockZ(); ++z) {
                    final Vector pt = new Vector(x, y, z).add(getOrigin());
                    if (region.contains(pt)) {
                        data[x][y][z] = editSession.getBlock(pt);
                    } else {
                        data[x][y][z] = null;
                    }
                }
            }
        }
    }

    /**
     * Paste the clipboard at the given location using the given {@code EditSession}.
     *
     * <p>This method blocks the server/game until the entire clipboard is
     * pasted. In the future, {@link ForwardExtentCopy} will be recommended,
     * which, if combined with the proposed operation scheduler framework,
     * will not freeze the game/server.</p>
     *
     * @param editSession the EditSession to which blocks are to be copied to
     * @param newOrigin the new origin point (must correspond to the minimum point of the cuboid)
     * @param noAir true to not copy air blocks in the source
     * @throws MaxChangedBlocksException thrown if too many blocks were changed
     */
    public void paste(EditSession editSession, Vector newOrigin, boolean noAir) throws MaxChangedBlocksException {
        paste(editSession, newOrigin, noAir, false);
    }

    /**
     * Paste the clipboard at the given location using the given {@code EditSession}.
     *
     * <p>This method blocks the server/game until the entire clipboard is
     * pasted. In the future, {@link ForwardExtentCopy} will be recommended,
     * which, if combined with the proposed operation scheduler framework,
     * will not freeze the game/server.</p>
     *
     * @param editSession the EditSession to which blocks are to be copied to
     * @param newOrigin the new origin point (must correspond to the minimum point of the cuboid)
     * @param noAir true to not copy air blocks in the source
     * @param entities true to copy entities
     * @throws MaxChangedBlocksException thrown if too many blocks were changed
     */
    public void paste(EditSession editSession, Vector newOrigin, boolean noAir, boolean entities) throws MaxChangedBlocksException {
        place(editSession, newOrigin.add(offset), noAir);
        if (entities) {
            pasteEntities(newOrigin.add(offset));
        }
    }

    /**
     * Paste the clipboard at the given location using the given {@code EditSession}.
     *
     * <p>This method blocks the server/game until the entire clipboard is
     * pasted. In the future, {@link ForwardExtentCopy} will be recommended,
     * which, if combined with the proposed operation scheduler framework,
     * will not freeze the game/server.</p>
     *
     * @param editSession the EditSession to which blocks are to be copied to
     * @param newOrigin the new origin point (must correspond to the minimum point of the cuboid)
     * @param noAir true to not copy air blocks in the source
     * @throws MaxChangedBlocksException thrown if too many blocks were changed
     */
    public void place(EditSession editSession, Vector newOrigin, boolean noAir) throws MaxChangedBlocksException {
        for (int x = 0; x < size.getBlockX(); ++x) {
            for (int y = 0; y < size.getBlockY(); ++y) {
                for (int z = 0; z < size.getBlockZ(); ++z) {
                    final BaseBlock block = data[x][y][z];
                    if (block == null) {
                        continue;
                    }

                    if (noAir && block.isAir()) {
                        continue;
                    }

                    editSession.setBlock(new Vector(x, y, z).add(newOrigin), block);
                }
            }
        }
    }

    /**
     * Paste the stored entities to the given position.
     *
     * @param newOrigin the new origin
     * @return a list of entities that were pasted
     */
    public LocalEntity[] pasteEntities(Vector newOrigin) {
        LocalEntity[] entities = new LocalEntity[this.entities.size()];
        for (int i = 0; i < this.entities.size(); ++i) {
            CopiedEntity copied = this.entities.get(i);
            if (copied.entity.spawn(copied.entity.getPosition().setPosition(copied.relativePosition.add(newOrigin)))) {
                entities[i] = copied.entity;
            }
        }
        return entities;
    }

    /**
     * Store an entity.
     *
     * @param entity the entity
     */
    public void storeEntity(LocalEntity entity) {
        this.entities.add(new CopiedEntity(entity));
    }

    /**
     * Get the block at the given position.
     *
     * <p>If the position is out of bounds, air will be returned.</p>
     *
     * @param position the point, relative to the origin of the copy (0, 0, 0) and not to the actual copy origin
     * @return air, if this block was outside the (non-cuboid) selection while copying
     * @throws ArrayIndexOutOfBoundsException if the position is outside the bounds of the CuboidClipboard
     * @deprecated use {@link #getBlock(Vector)} instead
     */
    @Deprecated
    public BaseBlock getPoint(Vector position) throws ArrayIndexOutOfBoundsException {
        final BaseBlock block = getBlock(position);
        if (block == null) {
            return new BaseBlock(BlockID.AIR);
        }

        return block;
    }

    /**
     * Get the block at the given position.
     *
     * <p>If the position is out of bounds, air will be returned.</p>
     *
     * @param position the point, relative to the origin of the copy (0, 0, 0) and not to the actual copy origin
     * @return null, if this block was outside the (non-cuboid) selection while copying
     * @throws ArrayIndexOutOfBoundsException if the position is outside the bounds of the CuboidClipboard
     */
    public BaseBlock getBlock(Vector position) throws ArrayIndexOutOfBoundsException {
        return data[position.getBlockX()][position.getBlockY()][position.getBlockZ()];
    }

    /**
     * Set the block at a position in the clipboard.
     *
     * @param position the point, relative to the origin of the copy (0, 0, 0) and not to the actual copy origin.
     * @param block the block to set
     * @throws ArrayIndexOutOfBoundsException if the position is outside the bounds of the CuboidClipboard
     */
    public void setBlock(Vector position, BaseBlock block) {
        data[position.getBlockX()][position.getBlockY()][position.getBlockZ()] = block;
    }

    /**
     * Get the dimensions of the clipboard.
     *
     * @return the dimensions, where (1, 1, 1) is 1 wide, 1 across, 1 deep
     */
    public Vector getSize() {
        return size;
    }

    /**
     * Saves the clipboard data to a .schematic-format file.
     *
     * @param path the path to the file to save
     * @throws IOException thrown on I/O error
     * @throws DataException thrown on error writing the data for other reasons
     * @deprecated use {@link SchematicFormat#MCEDIT}
     */
    @Deprecated
    public void saveSchematic(File path) throws IOException, DataException {
        checkNotNull(path);
        SchematicFormat.MCEDIT.save(this, path);
    }

    /**
     * Load a .schematic file into a clipboard.
     *
     * @param path the path to the file to load
     * @return a clipboard
     * @throws IOException thrown on I/O error
     * @throws DataException thrown on error writing the data for other reasons
     * @deprecated use {@link SchematicFormat#MCEDIT}
     */
    @Deprecated
    public static CuboidClipboard loadSchematic(File path) throws DataException, IOException {
        checkNotNull(path);
        return SchematicFormat.MCEDIT.load(path);
    }

    /**
     * Get the origin point, which corresponds to where the copy was
     * originally copied from. The origin is the lowest possible X, Y, and
     * Z components of the cuboid region that was copied.
     *
     * @return the origin
     */
    public Vector getOrigin() {
        return origin;
    }

    /**
     * Set the origin point, which corresponds to where the copy was
     * originally copied from. The origin is the lowest possible X, Y, and
     * Z components of the cuboid region that was copied.
     *
     * @param origin the origin to set
     */
    public void setOrigin(Vector origin) {
        checkNotNull(origin);
        this.origin = origin;
    }

    /**
     * Get the offset of the player to the clipboard's minimum point
     * (minimum X, Y, Z coordinates).
     *
     * <p>The offset is inverse (multiplied by -1).</p>
     *
     * @return the offset the offset
     */
    public Vector getOffset() {
        return offset;
    }

    /**
     * Set the offset of the player to the clipboard's minimum point
     * (minimum X, Y, Z coordinates).
     *
     * <p>The offset is inverse (multiplied by -1).</p>
     *
     * @param offset the new offset
     */
    public void setOffset(Vector offset) {
        this.offset = offset;
    }

    /**
     * Get the block distribution inside a clipboard.
     *
     * @return a block distribution
     */
    public List<Countable<Integer>> getBlockDistribution() {
        List<Countable<Integer>> distribution = new ArrayList<Countable<Integer>>();
        Map<Integer, Countable<Integer>> map = new HashMap<Integer, Countable<Integer>>();

        int maxX = getWidth();
        int maxY = getHeight();
        int maxZ = getLength();

        for (int x = 0; x < maxX; ++x) {
            for (int y = 0; y < maxY; ++y) {
                for (int z = 0; z < maxZ; ++z) {
                    final BaseBlock block = data[x][y][z];
                    if (block == null) {
                        continue;
                    }

                    int id = block.getId();

                    if (map.containsKey(id)) {
                        map.get(id).increment();
                    } else {
                        Countable<Integer> c = new Countable<Integer>(id, 1);
                        map.put(id, c);
                        distribution.add(c);
                    }
                }
            }
        }

        Collections.sort(distribution);
        // Collections.reverse(distribution);

        return distribution;
    }

    /**
     * Get the block distribution inside a clipboard with data values.
     *
     * @return a block distribution
     */
    // TODO reduce code duplication
    public List<Countable<BaseBlock>> getBlockDistributionWithData() {
        List<Countable<BaseBlock>> distribution = new ArrayList<Countable<BaseBlock>>();
        Map<BaseBlock, Countable<BaseBlock>> map = new HashMap<BaseBlock, Countable<BaseBlock>>();

        int maxX = getWidth();
        int maxY = getHeight();
        int maxZ = getLength();

        for (int x = 0; x < maxX; ++x) {
            for (int y = 0; y < maxY; ++y) {
                for (int z = 0; z < maxZ; ++z) {
                    final BaseBlock block = data[x][y][z];
                    if (block == null) {
                        continue;
                    }

                    // Strip the block from metadata that is not part of our key
                    final BaseBlock bareBlock = new BaseBlock(block.getId(), block.getData());

                    if (map.containsKey(bareBlock)) {
                        map.get(bareBlock).increment();
                    } else {
                        Countable<BaseBlock> c = new Countable<BaseBlock>(bareBlock, 1);
                        map.put(bareBlock, c);
                        distribution.add(c);
                    }
                }
            }
        }

        Collections.sort(distribution);
        // Collections.reverse(distribution);

        return distribution;
    }

    /**
     * Stores a copied entity.
     */
    private class CopiedEntity {
        private final LocalEntity entity;
        private final Vector relativePosition;

        private CopiedEntity(LocalEntity entity) {
            this.entity = entity;
            this.relativePosition = entity.getPosition().getPosition().subtract(getOrigin());
        }
    }

}
