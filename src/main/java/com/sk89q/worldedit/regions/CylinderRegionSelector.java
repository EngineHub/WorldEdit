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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.Vector2D;
import com.sk89q.worldedit.cui.CUIRegion;
import com.sk89q.worldedit.cui.SelectionCylinderEvent;
import com.sk89q.worldedit.cui.SelectionMinMaxEvent;
import com.sk89q.worldedit.cui.SelectionPointEvent;
import com.sk89q.worldedit.cui.SelectionShapeEvent;

/**
 * Selector for polygonal regions.
 */
public class CylinderRegionSelector implements RegionSelector, CUIRegion {
    
    protected CylinderRegion region;
    protected static final NumberFormat format;

    static {
        format = (NumberFormat) NumberFormat.getInstance().clone();
        format.setMaximumFractionDigits(3);
    }

    public CylinderRegionSelector(LocalWorld world) {
        region = new CylinderRegion(world);
    }

    public CylinderRegionSelector(RegionSelector oldSelector) {
        this(oldSelector.getIncompleteRegion().getWorld());
        if (oldSelector instanceof CylinderRegionSelector) {
            final CylinderRegionSelector cylSelector = (CylinderRegionSelector) oldSelector;

            region = new CylinderRegion(cylSelector.region);
        } else {
            final Region oldRegion;
            try {
                oldRegion = oldSelector.getRegion();
            } catch (IncompleteRegionException e) {
                return;
            }

            Vector pos1 = oldRegion.getMinimumPoint();
            Vector pos2 = oldRegion.getMaximumPoint();

            Vector center = pos1.add(pos2).divide(2).floor();
            region.setCenter(center.toVector2D());
            region.setRadius(pos2.toVector2D().subtract(center.toVector2D()));

            region.setMaximumY(Math.max(pos1.getBlockY(), pos2.getBlockY()));
            region.setMinimumY(Math.min(pos1.getBlockY(), pos2.getBlockY()));
        }
    }

    public CylinderRegionSelector(LocalWorld world, Vector2D center, Vector2D radius, int minY, int maxY) {
        this(world);

        region.setCenter(center);
        region.setRadius(radius);

        region.setMinimumY(Math.min(minY, maxY));
        region.setMaximumY(Math.max(minY, maxY));
    }

    @Override
    public boolean selectPrimary(Vector pos) {
        if (!region.getCenter().equals(new Vector(0, 0, 0)) && pos.compareTo(region.getCenter()) == 0) {
            return false;
        }

        region = new CylinderRegion(region.getWorld());
        region.setCenter(pos.toVector2D());
        region.setY(pos.getBlockY());

        return true;
    }

    @Override
    public boolean selectSecondary(Vector pos) {
        Vector center = region.getCenter();
        if ((center.compareTo(new Vector(0, 0, 0))) == 0) {
            return true;
        }

        final Vector2D diff = pos.subtract(center).toVector2D();
        final Vector2D minRadius = Vector2D.getMaximum(diff, diff.multiply(-1.0));
        region.extendRadius(minRadius);

        region.setY(pos.getBlockY());

        return true;
    }

    @Override
    public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        player.print("Starting a new cylindrical selection at " + pos + ".");

        session.describeCUI(player);
    }

    @Override
    public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        Vector center = region.getCenter();
        if (!center.equals(new Vector(0, 0, 0))) {
            player.print("Radius set to " + format.format(region.getRadius().getX()) + "/" + format.format(region.getRadius().getZ()) + " blocks. (" + region.getArea() + ").");
        } else {
            player.printError("You must select the center point before setting the radius.");
            return;
        }

        session.describeCUI(player);
    }

    @Override
    public void explainRegionAdjust(LocalPlayer player, LocalSession session) {
        session.describeCUI(player);
    }

    @Override
    public BlockVector getPrimaryPosition() throws IncompleteRegionException {
        if (!isDefined()) {
            throw new IncompleteRegionException();
        }

        return region.getCenter().toBlockVector();
    }

    @Override
    public CylinderRegion getRegion() throws IncompleteRegionException {
        if (!isDefined()) {
            throw new IncompleteRegionException();
        }

        return region;
    }

    @Override
    public CylinderRegion getIncompleteRegion() {
        return region;
    }

    @Override
    public boolean isDefined() {
        return !region.getRadius().equals(new Vector2D(0, 0));
    }

    @Override
    public void learnChanges() {
    }

    @Override
    public void clear() {
        region = new CylinderRegion(region.getWorld());
    }

    @Override
    public String getTypeName() {
        return "Cylinder";
    }

    @Override
    public List<String> getInformationLines() {
        final List<String> lines = new ArrayList<String>();

        if (!region.getCenter().equals(new Vector(0, 0, 0))) {
            lines.add("Center: " + region.getCenter());
        }
        if (!region.getRadius().equals(new Vector2D(0, 0))) {
            lines.add("Radius: " + region.getRadius());
        }

        return lines;
    }

    @Override
    public int getArea() {
        return region.getArea();
    }

    @Override
    public void describeCUI(LocalSession session, LocalPlayer player) {
        session.dispatchCUIEvent(player, new SelectionCylinderEvent(region.getCenter(), region.getRadius()));
        session.dispatchCUIEvent(player, new SelectionMinMaxEvent(region.getMinimumY(), region.getMaximumY()));
    }

    @Override
    public void describeLegacyCUI(LocalSession session, LocalPlayer player) {
        if (isDefined()) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(0, region.getMinimumPoint(), getArea()));
            session.dispatchCUIEvent(player, new SelectionPointEvent(1, region.getMaximumPoint(), getArea()));
        } else {
            session.dispatchCUIEvent(player, new SelectionShapeEvent(getLegacyTypeID()));
        }
    }

    @Override
    public int getProtocolVersion() {
        return 1;
    }

    @Override
    public String getTypeID() {
        return "cylinder";
    }

    @Override
    public String getLegacyTypeID() {
        return "cuboid";
    }
}
