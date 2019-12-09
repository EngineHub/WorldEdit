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

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.CUIRegion;
import com.sk89q.worldedit.internal.cui.SelectionEllipsoidPointEvent;
import com.sk89q.worldedit.internal.cui.SelectionPointEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Creates a {@code EllipsoidRegionSelector} from a user's selections.
 */
public class EllipsoidRegionSelector implements RegionSelector, CUIRegion {

    protected transient EllipsoidRegion region;
    protected transient boolean started = false;

    /**
     * Create a new selector with a {@code null} world.
     */
    public EllipsoidRegionSelector() {
        this((World) null);
    }

    /**
     * Create a new selector.
     *
     * @param world the world, which may be {@code null}
     */
    public EllipsoidRegionSelector(@Nullable World world) {
        region = new EllipsoidRegion(world, BlockVector3.ZERO, Vector3.ZERO);
    }

    /**
     * Create a new selector from the given selector.
     *
     * @param oldSelector the old selector
     */
    public EllipsoidRegionSelector(RegionSelector oldSelector) {
        this(checkNotNull(oldSelector).getIncompleteRegion().getWorld());
        if (oldSelector instanceof EllipsoidRegionSelector) {
            final EllipsoidRegionSelector ellipsoidRegionSelector = (EllipsoidRegionSelector) oldSelector;

            region = new EllipsoidRegion(ellipsoidRegionSelector.getIncompleteRegion());
        } else {
            Region oldRegion;
            try {
                oldRegion = oldSelector.getRegion();
            } catch (IncompleteRegionException e) {
                return;
            }

            BlockVector3 pos1 = oldRegion.getMinimumPoint();
            BlockVector3 pos2 = oldRegion.getMaximumPoint();

            BlockVector3 center = pos1.add(pos2).divide(2).floor();
            region.setCenter(center);
            region.setRadius(pos2.subtract(center).toVector3());
        }
    }

    /**
     * Create a new selector.
     *
     * @param world the world
     * @param center the center
     * @param radius the radius
     */
    public EllipsoidRegionSelector(@Nullable World world, BlockVector3 center, Vector3 radius) {
        this(world);

        region.setCenter(center);
        region.setRadius(radius);
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
    public boolean selectPrimary(BlockVector3 position, SelectorLimits limits) {
        if (position.equals(region.getCenter().toBlockPoint()) && region.getRadius().lengthSq() == 0) {
            return false;
        }

        region.setCenter(position);
        region.setRadius(Vector3.ZERO);
        started = true;

        return true;
    }

    @Override
    public boolean selectSecondary(BlockVector3 position, SelectorLimits limits) {
        if (!started) {
            return false;
        }

        final Vector3 diff = position.toVector3().subtract(region.getCenter());
        final Vector3 minRadius = diff.getMaximum(diff.multiply(-1.0));
        region.extendRadius(minRadius);
        return true;
    }

    @Override
    public void explainPrimarySelection(Actor player, LocalSession session, BlockVector3 pos) {
        if (isDefined()) {
            player.printInfo(TranslatableComponent.of(
                    "worldedit.selection.ellipsoid.explain.primary-area",
                    TextComponent.of(region.getCenter().toString()),
                    TextComponent.of(region.getArea())
            ));
        } else {
            player.printInfo(TranslatableComponent.of(
                    "worldedit.selection.ellipsoid.explain.primary",
                    TextComponent.of(region.getCenter().toString())
            ));
        }

        session.describeCUI(player);
    }

    @Override
    public void explainSecondarySelection(Actor player, LocalSession session, BlockVector3 pos) {
        if (isDefined()) {
            player.printInfo(TranslatableComponent.of(
                    "worldedit.selection.ellipsoid.explain.secondary-area",
                    TextComponent.of(region.getRadius().toString()),
                    TextComponent.of(region.getArea())
            ));
        } else {
            player.printInfo(TranslatableComponent.of(
                    "worldedit.selection.ellipsoid.explain.secondary",
                    TextComponent.of(region.getRadius().toString())
            ));
        }

        session.describeCUI(player);
    }

    @Override
    public void explainRegionAdjust(Actor player, LocalSession session) {
        session.describeCUI(player);
    }

    @Override
    public boolean isDefined() {
        return started && region.getRadius().lengthSq() > 0;
    }

    @Override
    public EllipsoidRegion getRegion() throws IncompleteRegionException {
        if (!isDefined()) {
            throw new IncompleteRegionException();
        }

        return region;
    }

    @Override
    public EllipsoidRegion getIncompleteRegion() {
        return region;
    }

    @Override
    public void learnChanges() {
    }

    @Override
    public void clear() {
        region.setCenter(BlockVector3.ZERO);
        region.setRadius(Vector3.ZERO);
    }

    @Override
    public String getTypeName() {
        return "ellipsoid";
    }

    @Override
    public List<Component> getSelectionInfoLines() {
        final List<Component> lines = new ArrayList<>();

        final Vector3 center = region.getCenter();
        if (center.lengthSq() > 0) {
            lines.add(TranslatableComponent.of("worldedit.selection.ellipsoid.info.center", TextComponent.of(center.toString())));
        }

        final Vector3 radius = region.getRadius();
        if (radius.lengthSq() > 0) {
            lines.add(TranslatableComponent.of("worldedit.selection.ellipsoid.info.radius", TextComponent.of(radius.toString())));
        }

        return lines;
    }

    @Override
    public int getArea() {
        return region.getArea();
    }

    @Override
    public void describeCUI(LocalSession session, Actor player) {
        session.dispatchCUIEvent(player, new SelectionEllipsoidPointEvent(0, region.getCenter().toBlockPoint()));
        session.dispatchCUIEvent(player, new SelectionEllipsoidPointEvent(1, region.getRadius().toBlockPoint()));
    }

    @Override
    public void describeLegacyCUI(LocalSession session, Actor player) {
        session.dispatchCUIEvent(player, new SelectionPointEvent(0, region.getMinimumPoint(), getArea()));
        session.dispatchCUIEvent(player, new SelectionPointEvent(1, region.getMaximumPoint(), getArea()));
    }

    @Override
    public String getLegacyTypeID() {
        return "cuboid";
    }

    @Override
    public int getProtocolVersion() {
        return 1;
    }

    @Override
    public String getTypeID() {
        return "ellipsoid";
    }

    @Override
    public BlockVector3 getPrimaryPosition() throws IncompleteRegionException {
        return region.getCenter().toBlockPoint();
    }

}
