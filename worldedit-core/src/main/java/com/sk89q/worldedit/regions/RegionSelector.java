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

package com.sk89q.worldedit.regions;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import com.sk89q.worldedit.world.World;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * Region selectors create {@link Region}s from a series of "selected points."
 * They are used, for example, to allow users to create a {@link CuboidRegion}
 * by selecting two corners of the cuboid.
 */
public interface RegionSelector {

    /**
     * Get the world for the region selector.
     *
     * @return a world, which may be null
     */
    @Nullable
    World getWorld();

    /**
     * Set the world for the region selector.
     *
     * @param world the world, which may be null
     */
    void setWorld(@Nullable World world);

    /**
     * Called when the first point is selected.
     *
     * @param position the position
     * @return true if something changed
     */
    boolean selectPrimary(BlockVector3 position, SelectorLimits limits);

    /**
     * Called when the second point is selected.
     *
     * @param position the position
     * @return true if something changed
     */
    boolean selectSecondary(BlockVector3 position, SelectorLimits limits);

    /**
     * Tell the player information about his/her primary selection.
     *
     * @param actor the actor
     * @param session the session
     * @param position position
     */
    void explainPrimarySelection(Actor actor, LocalSession session, BlockVector3 position);

    /**
     * Tell the player information about his/her secondary selection.
     *
     * @param actor the actor
     * @param session the session
     * @param position position
     */
    void explainSecondarySelection(Actor actor, LocalSession session, BlockVector3 position);

    /**
     * Tell the player information about the region's changes. This may resend
     * all the defining region information if needed.
     *
     * @param actor the actor
     * @param session the session
     */
    void explainRegionAdjust(Actor actor, LocalSession session);

    /**
     * Get the primary position.
     *
     * @return the primary position
     * @throws IncompleteRegionException thrown if a region has not been fully defined
     */
    BlockVector3 getPrimaryPosition() throws IncompleteRegionException;

    /**
     * Get the selection.
     *
     * @return the created region
     * @throws IncompleteRegionException thrown if a region has not been fully defined
     */
    Region getRegion() throws IncompleteRegionException;

    /**
     * Get the region even if it's not fully defined.
     *
     * @return an incomplete region object that is incomplete
     */
    Region getIncompleteRegion();

    /**
     * Returns whether the region has been fully defined.
     *
     * @return true if a selection is available
     */
    boolean isDefined();

    /**
     * Get the number of blocks inside the region.
     *
     * @return number of blocks, or -1 if undefined
     * @deprecated use {@link RegionSelector#getVolume()}
     */
    @Deprecated
    default int getArea() {
        return (int) getVolume();
    }

    /**
     * Get the number of blocks inside the region.
     *
     * @return number of blocks, or -1 if undefined
     * @apiNote This must be overridden by new subclasses. See {@link NonAbstractForCompatibility}
     *          for details
     */
    @NonAbstractForCompatibility(
        delegateName = "getArea",
        delegateParams = {}
    )
    default long getVolume() {
        DeprecationUtil.checkDelegatingOverride(getClass());

        return getArea();
    }

    /**
     * Update the selector with changes to the region.
     */
    void learnChanges();

    /**
     * Clear the selection.
     */
    void clear();

    /**
     * Get a lowercase name of this region selector type.
     *
     * @return a lower case name of the type
     */
    String getTypeName();

    /**
     * Get lines of information about the selection.
     *
     * @return a list of lines describing the region
     */
    @Deprecated
    default List<String> getInformationLines() {
        return Lists.newArrayList();
    }

    /**
     * Get lines of information about the selection.
     *
     * @return a list of lines describing the region.
     */
    default List<Component> getSelectionInfoLines() {
        return getInformationLines().stream()
                .map(line -> TextComponent.of(line, TextColor.LIGHT_PURPLE))
                .collect(Collectors.toList());
    }
}
