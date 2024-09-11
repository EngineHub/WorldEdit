/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Watchdog;
import com.sk89q.worldedit.extent.ChangeSetExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.MaskingExtent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.extent.TracingExtent;
import com.sk89q.worldedit.extent.buffer.ForgetfulExtentBuffer;
import com.sk89q.worldedit.extent.buffer.internal.BatchingExtent;
import com.sk89q.worldedit.extent.cache.LastAccessExtentCache;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.extent.inventory.BlockBagExtent;
import com.sk89q.worldedit.extent.reorder.ChunkBatchingExtent;
import com.sk89q.worldedit.extent.reorder.MultiStageReorder;
import com.sk89q.worldedit.extent.validation.BlockChangeLimiter;
import com.sk89q.worldedit.extent.validation.DataValidatorExtent;
import com.sk89q.worldedit.extent.world.ChunkLoadingExtent;
import com.sk89q.worldedit.extent.world.SideEffectExtent;
import com.sk89q.worldedit.extent.world.SurvivalModeExtent;
import com.sk89q.worldedit.extent.world.WatchdogTickingExtent;
import com.sk89q.worldedit.function.GroundFunction;
import com.sk89q.worldedit.function.RegionMaskingFilter;
import com.sk89q.worldedit.function.biome.BiomeReplace;
import com.sk89q.worldedit.function.block.BlockDistributionCounter;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.block.Counter;
import com.sk89q.worldedit.function.block.Naturalizer;
import com.sk89q.worldedit.function.block.SnowSimulator;
import com.sk89q.worldedit.function.generator.ForestGenerator;
import com.sk89q.worldedit.function.generator.GardenPatchGenerator;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.BlockStateMask;
import com.sk89q.worldedit.function.mask.BlockTypeMask;
import com.sk89q.worldedit.function.mask.BoundedHeightMask;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
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
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.WaterloggedRemover;
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
import com.sk89q.worldedit.internal.expression.ExpressionTimeoutException;
import com.sk89q.worldedit.internal.expression.LocalSlot.Variable;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.MathUtils;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.interpolation.Interpolation;
import com.sk89q.worldedit.math.interpolation.KochanekBartelsInterpolation;
import com.sk89q.worldedit.math.interpolation.Node;
import com.sk89q.worldedit.math.noise.RandomNoise;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.CylinderRegion;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionOperationException;
import com.sk89q.worldedit.regions.Regions;
import com.sk89q.worldedit.regions.shape.ArbitraryBiomeShape;
import com.sk89q.worldedit.regions.shape.ArbitraryShape;
import com.sk89q.worldedit.regions.shape.RegionShape;
import com.sk89q.worldedit.regions.shape.WorldEditExpressionEnvironment;
import com.sk89q.worldedit.util.Countable;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.util.collection.DoubleArrayList;
import com.sk89q.worldedit.util.eventbus.EventBus;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.NullWorld;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.regions.Regions.asFlatRegion;
import static com.sk89q.worldedit.regions.Regions.maximumBlockY;
import static com.sk89q.worldedit.regions.Regions.minimumBlockY;

/**
 * An {@link Extent} that handles history, {@link BlockBag}s, change limits,
 * block re-ordering, and much more. Most operations in WorldEdit use this class.
 *
 * <p>Most of the actual functionality is implemented with a number of other
 * {@link Extent}s that are chained together. For example, history is logged
 * using the {@link ChangeSetExtent}.</p>
 */
@SuppressWarnings({"FieldCanBeLocal"})
public class EditSession implements Extent, AutoCloseable {

    private static final Logger LOGGER = LogManagerCompat.getLogger();

    /**
     * Used by {@link EditSession#setBlock(BlockVector3, BlockStateHolder, Stage)} to
     * determine which {@link Extent}s should be bypassed.
     */
    public enum Stage {
        BEFORE_HISTORY,
        BEFORE_REORDER,
        BEFORE_CHANGE
    }

    /**
     * Reorder mode for {@link EditSession#setReorderMode(ReorderMode)}.
     *
     * <p>
     * MULTI_STAGE = Multi stage reorder, may not be great with mods.
     * FAST = Use the fast mode. Good for mods.
     * NONE = Place blocks without worrying about placement order.
     * </p>
     */
    @Deprecated
    public enum ReorderMode {
        MULTI_STAGE("multi"),
        FAST("fast"),
        NONE("none");

        private final String displayName;

        ReorderMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return this.displayName;
        }
    }

    @SuppressWarnings("ProtectedField")
    protected final World world;
    private final @Nullable Actor actor;
    private final ChangeSet changeSet = new BlockOptimizedHistory();

    private @Nullable SideEffectExtent sideEffectExtent;
    private final SurvivalModeExtent survivalExtent;
    private @Nullable BatchingExtent batchingExtent;
    private @Nullable ChunkBatchingExtent chunkBatchingExtent;
    private final BlockBagExtent blockBagExtent;
    @SuppressWarnings("deprecation")
    private final MultiStageReorder reorderExtent;
    private final MaskingExtent maskingExtent;
    private final BlockChangeLimiter changeLimiter;
    private @Nullable ChangeSetExtent changeSetExtent;
    private final List<WatchdogTickingExtent> watchdogExtents = new ArrayList<>(2);

    private final Extent bypassReorderHistory;
    private final Extent bypassHistory;
    private final Extent bypassNone;

    private final @Nullable List<TracingExtent> tracingExtents;

    @Deprecated
    private ReorderMode reorderMode = ReorderMode.FAST;

    private Mask oldMask;

    /**
     * Construct the object with a maximum number of blocks and a block bag.
     *
     * @param eventBus the event bus
     * @param world the world
     * @param maxBlocks the maximum number of blocks that can be changed, or -1 to use no limit
     * @param blockBag an optional {@link BlockBag} to use, otherwise null
     * @param actor the actor that owns the session
     * @param tracing if tracing is enabled. An actor is required if this is {@code true}
     */
    EditSession(EventBus eventBus, World world, int maxBlocks, @Nullable BlockBag blockBag,
                @Nullable Actor actor,
                boolean tracing) {
        checkNotNull(eventBus);
        checkArgument(maxBlocks >= -1, "maxBlocks >= -1 required");

        if (tracing) {
            this.tracingExtents = new ArrayList<>();
            checkNotNull(actor, "An actor is required while tracing");
        } else {
            this.tracingExtents = null;
        }

        this.world = world;
        this.actor = actor;

        if (world != null) {
            EditSessionEvent event = new EditSessionEvent(world, actor, maxBlocks, null);
            Watchdog watchdog = WorldEdit.getInstance().getPlatformManager()
                .queryCapability(Capability.GAME_HOOKS).getWatchdog();
            Extent extent;

            // These extents are ALWAYS used
            extent = traceIfNeeded(sideEffectExtent = new SideEffectExtent(world));
            if (watchdog != null) {
                // Reset watchdog before world placement
                WatchdogTickingExtent watchdogExtent = new WatchdogTickingExtent(extent, watchdog);
                extent = traceIfNeeded(watchdogExtent);
                watchdogExtents.add(watchdogExtent);
            }
            extent = traceIfNeeded(survivalExtent = new SurvivalModeExtent(extent, world));
            extent = traceIfNeeded(new ChunkLoadingExtent(extent, world));
            extent = traceIfNeeded(new LastAccessExtentCache(extent));
            extent = traceIfNeeded(blockBagExtent = new BlockBagExtent(extent, blockBag));
            extent = wrapExtent(extent, eventBus, event, Stage.BEFORE_CHANGE);
            this.bypassReorderHistory = traceIfNeeded(new DataValidatorExtent(extent, world));

            // This extent can be skipped by calling rawSetBlock()
            extent = traceIfNeeded(batchingExtent = new BatchingExtent(extent));
            @SuppressWarnings("deprecation")
            MultiStageReorder reorder = new MultiStageReorder(extent, false);
            extent = traceIfNeeded(reorderExtent = reorder);
            extent = traceIfNeeded(chunkBatchingExtent = new ChunkBatchingExtent(extent, false));
            extent = wrapExtent(extent, eventBus, event, Stage.BEFORE_REORDER);
            if (watchdog != null) {
                // reset before buffering extents, since they may buffer all changes
                // before the world-placement reset can happen, and still cause halts
                WatchdogTickingExtent watchdogExtent = new WatchdogTickingExtent(extent, watchdog);
                extent = traceIfNeeded(watchdogExtent);
                watchdogExtents.add(watchdogExtent);
            }
            this.bypassHistory = traceIfNeeded(new DataValidatorExtent(extent, world));

            // These extents can be skipped by calling smartSetBlock()
            extent = traceIfNeeded(changeSetExtent = new ChangeSetExtent(extent, changeSet));
            extent = traceIfNeeded(maskingExtent = new MaskingExtent(extent, Masks.alwaysTrue()));
            extent = traceIfNeeded(changeLimiter = new BlockChangeLimiter(extent, maxBlocks));
            extent = wrapExtent(extent, eventBus, event, Stage.BEFORE_HISTORY);
            this.bypassNone = traceIfNeeded(new DataValidatorExtent(extent, world));
        } else {
            Extent extent = new NullExtent();
            extent = traceIfNeeded(survivalExtent = new SurvivalModeExtent(extent, NullWorld.getInstance()));
            extent = traceIfNeeded(blockBagExtent = new BlockBagExtent(extent, blockBag));
            @SuppressWarnings("deprecation")
            MultiStageReorder reorder = new MultiStageReorder(extent, false);
            extent = traceIfNeeded(reorderExtent = reorder);
            extent = traceIfNeeded(maskingExtent = new MaskingExtent(extent, Masks.alwaysTrue()));
            extent = traceIfNeeded(changeLimiter = new BlockChangeLimiter(extent, maxBlocks));
            this.bypassReorderHistory = extent;
            this.bypassHistory = extent;
            this.bypassNone = extent;
        }

        setReorderMode(this.reorderMode);
    }

    private Extent traceIfNeeded(Extent input) {
        Extent output = input;
        if (tracingExtents != null) {
            TracingExtent newExtent = new TracingExtent(input);
            output = newExtent;
            tracingExtents.add(newExtent);
        }
        return output;
    }

    private Extent wrapExtent(Extent extent, EventBus eventBus, EditSessionEvent event, Stage stage) {
        // NB: the event does its own tracing
        event = event.clone(stage);
        event.setExtent(extent);
        boolean tracing = tracingExtents != null;
        event.setTracing(tracing);
        eventBus.post(event);
        if (tracing) {
            tracingExtents.addAll(event.getTracingExtents());
        }
        return event.getExtent();
    }

    private boolean commitRequired() {
        if (reorderExtent != null && reorderExtent.commitRequired()) {
            return true;
        }
        if (chunkBatchingExtent != null && chunkBatchingExtent.commitRequired()) {
            return true;
        }
        if (sideEffectExtent != null && sideEffectExtent.commitRequired()) {
            return true;
        }
        return false;
    }

    /**
     * Get the current list of active tracing extents.
     */
    private List<TracingExtent> getActiveTracingExtents() {
        if (tracingExtents == null) {
            return ImmutableList.of();
        }
        return tracingExtents.stream()
            .filter(TracingExtent::isActive)
            .toList();
    }

    /**
     * Turns on specific features for a normal WorldEdit session, such as
     * {@link #setBatchingChunks(boolean)
     * chunk batching}.
     */
    public void enableStandardMode() {
    }

    /**
     * Sets the {@link ReorderMode} of this EditSession, and flushes the session.
     *
     * @param reorderMode The reorder mode
     */
    @Deprecated
    public void setReorderMode(ReorderMode reorderMode) {
        if (world == null && reorderMode == ReorderMode.FAST) {
            // Fast requires a world, for now we can fallback to multi stage, but use "none" in the future.
            reorderMode = ReorderMode.MULTI_STAGE;
        }
        if (reorderMode == ReorderMode.FAST && sideEffectExtent == null) {
            throw new IllegalArgumentException("An EditSession without a fast mode tried to use it for reordering!");
        }
        if (reorderMode == ReorderMode.MULTI_STAGE && reorderExtent == null) {
            throw new IllegalArgumentException("An EditSession without a reorder extent tried to use it for reordering!");
        }
        if (commitRequired()) {
            internalFlushSession();
        }

        this.reorderMode = reorderMode;
        switch (reorderMode) {
            case MULTI_STAGE:
                if (sideEffectExtent != null) {
                    sideEffectExtent.setPostEditSimulationEnabled(false);
                }
                reorderExtent.setEnabled(true);
                break;
            case FAST:
                sideEffectExtent.setPostEditSimulationEnabled(true);
                if (reorderExtent != null) {
                    reorderExtent.setEnabled(false);
                }
                break;
            case NONE:
                if (sideEffectExtent != null) {
                    sideEffectExtent.setPostEditSimulationEnabled(false);
                }
                if (reorderExtent != null) {
                    reorderExtent.setEnabled(false);
                }
                break;
            default:
                break;
        }
    }

    /**
     * Get the reorder mode.
     *
     * @return the reorder mode
     */
    @Deprecated
    public ReorderMode getReorderMode() {
        return reorderMode;
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
     * @deprecated Use {@link EditSession#isBufferingEnabled()} instead.
     */
    @Deprecated
    public boolean isQueueEnabled() {
        return reorderMode == ReorderMode.MULTI_STAGE && reorderExtent.isEnabled();
    }

    /**
     * Queue certain types of block for better reproduction of those blocks.
     *
     * @deprecated There is no specific replacement, instead enable what you want specifically.
     */
    @Deprecated
    public void enableQueue() {
        setReorderMode(ReorderMode.MULTI_STAGE);
    }

    /**
     * Disable the queue. This will flush the session.
     *
     * @deprecated Use {@link EditSession#disableBuffering()} instead.
     */
    @Deprecated
    public void disableQueue() {
        if (isQueueEnabled()) {
            internalFlushSession();
        }
        setReorderMode(ReorderMode.NONE);
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
    @Deprecated
    public void setFastMode(boolean enabled) {
        if (sideEffectExtent != null) {
            sideEffectExtent.setSideEffectSet(enabled ? SideEffectSet.defaults() : SideEffectSet.none());
        }
    }

    /**
     * Set which block updates should occur.
     *
     * @param sideEffectSet side effects to enable
     */
    public void setSideEffectApplier(SideEffectSet sideEffectSet) {
        if (sideEffectExtent != null) {
            sideEffectExtent.setSideEffectSet(sideEffectSet);
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
    @Deprecated
    public boolean hasFastMode() {
        return sideEffectExtent != null && !this.sideEffectExtent.getSideEffectSet().doesApplyAny();
    }

    public SideEffectSet getSideEffectApplier() {
        if (sideEffectExtent == null) {
            return SideEffectSet.defaults();
        }
        return sideEffectExtent.getSideEffectSet();
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
     * Returns chunk batching status.
     *
     * @return whether chunk batching is enabled
     */
    public boolean isBatchingChunks() {
        return chunkBatchingExtent != null && chunkBatchingExtent.isEnabled();
    }

    /**
     * Enable or disable chunk batching. Disabling will flush the session.
     *
     * @param batchingChunks {@code true} to enable, {@code false} to disable
     */
    public void setBatchingChunks(boolean batchingChunks) {
        if (chunkBatchingExtent == null) {
            if (batchingChunks) {
                throw new UnsupportedOperationException("Chunk batching not supported by this session.");
            }
            return;
        }
        assert batchingExtent != null : "same nullness as chunkBatchingExtent";
        if (!batchingChunks && isBatchingChunks()) {
            internalFlushSession();
        }
        chunkBatchingExtent.setEnabled(batchingChunks);
        batchingExtent.setEnabled(!batchingChunks);
    }

    /**
     * Check if this session has any buffering extents enabled.
     *
     * @return {@code true} if any extents are buffering
     */
    public boolean isBufferingEnabled() {
        return isBatchingChunks() || (sideEffectExtent != null && sideEffectExtent.isPostEditSimulationEnabled());
    }

    /**
     * Disable all buffering extents.
     *
     * @see #setReorderMode(ReorderMode)
     * @see #setBatchingChunks(boolean)
     */
    public void disableBuffering() {
        // We optimize here to avoid repeated calls to flushSession.
        if (commitRequired()) {
            internalFlushSession();
        }
        if (sideEffectExtent != null) {
            sideEffectExtent.setPostEditSimulationEnabled(false);
        }
        setReorderMode(ReorderMode.NONE);
        if (chunkBatchingExtent != null) {
            chunkBatchingExtent.setEnabled(false);
            assert batchingExtent != null : "same nullness as chunkBatchingExtent";
            batchingExtent.setEnabled(true);
        }
    }

    /**
     * Check if this session will tick the watchdog.
     *
     * @return {@code true} if any watchdog extent is enabled
     */
    public boolean isTickingWatchdog() {
        return watchdogExtents.stream().anyMatch(WatchdogTickingExtent::isEnabled);
    }

    /**
     * Set all watchdog extents to the given mode.
     */
    public void setTickingWatchdog(boolean active) {
        for (WatchdogTickingExtent extent : watchdogExtents) {
            extent.setEnabled(active);
        }
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
    public BiomeType getBiome(BlockVector3 position) {
        return bypassNone.getBiome(position);
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        return bypassNone.setBiome(position, biome);
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        return world.getBlock(position);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
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
        return getHighestTerrainBlock(x, z, minY, maxY, null);
    }

    /**
     * Returns the highest solid 'terrain' block.
     *
     * @param x the X coordinate
     * @param z the Z coordinate
     * @param minY minimal height
     * @param maxY maximal height
     * @param filter a mask of blocks to consider, or null to consider any solid (movement-blocking) block
     * @return height of highest block found or 'minY'
     */
    public int getHighestTerrainBlock(int x, int z, int minY, int maxY, Mask filter) {
        for (int y = maxY; y >= minY; --y) {
            BlockVector3 pt = BlockVector3.at(x, y, z);
            if (filter == null
                    ? getBlock(pt).getBlockType().getMaterial().isMovementBlocker()
                    : filter.test(pt)) {
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
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, Stage stage) throws WorldEditException {
        return switch (stage) {
            case BEFORE_HISTORY -> bypassNone.setBlock(position, block);
            case BEFORE_CHANGE -> bypassHistory.setBlock(position, block);
            case BEFORE_REORDER -> bypassReorderHistory.setBlock(position, block);
            default -> throw new RuntimeException("New enum entry added that is unhandled here");
        };
    }

    /**
     * Set a block, bypassing both history and block re-ordering.
     *
     * @param position the position to set the block at
     * @param block the block
     * @return whether the block changed
     */
    public <B extends BlockStateHolder<B>> boolean rawSetBlock(BlockVector3 position, B block) {
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
    public <B extends BlockStateHolder<B>> boolean smartSetBlock(BlockVector3 position, B block) {
        try {
            return setBlock(position, block, Stage.BEFORE_REORDER);
        } catch (WorldEditException e) {
            throw new RuntimeException("Unexpected exception", e);
        }
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block) throws MaxChangedBlocksException {
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
    public boolean setBlock(BlockVector3 position, Pattern pattern) throws MaxChangedBlocksException {
        return setBlock(position, pattern.applyBlock(position));
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
    private int setBlocks(Set<BlockVector3> vset, Pattern pattern) throws MaxChangedBlocksException {
        int affected = 0;
        for (BlockVector3 v : vset) {
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
        editSession.internalFlushSession();
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
        editSession.internalFlushSession();
    }

    /**
     * Gets whether this EditSession will track history.
     *
     * @return whether history is tracked
     */
    public boolean isTrackingHistory() {
        return changeSetExtent != null && changeSetExtent.isEnabled();
    }

    /**
     * Sets whether this EditSession will track history.
     *
     * @param trackHistory whether to track history
     */
    public void setTrackingHistory(boolean trackHistory) {
        if (changeSetExtent != null) {
            changeSetExtent.setEnabled(trackHistory);
        } else if (trackHistory) {
            throw new IllegalStateException("No ChangeSetExtent is available");
        }
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
    public BlockVector3 getMinimumPoint() {
        return getWorld().getMinimumPoint();
    }

    @Override
    public BlockVector3 getMaximumPoint() {
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
     * Closing an EditSession flushes its buffers to the world, and performs other
     * cleanup tasks.
     */
    @Override
    public void close() {
        internalFlushSession();
        dumpTracingInformation();
    }

    private void dumpTracingInformation() {
        if (this.tracingExtents == null) {
            return;
        }
        List<TracingExtent> tracingExtents = getActiveTracingExtents();
        assert actor != null;
        if (tracingExtents.isEmpty()) {
            actor.printError(TranslatableComponent.of("worldedit.trace.no-tracing-extents"));
            return;
        }
        // find the common stacks
        Set<List<TracingExtent>> stacks = new LinkedHashSet<>();
        Map<List<TracingExtent>, BlockVector3> stackToPosition = new HashMap<>();
        Set<BlockVector3> touchedLocations = Collections.newSetFromMap(BlockMap.create());
        for (TracingExtent tracingExtent : tracingExtents) {
            touchedLocations.addAll(tracingExtent.getTouchedLocations());
        }
        for (BlockVector3 loc : touchedLocations) {
            List<TracingExtent> stack = tracingExtents.stream()
                    .filter(it -> it.getTouchedLocations().contains(loc))
                    .toList();
            boolean anyFailed = stack.stream()
                .anyMatch(it -> it.getFailedActions().containsKey(loc));
            if (anyFailed && stacks.add(stack)) {
                stackToPosition.put(stack, loc);
            }
        }
        stackToPosition.forEach((stack, position) -> {
            // stack can never be empty, something has to have touched the position
            TracingExtent failure = stack.get(0);
            actor.printDebug(TranslatableComponent.builder("worldedit.trace.action-failed")
                .args(
                    TextComponent.of(failure.getFailedActions().get(position).toString()),
                    TextComponent.of(position.toString()),
                    TextComponent.of(failure.getExtent().getClass().getName())
                )
                .build());
        });
    }

    /**
     * Communicate to the EditSession that all block changes are complete,
     * and that it should apply them to the world.
     *
     * @deprecated Replace with {@link #close()} for proper cleanup behavior.
     */
    @Deprecated
    public void flushSession() {
        internalFlushSession();
    }

    private void internalFlushSession() {
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
     * @return the number of blocks that matched the block
     */
    public int countBlocks(Region region, Set<BaseBlock> searchBlocks) {
        BlockMask mask = new BlockMask(this, searchBlocks);
        return countBlocks(region, mask);
    }

    /**
     * Count the number of blocks of a list of types in a region.
     *
     * @param region the region
     * @param searchMask mask to match
     * @return the number of blocks that matched the mask
     */
    public int countBlocks(Region region, Mask searchMask) {
        Counter count = new Counter();
        RegionMaskingFilter filter = new RegionMaskingFilter(searchMask, count);
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
    public <B extends BlockStateHolder<B>> int fillXZ(BlockVector3 origin, B block, double radius, int depth, boolean recursive) throws MaxChangedBlocksException {
        return fillXZ(origin, (Pattern) block, radius, depth, recursive);
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
    public int fillXZ(BlockVector3 origin, Pattern pattern, double radius, int depth, boolean recursive) throws MaxChangedBlocksException {
        checkNotNull(origin);
        checkNotNull(pattern);
        checkArgument(radius >= 0, "radius >= 0");
        checkArgument(depth >= 1, "depth >= 1");

        // Avoid int overflow (negative coordinate space allows for overflow back round to positive if the depth is large enough).
        // Depth is always 1 or greater, thus the lower bound should always be <= origin y.
        int lowerBound = origin.y() - depth + 1;
        if (lowerBound > origin.y()) {
            lowerBound = Integer.MIN_VALUE;
        }

        MaskIntersection mask = new MaskIntersection(
                new RegionMask(new EllipsoidRegion(null, origin, Vector3.at(radius, radius, radius))),
                new BoundedHeightMask(
                        Math.max(lowerBound, getWorld().getMinY()),
                        Math.min(getWorld().getMaxY(), origin.y())),
                Masks.negate(new ExistingBlockMask(this)));

        // Want to replace blocks
        BlockReplace replace = new BlockReplace(this, pattern);

        // Pick how we're going to visit blocks
        RecursiveVisitor visitor;
        if (recursive) {
            visitor = new RecursiveVisitor(mask, replace);
        } else {
            visitor = new DownwardVisitor(mask, replace, origin.y());
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
    public int removeAbove(BlockVector3 position, int apothem, int height) throws MaxChangedBlocksException {
        checkNotNull(position);
        checkArgument(apothem >= 1, "apothem >= 1");
        checkArgument(height >= 1, "height >= 1");

        Region region = new CuboidRegion(
                getWorld(), // Causes clamping of Y range
                position.add(-apothem + 1, 0, -apothem + 1),
                position.add(apothem - 1, height - 1, apothem - 1));
        return setBlocks(region, BlockTypes.AIR.getDefaultState());
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
    public int removeBelow(BlockVector3 position, int apothem, int height) throws MaxChangedBlocksException {
        checkNotNull(position);
        checkArgument(apothem >= 1, "apothem >= 1");
        checkArgument(height >= 1, "height >= 1");

        Region region = new CuboidRegion(
                getWorld(), // Causes clamping of Y range
                position.add(-apothem + 1, 0, -apothem + 1),
                position.add(apothem - 1, -height + 1, apothem - 1));
        return setBlocks(region, BlockTypes.AIR.getDefaultState());
    }

    /**
     * Remove blocks of a certain type nearby a given position.
     *
     * @param position center position of cuboid
     * @param mask the mask to match
     * @param apothem an apothem of the cuboid, where the minimum is 1
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int removeNear(BlockVector3 position, Mask mask, int apothem) throws MaxChangedBlocksException {
        checkNotNull(position);
        checkArgument(apothem >= 1, "apothem >= 1");

        BlockVector3 adjustment = BlockVector3.ONE.multiply(apothem - 1);
        Region region = new CuboidRegion(
                getWorld(), // Causes clamping of Y range
                position.add(adjustment.multiply(-1)),
                position.add(adjustment));
        return replaceBlocks(region, mask, BlockTypes.AIR.getDefaultState());
    }

    /**
     * Sets all the blocks inside a region to a given block type.
     *
     * @param region the region
     * @param block the block
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public <B extends BlockStateHolder<B>> int setBlocks(Region region, B block) throws MaxChangedBlocksException {
        return setBlocks(region, (Pattern) block);
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
    public <B extends BlockStateHolder<B>> int replaceBlocks(Region region, Set<BaseBlock> filter, B replacement) throws MaxChangedBlocksException {
        return replaceBlocks(region, filter, (Pattern) replacement);
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
    public int replaceBlocks(Region region, Set<BaseBlock> filter, Pattern pattern) throws MaxChangedBlocksException {
        Mask mask = filter == null ? new ExistingBlockMask(this) : new BlockMask(this, filter);
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

        Vector3 center = region.getCenter();
        Region centerRegion = new CuboidRegion(
                getWorld(), // Causes clamping of Y range
                BlockVector3.at(((int) center.x()), ((int) center.y()), ((int) center.z())),
                BlockVector3.at(
                        MathUtils.roundHalfUp(center.x()),
                        MathUtils.roundHalfUp(center.y()),
                        MathUtils.roundHalfUp(center.z())));
        return setBlocks(centerRegion, pattern);
    }

    /**
     * Make the faces of the given region as if it was a {@link CuboidRegion}.
     *
     * @param region the region
     * @param block the block to place
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     * @deprecated Use {@link EditSession#makeCuboidFaces(Region, Pattern)}.
     */
    @Deprecated
    public <B extends BlockStateHolder<B>> int makeCuboidFaces(Region region, B block) throws MaxChangedBlocksException {
        return makeCuboidFaces(region, (Pattern) block);
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
    public <B extends BlockStateHolder<B>> int makeCuboidWalls(Region region, B block) throws MaxChangedBlocksException {
        return makeCuboidWalls(region, (Pattern) block);
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
            final int minY = region.getMinimumPoint().y();
            final int maxY = region.getMaximumPoint().y();
            final ArbitraryShape shape = new RegionShape(region) {
                @Override
                protected BaseBlock getMaterial(int x, int y, int z, BaseBlock defaultMaterial) {
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
     * @deprecated Use {@link EditSession#overlayCuboidBlocks(Region, Pattern)}.
     */
    @Deprecated
    public <B extends BlockStateHolder<B>> int overlayCuboidBlocks(Region region, B block) throws MaxChangedBlocksException {
        checkNotNull(block);

        return overlayCuboidBlocks(region, (Pattern) block);
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
        RegionOffset offset = new RegionOffset(BlockVector3.UNIT_Y, replace);
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
     * Stack a cuboid region. For compatibility, entities are copied by biomes are not.
     * Use {@link #stackCuboidRegion(Region, BlockVector3, int, boolean, boolean, Mask)} to fine tune.
     *
     * @param region the region to stack
     * @param dir the direction to stack
     * @param count the number of times to stack
     * @param copyAir true to also copy air blocks
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int stackCuboidRegion(Region region, BlockVector3 dir, int count, boolean copyAir) throws MaxChangedBlocksException {
        return stackCuboidRegion(region, dir, count, true, false, copyAir ? null : new ExistingBlockMask(this));
    }

    /**
     * Stack a cuboid region.
     *
     * @param region the region to stack
     * @param offset how far to move the contents each stack
     * @param count the number of times to stack
     * @param copyEntities true to copy entities
     * @param copyBiomes true to copy biomes
     * @param mask source mask for the operation (only matching blocks are copied)
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int stackCuboidRegion(Region region, BlockVector3 offset, int count,
                                 boolean copyEntities, boolean copyBiomes, Mask mask) throws MaxChangedBlocksException {
        checkNotNull(region);
        checkNotNull(offset);

        BlockVector3 size = region.getMaximumPoint().subtract(region.getMinimumPoint()).add(1, 1, 1);
        try {
            return stackRegionBlockUnits(region, offset.multiply(size), count, copyEntities, copyBiomes, mask);
        } catch (RegionOperationException e) {
            // Should never be able to happen
            throw new AssertionError(e);
        }
    }

    /**
     * Stack a region using block units.
     *
     * @param region the region to stack
     * @param offset how far to move the contents each stack in block units
     * @param count the number of times to stack
     * @param copyEntities true to copy entities
     * @param copyBiomes true to copy biomes
     * @param mask source mask for the operation (only matching blocks are copied)
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     * @throws RegionOperationException thrown if the region operation is invalid
     */
    public int stackRegionBlockUnits(Region region, BlockVector3 offset, int count,
                                     boolean copyEntities, boolean copyBiomes, Mask mask) throws MaxChangedBlocksException, RegionOperationException {
        checkNotNull(region);
        checkNotNull(offset);
        checkArgument(count >= 1, "count >= 1 required");

        BlockVector3 size = region.getMaximumPoint().subtract(region.getMinimumPoint()).add(1, 1, 1);
        BlockVector3 offsetAbs = offset.abs();
        if (offsetAbs.x() < size.x() && offsetAbs.y() < size.y() && offsetAbs.z() < size.z()) {
            throw new RegionOperationException(TranslatableComponent.of("worldedit.stack.intersecting-region"));
        }
        BlockVector3 to = region.getMinimumPoint();
        ForwardExtentCopy copy = new ForwardExtentCopy(this, region, this, to);
        copy.setRepetitions(count);
        copy.setTransform(new AffineTransform().translate(offset));
        copy.setCopyingEntities(copyEntities);
        copy.setCopyingBiomes(copyBiomes);
        if (mask != null) {
            copy.setSourceMask(mask);
        }
        Operations.completeLegacy(copy);
        return copy.getAffected();
    }

    /**
     * Move the blocks in a region a certain direction.
     *
     * @param region the region to move
     * @param offset the offset
     * @param multiplier the number to multiply the offset by
     * @param copyAir true to copy air blocks
     * @param replacement the replacement pattern to fill in after moving, or null to use air
     * @return number of blocks moved
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int moveRegion(Region region, BlockVector3 offset, int multiplier, boolean copyAir, Pattern replacement) throws MaxChangedBlocksException {
        return moveRegion(region, offset, multiplier, true, false, copyAir ? new ExistingBlockMask(this) : null, replacement);
    }

    /**
     * Move the blocks in a region a certain direction.
     *
     * @param region the region to move
     * @param offset the offset
     * @param multiplier the number to multiply the offset by
     * @param moveEntities true to move entities
     * @param copyBiomes true to copy biomes (source biome is unchanged)
     * @param mask source mask for the operation (only matching blocks are moved)
     * @param replacement the replacement pattern to fill in after moving, or null to use air
     * @return number of blocks moved
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     * @throws IllegalArgumentException thrown if the region is not a flat region, but copyBiomes is true
     */
    public int moveRegion(Region region, BlockVector3 offset, int multiplier,
                          boolean moveEntities, boolean copyBiomes, Mask mask, Pattern replacement) throws MaxChangedBlocksException {
        checkNotNull(region);
        checkNotNull(offset);
        checkArgument(multiplier >= 1, "multiplier >= 1 required");
        checkArgument(!copyBiomes || region instanceof FlatRegion, "can't copy biomes from non-flat region");

        BlockVector3 to = region.getMinimumPoint();

        // Remove the original blocks
        Pattern pattern = replacement != null
            ? replacement
            : BlockTypes.AIR.getDefaultState();
        BlockReplace remove = new BlockReplace(this, pattern);

        // Copy to a buffer so we don't destroy our original before we can copy all the blocks from it
        ForgetfulExtentBuffer buffer = new ForgetfulExtentBuffer(this, new RegionMask(region));
        ForwardExtentCopy copy = new ForwardExtentCopy(this, region, buffer, to);
        copy.setTransform(new AffineTransform().translate(offset.multiply(multiplier)));
        copy.setSourceFunction(remove); // Remove

        copy.setCopyingEntities(moveEntities);
        copy.setRemovingEntities(moveEntities);
        copy.setCopyingBiomes(copyBiomes);

        if (mask != null) {
            copy.setSourceMask(mask);
        }

        // Then we need to copy the buffer to the world
        BlockReplace replace = new BlockReplace(this, buffer);
        RegionVisitor visitor = new RegionVisitor(buffer.asRegion(), replace);

        OperationQueue operation = new OperationQueue(copy, visitor);

        if (copyBiomes) {
            BiomeReplace biomeReplace = new BiomeReplace(this, buffer);
            RegionVisitor biomeVisitor = new RegionVisitor(buffer.asRegion(), biomeReplace);
            operation.offer(biomeVisitor);
        }

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
     * @param replacement the replacement pattern to fill in after moving, or null to use air
     * @return number of blocks moved
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int moveCuboidRegion(Region region, BlockVector3 dir, int distance, boolean copyAir, Pattern replacement) throws MaxChangedBlocksException {
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
    public int drainArea(BlockVector3 origin, double radius) throws MaxChangedBlocksException {
        return drainArea(origin, radius, false);
    }

    /**
     * Drain nearby pools of water or lava, optionally removed waterlogged states from blocks.
     *
     * @param origin the origin to drain from, which will search a 3x3 area
     * @param radius the radius of the removal, where a value should be 0 or greater
     * @param waterlogged true to make waterlogged blocks non-waterlogged as well
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int drainArea(BlockVector3 origin, double radius, boolean waterlogged) throws MaxChangedBlocksException {
        checkNotNull(origin);
        checkArgument(radius >= 0, "radius >= 0 required");

        Mask waterloggedMask = null;
        if (waterlogged) {
            Map<String, String> stateMap = new HashMap<>();
            stateMap.put("waterlogged", "true");
            waterloggedMask = new BlockStateMask(this, stateMap, true);
        }
        MaskIntersection mask = new MaskIntersection(
                new BoundedHeightMask(getWorld().getMinY(), getWorld().getMaxY()),
                new RegionMask(new EllipsoidRegion(null, origin, Vector3.at(radius, radius, radius))),
                waterlogged ? new MaskUnion(getWorld().createLiquidMask(), waterloggedMask)
                            : getWorld().createLiquidMask());

        BlockReplace replace;
        if (waterlogged) {
            replace = new BlockReplace(this, new WaterloggedRemover(this));
        } else {
            replace = new BlockReplace(this, BlockTypes.AIR.getDefaultState());
        }
        RecursiveVisitor visitor = new RecursiveVisitor(mask, replace);

        // Around the origin in a 3x3 block
        for (BlockVector3 position : CuboidRegion.fromCenter(origin, 1)) {
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
    public int fixLiquid(BlockVector3 origin, double radius, BlockType fluid) throws MaxChangedBlocksException {
        checkNotNull(origin);
        checkArgument(radius >= 0, "radius >= 0 required");

        // Our origins can only be liquids
        Mask liquidMask = new BlockTypeMask(this, fluid);

        // But we will also visit air blocks
        MaskIntersection blockMask = new MaskUnion(liquidMask, Masks.negate(new ExistingBlockMask(this)));

        // There are boundaries that the routine needs to stay in
        MaskIntersection mask = new MaskIntersection(
                new BoundedHeightMask(getWorld().getMinY(), Math.min(origin.y(), getWorld().getMaxY())),
                new RegionMask(new EllipsoidRegion(null, origin, Vector3.at(radius, radius, radius))),
                blockMask
        );

        BlockReplace replace = new BlockReplace(this, fluid.getDefaultState());
        NonRisingVisitor visitor = new NonRisingVisitor(mask, replace);

        // Around the origin in a 3x3 block
        for (BlockVector3 position : CuboidRegion.fromCenter(origin, 1)) {
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
    public int makeCylinder(BlockVector3 pos, Pattern block, double radius, int height, boolean filled) throws MaxChangedBlocksException {
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
    public int makeCylinder(BlockVector3 pos, Pattern block, double radiusX, double radiusZ, int height, boolean filled) throws MaxChangedBlocksException {
        int affected = 0;

        radiusX += 0.5;
        radiusZ += 0.5;

        if (height == 0) {
            return 0;
        } else if (height < 0) {
            height = -height;
            pos = pos.subtract(0, height, 0);
        }

        if (pos.y() < world.getMinY()) {
            pos = pos.withY(world.getMinY());
        } else if (pos.y() + height - 1 > world.getMaxY()) {
            height = world.getMaxY() - pos.y() + 1;
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
     * Makes a cone.
     *
     * @param pos Center of the cone
     * @param block The block pattern to use
     * @param radiusX The cone's largest north/south extent
     * @param radiusZ The cone's largest east/west extent
     * @param height The cone's up/down extent. If negative, extend downward.
     * @param filled If false, only a shell will be generated.
     * @param thickness The cone's wall thickness, if it's hollow.
     * @return number of blocks changed
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeCone(BlockVector3 pos, Pattern block, double radiusX, double radiusZ, int height, boolean filled,
                        double thickness) throws MaxChangedBlocksException {
        int affected = 0;

        final int ceilRadiusX = (int) Math.ceil(radiusX);
        final int ceilRadiusZ = (int) Math.ceil(radiusZ);
        final double radiusXPow = Math.pow(radiusX, 2);
        final double radiusZPow = Math.pow(radiusZ, 2);
        final double heightPow = Math.pow(height, 2);

        for (int y = 0; y < height; ++y) {
            double ySquaredMinusHeightOverHeightSquared = Math.pow(y - height, 2) / heightPow;

            forX:
            for (int x = 0; x <= ceilRadiusX; ++x) {
                double xSquaredOverRadiusX = Math.pow(x, 2) / radiusXPow;

                for (int z = 0; z <= ceilRadiusZ; ++z) {
                    double zSquaredOverRadiusZ = Math.pow(z, 2) / radiusZPow;
                    double distanceFromOriginMinusHeightSquared = xSquaredOverRadiusX + zSquaredOverRadiusZ
                        - ySquaredMinusHeightOverHeightSquared;

                    if (distanceFromOriginMinusHeightSquared > 1) {
                        if (z == 0) {
                            break forX;
                        }
                        break;
                    }

                    if (!filled) {
                        double xNext = Math.pow(x + thickness, 2) / radiusXPow
                            + zSquaredOverRadiusZ - ySquaredMinusHeightOverHeightSquared;
                        double yNext = xSquaredOverRadiusX + zSquaredOverRadiusZ
                            - Math.pow(y + thickness - height, 2) / heightPow;
                        double zNext = xSquaredOverRadiusX + Math.pow(z + thickness, 2)
                            / radiusZPow - ySquaredMinusHeightOverHeightSquared;
                        if (xNext <= 0 && zNext <= 0 && (yNext <= 0 && y + thickness != height)) {
                            continue;
                        }
                    }

                    if (distanceFromOriginMinusHeightSquared <= 0) {
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
    public int makeSphere(BlockVector3 pos, Pattern block, double radius, boolean filled) throws MaxChangedBlocksException {
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
    public int makeSphere(BlockVector3 pos, Pattern block, double radiusX, double radiusY, double radiusZ, boolean filled) throws MaxChangedBlocksException {
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
    public int makePyramid(BlockVector3 position, Pattern block, int size, boolean filled) throws MaxChangedBlocksException {
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
     * @deprecated Use {@link #thaw(BlockVector3, double, int)}.
     */
    @Deprecated
    public int thaw(BlockVector3 position, double radius)
        throws MaxChangedBlocksException {
        return thaw(position, radius,
            WorldEdit.getInstance().getConfiguration().defaultVerticalHeight);
    }

    /**
     * Thaw blocks in a cylinder.
     *
     * @param position the position
     * @param radius the radius
     * @param height the height (upwards and downwards)
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int thaw(BlockVector3 position, double radius, int height)
        throws MaxChangedBlocksException {
        int affected = 0;
        double radiusSq = radius * radius;

        int ox = position.x();
        int oy = position.y();
        int oz = position.z();

        BlockState air = BlockTypes.AIR.getDefaultState();
        BlockState water = BlockTypes.WATER.getDefaultState();

        int centerY = Math.max(getWorld().getMinY(), Math.min(getWorld().getMaxY(), oy));
        int minY = Math.max(getWorld().getMinY(), centerY - height);
        int maxY = Math.min(getWorld().getMaxY(), centerY + height);

        int ceilRadius = (int) Math.ceil(radius);
        for (int x = ox - ceilRadius; x <= ox + ceilRadius; ++x) {
            for (int z = oz - ceilRadius; z <= oz + ceilRadius; ++z) {
                if ((BlockVector3.at(x, oy, z)).distanceSq(position) > radiusSq) {
                    continue;
                }

                for (int y = maxY; y > minY; --y) {
                    BlockVector3 pt = BlockVector3.at(x, y, z);
                    BlockType id = getBlock(pt).getBlockType();

                    if (id == BlockTypes.ICE) {
                        if (setBlock(pt, water)) {
                            ++affected;
                        }
                    } else if (id == BlockTypes.SNOW) {
                        if (setBlock(pt, air)) {
                            ++affected;
                        }
                    } else if (id.getMaterial().isAir()) {
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
     * @deprecated Use {@link #simulateSnow(BlockVector3, double, int)}.
     */
    @Deprecated
    public int simulateSnow(BlockVector3 position, double radius) throws MaxChangedBlocksException {
        return simulateSnow(position, radius,
            WorldEdit.getInstance().getConfiguration().defaultVerticalHeight);
    }

    /**
     * Make snow in a cylinder.
     *
     * @param position a position
     * @param radius a radius
     * @param height the height (upwards and downwards)
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int simulateSnow(BlockVector3 position, double radius, int height)
        throws MaxChangedBlocksException {

        return simulateSnow(new CylinderRegion(position, Vector2.at(radius, radius), position.y(), height), false);
    }


    /**
     * Make snow in a region.
     *
     * @param region the region to simulate snow in
     * @param stack whether it should stack existing snow
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int simulateSnow(FlatRegion region, boolean stack)
            throws MaxChangedBlocksException {
        checkNotNull(region);

        SnowSimulator snowSimulator = new SnowSimulator(this, stack);
        LayerVisitor layerVisitor = new LayerVisitor(region, region.getMinimumY(), region.getMaximumY(), snowSimulator);
        Operations.completeLegacy(layerVisitor);
        return snowSimulator.getAffected();
    }

    /**
     * Make dirt green.
     *
     * @param position a position
     * @param radius a radius
     * @param onlyNormalDirt only affect normal dirt (all default properties)
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     * @deprecated Use {@link #green(BlockVector3, double, int, boolean)}.
     */
    @Deprecated
    public int green(BlockVector3 position, double radius, boolean onlyNormalDirt)
        throws MaxChangedBlocksException {
        return green(position, radius,
            WorldEdit.getInstance().getConfiguration().defaultVerticalHeight, onlyNormalDirt);
    }

    /**
     * Make dirt green in a cylinder.
     *
     * @param position the position
     * @param radius the radius
     * @param height the height
     * @param onlyNormalDirt only affect normal dirt (all default properties)
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int green(BlockVector3 position, double radius, int height, boolean onlyNormalDirt)
        throws MaxChangedBlocksException {
        int affected = 0;
        final double radiusSq = radius * radius;

        final int ox = position.x();
        final int oy = position.y();
        final int oz = position.z();

        final BlockState grass = BlockTypes.GRASS_BLOCK.getDefaultState();

        final int centerY = Math.max(getWorld().getMinY(), Math.min(getWorld().getMaxY(), oy));
        final int minY = Math.max(getWorld().getMinY(), centerY - height);
        final int maxY = Math.min(getWorld().getMaxY(), centerY + height);

        final int ceilRadius = (int) Math.ceil(radius);
        for (int x = ox - ceilRadius; x <= ox + ceilRadius; ++x) {
            for (int z = oz - ceilRadius; z <= oz + ceilRadius; ++z) {
                if ((BlockVector3.at(x, oy, z)).distanceSq(position) > radiusSq) {
                    continue;
                }

                for (int y = maxY; y > minY; --y) {
                    final BlockVector3 pt = BlockVector3.at(x, y, z);
                    final BlockState block = getBlock(pt);

                    if (block.getBlockType() == BlockTypes.DIRT
                        || (!onlyNormalDirt && block.getBlockType() == BlockTypes.COARSE_DIRT)) {
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
    public int makePumpkinPatches(BlockVector3 position, int apothem) throws MaxChangedBlocksException {
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
    public int makeForest(BlockVector3 basePosition, int size, double density, TreeGenerator.TreeType treeType) throws MaxChangedBlocksException {
        return makeForest(CuboidRegion.fromCenter(basePosition, size), density, treeType);
    }

    /**
     * Makes a forest.
     *
     * @param region the region to generate trees in
     * @param density between 0 and 1, inclusive
     * @param treeType the tree type
     * @return number of trees created
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int makeForest(Region region, double density, TreeGenerator.TreeType treeType) throws MaxChangedBlocksException {
        ForestGenerator generator = new ForestGenerator(this, treeType);
        GroundFunction ground = new GroundFunction(new ExistingBlockMask(this), generator);
        LayerVisitor visitor = new LayerVisitor(asFlatRegion(region), minimumBlockY(region), maximumBlockY(region), ground);
        visitor.setMask(new NoiseFilter2D(new RandomNoise(), density));
        Operations.completeLegacy(visitor);
        return ground.getAffected();
    }

    /**
     * Get the block distribution inside a region.
     *
     * @param region a region
     * @return the results
     */
    public List<Countable<BlockState>> getBlockDistribution(Region region, boolean separateStates) {
        BlockDistributionCounter count = new BlockDistributionCounter(this, separateStates);
        RegionVisitor visitor = new RegionVisitor(region, count);
        Operations.completeBlindly(visitor);
        return count.getDistribution();
    }

    /**
     * Generate a shape for the given expression.
     *
     * @param region the region to generate the shape in
     * @param zero the coordinate origin for x/y/z variables
     * @param unit the scale of the x/y/z/ variables
     * @param pattern the default material to make the shape from
     * @param expressionString the expression defining the shape
     * @param hollow whether the shape should be hollow
     * @return number of blocks changed
     * @throws ExpressionException if there is a problem with the expression
     * @throws MaxChangedBlocksException if the maximum block change limit is exceeded
     */
    public int makeShape(final Region region, final Vector3 zero, final Vector3 unit,
                         final Pattern pattern, final String expressionString, final boolean hollow)
            throws ExpressionException, MaxChangedBlocksException {
        return makeShape(region, zero, unit, pattern, expressionString, hollow, WorldEdit.getInstance().getConfiguration().calculationTimeout);
    }

    /**
     * Generate a shape for the given expression.
     *
     * @param region the region to generate the shape in
     * @param zero the coordinate origin for x/y/z variables
     * @param unit the scale of the x/y/z/ variables
     * @param pattern the default material to make the shape from
     * @param expressionString the expression defining the shape
     * @param hollow whether the shape should be hollow
     * @param timeout the time, in milliseconds, to wait for each expression evaluation before halting it. -1 to disable
     * @return number of blocks changed
     * @throws ExpressionException if there is a problem with the expression
     * @throws MaxChangedBlocksException if the maximum block change limit is exceeded
     */
    public int makeShape(final Region region, final Vector3 zero, final Vector3 unit,
                         final Pattern pattern, final String expressionString, final boolean hollow, final int timeout)
            throws ExpressionException, MaxChangedBlocksException {
        final Expression expression = Expression.compile(expressionString, "x", "y", "z", "type", "data");
        expression.optimize();
        return makeShape(region, zero, unit, pattern, expression, hollow, timeout);
    }

    /**
     * Internal version of {@link EditSession#makeShape(Region, Vector3, Vector3, Pattern, String, boolean, int)}.
     *
     * <p>
     * The Expression class is subject to change. Expressions should be provided via the string overload.
     * </p>
     */
    public int makeShape(final Region region, final Vector3 zero, final Vector3 unit,
                         final Pattern pattern, final Expression expression, final boolean hollow, final int timeout)
            throws ExpressionException, MaxChangedBlocksException {

        expression.getSlots().getVariable("x")
            .orElseThrow(IllegalStateException::new);
        expression.getSlots().getVariable("y")
            .orElseThrow(IllegalStateException::new);
        expression.getSlots().getVariable("z")
            .orElseThrow(IllegalStateException::new);

        final Variable typeVariable = expression.getSlots().getVariable("type")
            .orElseThrow(IllegalStateException::new);
        final Variable dataVariable = expression.getSlots().getVariable("data")
            .orElseThrow(IllegalStateException::new);

        final WorldEditExpressionEnvironment environment = new WorldEditExpressionEnvironment(this, unit, zero);
        expression.setEnvironment(environment);

        final int[] timedOut = {0};
        final ArbitraryShape shape = new ArbitraryShape(region) {
            @Override
            protected BaseBlock getMaterial(int x, int y, int z, BaseBlock defaultMaterial) {
                final Vector3 current = Vector3.at(x, y, z);
                environment.setCurrentBlock(current);
                final Vector3 scaled = current.subtract(zero).divide(unit);

                try {
                    int[] legacy = LegacyMapper.getInstance().getLegacyFromBlock(defaultMaterial.toImmutableState());
                    int typeVar = 0;
                    int dataVar = 0;
                    if (legacy != null) {
                        typeVar = legacy[0];
                        if (legacy.length > 1) {
                            dataVar = legacy[1];
                        }
                    }
                    if (expression.evaluate(new double[]{ scaled.x(), scaled.y(), scaled.z(), typeVar, dataVar}, timeout) <= 0) {
                        return null;
                    }
                    int newType = (int) typeVariable.value();
                    int newData = (int) dataVariable.value();
                    if (newType != typeVar || newData != dataVar) {
                        BlockState state = LegacyMapper.getInstance().getBlockFromLegacy(newType, newData);
                        return state == null ? defaultMaterial : state.toBaseBlock();
                    } else {
                        return defaultMaterial;
                    }
                } catch (ExpressionTimeoutException e) {
                    timedOut[0] = timedOut[0] + 1;
                    return null;
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        int changed = shape.generate(this, pattern, hollow);
        if (timedOut[0] > 0) {
            throw new ExpressionTimeoutException(
                    String.format("%d blocks changed. %d blocks took too long to evaluate (increase with //timeout).",
                            changed, timedOut[0]));
        }
        return changed;
    }

    /**
     * Deforms the region by a given expression. A deform provides a block's x, y, and z coordinates (possibly scaled)
     * to an expression, and then sets the block to the block given by the resulting values of the variables, if they
     * have changed.
     *
     * @param region the region to deform
     * @param zero the origin of the coordinate system
     * @param unit the scale of the coordinate system
     * @param expressionString the expression to evaluate for each block
     *
     * @return number of blocks changed
     *
     * @throws ExpressionException thrown on invalid expression input
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int deformRegion(final Region region, final Vector3 zero, final Vector3 unit, final String expressionString)
            throws ExpressionException, MaxChangedBlocksException {
        return deformRegion(region, zero, unit, expressionString, WorldEdit.getInstance().getConfiguration().calculationTimeout);
    }

    /**
     * Deforms the region by a given expression. A deform provides a block's x, y, and z coordinates (possibly scaled)
     * to an expression, and then sets the block to the block given by the resulting values of the variables, if they
     * have changed.
     *
     * @param region the region to deform
     * @param zero the origin of the coordinate system
     * @param unit the scale of the coordinate system
     * @param expressionString the expression to evaluate for each block
     * @param timeout maximum time for the expression to evaluate for each block. -1 for unlimited.
     *
     * @return number of blocks changed
     *
     * @throws ExpressionException thrown on invalid expression input
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int deformRegion(final Region region, final Vector3 zero, final Vector3 unit, final String expressionString,
                            final int timeout) throws ExpressionException, MaxChangedBlocksException {
        final Expression expression = Expression.compile(expressionString, "x", "y", "z");
        expression.optimize();
        return deformRegion(region, zero, unit, expression, timeout);
    }

    /**
     * Internal version of {@link EditSession#deformRegion(Region, Vector3, Vector3, String, int)}.
     *
     * <p>
     * The Expression class is subject to change. Expressions should be provided via the string overload.
     * </p>
     */
    public int deformRegion(final Region region, final Vector3 zero, final Vector3 unit, final Expression expression,
                            final int timeout) throws ExpressionException, MaxChangedBlocksException {
        final Variable x = expression.getSlots().getVariable("x")
            .orElseThrow(IllegalStateException::new);
        final Variable y = expression.getSlots().getVariable("y")
            .orElseThrow(IllegalStateException::new);
        final Variable z = expression.getSlots().getVariable("z")
            .orElseThrow(IllegalStateException::new);

        final WorldEditExpressionEnvironment environment = new WorldEditExpressionEnvironment(this, unit, zero);
        expression.setEnvironment(environment);

        final DoubleArrayList<BlockVector3, BaseBlock> queue = new DoubleArrayList<>(false);

        for (BlockVector3 targetBlockPosition : region) {
            final Vector3 targetPosition = targetBlockPosition.toVector3();
            environment.setCurrentBlock(targetPosition);

            // offset, scale
            final Vector3 scaled = targetPosition.subtract(zero).divide(unit);

            // transform
            expression.evaluate(new double[]{ scaled.x(), scaled.y(), scaled.z() }, timeout);

            final BlockVector3 sourcePosition = environment.toWorld(x.value(), y.value(), z.value());

            // read block from world
            final BaseBlock material = world.getFullBlock(sourcePosition);

            // queue operation
            queue.put(targetBlockPosition, material);
        }

        int affected = 0;
        for (Map.Entry<BlockVector3, BaseBlock> entry : queue) {
            BlockVector3 position = entry.getKey();
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

        final Set<BlockVector3> outside = new HashSet<>();

        final BlockVector3 min = region.getMinimumPoint();
        final BlockVector3 max = region.getMaximumPoint();

        final int minX = min.x();
        final int minY = min.y();
        final int minZ = min.z();
        final int maxX = max.x();
        final int maxY = max.y();
        final int maxZ = max.z();

        for (int x = minX; x <= maxX; ++x) {
            for (int y = minY; y <= maxY; ++y) {
                recurseHollow(region, BlockVector3.at(x, y, minZ), outside);
                recurseHollow(region, BlockVector3.at(x, y, maxZ), outside);
            }
        }

        for (int y = minY; y <= maxY; ++y) {
            for (int z = minZ; z <= maxZ; ++z) {
                recurseHollow(region, BlockVector3.at(minX, y, z), outside);
                recurseHollow(region, BlockVector3.at(maxX, y, z), outside);
            }
        }

        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                recurseHollow(region, BlockVector3.at(x, minY, z), outside);
                recurseHollow(region, BlockVector3.at(x, maxY, z), outside);
            }
        }

        final Set<BlockVector3> newOutside = new HashSet<>();
        for (int i = 1; i < thickness; ++i) {
            outer: for (BlockVector3 position : region) {
                for (BlockVector3 recurseDirection : recurseDirections) {
                    BlockVector3 neighbor = position.add(recurseDirection);

                    if (outside.contains(neighbor)) {
                        newOutside.add(position);
                        continue outer;
                    }
                }
            }

            outside.addAll(newOutside);
            newOutside.clear();
        }

        outer: for (BlockVector3 position : region) {
            for (BlockVector3 recurseDirection : recurseDirections) {
                BlockVector3 neighbor = position.add(recurseDirection);

                if (outside.contains(neighbor)) {
                    continue outer;
                }
            }

            if (setBlock(position, pattern.applyBlock(position))) {
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
     *
     * @see #drawLine(Pattern, List, double, boolean)
     */
    public int drawLine(Pattern pattern, BlockVector3 pos1, BlockVector3 pos2, double radius, boolean filled)
            throws MaxChangedBlocksException {
        return drawLine(pattern, ImmutableList.of(pos1, pos2), radius, filled);
    }

    /**
     * Draws a line (out of blocks) between two or more vectors.
     *
     * @param pattern The block pattern used to draw the line.
     * @param vectors the list of vectors to draw the line between
     * @param radius The radius (thickness) of the line.
     * @param filled If false, only a shell will be generated.
     *
     * @return number of blocks affected
     * @throws MaxChangedBlocksException thrown if too many blocks are changed
     */
    public int drawLine(Pattern pattern, List<BlockVector3> vectors, double radius, boolean filled)
            throws MaxChangedBlocksException {

        Set<BlockVector3> vset = new HashSet<>();

        for (int i = 0; !vectors.isEmpty() && i < vectors.size() - 1; i++) {
            BlockVector3 pos1 = vectors.get(i);
            BlockVector3 pos2 = vectors.get(i + 1);

            int x1 = pos1.x();
            int y1 = pos1.y();
            int z1 = pos1.z();
            int x2 = pos2.x();
            int y2 = pos2.y();
            int z2 = pos2.z();
            int tipx = x1;
            int tipy = y1;
            int tipz = z1;
            int dx = Math.abs(x2 - x1);
            int dy = Math.abs(y2 - y1);
            int dz = Math.abs(z2 - z1);

            if (dx + dy + dz == 0) {
                vset.add(BlockVector3.at(tipx, tipy, tipz));
                continue;
            }

            int dMax = Math.max(Math.max(dx, dy), dz);
            if (dMax == dx) {
                for (int domstep = 0; domstep <= dx; domstep++) {
                    tipx = x1 + domstep * (x2 - x1 > 0 ? 1 : -1);
                    tipy = (int) Math.round(y1 + domstep * ((double) dy) / ((double) dx) * (y2 - y1 > 0 ? 1 : -1));
                    tipz = (int) Math.round(z1 + domstep * ((double) dz) / ((double) dx) * (z2 - z1 > 0 ? 1 : -1));

                    vset.add(BlockVector3.at(tipx, tipy, tipz));
                }
            } else if (dMax == dy) {
                for (int domstep = 0; domstep <= dy; domstep++) {
                    tipy = y1 + domstep * (y2 - y1 > 0 ? 1 : -1);
                    tipx = (int) Math.round(x1 + domstep * ((double) dx) / ((double) dy) * (x2 - x1 > 0 ? 1 : -1));
                    tipz = (int) Math.round(z1 + domstep * ((double) dz) / ((double) dy) * (z2 - z1 > 0 ? 1 : -1));

                    vset.add(BlockVector3.at(tipx, tipy, tipz));
                }
            } else /* if (dMax == dz) */ {
                for (int domstep = 0; domstep <= dz; domstep++) {
                    tipz = z1 + domstep * (z2 - z1 > 0 ? 1 : -1);
                    tipy = (int) Math.round(y1 + domstep * ((double) dy) / ((double) dz) * (y2 - y1 > 0 ? 1 : -1));
                    tipx = (int) Math.round(x1 + domstep * ((double) dx) / ((double) dz) * (x2 - x1 > 0 ? 1 : -1));

                    vset.add(BlockVector3.at(tipx, tipy, tipz));
                }
            }
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
    public int drawSpline(Pattern pattern, List<BlockVector3> nodevectors, double tension, double bias,
                          double continuity, double quality, double radius, boolean filled)
            throws MaxChangedBlocksException {

        Set<BlockVector3> vset = new HashSet<>();
        List<Node> nodes = new ArrayList<>(nodevectors.size());

        Interpolation interpol = new KochanekBartelsInterpolation();

        for (BlockVector3 nodevector : nodevectors) {
            Node n = new Node(nodevector.toVector3().add(Vector3.at(0.5D, 0.5D, 0.5D)));
            n.setTension(tension);
            n.setBias(bias);
            n.setContinuity(continuity);
            nodes.add(n);
        }

        interpol.setNodes(nodes);
        double splinelength = interpol.arcLength(0, 1);
        for (double loop = 0; loop <= 1; loop += 1D / splinelength / quality) {
            Vector3 tipv = interpol.getPosition(loop);

            vset.add(tipv.toBlockPoint());
        }

        vset = getBallooned(vset, radius);
        if (!filled) {
            vset = getHollowed(vset);
        }
        return setBlocks(vset, pattern);
    }

    private static Set<BlockVector3> getBallooned(Set<BlockVector3> vset, double radius) {
        Set<BlockVector3> returnset = new HashSet<>();
        int ceilrad = (int) Math.ceil(radius);
        double radiusSquare = Math.pow(radius, 2);

        for (BlockVector3 v : vset) {
            int tipx = v.x();
            int tipy = v.y();
            int tipz = v.z();

            for (int loopx = tipx - ceilrad; loopx <= tipx + ceilrad; loopx++) {
                for (int loopy = tipy - ceilrad; loopy <= tipy + ceilrad; loopy++) {
                    for (int loopz = tipz - ceilrad; loopz <= tipz + ceilrad; loopz++) {
                        if (lengthSq(loopx - tipx, loopy - tipy, loopz - tipz) <= radiusSquare) {
                            returnset.add(BlockVector3.at(loopx, loopy, loopz));
                        }
                    }
                }
            }
        }
        return returnset;
    }

    private static Set<BlockVector3> getHollowed(Set<BlockVector3> vset) {
        Set<BlockVector3> returnset = new HashSet<>();
        for (BlockVector3 v : vset) {
            double x = v.x();
            double y = v.y();
            double z = v.z();
            if (!(vset.contains(BlockVector3.at(x + 1, y, z))
                && vset.contains(BlockVector3.at(x - 1, y, z))
                && vset.contains(BlockVector3.at(x, y + 1, z))
                && vset.contains(BlockVector3.at(x, y - 1, z))
                && vset.contains(BlockVector3.at(x, y, z + 1))
                && vset.contains(BlockVector3.at(x, y, z - 1)))) {
                returnset.add(v);
            }
        }
        return returnset;
    }

    private void recurseHollow(Region region, BlockVector3 origin, Set<BlockVector3> outside) {
        final LinkedList<BlockVector3> queue = new LinkedList<>();
        queue.addLast(origin);

        while (!queue.isEmpty()) {
            final BlockVector3 current = queue.removeFirst();
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

            for (BlockVector3 recurseDirection : recurseDirections) {
                queue.addLast(current.add(recurseDirection));
            }
        }
    }

    public int makeBiomeShape(final Region region, final Vector3 zero, final Vector3 unit, final BiomeType biomeType,
                              final String expressionString, final boolean hollow) throws ExpressionException {
        return makeBiomeShape(region, zero, unit, biomeType, expressionString, hollow, WorldEdit.getInstance().getConfiguration().calculationTimeout);
    }

    public int makeBiomeShape(final Region region, final Vector3 zero, final Vector3 unit, final BiomeType biomeType,
                              final String expressionString, final boolean hollow, final int timeout) throws ExpressionException {

        final Expression expression = Expression.compile(expressionString, "x", "y", "z");
        expression.optimize();

        final EditSession editSession = this;
        final WorldEditExpressionEnvironment environment = new WorldEditExpressionEnvironment(editSession, unit, zero);
        expression.setEnvironment(environment);

        AtomicInteger timedOut = new AtomicInteger();
        final ArbitraryBiomeShape shape = new ArbitraryBiomeShape(region) {
            @Override
            protected BiomeType getBiome(int x, int y, int z, BiomeType defaultBiomeType) {
                final Vector3 current = Vector3.at(x, y, z);
                environment.setCurrentBlock(current);
                final Vector3 scaled = current.subtract(zero).divide(unit);

                try {
                    if (expression.evaluate(new double[]{ scaled.x(), scaled.y(), scaled.z() }, timeout) <= 0) {
                        return null;
                    }

                    // TODO: Allow biome setting via a script variable (needs BiomeType<->int mapping)
                    return defaultBiomeType;
                } catch (ExpressionTimeoutException e) {
                    timedOut.getAndIncrement();
                    return null;
                } catch (Exception e) {
                    LOGGER.warn("Failed to create shape", e);
                    return null;
                }
            }
        };
        int changed = shape.generate(this, biomeType, hollow);
        if (timedOut.get() > 0) {
            throw new ExpressionTimeoutException(
                    String.format("%d biomes changed. %d biomes took too long to evaluate (increase time with //timeout)",
                            changed, timedOut.get()));
        }
        return changed;
    }

    public int morph(BlockVector3 position, double brushSize, int minErodeFaces, int numErodeIterations, int minDilateFaces, int numDilateIterations) throws MaxChangedBlocksException {
        int ceilBrushSize = (int) Math.ceil(brushSize);
        int bufferSize = ceilBrushSize * 2 + 3;  // + 1 due to checking the adjacent blocks, plus the 0th block
        // Store block states in a 3d array so we can do multiple mutations then commit.
        // Two are required as for each iteration, one is "current" and the other is "new"
        BlockState[][][] currentBuffer = new BlockState[bufferSize][bufferSize][bufferSize];
        BlockState[][][] nextBuffer = new BlockState[bufferSize][bufferSize][bufferSize];

        // Simply used for swapping the two
        BlockState[][][] tmp;

        // Load into buffer
        for (int x = 0; x < bufferSize; x++) {
            for (int y = 0; y < bufferSize; y++) {
                for (int z = 0; z < bufferSize; z++) {
                    BlockState blockState = getBlock(position.add(x - ceilBrushSize - 1, y - ceilBrushSize - 1, z - ceilBrushSize - 1));
                    currentBuffer[x][y][z] = blockState;
                    nextBuffer[x][y][z] = blockState;
                }
            }
        }

        double brushSizeSq = brushSize * brushSize;
        Map<BlockState, Integer> blockStateFrequency = new HashMap<>();
        int totalFaces;
        int highestFreq;
        BlockState highestState;
        for (int i = 0; i < numErodeIterations; i++) {
            for (int x = 0; x <= ceilBrushSize * 2; x++) {
                for (int y = 0; y <= ceilBrushSize * 2; y++) {
                    for (int z = 0; z <= ceilBrushSize * 2; z++) {
                        int realX = x - ceilBrushSize;
                        int realY = y - ceilBrushSize;
                        int realZ = z - ceilBrushSize;
                        if (lengthSq(realX, realY, realZ) > brushSizeSq) {
                            continue;
                        }

                        // Copy across changes
                        nextBuffer[x + 1][y + 1][z + 1] = currentBuffer[x + 1][y + 1][z + 1];

                        BlockState blockState = currentBuffer[x + 1][y + 1][z + 1];

                        if (blockState.getBlockType().getMaterial().isLiquid() || blockState.getBlockType().getMaterial().isAir()) {
                            continue;
                        }

                        blockStateFrequency.clear();
                        totalFaces = 0;
                        highestFreq = 0;
                        highestState = blockState;
                        for (BlockVector3 vec3 : recurseDirections) {
                            BlockState adj = currentBuffer[x + 1 + vec3.x()][y + 1 + vec3.y()][z + 1 + vec3.z()];

                            if (!adj.getBlockType().getMaterial().isLiquid() && !adj.getBlockType().getMaterial().isAir()) {
                                continue;
                            }

                            totalFaces++;
                            int newFreq = blockStateFrequency.getOrDefault(adj, 0) + 1;
                            blockStateFrequency.put(adj, newFreq);

                            if (newFreq > highestFreq) {
                                highestFreq = newFreq;
                                highestState = adj;
                            }
                        }

                        if (totalFaces >= minErodeFaces) {
                            nextBuffer[x + 1][y + 1][z + 1] = highestState;
                        }
                    }
                }
            }
            // Swap current and next
            tmp = currentBuffer;
            currentBuffer = nextBuffer;
            nextBuffer = tmp;
        }

        for (int i = 0; i < numDilateIterations; i++) {
            for (int x = 0; x <= ceilBrushSize * 2; x++) {
                for (int y = 0; y <= ceilBrushSize * 2; y++) {
                    for (int z = 0; z <= ceilBrushSize * 2; z++) {
                        int realX = x - ceilBrushSize;
                        int realY = y - ceilBrushSize;
                        int realZ = z - ceilBrushSize;
                        if (lengthSq(realX, realY, realZ) > brushSizeSq) {
                            continue;
                        }

                        // Copy across changes
                        nextBuffer[x + 1][y + 1][z + 1] = currentBuffer[x + 1][y + 1][z + 1];

                        BlockState blockState = currentBuffer[x + 1][y + 1][z + 1];
                        // Needs to be empty
                        if (!blockState.getBlockType().getMaterial().isLiquid() && !blockState.getBlockType().getMaterial().isAir()) {
                            continue;
                        }

                        blockStateFrequency.clear();
                        totalFaces = 0;
                        highestFreq = 0;
                        highestState = blockState;
                        for (BlockVector3 vec3 : recurseDirections) {
                            BlockState adj = currentBuffer[x + 1 + vec3.x()][y + 1 + vec3.y()][z + 1 + vec3.z()];
                            if (adj.getBlockType().getMaterial().isLiquid() || adj.getBlockType().getMaterial().isAir()) {
                                continue;
                            }

                            totalFaces++;
                            int newFreq = blockStateFrequency.getOrDefault(adj, 0) + 1;
                            blockStateFrequency.put(adj, newFreq);

                            if (newFreq > highestFreq) {
                                highestFreq = newFreq;
                                highestState = adj;
                            }
                        }

                        if (totalFaces >= minDilateFaces) {
                            nextBuffer[x + 1][y + 1][z + 1] = highestState;
                        }
                    }
                }
            }
            // Swap current and next
            tmp = currentBuffer;
            currentBuffer = nextBuffer;
            nextBuffer = tmp;
        }

        // Commit to world
        int changed = 0;
        for (int x = 0; x < bufferSize; x++) {
            for (int y = 0; y < bufferSize; y++) {
                for (int z = 0; z < bufferSize; z++) {
                    if (setBlock(position.add(x - ceilBrushSize - 1, y - ceilBrushSize - 1, z - ceilBrushSize - 1), currentBuffer[x][y][z])) {
                        changed++;
                    }
                }
            }
        }

        return changed;
    }

    private static final BlockVector3[] recurseDirections = {
            Direction.NORTH.toBlockVector(),
            Direction.EAST.toBlockVector(),
            Direction.SOUTH.toBlockVector(),
            Direction.WEST.toBlockVector(),
            Direction.UP.toBlockVector(),
            Direction.DOWN.toBlockVector(),
    };

    private static double lengthSq(double x, double y, double z) {
        return (x * x) + (y * y) + (z * z);
    }

    private static double lengthSq(double x, double z) {
        return (x * x) + (z * z);
    }

}
