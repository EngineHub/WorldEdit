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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.regions.Regions.asFlatRegion;
import static com.sk89q.worldedit.regions.Regions.maximumBlockY;
import static com.sk89q.worldedit.regions.Regions.minimumBlockY;

import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.ChangeSetExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.MaskingExtent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.extent.buffer.ForgetfulExtentBuffer;
import com.sk89q.worldedit.extent.cache.LastAccessExtentCache;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.extent.inventory.BlockBagExtent;
import com.sk89q.worldedit.extent.reorder.MultiStageReorder;
import com.sk89q.worldedit.extent.validation.BlockChangeLimiter;
import com.sk89q.worldedit.extent.validation.DataValidatorExtent;
import com.sk89q.worldedit.extent.world.BlockQuirkExtent;
import com.sk89q.worldedit.extent.world.ChunkLoadingExtent;
import com.sk89q.worldedit.extent.world.FastModeExtent;
import com.sk89q.worldedit.extent.world.SurvivalModeExtent;
import com.sk89q.worldedit.function.GroundFunction;
import com.sk89q.worldedit.function.RegionMaskingFilter;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.block.Counter;
import com.sk89q.worldedit.function.block.Naturalizer;
import com.sk89q.worldedit.function.generator.GardenPatchGenerator;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.BoundedHeightMask;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.FuzzyBlockMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.MaskUnion;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.mask.NoiseFilter2D;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.function.operation.ChangeSetExecutor;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.OperationQueue;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.util.RegionOffset;
import com.sk89q.worldedit.function.visitor.DownwardVisitor;
import com.sk89q.worldedit.function.visitor.LayerVisitor;
import com.sk89q.worldedit.function.visitor.NonRisingVisitor;
import com.sk89q.worldedit.function.visitor.RecursiveVisitor;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.history.UndoContext;
import com.sk89q.worldedit.history.changeset.BlockOptimizedHistory;
import com.sk89q.worldedit.history.changeset.ChangeSet;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.internal.expression.runtime.RValue;
import com.sk89q.worldedit.math.MathUtils;
import com.sk89q.worldedit.math.interpolation.Interpolation;
import com.sk89q.worldedit.math.interpolation.KochanekBartelsInterpolation;
import com.sk89q.worldedit.math.interpolation.Node;
import com.sk89q.worldedit.math.noise.RandomNoise;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.Regions;
import com.sk89q.worldedit.regions.shape.ArbitraryBiomeShape;
import com.sk89q.worldedit.regions.shape.ArbitraryShape;
import com.sk89q.worldedit.regions.shape.RegionShape;
import com.sk89q.worldedit.regions.shape.WorldEditExpressionEnvironment;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.collection.DoubleArrayList;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.world.NullWorld;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nullable;

/**
 * An {@link Extent} that handles history, {@link BlockBag}s, change limits,
 * block re-ordering, and much more. Most operations in WorldEdit use this class.
 *
 * <p>Most of the actual functionality is implemented with a number of other
 * {@link Extent}s that are chained together. For example, history is logged
 * using the {@link ChangeSetExtent}.</p>
 */
@SuppressWarnings({"FieldCanBeLocal"})
public class EditSession implements Extent {

    private static final Logger log = Logger.getLogger(EditSession.class.getCanonicalName());

    /**
     * Used by {@link #setBlock(Vector, BlockStateHolder, Stage)} to
     * determine which {@link Extent}s should be bypassed.
     */
    public enum Stage {
        BEFORE_HISTORY,
        BEFORE_REORDER,
        BEFORE_CHANGE
    }

    @SuppressWarnings("ProtectedField")
    protected final World world;
    private final ChangeSet changeSet = new BlockOptimizedHistory();

    private @Nullable FastModeExtent fastModeExtent;
    private final SurvivalModeExtent survivalExtent;
    private @Nullable ChunkLoadingExtent chunkLoadingExtent;
    private @Nullable LastAccessExtentCache cacheExtent;
    private @Nullable BlockQuirkExtent quirkExtent;
    private @Nullable DataValidatorExtent validator;
    private final BlockBagExtent blockBagExtent;
    private final MultiStageReorder reorderExtent;
    private @Nullable ChangeSetExtent changeSetExtent;
    private final MaskingExtent maskingExtent;
    private final BlockChangeLimiter changeLimiter;

    private final Extent bypassReorderHistory;
    private final Extent bypassHistory;
    private final Extent bypassNone;

    private Mask oldMask;

    /**
     * Construct the object with a maximum number of blocks and a block bag.
     *
     * @param eventBus the event bus
     * @param world the world
     * @param maxBlocks the maximum number of blocks that can be changed, or -1 to use no limit
     * @param blockBag an optional {@link BlockBag} to use, otherwise null
     * @param event the event to call with the extent
     */
    EditSession(EventBus eventBus, World world, int maxBlocks, @Nullable BlockBag blockBag, EditSessionEvent event) {
        checkNotNull(eventBus);
        checkArgument(maxBlocks >= -1, "maxBlocks >= -1 required");
        checkNotNull(event);

        this.world = world;

        if (world != null) {
            Extent extent;

            // These extents are ALWAYS used
            extent = fastModeExtent = new FastModeExtent(world, false);
            extent = survivalExtent = new SurvivalModeExtent(extent, world);
            extent = quirkExtent = new BlockQuirkExtent(extent, world);
            extent = chunkLoadingExtent = new ChunkLoadingExtent(extent, world);
            extent = cacheExtent = new LastAccessExtentCache(extent);
            extent = wrapExtent(extent, eventBus, event, Stage.BEFORE_CHANGE);
            extent = validator = new DataValidatorExtent(extent, world);
            extent = blockBagExtent = new BlockBagExtent(extent, blockBag);

            // This extent can be skipped by calling rawSetBlock()
            extent = reorderExtent = new MultiStageReorder(extent, false);
            extent = wrapExtent(extent, eventBus, event, Stage.BEFORE_REORDER);

            // These extents can be skipped by calling smartSetBlock()
            extent = changeSetExtent = new ChangeSetExtent(extent, changeSet);
            extent = maskingExtent = new MaskingExtent(extent, Masks.alwaysTrue());
            extent = changeLimiter = new BlockChangeLimiter(extent, maxBlocks);
            extent = wrapExtent(extent, eventBus, event, Stage.BEFORE_HISTORY);

            this.bypassReorderHistory = blockBagExtent;
            this.bypassHistory = reorderExtent;
            this.bypassNone = extent;
        } else {
            Extent extent = new NullExtent();
            extent = survivalExtent = new SurvivalModeExtent(extent, NullWorld.getInstance());
            extent = blockBagExtent = new BlockBagExtent(extent, blockBag);
            extent = reorderExtent = new MultiStageReorder(extent, false);
            extent = maskingExtent = new MaskingExtent(extent, Masks.alwaysTrue());
            extent = changeLimiter = new BlockChangeLimiter(extent, maxBlocks);
            this.bypassReorderHistory = extent;
            this.bypassHistory = extent;
            this.bypassNone = extent;
        }
    }

    private Extent wrapExtent(Extent extent, EventBus eventBus, EditSessionEvent event, Stage stage) {
        event = event.clone(stage);
        event.setExtent(extent);
        eventBus.post(event);
        return event.getExtent();
    }

    /**
     * Get the world.
     *
     * @return the world
     */
    public World getWorld() {
        return world;
    }

    /**
     * Get the underlying {@link ChangeSet}.
     *
     * @return the change set
     */
    public ChangeSet getChangeSet() {
        return changeSet;
    }

    /**
     * Get the maximum number of blocks that can be changed. -1 will be returned
     * if it the limit disabled.
     *
     * @return the limit (&gt;= 0) or -1 for no limit
     */
    public int getBlockChangeLimit() {
        return changeLimiter.getLimit();
    }

    /**
     * Set the maximum number of blocks that can be changed.
     *
     * @param limit the limit (&gt;= 0) or -1 for no limit
     */
    public void setBlockChangeLimit(int limit) {
        changeLimiter.setLimit(limit);
    }

    /**
     * Returns queue status.
     *
     * @return whether the queue is enabled
     */
    public boolean isQueueEnabled() {
        return reorderExtent.isEnabled();
    }

    /**
     * Queue certain types of block for better reproduction of those blocks.
     */
    public void enableQueue() {
        reorderExtent.setEnabled(true);
    }

    /**
     * Disable the queue. This will flush the queue.
     */
    public void disableQueue() {
        if (isQueueEnabled()) {
            flushQueue();
        }
        reorderExtent.setEnabled(true);
    }

    /**
     * Get the mask.
     *
     * @return mask, may be null
     */
    public Mask getMask() {
        return oldMask;
    }

    /**
     * Set a mask.
     *
     * @param mask mask or null
     */
    public void setMask(Mask mask) {
        this.oldMask = mask;
        if (mask == null) {
            maskingExtent.setMask(Masks.alwaysTrue());
        } else {
            maskingExtent.setMask(mask);
        }
    }

    /**
     * Get the {@link SurvivalModeExtent}.
     *
     * @return the survival simulation extent
     */
    public SurvivalModeExtent getSurvivalExtent() {
        return survivalExtent;
    }

    /**
     * Set whether fast mode is enabled.
     *
     * <p>Fast mode may skip lighting checks or adjacent block
     * notification.</p>
     *
     * @param enabled true to enable
     */
    public void setFastMode(boolean enabled) {
        if (fastModeExtent != null) {
            fastModeExtent.setEnabled(enabled);
        }
    }

    /**
     * Return fast mode status.
     *
     * <p>Fast mode may skip lighting checks or adjacent block
     * notification.</p>
     *
     * @return true if enabled
     */
    public boolean hasFastMode() {
        return fastModeExtent != null && fastModeExtent.isEnabled();
    }

    /**
     * Get the {@link BlockBag} is used.
     *
     * @return a block bag or null
     */
    public BlockBag getBlockBag() {
        return blockBagExtent.getBlockBag();
    }

    /**
     * Set a {@link BlockBag} to use.
     *
     * @param blockBag the block bag to set, or null to use none
     */
    public void setBlockBag(BlockBag blockBag) {
        blockBagExtent.setBlockBag(blockBag);
    }

    /**
     * Gets the list of missing blocks and clears the list for the next
     * operation.
     *
     * @return a map of missing blocks
     */
    public Map<BlockType, Integer> popMissingBlocks() {
        return blockBagExtent.popMissing();
    }

    /**
     * Get the number of blocks changed, including repeated block changes.
     *
     * <p>This number may not be accurate.</p>
     *
     * @return the number of block changes
     */
    public int getBlockChangeCount() {
        return changeSet.size();
    }

    @Override
    public BaseBiome getBiome(Vector2D position) {
        return bypassNone.getBiome(position);
    }

    @Override
    public boolean setBiome(Vector2D position, BaseBiome biome) {
        return bypassNone.setBiome(position, biome);
    }

    @Override
    public BlockState getBlock(Vector position) {
        return world.getBlock(position);
    }

    @Override
    public BaseBlock getFullBlock(Vector position) {
        return world.getFullBlock(position);
    }

    /**
     * Returns the highest solid 'terrain' block.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     * @param minY minimal height
     * @param maxY maximal height
     * @return height of highest block found or 'minY'
     */
    public int getHighestTerrainBlock(int x, int z, int minY, int maxY) {
        for (int y = maxY; y >= minY; --y) {
            Vector pt = new Vector(x, y, z);
            BlockState block = getBlock(pt);
            if (block.getBlockType().getMaterial().isMovementBlocker()) {
                return y;
            }
        }

        return minY;
    }

    /**
     * Set a block, bypassing both history and block re-ordering.
     *
     * @param position the position to set the block at
     * @param block the block
     * @param stage the level
     * @return whether the block changed
     * @throws WorldEditException thrown on a set error
     */
    public boolean setBlock(Vector position, BlockStateHolder block, Stage stage) throws WorldEditException {
        switch (stage) {
            case BEFORE_HISTORY:
                return bypassNone.setBlock(position, block);
            case BEFORE_CHANGE:
                return bypassHistory.setBlock(position, block);
            case BEFORE_REORDER:
                return bypassReorderHistory.setBlock(position, block);
        }

        throw new RuntimeException("New enum entry added that is unhandled here");
    }

    /**
     * Set a block, bypassing both history and block re-ordering.
     *
     * @param position the position to set the block at
     * @param block the block
     * @return whether the block changed
     */
    public boolean rawSetBlock(Vector position, BlockStateHolder block) {
        try {
            return setBlock(position, block, Stage.BEFORE_CHANGE);
        } catch (WorldEditException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    /**
     * Set a block, bypassing history but still utilizing block re-ordering.
     *
     * @param position the position to set the block at
     * @param block the block
     * @return whether the block changed
     */
    public boolean smartSetBlock(Vector position, BlockStateHolder block) {
        try {
            return setBlock(position, block, Stage.BEFORE_REORDER);
        } catch (WorldEditException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    @Override
    public boolean setBlock(Vector position, BlockStateHolder block) throws MaxChangedBlocksException {
        try {
            return setBlock(position, block, Stage.BEFORE_HISTORY);
        } catch (MaxChangedBlocksException e) {
            throw e;
        } catch (WorldEditException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    /**
     * Sets the block at a position, subject to both history and block re-ordering.
     *
     * @param position the position
     * @param pattern a pattern to use
     * @return Whether the block changed -- not entirely dependable
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public boolean setBlock(Vector position, Pattern pattern) throws MaxChangedBlocksException {
        return setBlock(position, pattern.apply(position));
    }

    /**
     * Set blocks that are in a set of positions and return the number of times
     * that the block set calls returned true.
     *
     * @param vset a set of positions
     * @param pattern the pattern
     * @return the number of changed blocks
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    private int setBlocks(Set<Vector> vset, Pattern pattern) throws MaxChangedBlocksException {
        int affected = 0;
        for (Vector v : vset) {
            affected += setBlock(v, pattern) ? 1 : 0;
        }
        return affected;
    }

    @Override
    @Nullable
    public Entity createEntity(com.sk89q.worldedit.util.Location location, BaseEntity entity) {
        return bypassNone.createEntity(location, entity);
    }

    /**
     * Restores all blocks to their initial state.
     *
     * @param editSession a new {@link EditSession} to perform the undo in
     */
    public void undo(EditSession editSession) {
        UndoContext context = new UndoContext();
        context.setExtent(editSession.bypassHistory);
        Operations.completeBlindly(ChangeSetExecutor.createUndo(changeSet, context));
        editSession.flushQueue();
    }

    /**
     * Sets to new state.
     *
     * @param editSession a new {@link EditSession} to perform the redo in
     */
    public void redo(EditSession editSession) {
        UndoContext context = new UndoContext();
        context.setExtent(editSession.bypassHistory);
        Operations.completeBlindly(ChangeSetExecutor.createRedo(changeSet, context));
        editSession.flushQueue();
    }

    /**
     * Get the number of changed blocks.
     *
     * @return the number of changes
     */
    public int size() {
        return getBlockChangeCount();
    }

    @Override
    public Vector getMinimumPoint() {
        return getWorld().getMinimumPoint();
    }

    @Override
    public Vector getMaximumPoint() {
        return getWorld().getMaximumPoint();
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return bypassNone.getEntities(region);
    }

    @Override
    public List<? extends Entity> getEntities() {
        return bypassNone.getEntities();
    }

    /**
     * Finish off the queue.
     */
    public void flushQueue() {
        Operations.completeBlindly(commit());
    }

    @Override
    public @Nullable Operation commit() {
        return bypassNone.commit();
    }

    /**
     * Count the number of blocks of a list of types in a region.
     *
     * @param region the region
     * @param searchBlocks the list of blocks to search
     * @return the number of blocks that matched the pattern
     */
    public int countBlocks(Region region, Set<BlockStateHolder> searchBlocks) {
        FuzzyBlockMask mask = new FuzzyBlockMask(this, searchBlocks);
        Counter count = new Counter();
        RegionMaskingFilter filter = new RegionMaskingFilter(mask, count);
        RegionVisitor visitor = new RegionVisitor(region, filter);
        Operations.completeBlindly(visitor); // We can't throw exceptions, nor do we expect any
        return count.getCount();
    }

    /**
     * Fills an area recursively in the X/Z directions.
     *
     * @param origin the location to start from
     * @param block the block to fill with
     * @param radius the radius of the spherical area to fill
     * @param depth the maximum depth, starting from the origin
     * @param recursive whether a breadth-first search should be performed
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int fillXZ(Vector origin, BlockStateHolder block, double radius, int depth, boolean recursive) throws MaxChangedBlocksException {
        return fillXZ(origin, new BlockPattern(block), radius, depth, recursive);
    }

    /**
     * Fills an area recursively in the X/Z directions.
     *
     * @param origin the origin to start the fill from
     * @param pattern the pattern to fill with
     * @param radius the radius of the spherical area to fill, with 0 as the smallest radius
     * @param depth the maximum depth, starting from the origin, with 1 as the smallest depth
     * @param recursive whether a breadth-first search should be performed
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int fillXZ(Vector origin, Pattern pattern, double radius, int depth, boolean recursive) throws MaxChangedBlocksException {
        checkNotNull(origin);
        checkNotNull(pattern);
        checkArgument(radius >= 0, "radius >= 0");
        checkArgument(depth >= 1, "depth >= 1");

        MaskIntersection mask = new MaskIntersection(
                new RegionMask(new EllipsoidRegion(null, origin, new Vector(radius, radius, radius))),
                new BoundedHeightMask(
                        Math.max(origin.getBlockY() - depth + 1, 0),
                        Math.min(getWorld().getMaxY(), origin.getBlockY())),
                Masks.negate(new ExistingBlockMask(this)));

        // Want to replace blocks
        BlockReplace replace = new BlockReplace(this, pattern);

        // Pick how we're going to visit blocks
        RecursiveVisitor visitor;
        if (recursive) {
            visitor = new RecursiveVisitor(mask, replace);
        } else {
            visitor = new DownwardVisitor(mask, replace, origin.getBlockY());
        }

        // Start at the origin
        visitor.visit(origin);

        // Execute
        Operations.completeLegacy(visitor);

        return visitor.getAffected();
    }

    /**
     * Remove a cuboid above the given position with a given apothem and a given height.
     *
     * @param position base position
     * @param apothem an apothem of the cuboid (on the XZ plane), where the minimum is 1
     * @param height the height of the cuboid, where the minimum is 1
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int removeAbove(Vector position, int apothem, int height) throws MaxChangedBlocksException {
        checkNotNull(position);
        checkArgument(apothem >= 1, "apothem >= 1");
        checkArgument(height >= 1, "height >= 1");

        Region region = new CuboidRegion(
                getWorld(), // Causes clamping of Y range
                position.add(-apothem + 1, 0, -apothem + 1),
                position.add(apothem - 1, height - 1, apothem - 1));
        Pattern pattern = new BlockPattern(BlockTypes.AIR.getDefaultState());
        return setBlocks(region, pattern);
    }

    /**
     * Remove a cuboid below the given position with a given apothem and a given height.
     *
     * @param position base position
     * @param apothem an apothem of the cuboid (on the XZ plane), where the minimum is 1
     * @param height the height of the cuboid, where the minimum is 1
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int removeBelow(Vector position, int apothem, int height) throws MaxChangedBlocksException {
        checkNotNull(position);
        checkArgument(apothem >= 1, "apothem >= 1");
        checkArgument(height >= 1, "height >= 1");

        Region region = new CuboidRegion(
                getWorld(), // Causes clamping of Y range
                position.add(-apothem + 1, 0, -apothem + 1),
                position.add(apothem - 1, -height + 1, apothem - 1));
        Pattern pattern = new BlockPattern(BlockTypes.AIR.getDefaultState());
        return setBlocks(region, pattern);
    }

    /**
     * Remove blocks of a certain type nearby a given position.
     *
     * @param position center position of cuboid
     * @param blockType the block type to match
     * @param apothem an apothem of the cuboid, where the minimum is 1
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int removeNear(Vector position, BlockType blockType, int apothem) throws MaxChangedBlocksException {
        checkNotNull(position);
        checkArgument(apothem >= 1, "apothem >= 1");

        Mask mask = new FuzzyBlockMask(this, blockType.getDefaultState().toFuzzy());
        Vector adjustment = new Vector(1, 1, 1).multiply(apothem - 1);
        Region region = new CuboidRegion(
                getWorld(), // Causes clamping of Y range
                position.add(adjustment.multiply(-1)),
                position.add(adjustment));
        Pattern pattern = new BlockPattern(BlockTypes.AIR.getDefaultState());
        return replaceBlocks(region, mask, pattern);
    }

    /**
     * Sets all the blocks inside a region to a given block type.
     *
     * @param region the region
     * @param block the block
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int setBlocks(Region region, BlockStateHolder block) throws MaxChangedBlocksException {
        return setBlocks(region, new BlockPattern(block));
    }

    /**
     * Sets all the blocks inside a region to a given pattern.
     *
     * @param region the region
     * @param pattern the pattern that provides the replacement block
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int setBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
        checkNotNull(region);
        checkNotNull(pattern);

        BlockReplace replace = new BlockReplace(this, pattern);
        RegionVisitor visitor = new RegionVisitor(region, replace);
        Operations.completeLegacy(visitor);
        return visitor.getAffected();
    }

    /**
     * Replaces all the blocks matching a given filter, within a given region, to a block
     * returned by a given pattern.
     *
     * @param region the region to replace the blocks within
     * @param filter a list of block types to match, or null to use {@link com.sk89q.worldedit.function.mask.ExistingBlockMask}
     * @param replacement the replacement block
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int replaceBlocks(Region region, Set<BlockStateHolder> filter, BlockStateHolder replacement) throws MaxChangedBlocksException {
        return replaceBlocks(region, filter, new BlockPattern(replacement));
    }

    /**
     * Replaces all the blocks matching a given filter, within a given region, to a block
     * returned by a given pattern.
     *
     * @param region the region to replace the blocks within
     * @param filter a list of block types to match, or null to use {@link com.sk89q.worldedit.function.mask.ExistingBlockMask}
     * @param pattern the pattern that provides the new blocks
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int replaceBlocks(Region region, Set<BlockStateHolder> filter, Pattern pattern) throws MaxChangedBlocksException {
        Mask mask = filter == null ? new ExistingBlockMask(this) : new FuzzyBlockMask(this, filter);
        return replaceBlocks(region, mask, pattern);
    }

    /**
     * Replaces all the blocks matching a given mask, within a given region, to a block
     * returned by a given pattern.
     *
     * @param region the region to replace the blocks within
     * @param mask the mask that blocks must match
     * @param pattern the pattern that provides the new blocks
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int replaceBlocks(Region region, Mask mask, Pattern pattern) throws MaxChangedBlocksException {
        checkNotNull(region);
        checkNotNull(mask);
        checkNotNull(pattern);

        BlockReplace replace = new BlockReplace(this, pattern);
        RegionMaskingFilter filter = new RegionMaskingFilter(mask, replace);
        RegionVisitor visitor = new RegionVisitor(region, filter);
        Operations.completeLegacy(visitor);
        return visitor.getAffected();
    }

    /**
     * Sets the blocks at the center of the given region to the given pattern.
     * If the center sits between two blocks on a certain axis, then two blocks
     * will be placed to mark the center.
     *
     * @param region the region to find the center of
     * @param pattern the replacement pattern
     * @return the number of blocks placed
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int center(Region region, Pattern pattern) throws MaxChangedBlocksException {
        checkNotNull(region);
        checkNotNull(pattern);

        Vector center = region.getCenter();
        Region centerRegion = new CuboidRegion(
                getWorld(), // Causes clamping of Y range
                new Vector(((int) center.getX()), ((int) center.getY()), ((int) center.getZ())),
                new Vector(MathUtils.roundHalfUp(center.getX()),
                            center.getY(), MathUtils.roundHalfUp(center.getZ())));
        return setBlocks(centerRegion, pattern);
    }

    /**
     * Make the faces of the given region as if it was a {@link CuboidRegion}.
     *
     * @param region the region
     * @param block the block to place
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeCuboidFaces(Region region, BaseBlock block) throws MaxChangedBlocksException {
        return makeCuboidFaces(region, new BlockPattern(block));
    }

    /**
     * Make the faces of the given region as if it was a {@link CuboidRegion}.
     *
     * @param region the region
     * @param pattern the pattern to place
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeCuboidFaces(Region region, Pattern pattern) throws MaxChangedBlocksException {
        checkNotNull(region);
        checkNotNull(pattern);

        CuboidRegion cuboid = CuboidRegion.makeCuboid(region);
        Region faces = cuboid.getFaces();
        return setBlocks(faces, pattern);
    }

    /**
     * Make the faces of the given region. The method by which the faces are found
     * may be inefficient, because there may not be an efficient implementation supported
     * for that specific shape.
     *
     * @param region the region
     * @param pattern the pattern to place
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeFaces(final Region region, Pattern pattern) throws MaxChangedBlocksException {
        checkNotNull(region);
        checkNotNull(pattern);

        if (region instanceof CuboidRegion) {
            return makeCuboidFaces(region, pattern);
        } else {
            return new RegionShape(region).generate(this, pattern, true);
        }
    }


    /**
     * Make the walls (all faces but those parallel to the X-Z plane) of the given region
     * as if it was a {@link CuboidRegion}.
     *
     * @param region the region
     * @param block the block to place
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeCuboidWalls(Region region, BaseBlock block) throws MaxChangedBlocksException {
        return makeCuboidWalls(region, new BlockPattern(block));
    }

    /**
     * Make the walls (all faces but those parallel to the X-Z plane) of the given region
     * as if it was a {@link CuboidRegion}.
     *
     * @param region the region
     * @param pattern the pattern to place
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeCuboidWalls(Region region, Pattern pattern) throws MaxChangedBlocksException {
        checkNotNull(region);
        checkNotNull(pattern);

        CuboidRegion cuboid = CuboidRegion.makeCuboid(region);
        Region faces = cuboid.getWalls();
        return setBlocks(faces, pattern);
    }

    /**
     * Make the walls of the given region. The method by which the walls are found
     * may be inefficient, because there may not be an efficient implementation supported
     * for that specific shape.
     *
     * @param region the region
     * @param pattern the pattern to place
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeWalls(final Region region, Pattern pattern) throws MaxChangedBlocksException {
        checkNotNull(region);
        checkNotNull(pattern);

        if (region instanceof CuboidRegion) {
            return makeCuboidWalls(region, pattern);
        } else {
            final int minY = region.getMinimumPoint().getBlockY();
            final int maxY = region.getMaximumPoint().getBlockY();
            final ArbitraryShape shape = new RegionShape(region) {
                @Override
                protected BlockStateHolder getMaterial(int x, int y, int z, BlockStateHolder defaultMaterial) {
                    if (y > maxY || y < minY) {
                        // Put holes into the floor and ceiling by telling ArbitraryShape that the shape goes on outside the region
                        return defaultMaterial;
                    }

                    return super.getMaterial(x, y, z, defaultMaterial);
                }
            };
            return shape.generate(this, pattern, true);
        }
    }

    /**
     * Places a layer of blocks on top of ground blocks in the given region
     * (as if it were a cuboid).
     *
     * @param region the region
     * @param block the placed block
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int overlayCuboidBlocks(Region region, BaseBlock block) throws MaxChangedBlocksException {
        checkNotNull(block);

        return overlayCuboidBlocks(region, new BlockPattern(block));
    }

    /**
     * Places a layer of blocks on top of ground blocks in the given region
     * (as if it were a cuboid).
     *
     * @param region the region
     * @param pattern the placed block pattern
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int overlayCuboidBlocks(Region region, Pattern pattern) throws MaxChangedBlocksException {
        checkNotNull(region);
        checkNotNull(pattern);

        BlockReplace replace = new BlockReplace(this, pattern);
        RegionOffset offset = new RegionOffset(new Vector(0, 1, 0), replace);
        GroundFunction ground = new GroundFunction(new ExistingBlockMask(this), offset);
        LayerVisitor visitor = new LayerVisitor(asFlatRegion(region), minimumBlockY(region), maximumBlockY(region), ground);
        Operations.completeLegacy(visitor);
        return ground.getAffected();
    }

    /**
     * Turns the first 3 layers into dirt/grass and the bottom layers
     * into rock, like a natural Minecraft mountain.
     *
     * @param region the region to affect
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int naturalizeCuboidBlocks(Region region) throws MaxChangedBlocksException {
        checkNotNull(region);

        Naturalizer naturalizer = new Naturalizer(this);
        FlatRegion flatRegion = Regions.asFlatRegion(region);
        LayerVisitor visitor = new LayerVisitor(flatRegion, minimumBlockY(region), maximumBlockY(region), naturalizer);
        Operations.completeLegacy(visitor);
        return naturalizer.getAffected();
    }

    /**
     * Stack a cuboid region.
     *
     * @param region the region to stack
     * @param dir the direction to stack
     * @param count the number of times to stack
     * @param copyAir true to also copy air blocks
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int stackCuboidRegion(Region region, Vector dir, int count, boolean copyAir) throws MaxChangedBlocksException {
        checkNotNull(region);
        checkNotNull(dir);
        checkArgument(count >= 1, "count >= 1 required");

        Vector size = region.getMaximumPoint().subtract(region.getMinimumPoint()).add(1, 1, 1);
        Vector to = region.getMinimumPoint();
        ForwardExtentCopy copy = new ForwardExtentCopy(this, region, this, to);
        copy.setRepetitions(count);
        copy.setTransform(new AffineTransform().translate(dir.multiply(size)));
        if (!copyAir) {
            copy.setSourceMask(new ExistingBlockMask(this));
        }
        Operations.completeLegacy(copy);
        return copy.getAffected();
    }

    /**
     * Move the blocks in a region a certain direction.
     *
     * @param region the region to move
     * @param dir the direction
     * @param distance the distance to move
     * @param copyAir true to copy air blocks
     * @param replacement the replacement block to fill in after moving, or null to use air
     * @return number of blocks moved
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int moveRegion(Region region, Vector dir, int distance, boolean copyAir, BlockStateHolder replacement) throws MaxChangedBlocksException {
        checkNotNull(region);
        checkNotNull(dir);
        checkArgument(distance >= 1, "distance >= 1 required");

        Vector to = region.getMinimumPoint();

        // Remove the original blocks
        com.sk89q.worldedit.function.pattern.Pattern pattern = replacement != null ?
                new BlockPattern(replacement) :
                new BlockPattern(BlockTypes.AIR.getDefaultState());
        BlockReplace remove = new BlockReplace(this, pattern);

        // Copy to a buffer so we don't destroy our original before we can copy all the blocks from it
        ForgetfulExtentBuffer buffer = new ForgetfulExtentBuffer(this, new RegionMask(region));
        ForwardExtentCopy copy = new ForwardExtentCopy(this, region, buffer, to);
        copy.setTransform(new AffineTransform().translate(dir.multiply(distance)));
        copy.setSourceFunction(remove); // Remove
        copy.setRemovingEntities(true);
        if (!copyAir) {
            copy.setSourceMask(new ExistingBlockMask(this));
        }

        // Then we need to copy the buffer to the world
        BlockReplace replace = new BlockReplace(this, buffer);
        RegionVisitor visitor = new RegionVisitor(buffer.asRegion(), replace);

        OperationQueue operation = new OperationQueue(copy, visitor);
        Operations.completeLegacy(operation);

        return copy.getAffected();
    }

    /**
     * Move the blocks in a region a certain direction.
     *
     * @param region the region to move
     * @param dir the direction
     * @param distance the distance to move
     * @param copyAir true to copy air blocks
     * @param replacement the replacement block to fill in after moving, or null to use air
     * @return number of blocks moved
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int moveCuboidRegion(Region region, Vector dir, int distance, boolean copyAir, BaseBlock replacement) throws MaxChangedBlocksException {
        return moveRegion(region, dir, distance, copyAir, replacement);
    }

    /**
     * Drain nearby pools of water or lava.
     *
     * @param origin the origin to drain from, which will search a 3x3 area
     * @param radius the radius of the removal, where a value should be 0 or greater
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int drainArea(Vector origin, double radius) throws MaxChangedBlocksException {
        checkNotNull(origin);
        checkArgument(radius >= 0, "radius >= 0 required");

        MaskIntersection mask = new MaskIntersection(
                new BoundedHeightMask(0, getWorld().getMaxY()),
                new RegionMask(new EllipsoidRegion(null, origin, new Vector(radius, radius, radius))),
                getWorld().createLiquidMask());

        BlockReplace replace = new BlockReplace(this, new BlockPattern(BlockTypes.AIR.getDefaultState()));
        RecursiveVisitor visitor = new RecursiveVisitor(mask, replace);

        // Around the origin in a 3x3 block
        for (BlockVector position : CuboidRegion.fromCenter(origin, 1)) {
            if (mask.test(position)) {
                visitor.visit(position);
            }
        }

        Operations.completeLegacy(visitor);

        return visitor.getAffected();
    }

    /**
     * Fix liquids so that they turn into stationary blocks and extend outward.
     *
     * @param origin the original position
     * @param radius the radius to fix
     * @param fluid the type of the fluid
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int fixLiquid(Vector origin, double radius, BlockType fluid) throws MaxChangedBlocksException {
        checkNotNull(origin);
        checkArgument(radius >= 0, "radius >= 0 required");

        // Our origins can only be liquids
        BlockMask liquidMask = new BlockMask(this, fluid.getDefaultState().toFuzzy());

        // But we will also visit air blocks
        MaskIntersection blockMask = new MaskUnion(liquidMask, new BlockMask(this, BlockTypes.AIR.getDefaultState()));

        // There are boundaries that the routine needs to stay in
        MaskIntersection mask = new MaskIntersection(
                new BoundedHeightMask(0, Math.min(origin.getBlockY(), getWorld().getMaxY())),
                new RegionMask(new EllipsoidRegion(null, origin, new Vector(radius, radius, radius))),
                blockMask
        );

        BlockReplace replace = new BlockReplace(this, new BlockPattern(fluid.getDefaultState()));
        NonRisingVisitor visitor = new NonRisingVisitor(mask, replace);

        // Around the origin in a 3x3 block
        for (BlockVector position : CuboidRegion.fromCenter(origin, 1)) {
            if (liquidMask.test(position)) {
                visitor.visit(position);
            }
        }

        Operations.completeLegacy(visitor);

        return visitor.getAffected();
    }

    /**
     * Makes a cylinder.
     *
     * @param pos Center of the cylinder
     * @param block The block pattern to use
     * @param radius The cylinder's radius
     * @param height The cylinder's up/down extent. If negative, extend downward.
     * @param filled If false, only a shell will be generated.
     * @return number of blocks changed
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeCylinder(Vector pos, Pattern block, double radius, int height, boolean filled) throws MaxChangedBlocksException {
        return makeCylinder(pos, block, radius, radius, height, filled);
    }

    /**
     * Makes a cylinder.
     *
     * @param pos Center of the cylinder
     * @param block The block pattern to use
     * @param radiusX The cylinder's largest north/south extent
     * @param radiusZ The cylinder's largest east/west extent
     * @param height The cylinder's up/down extent. If negative, extend downward.
     * @param filled If false, only a shell will be generated.
     * @return number of blocks changed
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeCylinder(Vector pos, Pattern block, double radiusX, double radiusZ, int height, boolean filled) throws MaxChangedBlocksException {
        int affected = 0;

        radiusX += 0.5;
        radiusZ += 0.5;

        if (height == 0) {
            return 0;
        } else if (height < 0) {
            height = -height;
            pos = pos.subtract(0, height, 0);
        }

        if (pos.getBlockY() < 0) {
            pos = pos.setY(0);
        } else if (pos.getBlockY() + height - 1 > world.getMaxY()) {
            height = world.getMaxY() - pos.getBlockY() + 1;
        }

        final double invRadiusX = 1 / radiusX;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        double nextXn = 0;
        forX: for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextZn = 0;
            forZ: for (int z = 0; z <= ceilRadiusZ; ++z) {
                final double zn = nextZn;
                nextZn = (z + 1) * invRadiusZ;

                double distanceSq = lengthSq(xn, zn);
                if (distanceSq > 1) {
                    if (z == 0) {
                        break forX;
                    }
                    break forZ;
                }

                if (!filled) {
                    if (lengthSq(nextXn, zn) <= 1 && lengthSq(xn, nextZn) <= 1) {
                        continue;
                    }
                }

                for (int y = 0; y < height; ++y) {
                    if (setBlock(pos.add(x, y, z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(-x, y, z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(x, y, -z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(-x, y, -z), block)) {
                        ++affected;
                    }
                }
            }
        }

        return affected;
    }

    /**
    * Makes a sphere.
    *
    * @param pos Center of the sphere or ellipsoid
    * @param block The block pattern to use
    * @param radius The sphere's radius
    * @param filled If false, only a shell will be generated.
    * @return number of blocks changed
    * @throws MaxChangedBlocksException thrown if too many blocks are changed
    */
    public int makeSphere(Vector pos, Pattern block, double radius, boolean filled) throws MaxChangedBlocksException {
        return makeSphere(pos, block, radius, radius, radius, filled);
    }

    /**
     * Makes a sphere or ellipsoid.
     *
     * @param pos Center of the sphere or ellipsoid
     * @param block The block pattern to use
     * @param radiusX The sphere/ellipsoid's largest north/south extent
     * @param radiusY The sphere/ellipsoid's largest up/down extent
     * @param radiusZ The sphere/ellipsoid's largest east/west extent
     * @param filled If false, only a shell will be generated.
     * @return number of blocks changed
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeSphere(Vector pos, Pattern block, double radiusX, double radiusY, double radiusZ, boolean filled) throws MaxChangedBlocksException {
        int affected = 0;

        radiusX += 0.5;
        radiusY += 0.5;
        radiusZ += 0.5;

        final double invRadiusX = 1 / radiusX;
        final double invRadiusY = 1 / radiusY;
        final double invRadiusZ = 1 / radiusZ;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusY = (int) Math.ceil(radiusY);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);

        double nextXn = 0;
        forX: for (int x = 0; x <= ceilRadiusX; ++x) {
            final double xn = nextXn;
            nextXn = (x + 1) * invRadiusX;
            double nextYn = 0;
            forY: for (int y = 0; y <= ceilRadiusY; ++y) {
                final double yn = nextYn;
                nextYn = (y + 1) * invRadiusY;
                double nextZn = 0;
                forZ: for (int z = 0; z <= ceilRadiusZ; ++z) {
                    final double zn = nextZn;
                    nextZn = (z + 1) * invRadiusZ;

                    double distanceSq = lengthSq(xn, yn, zn);
                    if (distanceSq > 1) {
                        if (z == 0) {
                            if (y == 0) {
                                break forX;
                            }
                            break forY;
                        }
                        break forZ;
                    }

                    if (!filled) {
                        if (lengthSq(nextXn, yn, zn) <= 1 && lengthSq(xn, nextYn, zn) <= 1 && lengthSq(xn, yn, nextZn) <= 1) {
                            continue;
                        }
                    }

                    if (setBlock(pos.add(x, y, z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(-x, y, z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(x, -y, z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(x, y, -z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(-x, -y, z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(x, -y, -z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(-x, y, -z), block)) {
                        ++affected;
                    }
                    if (setBlock(pos.add(-x, -y, -z), block)) {
                        ++affected;
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Makes a pyramid.
     *
     * @param position a position
     * @param block a block
     * @param size size of pyramid
     * @param filled true if filled
     * @return number of blocks changed
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makePyramid(Vector position, Pattern block, int size, boolean filled) throws MaxChangedBlocksException {
        int affected = 0;

        int height = size;

        for (int y = 0; y <= height; ++y) {
            size--;
            for (int x = 0; x <= size; ++x) {
                for (int z = 0; z <= size; ++z) {

                    if ((filled && z <= size && x <= size) || z == size || x == size) {

                        if (setBlock(position.add(x, y, z), block)) {
                            ++affected;
                        }
                        if (setBlock(position.add(-x, y, z), block)) {
                            ++affected;
                        }
                        if (setBlock(position.add(x, y, -z), block)) {
                            ++affected;
                        }
                        if (setBlock(position.add(-x, y, -z), block)) {
                            ++affected;
                        }
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Thaw blocks in a radius.
     *
     * @param position the position
     * @param radius the radius
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int thaw(Vector position, double radius)
            throws MaxChangedBlocksException {
        int affected = 0;
        double radiusSq = radius * radius;

        int ox = position.getBlockX();
        int oy = position.getBlockY();
        int oz = position.getBlockZ();

        BlockState air = BlockTypes.AIR.getDefaultState();
        BlockState water = BlockTypes.WATER.getDefaultState();

        int ceilRadius = (int) Math.ceil(radius);
        for (int x = ox - ceilRadius; x <= ox + ceilRadius; ++x) {
            for (int z = oz - ceilRadius; z <= oz + ceilRadius; ++z) {
                if ((new Vector(x, oy, z)).distanceSq(position) > radiusSq) {
                    continue;
                }

                for (int y = world.getMaxY(); y >= 1; --y) {
                    Vector pt = new Vector(x, y, z);
                    BlockType id = getBlock(pt).getBlockType();

                    if (id == BlockTypes.ICE) {
                        if (setBlock(pt, water)) {
                            ++affected;
                        }
                    } else if (id == BlockTypes.SNOW) {
                        if (setBlock(pt, air)) {
                            ++affected;
                        }
                    } else if (id == BlockTypes.AIR) {
                        continue;
                    }

                    break;
                }
            }
        }

        return affected;
    }

    /**
     * Make snow in a radius.
     *
     * @param position a position
     * @param radius a radius
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int simulateSnow(Vector position, double radius) throws MaxChangedBlocksException {
        int affected = 0;
        double radiusSq = radius * radius;

        int ox = position.getBlockX();
        int oy = position.getBlockY();
        int oz = position.getBlockZ();

        BlockState ice = BlockTypes.ICE.getDefaultState();
        BlockState snow = BlockTypes.SNOW.getDefaultState();

        int ceilRadius = (int) Math.ceil(radius);
        for (int x = ox - ceilRadius; x <= ox + ceilRadius; ++x) {
            for (int z = oz - ceilRadius; z <= oz + ceilRadius; ++z) {
                if ((new Vector(x, oy, z)).distanceSq(position) > radiusSq) {
                    continue;
                }

                for (int y = world.getMaxY(); y >= 1; --y) {
                    Vector pt = new Vector(x, y, z);
                    BlockType id = getBlock(pt).getBlockType();

                    if (id == BlockTypes.AIR) {
                        continue;
                    }

                    // Ice!
                    if (id == BlockTypes.WATER) {
                        if (setBlock(pt, ice)) {
                            ++affected;
                        }
                        break;
                    }

                    // Snow should not cover these blocks
                    if (id.getMaterial().isTranslucent()) {
                        // Add snow on leaves
                        if (!BlockCategories.LEAVES.contains(id)) {
                            break;
                        }
                    }

                    // Too high?
                    if (y == world.getMaxY()) {
                        break;
                    }

                    // add snow cover
                    if (setBlock(pt.add(0, 1, 0), snow)) {
                        ++affected;
                    }
                    break;
                }
            }
        }

        return affected;
    }

    /**
     * Make dirt green.
     *
     * @param position a position
     * @param radius a radius
     * @param onlyNormalDirt only affect normal dirt (data value 0)
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int green(Vector position, double radius, boolean onlyNormalDirt)
            throws MaxChangedBlocksException {
        int affected = 0;
        final double radiusSq = radius * radius;

        final int ox = position.getBlockX();
        final int oy = position.getBlockY();
        final int oz = position.getBlockZ();

        final BlockState grass = BlockTypes.GRASS_BLOCK.getDefaultState();

        final int ceilRadius = (int) Math.ceil(radius);
        for (int x = ox - ceilRadius; x <= ox + ceilRadius; ++x) {
            for (int z = oz - ceilRadius; z <= oz + ceilRadius; ++z) {
                if ((new Vector(x, oy, z)).distanceSq(position) > radiusSq) {
                    continue;
                }

                for (int y = world.getMaxY(); y >= 1; --y) {
                    final Vector pt = new Vector(x, y, z);
                    final BlockState block = getBlock(pt);

                    if (block.getBlockType() == BlockTypes.DIRT ||
                            (!onlyNormalDirt && block.getBlockType() == BlockTypes.COARSE_DIRT)) {
                        if (setBlock(pt, grass)) {
                            ++affected;
                        }
                        break;
                    } else if (block.getBlockType() == BlockTypes.WATER || block.getBlockType() == BlockTypes.LAVA) {
                        break;
                    } else if (block.getBlockType().getMaterial().isMovementBlocker()) {
                        break;
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Makes pumpkin patches randomly in an area around the given position.
     *
     * @param position the base position
     * @param apothem the apothem of the (square) area
     * @return number of patches created
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makePumpkinPatches(Vector position, int apothem) throws MaxChangedBlocksException {
        // We want to generate pumpkins
        GardenPatchGenerator generator = new GardenPatchGenerator(this);
        generator.setPlant(GardenPatchGenerator.getPumpkinPattern());

        // In a region of the given radius
        FlatRegion region = new CuboidRegion(
                getWorld(), // Causes clamping of Y range
                position.add(-apothem, -5, -apothem),
                position.add(apothem, 10, apothem));
        double density = 0.02;

        GroundFunction ground = new GroundFunction(new ExistingBlockMask(this), generator);
        LayerVisitor visitor = new LayerVisitor(region, minimumBlockY(region), maximumBlockY(region), ground);
        visitor.setMask(new NoiseFilter2D(new RandomNoise(), density));
        Operations.completeLegacy(visitor);
        return ground.getAffected();
    }

    /**
     * Makes a forest.
     *
     * @param basePosition a position
     * @param size a size
     * @param density between 0 and 1, inclusive
     * @param treeType the tree type
     * @return number of trees created
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeForest(Vector basePosition, int size, double density, TreeGenerator.TreeType treeType) throws MaxChangedBlocksException {
        int affected = 0;

        for (int x = basePosition.getBlockX() - size; x <= basePosition.getBlockX()
                + size; ++x) {
            for (int z = basePosition.getBlockZ() - size; z <= basePosition.getBlockZ()
                    + size; ++z) {
                // Don't want to be in the ground
                if (getBlock(new Vector(x, basePosition.getBlockY(), z)).getBlockType() != BlockTypes.AIR) {
                    continue;
                }
                // The gods don't want a tree here
                if (Math.random() >= density) {
                    continue;
                } // def 0.05

                for (int y = basePosition.getBlockY(); y >= basePosition.getBlockY() - 10; --y) {
                    // Check if we hit the ground
                    BlockType t = getBlock(new Vector(x, y, z)).getBlockType();
                    if (t == BlockTypes.GRASS_BLOCK || t == BlockTypes.DIRT) {
                        treeType.generate(this, new Vector(x, y + 1, z));
                        ++affected;
                        break;
                    } else if (t == BlockTypes.SNOW) {
                        setBlock(new Vector(x, y, z), BlockTypes.AIR.getDefaultState());
                    } else if (t != BlockTypes.AIR) { // Trees won't grow on this!
                        break;
                    }
                }
            }
        }

        return affected;
    }

    /**
     * Get the block distribution inside a region.
     *
     * @param region a region
     * @return the results
     */
    public List<Countable<BlockType>> getBlockDistribution(Region region) {
        List<Countable<BlockType>> distribution = new ArrayList<>();
        Map<BlockType, Countable<BlockType>> map = new HashMap<>();

        if (region instanceof CuboidRegion) {
            // Doing this for speed
            Vector min = region.getMinimumPoint();
            Vector max = region.getMaximumPoint();

            int minX = min.getBlockX();
            int minY = min.getBlockY();
            int minZ = min.getBlockZ();
            int maxX = max.getBlockX();
            int maxY = max.getBlockY();
            int maxZ = max.getBlockZ();

            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    for (int z = minZ; z <= maxZ; ++z) {
                        Vector pt = new Vector(x, y, z);

                        BlockType type = getBlock(pt).getBlockType();

                        if (map.containsKey(type)) {
                            map.get(type).increment();
                        } else {
                            Countable<BlockType> c = new Countable<>(type, 1);
                            map.put(type, c);
                            distribution.add(c);
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                BlockType type = getBlock(pt).getBlockType();

                if (map.containsKey(type)) {
                    map.get(type).increment();
                } else {
                    Countable<BlockType> c = new Countable<>(type, 1);
                    map.put(type, c);
                }
            }
        }

        Collections.sort(distribution);
        // Collections.reverse(distribution);

        return distribution;
    }

    /**
     * Get the block distribution (with data values) inside a region.
     *
     * @param region a region
     * @return the results
     */
    // TODO reduce code duplication - probably during ops-redux
    public List<Countable<BlockStateHolder>> getBlockDistributionWithData(Region region) {
        List<Countable<BlockStateHolder>> distribution = new ArrayList<>();
        Map<BlockStateHolder, Countable<BlockStateHolder>> map = new HashMap<>();

        if (region instanceof CuboidRegion) {
            // Doing this for speed
            Vector min = region.getMinimumPoint();
            Vector max = region.getMaximumPoint();

            int minX = min.getBlockX();
            int minY = min.getBlockY();
            int minZ = min.getBlockZ();
            int maxX = max.getBlockX();
            int maxY = max.getBlockY();
            int maxZ = max.getBlockZ();

            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    for (int z = minZ; z <= maxZ; ++z) {
                        Vector pt = new Vector(x, y, z);

                        BlockStateHolder blk = getBlock(pt);

                        if (map.containsKey(blk)) {
                            map.get(blk).increment();
                        } else {
                            Countable<BlockStateHolder> c = new Countable<>(blk, 1);
                            map.put(blk, c);
                            distribution.add(c);
                        }
                    }
                }
            }
        } else {
            for (Vector pt : region) {
                BlockStateHolder blk = getBlock(pt);

                if (map.containsKey(blk)) {
                    map.get(blk).increment();
                } else {
                    Countable<BlockStateHolder> c = new Countable<>(blk, 1);
                    map.put(blk, c);
                }
            }
        }

        Collections.sort(distribution);
        // Collections.reverse(distribution);

        return distribution;
    }

    public int makeShape(final Region region, final Vector zero, final Vector unit, final Pattern pattern, final String expressionString, final boolean hollow) throws ExpressionException, MaxChangedBlocksException {
        final Expression expression = Expression.compile(expressionString, "x", "y", "z", "type", "data");
        expression.optimize();

        final RValue typeVariable = expression.getVariable("type", false);
        final RValue dataVariable = expression.getVariable("data", false);

        final WorldEditExpressionEnvironment environment = new WorldEditExpressionEnvironment(this, unit, zero);
        expression.setEnvironment(environment);

        final ArbitraryShape shape = new ArbitraryShape(region) {
            @Override
            protected BlockStateHolder getMaterial(int x, int y, int z, BlockStateHolder defaultMaterial) {
                final Vector current = new Vector(x, y, z);
                environment.setCurrentBlock(current);
                final Vector scaled = current.subtract(zero).divide(unit);

                try {
                    if (expression.evaluate(scaled.getX(), scaled.getY(), scaled.getZ(), defaultMaterial.getBlockType().getLegacyId(), 0) <= 0) {
                        // TODO data
                        return null;
                    }

                    return LegacyMapper.getInstance().getBlockFromLegacy((int) typeVariable.getValue(), (int) dataVariable.getValue());
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to create shape", e);
                    return null;
                }
            }
        };

        return shape.generate(this, pattern, hollow);
    }

    public int deformRegion(final Region region, final Vector zero, final Vector unit, final String expressionString) throws ExpressionException, MaxChangedBlocksException {
        final Expression expression = Expression.compile(expressionString, "x", "y", "z");
        expression.optimize();

        final RValue x = expression.getVariable("x", false);
        final RValue y = expression.getVariable("y", false);
        final RValue z = expression.getVariable("z", false);

        final WorldEditExpressionEnvironment environment = new WorldEditExpressionEnvironment(this, unit, zero);
        expression.setEnvironment(environment);

        final DoubleArrayList<BlockVector, BaseBlock> queue = new DoubleArrayList<>(false);

        for (BlockVector position : region) {
            // offset, scale
            final Vector scaled = position.subtract(zero).divide(unit);

            // transform
            expression.evaluate(scaled.getX(), scaled.getY(), scaled.getZ());

            final BlockVector sourcePosition = environment.toWorld(x.getValue(), y.getValue(), z.getValue());

            // read block from world
            final BaseBlock material = world.getFullBlock(sourcePosition);

            // queue operation
            queue.put(position, material);
        }

        int affected = 0;
        for (Map.Entry<BlockVector, BaseBlock> entry : queue) {
            BlockVector position = entry.getKey();
            BaseBlock material = entry.getValue();

            // set at new position
            if (setBlock(position, material)) {
                ++affected;
            }
        }

        return affected;
    }

    /**
     * Hollows out the region (Semi-well-defined for non-cuboid selections).
     *
     * @param region the region to hollow out.
     * @param thickness the thickness of the shell to leave (manhattan distance)
     * @param pattern The block pattern to use
     *
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int hollowOutRegion(Region region, int thickness, Pattern pattern) throws MaxChangedBlocksException {
        int affected = 0;

        final Set<BlockVector> outside = new HashSet<>();

        final Vector min = region.getMinimumPoint();
        final Vector max = region.getMaximumPoint();

        final int minX = min.getBlockX();
        final int minY = min.getBlockY();
        final int minZ = min.getBlockZ();
        final int maxX = max.getBlockX();
        final int maxY = max.getBlockY();
        final int maxZ = max.getBlockZ();

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                recurseHollow(region, new BlockVector(x, y, minZ), outside);
                recurseHollow(region, new BlockVector(x, y, maxZ), outside);
            }
        }

        for (int y = minY; y <= maxY; ++y) {
            for (int z = minZ; z <= maxZ; ++z) {
                recurseHollow(region, new BlockVector(minX, y, z), outside);
                recurseHollow(region, new BlockVector(maxX, y, z), outside);
            }
        }

        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                recurseHollow(region, new BlockVector(x, minY, z), outside);
                recurseHollow(region, new BlockVector(x, maxY, z), outside);
            }
        }

        for (int i = 1; i < thickness; ++i) {
            final Set<BlockVector> newOutside = new HashSet<>();
            outer: for (BlockVector position : region) {
                for (Vector recurseDirection: recurseDirections) {
                    BlockVector neighbor = position.add(recurseDirection).toBlockVector();

                    if (outside.contains(neighbor)) {
                        newOutside.add(position);
                        continue outer;
                    }
                }
            }

            outside.addAll(newOutside);
        }

        outer: for (BlockVector position : region) {
            for (Vector recurseDirection: recurseDirections) {
                BlockVector neighbor = position.add(recurseDirection).toBlockVector();

                if (outside.contains(neighbor)) {
                    continue outer;
                }
            }

            if (setBlock(position, pattern.apply(position))) {
                ++affected;
            }
        }

        return affected;
    }

    /**
     * Draws a line (out of blocks) between two vectors.
     *
     * @param pattern The block pattern used to draw the line.
     * @param pos1 One of the points that define the line.
     * @param pos2 The other point that defines the line.
     * @param radius The radius (thickness) of the line.
     * @param filled If false, only a shell will be generated.
     *
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int drawLine(Pattern pattern, Vector pos1, Vector pos2, double radius, boolean filled)
            throws MaxChangedBlocksException {

        Set<Vector> vset = new HashSet<>();
        boolean notdrawn = true;

        int x1 = pos1.getBlockX(), y1 = pos1.getBlockY(), z1 = pos1.getBlockZ();
        int x2 = pos2.getBlockX(), y2 = pos2.getBlockY(), z2 = pos2.getBlockZ();
        int tipx = x1, tipy = y1, tipz = z1;
        int dx = Math.abs(x2 - x1), dy = Math.abs(y2 - y1), dz = Math.abs(z2 - z1);

        if (dx + dy + dz == 0) {
            vset.add(new Vector(tipx, tipy, tipz));
            notdrawn = false;
        }

        if (Math.max(Math.max(dx, dy), dz) == dx && notdrawn) {
            for (int domstep = 0; domstep <= dx; domstep++) {
                tipx = x1 + domstep * (x2 - x1 > 0 ? 1 : -1);
                tipy = (int) Math.round(y1 + domstep * ((double) dy) / ((double) dx) * (y2 - y1 > 0 ? 1 : -1));
                tipz = (int) Math.round(z1 + domstep * ((double) dz) / ((double) dx) * (z2 - z1 > 0 ? 1 : -1));

                vset.add(new Vector(tipx, tipy, tipz));
            }
            notdrawn = false;
        }

        if (Math.max(Math.max(dx, dy), dz) == dy && notdrawn) {
            for (int domstep = 0; domstep <= dy; domstep++) {
                tipy = y1 + domstep * (y2 - y1 > 0 ? 1 : -1);
                tipx = (int) Math.round(x1 + domstep * ((double) dx) / ((double) dy) * (x2 - x1 > 0 ? 1 : -1));
                tipz = (int) Math.round(z1 + domstep * ((double) dz) / ((double) dy) * (z2 - z1 > 0 ? 1 : -1));

                vset.add(new Vector(tipx, tipy, tipz));
            }
            notdrawn = false;
        }

        if (Math.max(Math.max(dx, dy), dz) == dz && notdrawn) {
            for (int domstep = 0; domstep <= dz; domstep++) {
                tipz = z1 + domstep * (z2 - z1 > 0 ? 1 : -1);
                tipy = (int) Math.round(y1 + domstep * ((double) dy) / ((double) dz) * (y2-y1>0 ? 1 : -1));
                tipx = (int) Math.round(x1 + domstep * ((double) dx) / ((double) dz) * (x2-x1>0 ? 1 : -1));

                vset.add(new Vector(tipx, tipy, tipz));
            }
            notdrawn = false;
        }

        vset = getBallooned(vset, radius);
        if (!filled) {
            vset = getHollowed(vset);
        }
        return setBlocks(vset, pattern);
    }

    /**
     * Draws a spline (out of blocks) between specified vectors.
     *
     * @param pattern The block pattern used to draw the spline.
     * @param nodevectors The list of vectors to draw through.
     * @param tension The tension of every node.
     * @param bias The bias of every node.
     * @param continuity The continuity of every node.
     * @param quality The quality of the spline. Must be greater than 0.
     * @param radius The radius (thickness) of the spline.
     * @param filled If false, only a shell will be generated.
     *
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int drawSpline(Pattern pattern, List<Vector> nodevectors, double tension, double bias, double continuity, double quality, double radius, boolean filled)
            throws MaxChangedBlocksException {

        Set<Vector> vset = new HashSet<>();
        List<Node> nodes = new ArrayList<>(nodevectors.size());

        Interpolation interpol = new KochanekBartelsInterpolation();

        for (Vector nodevector : nodevectors) {
            Node n = new Node(nodevector);
            n.setTension(tension);
            n.setBias(bias);
            n.setContinuity(continuity);
            nodes.add(n);
        }

        interpol.setNodes(nodes);
        double splinelength = interpol.arcLength(0, 1);
        for (double loop = 0; loop <= 1; loop += 1D / splinelength / quality) {
            Vector tipv = interpol.getPosition(loop);
            int tipx = (int) Math.round(tipv.getX());
            int tipy = (int) Math.round(tipv.getY());
            int tipz = (int) Math.round(tipv.getZ());

            vset.add(new Vector(tipx, tipy, tipz));
        }

        vset = getBallooned(vset, radius);
        if (!filled) {
            vset = getHollowed(vset);
        }
        return setBlocks(vset, pattern);
    }

    private static double hypot(double... pars) {
        double sum = 0;
        for (double d : pars) {
            sum += Math.pow(d, 2);
        }
        return Math.sqrt(sum);
    }

    private static Set<Vector> getBallooned(Set<Vector> vset, double radius) {
        Set<Vector> returnset = new HashSet<>();
        int ceilrad = (int) Math.ceil(radius);

        for (Vector v : vset) {
            int tipx = v.getBlockX(), tipy = v.getBlockY(), tipz = v.getBlockZ();

            for (int loopx = tipx - ceilrad; loopx <= tipx + ceilrad; loopx++) {
                for (int loopy = tipy - ceilrad; loopy <= tipy + ceilrad; loopy++) {
                    for (int loopz = tipz - ceilrad; loopz <= tipz + ceilrad; loopz++) {
                        if (hypot(loopx - tipx, loopy - tipy, loopz - tipz) <= radius) {
                            returnset.add(new Vector(loopx, loopy, loopz));
                        }
                    }
                }
            }
        }
        return returnset;
    }

    private static Set<Vector> getHollowed(Set<Vector> vset) {
        Set<Vector> returnset = new HashSet<>();
        for (Vector v : vset) {
            double x = v.getX(), y = v.getY(), z = v.getZ();
            if (!(vset.contains(new Vector(x + 1, y, z)) &&
            vset.contains(new Vector(x - 1, y, z)) &&
            vset.contains(new Vector(x, y + 1, z)) &&
            vset.contains(new Vector(x, y - 1, z)) &&
            vset.contains(new Vector(x, y, z + 1)) &&
            vset.contains(new Vector(x, y, z - 1)))) {
                returnset.add(v);
            }
        }
        return returnset;
    }

    private void recurseHollow(Region region, BlockVector origin, Set<BlockVector> outside) {
        final LinkedList<BlockVector> queue = new LinkedList<>();
        queue.addLast(origin);

        while (!queue.isEmpty()) {
            final BlockVector current = queue.removeFirst();
            final BlockState block = getBlock(current);
            if (block.getBlockType().getMaterial().isMovementBlocker()) {
                continue;
            }

            if (!outside.add(current)) {
                continue;
            }

            if (!region.contains(current)) {
                continue;
            }

            for (Vector recurseDirection: recurseDirections) {
                queue.addLast(current.add(recurseDirection).toBlockVector());
            }
        } // while
    }

    public int makeBiomeShape(final Region region, final Vector zero, final Vector unit, final BaseBiome biomeType, final String expressionString, final boolean hollow) throws ExpressionException, MaxChangedBlocksException {
        final Vector2D zero2D = zero.toVector2D();
        final Vector2D unit2D = unit.toVector2D();

        final Expression expression = Expression.compile(expressionString, "x", "z");
        expression.optimize();

        final EditSession editSession = this;
        final WorldEditExpressionEnvironment environment = new WorldEditExpressionEnvironment(editSession, unit, zero);
        expression.setEnvironment(environment);

        final ArbitraryBiomeShape shape = new ArbitraryBiomeShape(region) {
            @Override
            protected BaseBiome getBiome(int x, int z, BaseBiome defaultBiomeType) {
                final Vector2D current = new Vector2D(x, z);
                environment.setCurrentBlock(current.toVector(0));
                final Vector2D scaled = current.subtract(zero2D).divide(unit2D);

                try {
                    if (expression.evaluate(scaled.getX(), scaled.getZ()) <= 0) {
                        return null; // TODO should return OUTSIDE? seems to cause issues otherwise, workedaround for now
                    }

                    // TODO: Allow biome setting via a script variable (needs BiomeType<->int mapping)
                    return defaultBiomeType;
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to create shape", e);
                    return null;
                }
            }
        };

        return shape.generate(this, biomeType, hollow);
    }

    private static final Vector[] recurseDirections = {
            Direction.NORTH.toVector(),
            Direction.EAST.toVector(),
            Direction.SOUTH.toVector(),
            Direction.WEST.toVector(),
            Direction.UP.toVector(),
            Direction.DOWN.toVector(),
    };

    private static double lengthSq(double x, double y, double z) {
        return (x * x) + (y * y) + (z * z);
    }

    private static double lengthSq(double x, double z) {
        return (x * x) + (z * z);
    }

}
