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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.type.BlockStateHolder;
import com.sk89q.worldedit.blocks.type.BlockTypes;
import com.sk89q.worldedit.command.ClipboardCommands;
import com.sk89q.worldedit.command.SchematicCommands;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private BlockStateHolder[][][] data;
    private Vector offset;
    private Vector origin;
    private Vector size;
    private List<CopiedEntity> entities = new ArrayList<>();

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
                    final BlockStateHolder block = data[x][y][z];
                    if (block == null) {
                        continue;
                    }

                    if (noAir && block.getBlockType() == BlockTypes.AIR) {
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
    public Entity[] pasteEntities(Vector newOrigin) {
        Entity[] entities = new Entity[this.entities.size()];
        for (int i = 0; i < this.entities.size(); ++i) {
            CopiedEntity copied = this.entities.get(i);
            if (copied.entity.getExtent().createEntity(
                    copied.entity.getLocation().setPosition(copied.relativePosition.add(newOrigin)),
                    copied.entity.getState()
            ) != null) {
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
    public void storeEntity(Entity entity) {
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
    public BlockStateHolder getPoint(Vector position) throws ArrayIndexOutOfBoundsException {
        final BlockStateHolder block = getBlock(position);
        if (block == null) {
            return BlockTypes.AIR.getDefaultState();
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
    public BlockStateHolder getBlock(Vector position) throws ArrayIndexOutOfBoundsException {
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
     * Stores a copied entity.
     */
    private class CopiedEntity {
        private final Entity entity;
        private final Vector relativePosition;

        private CopiedEntity(Entity entity) {
            this.entity = entity;
            this.relativePosition = entity.getLocation().toVector().subtract(getOrigin());
        }
    }

}
