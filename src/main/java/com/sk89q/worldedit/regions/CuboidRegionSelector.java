// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.worldedit.regions;

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.client.bridge.CUIRegion;
import com.sk89q.worldedit.client.bridge.SelectionPointEvent;

/**
 * Selector for cuboids.
 */
public class CuboidRegionSelector implements RegionSelector, CUIRegion {
    protected BlockVector pos1;
    protected BlockVector pos2;
    protected CuboidRegion region;

    public CuboidRegionSelector(LocalWorld world) {
        region = new CuboidRegion(world, new Vector(), new Vector());
    }

    public CuboidRegionSelector() {
        this((LocalWorld) null);
    }

    public CuboidRegionSelector(RegionSelector oldSelector) {
        this(oldSelector.getIncompleteRegion().getWorld());
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

    public CuboidRegionSelector(LocalWorld world, Vector pos1, Vector pos2) {
        this(world);
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
    public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        if (pos1 != null && pos2 != null) {
            player.print("First position set to " + pos1 + " (" + region.getArea() + ").");
        } else {
            player.print("First position set to " + pos1 + ".");
        }

        session.dispatchCUIEvent(player, new SelectionPointEvent(0, pos, getArea()));
    }

    @Override
    public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        if (pos1 != null && pos2 != null) {
            player.print("Second position set to " + pos2 + " (" + region.getArea() + ").");
        } else {
            player.print("Second position set to " + pos2 + ".");
        }

        session.dispatchCUIEvent(player, new SelectionPointEvent(1, pos, getArea()));
    }

    @Override
    public void explainRegionAdjust(LocalPlayer player, LocalSession session) {
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
    public void describeCUI(LocalSession session, LocalPlayer player) {
        if (pos1 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(0, pos1, getArea()));
        }

        if (pos2 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(1, pos2, getArea()));
        }
    }

    @Override
    public void describeLegacyCUI(LocalSession session, LocalPlayer player) {
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
