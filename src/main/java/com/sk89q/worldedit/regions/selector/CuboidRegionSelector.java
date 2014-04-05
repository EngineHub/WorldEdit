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

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.CUIRegion;
import com.sk89q.worldedit.internal.cui.SelectionPointEvent;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link RegionSelector} for {@link CuboidRegion}s.
 */
public class CuboidRegionSelector extends com.sk89q.worldedit.regions.CuboidRegionSelector implements RegionSelector, CUIRegion {

    protected BlockVector pos1;
    protected BlockVector pos2;
    protected CuboidRegion region;

    /**
     * Create a new region selector with no world.
     */
    public CuboidRegionSelector() {
        this((World) null);
    }

    @Deprecated
    public CuboidRegionSelector(@Nullable LocalWorld world) {
        this((World) world);
    }

    /**
     * Create a new region selector.
     *
     * @param world the world
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

            pos1 = cuboidRegionSelector.pos1;
            pos2 = cuboidRegionSelector.pos2;
        } else {
            final Region oldRegion;
            try {
                oldRegion = oldSelector.getRegion();
            } catch (IncompleteRegionException e) {
                return;
            }

            pos1 = oldRegion.getMinimumPoint().toBlockVector();
            pos2 = oldRegion.getMaximumPoint().toBlockVector();
        }

        region.setPos1(pos1);
        region.setPos2(pos2);
    }

    @Deprecated
    public CuboidRegionSelector(@Nullable LocalWorld world, Vector pos1, Vector pos2) {
        this((World) world, pos1, pos2);
    }

    /**
     * Create a new region selector with the given two positions.
     *
     * @param world the world
     * @param pos1 position 1
     * @param pos2 position 2
     */
    public CuboidRegionSelector(@Nullable World world, Vector pos1, Vector pos2) {
        this(world);
        checkNotNull(world);
        checkNotNull(pos1);
        checkNotNull(pos2);
        this.pos1 = pos1.toBlockVector();
        this.pos2 = pos2.toBlockVector();
        region.setPos1(pos1);
        region.setPos2(pos2);
    }

    @Override
    public boolean selectPrimary(Vector pos) {
        if (pos1 != null && (pos.compareTo(pos1) == 0)) {
            return false;
        }

        pos1 = pos.toBlockVector();
        region.setPos1(pos1);
        return true;
    }

    @Override
    public boolean selectSecondary(Vector pos) {
        if (pos2 != null && (pos.compareTo(pos2)) == 0) {
            return false;
        }

        pos2 = pos.toBlockVector();
        region.setPos2(pos2);
        return true;
    }

    @Override
    public void explainPrimarySelection(Actor player, LocalSession session, Vector pos) {
        if (pos1 != null && pos2 != null) {
            player.print("First position set to " + pos1 + " (" + region.getArea() + ").");
        } else {
            player.print("First position set to " + pos1 + ".");
        }

        session.dispatchCUIEvent(player, new SelectionPointEvent(0, pos, getArea()));
    }

    @Override
    public void explainSecondarySelection(Actor player, LocalSession session, Vector pos) {
        if (pos1 != null && pos2 != null) {
            player.print("Second position set to " + pos2 + " (" + region.getArea() + ").");
        } else {
            player.print("Second position set to " + pos2 + ".");
        }

        session.dispatchCUIEvent(player, new SelectionPointEvent(1, pos, getArea()));
    }

    @Override
    public void explainRegionAdjust(Actor player, LocalSession session) {
        if (pos1 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(0, pos1, getArea()));
        }

        if (pos2 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(1, pos2, getArea()));
        }
    }

    @Override
    public BlockVector getPrimaryPosition() throws IncompleteRegionException {
        if (pos1 == null) {
            throw new IncompleteRegionException();
        }

        return pos1;
    }

    @Override
    public boolean isDefined() {
        return pos1 != null && pos2 != null;
    }

    @Override
    public CuboidRegion getRegion() throws IncompleteRegionException {
        if (pos1 == null || pos2 == null) {
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
        pos1 = region.getPos1().toBlockVector();
        pos2 = region.getPos2().toBlockVector();
    }

    @Override
    public void clear() {
        pos1 = null;
        pos2 = null;
    }

    @Override
    public String getTypeName() {
        return "cuboid";
    }

    @Override
    public List<String> getInformationLines() {
        final List<String> lines = new ArrayList<String>();

        if (pos1 != null) {
            lines.add("Position 1: " + pos1);
        }

        if (pos2 != null) {
            lines.add("Position 2: " + pos2);
        }

        return lines;
    }

    @Override
    public int getArea() {
        if (pos1 == null) {
            return -1;
        }

        if (pos2 == null) {
            return -1;
        }

        return region.getArea();
    }

    @Override
    public void describeCUI(LocalSession session, Actor player) {
        if (pos1 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(0, pos1, getArea()));
        }

        if (pos2 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(1, pos2, getArea()));
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

    @Override
    public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector position) {
        explainPrimarySelection((Actor) player, session, position);
    }

    @Override
    public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector position) {
        explainSecondarySelection((Actor) player, session, position);
    }

    @Override
    public void explainRegionAdjust(LocalPlayer player, LocalSession session) {
        explainRegionAdjust((Actor) player, session);
    }
    
}
