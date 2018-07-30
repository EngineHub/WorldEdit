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

package com.sk89q.worldedit.regions.selector;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.CUIRegion;
import com.sk89q.worldedit.internal.cui.SelectionPointEvent;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.world.World;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Creates a {@code CuboidRegion} from a user's selections.
 */
public class CuboidRegionSelector implements RegionSelector, CUIRegion {

    protected transient BlockVector position1;
    protected transient BlockVector position2;
    protected transient CuboidRegion region;

    /**
     * Create a new region selector with a {@code null} world.
     */
    public CuboidRegionSelector() {
        this((World) null);
    }

    /**
     * Create a new region selector.
     *
     * @param world the world, which may be {@code null}
     */
    public CuboidRegionSelector(@Nullable World world) {
        region = new CuboidRegion(world, new Vector(), new Vector());
    }

    /**
     * Create a copy of another selector.
     *
     * @param oldSelector another selector
     */
    public CuboidRegionSelector(RegionSelector oldSelector) {
        this(checkNotNull(oldSelector).getIncompleteRegion().getWorld());

        if (oldSelector instanceof CuboidRegionSelector) {
            final CuboidRegionSelector cuboidRegionSelector = (CuboidRegionSelector) oldSelector;

            position1 = cuboidRegionSelector.position1;
            position2 = cuboidRegionSelector.position2;
        } else {
            final Region oldRegion;
            try {
                oldRegion = oldSelector.getRegion();
            } catch (IncompleteRegionException e) {
                return;
            }

            position1 = oldRegion.getMinimumPoint().toBlockVector();
            position2 = oldRegion.getMaximumPoint().toBlockVector();
        }

        region.setPos1(position1);
        region.setPos2(position2);
    }

    /**
     * Create a new region selector with the given two positions.
     *
     * @param world the world
     * @param position1 position 1
     * @param position2 position 2
     */
    public CuboidRegionSelector(@Nullable World world, Vector position1, Vector position2) {
        this(world);
        checkNotNull(position1);
        checkNotNull(position2);
        this.position1 = position1.toBlockVector();
        this.position2 = position2.toBlockVector();
        region.setPos1(position1);
        region.setPos2(position2);
    }

    @Nullable
    @Override
    public World getWorld() {
        return region.getWorld();
    }

    @Override
    public void setWorld(@Nullable World world) {
        region.setWorld(world);
    }

    @Override
    public boolean selectPrimary(Vector position, SelectorLimits limits) {
        checkNotNull(position);

        if (position1 != null && (position.compareTo(position1) == 0)) {
            return false;
        }

        position1 = position.toBlockVector();
        region.setPos1(position1);
        return true;
    }

    @Override
    public boolean selectSecondary(Vector position, SelectorLimits limits) {
        checkNotNull(position);

        if (position2 != null && (position.compareTo(position2)) == 0) {
            return false;
        }

        position2 = position.toBlockVector();
        region.setPos2(position2);
        return true;
    }

    @Override
    public void explainPrimarySelection(Actor player, LocalSession session, Vector pos) {
        checkNotNull(player);
        checkNotNull(session);
        checkNotNull(pos);

        if (position1 != null && position2 != null) {
            player.print("First position set to " + position1 + " (" + region.getArea() + ").");
        } else {
            player.print("First position set to " + position1 + ".");
        }

        session.dispatchCUIEvent(player, new SelectionPointEvent(0, pos, getArea()));
    }

    @Override
    public void explainSecondarySelection(Actor player, LocalSession session, Vector pos) {
        checkNotNull(player);
        checkNotNull(session);
        checkNotNull(pos);

        if (position1 != null && position2 != null) {
            player.print("Second position set to " + position2 + " (" + region.getArea() + ").");
        } else {
            player.print("Second position set to " + position2 + ".");
        }

        session.dispatchCUIEvent(player, new SelectionPointEvent(1, pos, getArea()));
    }

    @Override
    public void explainRegionAdjust(Actor player, LocalSession session) {
        checkNotNull(player);
        checkNotNull(session);

        if (position1 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(0, position1, getArea()));
        }

        if (position2 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(1, position2, getArea()));
        }
    }

    @Override
    public BlockVector getPrimaryPosition() throws IncompleteRegionException {
        if (position1 == null) {
            throw new IncompleteRegionException();
        }

        return position1;
    }

    @Override
    public boolean isDefined() {
        return position1 != null && position2 != null;
    }

    @Override
    public CuboidRegion getRegion() throws IncompleteRegionException {
        if (position1 == null || position2 == null) {
            throw new IncompleteRegionException();
        }

        return region;
    }

    @Override
    public CuboidRegion getIncompleteRegion() {
        return region;
    }

    @Override
    public void learnChanges() {
        position1 = region.getPos1().toBlockVector();
        position2 = region.getPos2().toBlockVector();
    }

    @Override
    public void clear() {
        position1 = null;
        position2 = null;
    }

    @Override
    public String getTypeName() {
        return "cuboid";
    }

    @Override
    public List<String> getInformationLines() {
        final List<String> lines = new ArrayList<>();

        if (position1 != null) {
            lines.add("Position 1: " + position1);
        }

        if (position2 != null) {
            lines.add("Position 2: " + position2);
        }

        return lines;
    }

    @Override
    public int getArea() {
        if (position1 == null) {
            return -1;
        }

        if (position2 == null) {
            return -1;
        }

        return region.getArea();
    }

    @Override
    public void describeCUI(LocalSession session, Actor player) {
        if (position1 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(0, position1, getArea()));
        }

        if (position2 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(1, position2, getArea()));
        }
    }

    @Override
    public void describeLegacyCUI(LocalSession session, Actor player) {
        describeCUI(session, player);
    }

    @Override
    public int getProtocolVersion() {
        return 0;
    }

    @Override
    public String getTypeID() {
        return "cuboid";
    }

    @Override
    public String getLegacyTypeID() {
        return "cuboid";
    }
    
}
