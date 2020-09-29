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

package com.sk89q.worldedit.world;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.registry.Keyed;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.weather.WeatherType;

import java.nio.file.Path;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Represents a world (dimension).
 */
public interface World extends Extent, Keyed {

    /**
     * Get the name of the world.
     *
     * @return a name for the world
     */
    String getName();

    /**
     * Get the folder in which this world is stored. May return null if unknown
     * or if this world is not serialized to disk.
     *
     * @return world storage path
     */
    @Nullable
    Path getStoragePath();

    /**
     * Get the minimum Y.
     *
     * @return the minimum Y
     */
    int getMinY();

    /**
     * Get the maximum Y.
     *
     * @return the maximum Y
     */
    int getMaxY();

    /**
     * Create a mask that matches all liquids.
     *
     * <p>Implementations should override this so that custom liquids
     * are supported.</p>
     *
     * @return a mask
     */
    Mask createLiquidMask();

    /**
     * Use the given item on the block at the given location on the given side.
     *
     * @param item The item
     * @param face The face
     * @return Whether it succeeded
     */
    boolean useItem(BlockVector3 position, BaseItem item, Direction face);

    /**
     * Similar to {@link Extent#setBlock(BlockVector3, BlockStateHolder)} but a
     * {@code notifyAndLight} parameter indicates whether adjacent blocks
     * should be notified that changes have been made and lighting operations
     * should be executed.
     *
     * <p>If it's not possible to skip lighting, or if it's not possible to
     * avoid notifying adjacent blocks, then attempt to meet the
     * specification as best as possible.</p>
     *
     * <p>On implementations where the world is not simulated, the
     * {@code notifyAndLight} parameter has no effect either way.</p>
     *
     * @param position position of the block
     * @param block block to set
     * @param notifyAndLight true to to notify and light
     * @return true if the block was successfully set (return value may not be accurate)
     */
    @Deprecated
    default <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, boolean notifyAndLight) throws WorldEditException {
        return setBlock(position, block, notifyAndLight ? SideEffectSet.defaults() : SideEffectSet.none());
    }

    /**
     * Similar to {@link Extent#setBlock(BlockVector3, BlockStateHolder)} but a
     * {@code sideEffects} parameter indicates which side effects should be applied
     * to the block. This includes block updates, lighting, and others. See {@link SideEffect}
     * for a full list.
     *
     * <p>Not all implementations support all side effects. Use
     * {@link Platform#getSupportedSideEffects()} for a list of supported side effects.
     * Non-supported side effects will be ignored.</p>
     *
     * @param position position of the block
     * @param block block to set
     * @param sideEffects which side effects to perform
     * @return true if the block was successfully set (return value may not be accurate)
     */
    <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) throws WorldEditException;

    /**
     * Notifies the simulation that the block at the given location has
     * been changed and it must be re-lighted (and issue other events).
     *
     * @param position position of the block
     * @param previousType the type of the previous block that was there
     * @return true if the block was successfully notified
     */
    @Deprecated
    default boolean notifyAndLightBlock(BlockVector3 position, BlockState previousType) throws WorldEditException {
        return !applySideEffects(position, previousType, SideEffectSet.defaults()).isEmpty();
    }

    /**
     * Applies a set of side effects on the given block.
     *
     * @param position position of the block
     * @param previousType the type of the previous block that was there
     * @param sideEffectSet which side effects to perform
     * @return a set of side effects that were applied
     */
    Set<SideEffect> applySideEffects(BlockVector3 position, BlockState previousType, SideEffectSet sideEffectSet) throws WorldEditException;

    /**
     * Get the light level at the given block.
     *
     * @param position the position
     * @return the light level (0-15)
     */
    int getBlockLightLevel(BlockVector3 position);

    /**
     * Clear a chest's contents.
     *
     * @param position the position
     * @return true if the container was cleared
     */
    boolean clearContainerBlockContents(BlockVector3 position);

    /**
     * Drop an item at the given position.
     *
     * @param position the position
     * @param item the item to drop
     * @param count the number of individual stacks to drop (number of item entities)
     */
    void dropItem(Vector3 position, BaseItemStack item, int count);

    /**
     * Drop one stack of the item at the given position.
     *
     * @param position the position
     * @param item the item to drop
     * @see #dropItem(Vector3, BaseItemStack, int) shortcut method to specify the number of stacks
     */
    void dropItem(Vector3 position, BaseItemStack item);

    /**
     * Simulate a block being mined at the given position.
     *
     * @param position the position
     */
    void simulateBlockMine(BlockVector3 position);

    /**
     * Gets whether the given {@link BlockState} can be placed here.
     *
     * @param position The position
     * @param blockState The blockstate
     * @return If it can be placed
     */
    default boolean canPlaceAt(BlockVector3 position, BlockState blockState) {
        return true;
    }

    /**
     * Regenerate an area.
     *
     * @param region the region
     * @param editSession the {@link EditSession}
     * @return true if re-generation was successful
     */
    default boolean regenerate(Region region, EditSession editSession) {
        return regenerate(region, editSession, RegenOptions.builder().build());
    }

    /**
     * Regenerate an area.
     *
     * @param region the region
     * @param extent the {@link Extent}
     * @return true if re-generation was successful
     */
    default boolean regenerate(Region region, Extent extent) {
        return regenerate(region, extent, RegenOptions.builder().build());
    }

    /**
     * Regenerate an area.
     *
     * @param region the region
     * @param extent the {@link Extent}
     * @param options the regeneration options
     * @return true if regeneration was successful
     * @apiNote This must be overridden by new subclasses. See {@link NonAbstractForCompatibility}
     *          for details
     */
    @NonAbstractForCompatibility(
        delegateName = "regenerate",
        delegateParams = { Region.class, EditSession.class }
    )
    default boolean regenerate(Region region, Extent extent, RegenOptions options) {
        DeprecationUtil.checkDelegatingOverride(getClass());
        if (extent instanceof EditSession) {
            return regenerate(region, (EditSession) extent);
        }
        throw new UnsupportedOperationException("This World class ("
            + getClass().getName()
            + ") does not implement the general Extent variant of this method");
    }

    /**
     * Generate a tree at the given position.
     *
     * @param type the tree type
     * @param editSession the {@link EditSession}
     * @param position the position
     * @return true if generation was successful
     * @throws MaxChangedBlocksException thrown if too many blocks were changed
     */
    boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, BlockVector3 position) throws MaxChangedBlocksException;

    /**
     * Load the chunk at the given position if it isn't loaded.
     *
     * @param position the position
     */
    void checkLoadedChunk(BlockVector3 position);

    /**
     * Fix the given chunks after fast mode was used.
     *
     * <p>Fast mode makes calls to {@link #setBlock(BlockVector3, BlockStateHolder, boolean)}
     * with {@code false} for the {@code notifyAndLight} parameter, which
     * may causes lighting errors to accumulate. Use of this method, if
     * it is implemented by the underlying world, corrects those lighting
     * errors and may trigger block change notifications.</p>
     *
     * @param chunks a list of chunk coordinates to fix
     */
    void fixAfterFastMode(Iterable<BlockVector2> chunks);

    /**
     * Relight the given chunks if possible.
     *
     * @param chunks a list of chunk coordinates to fix
     */
    void fixLighting(Iterable<BlockVector2> chunks);

    /**
     * Play the given effect.
     *
     * @param position the position
     * @param type the effect type
     * @param data the effect data
     * @return true if the effect was played
     */
    boolean playEffect(Vector3 position, int type, int data);

    /**
     * Queue a block break effect.
     *
     * @param server the server
     * @param position the position
     * @param blockType the block type
     * @param priority the priority
     * @return true if the effect was played
     */
    boolean queueBlockBreakEffect(Platform server, BlockVector3 position, BlockType blockType, double priority);

    /**
     * Gets the weather type of the world.
     *
     * @return The weather
     */
    WeatherType getWeather();

    /**
     * Gets the remaining weather duration.
     *
     * @return The weather duration
     */
    long getRemainingWeatherDuration();

    /**
     * Sets the weather type of the world.
     *
     * @param weatherType The weather type
     */
    void setWeather(WeatherType weatherType);

    /**
     * Sets the weather type of the world.
     *
     * @param weatherType The weather type
     * @param duration The duration of the weather
     */
    void setWeather(WeatherType weatherType, long duration);

    /**
     * Gets the spawn position of this world.
     *
     * @return The spawn position
     */
    BlockVector3 getSpawnPosition();

    @Override
    boolean equals(Object other);

    @Override
    int hashCode();

}
