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
import com.sk89q.worldedit.cui.SelectionEllipsoidPointEvent;
import com.sk89q.worldedit.cui.SelectionPointEvent;

/**
 * Selector for ellipsoids.
 *
 * @author TomyLobo
 */
public class EllipsoidRegionSelector implements RegionSelector, CUIRegion {
    protected EllipsoidRegion region;

    public EllipsoidRegionSelector(LocalWorld world) {
        region = new EllipsoidRegion(world, new Vector(), new Vector());
    }

    public EllipsoidRegionSelector() {
        this((LocalWorld) null);
    }

    public EllipsoidRegionSelector(RegionSelector oldSelector) {
        this(oldSelector.getIncompleteRegion().getWorld());
        if (oldSelector instanceof EllipsoidRegionSelector) {
            final EllipsoidRegionSelector ellipsoidRegionSelector = (EllipsoidRegionSelector) oldSelector;

            region = new EllipsoidRegion(ellipsoidRegionSelector.getIncompleteRegion());
        } else {
            Region oldRegion = null;
            try {
                oldRegion = oldSelector.getRegion();
            } catch (IncompleteRegionException e) {
                return;
            }

            BlockVector pos1 = oldRegion.getMinimumPoint().toBlockVector();
            BlockVector pos2 = oldRegion.getMaximumPoint().toBlockVector();

            Vector center = pos1.add(pos2).divide(2).floor();
            region.setCenter(center);
            region.setRadius(pos2.subtract(center));
        }
    }

    public EllipsoidRegionSelector(LocalWorld world, Vector center, Vector radius) {
        this(world);

        region.setCenter(center);
        region.setRadius(radius);
    }

    public boolean selectPrimary(Vector pos) {
        if (pos.equals(region.getCenter()) && region.getRadius().lengthSq() == 0) {
            return false;
        }

        region.setCenter(pos.toBlockVector());
        region.setRadius(new Vector());
        return true;
    }

    public boolean selectSecondary(Vector pos) {
        final Vector diff = pos.subtract(region.getCenter());
        final Vector minRadius = Vector.getMaximum(diff, diff.multiply(-1.0));
        region.extendRadius(minRadius);
        return true;
    }

    public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        if (isDefined()) {
            player.print("Center position set to " + region.getCenter() + " (" + region.getArea() + ").");
        } else {
            player.print("Center position set to " + region.getCenter() + ".");
        }

        session.describeCUI(player);
    }

    public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        if (isDefined()) {
            player.print("Radius set to " + region.getRadius() + " (" + region.getArea() + ").");
        } else {
            player.print("Radius set to " + region.getRadius() + ".");
        }

        session.describeCUI(player);
    }

    public void explainRegionAdjust(LocalPlayer player, LocalSession session) {
        session.describeCUI(player);
    }

    public boolean isDefined() {
        return region.getRadius().lengthSq() > 0;
    }

    public EllipsoidRegion getRegion() throws IncompleteRegionException {
        if (!isDefined()) {
            throw new IncompleteRegionException();
        }

        return region;
    }

    public EllipsoidRegion getIncompleteRegion() {
        return region;
    }

    public void learnChanges() {
    }

    public void clear() {
        region.setCenter(new Vector());
        region.setRadius(new Vector());
    }

    public String getTypeName() {
        return "ellipsoid";
    }

    public List<String> getInformationLines() {
        final List<String> lines = new ArrayList<String>();

        final Vector center = region.getCenter();
        if (center.lengthSq() > 0) {
            lines.add("Center: " + center);
        }

        final Vector radius = region.getRadius();
        if (radius.lengthSq() > 0) {
            lines.add("X/Y/Z radius: " + radius);
        }

        return lines;
    }

    public int getArea() {
        return region.getArea();
    }

    public void describeCUI(LocalSession session, LocalPlayer player) {
        session.dispatchCUIEvent(player, new SelectionEllipsoidPointEvent(0, region.getCenter()));
        session.dispatchCUIEvent(player, new SelectionEllipsoidPointEvent(1, region.getRadius()));
    }

    public void describeLegacyCUI(LocalSession session, LocalPlayer player) {
        session.dispatchCUIEvent(player, new SelectionPointEvent(0, region.getMinimumPoint(), getArea()));
        session.dispatchCUIEvent(player, new SelectionPointEvent(1, region.getMaximumPoint(), getArea()));
    }

    public String getLegacyTypeID() {
        return "cuboid";
    }

    public int getProtocolVersion() {
        return 1;
    }

    public String getTypeID() {
        return "ellipsoid";
    }

    @Override
    public BlockVector getPrimaryPosition() throws IncompleteRegionException {
        return region.getCenter().toBlockVector();
    }
}
