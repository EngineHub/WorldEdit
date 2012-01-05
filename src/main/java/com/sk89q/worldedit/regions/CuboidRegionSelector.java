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
import com.sk89q.worldedit.cui.CUIRegion;
import com.sk89q.worldedit.cui.SelectionPointEvent;

/**
 * Selector for cuboids.
 *
 * @author sk89q
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

    public boolean selectPrimary(Vector pos) {
        if (pos.equals(pos1)) {
            return false;
        }

        pos1 = pos.toBlockVector();
        region.setPos1(pos1);
        return true;
    }

    public boolean selectSecondary(Vector pos) {
        if (pos.equals(pos2)) {
            return false;
        }

        pos2 = pos.toBlockVector();
        region.setPos2(pos2);
        return true;
    }

    public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        if (pos1 != null && pos2 != null) {
            player.print("First position set to " + pos1 + " (" + region.getArea() + ").");
        } else {
            player.print("First position set to " + pos1 + ".");
        }

        session.dispatchCUIEvent(player, new SelectionPointEvent(0, pos, getArea()));
    }

    public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        if (pos1 != null && pos2 != null) {
            player.print("Second position set to " + pos2 + " (" + region.getArea() + ").");
        } else {
            player.print("Second position set to " + pos2 + ".");
        }

        session.dispatchCUIEvent(player, new SelectionPointEvent(1, pos, getArea()));
    }

    public void explainRegionAdjust(LocalPlayer player, LocalSession session) {
        if (pos1 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(0, pos1, getArea()));
        }

        if (pos2 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(1, pos2, getArea()));
        }
    }

    public BlockVector getPrimaryPosition() throws IncompleteRegionException {
        if (pos1 == null) {
            throw new IncompleteRegionException();
        }

        return pos1;
    }

    public boolean isDefined() {
        return pos1 != null && pos2 != null;
    }

    public CuboidRegion getRegion() throws IncompleteRegionException {
        if (pos1 == null || pos2 == null) {
            throw new IncompleteRegionException();
        }

        return region;
    }

    public CuboidRegion getIncompleteRegion() {
        return region;
    }

    public void learnChanges() {
        pos1 = region.getPos1().toBlockVector();
        pos2 = region.getPos2().toBlockVector();
    }

    public void clear() {
        pos1 = null;
        pos2 = null;
    }

    public String getTypeName() {
        return "cuboid";
    }

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

    public int getArea() {
        if (pos1 == null) {
            return -1;
        }

        if (pos2 == null) {
            return -1;
        }

        return region.getArea();
    }

    public void describeCUI(LocalSession session, LocalPlayer player) {
        if (pos1 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(0, pos1, getArea()));
        }

        if (pos2 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(1, pos2, getArea()));
        }
    }

    public void describeLegacyCUI(LocalSession session, LocalPlayer player) {
        describeCUI(session, player);
    }

    public int getProtocolVersion() {
        return 0;
    }

    public String getTypeID() {
        return "cuboid";
    }

    public String getLegacyTypeID() {
        return "cuboid";
    }

    
}
