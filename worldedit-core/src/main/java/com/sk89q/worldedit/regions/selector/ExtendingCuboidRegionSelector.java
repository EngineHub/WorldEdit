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

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;

/**
 * Creates a {@code CuboidRegion} from a user's selections by expanding
 * the region on every right click.
 */
public class ExtendingCuboidRegionSelector extends CuboidRegionSelector {

    /**
     * Create a new selector with a {@code null} world.
     */
    public ExtendingCuboidRegionSelector() {
        super((World) null);
    }

    /**
     * Create a new selector.
     *
     * @param world the world, which may be {@code null}
     */
    public ExtendingCuboidRegionSelector(@Nullable World world) {
        super(world);
    }

    /**
     * Create a new selector from another one.
     *
     * @param oldSelector the other selector
     */
    public ExtendingCuboidRegionSelector(RegionSelector oldSelector) {
        super(oldSelector);

        if (position1 == null || position2 == null) {
            return;
        }

        position1 = region.getMinimumPoint();
        position2 = region.getMaximumPoint();
        region.setPos1(position1);
        region.setPos2(position2);
    }

    /**
     * Create a new selector.
     *
     * @param world the world
     * @param position1 the first position
     * @param position2 the second position
     */
    public ExtendingCuboidRegionSelector(@Nullable World world, BlockVector3 position1, BlockVector3 position2) {
        this(world);
        position1 = position1.getMinimum(position2);
        position2 = position1.getMaximum(position2);
        region.setPos1(position1);
        region.setPos2(position2);
    }

    @Override
    public boolean selectPrimary(BlockVector3 position, SelectorLimits limits) {
        if (position1 != null && position2 != null && position.equals(position1) && position.equals(position2)) {
            return false;
        }

        position1 = position2 = position;
        region.setPos1(position1);
        region.setPos2(position2);
        return true;
    }

    @Override
    public boolean selectSecondary(BlockVector3 position, SelectorLimits limits) {
        if (position1 == null || position2 == null) {
            return selectPrimary(position, limits);
        }

        if (region.contains(position)) {
            return false;
        }

        double x1 = Math.min(position.getX(), position1.getX());
        double y1 = Math.min(position.getY(), position1.getY());
        double z1 = Math.min(position.getZ(), position1.getZ());

        double x2 = Math.max(position.getX(), position2.getX());
        double y2 = Math.max(position.getY(), position2.getY());
        double z2 = Math.max(position.getZ(), position2.getZ());

        final BlockVector3 o1 = position1;
        final BlockVector3 o2 = position2;
        position1 = BlockVector3.at(x1, y1, z1);
        position2 = BlockVector3.at(x2, y2, z2);
        region.setPos1(position1);
        region.setPos2(position2);

        assert(region.contains(o1));
        assert(region.contains(o2));
        assert(region.contains(position));

        return true;
    }

    @Override
    public void explainPrimarySelection(Actor player, LocalSession session, BlockVector3 pos) {
        player.printInfo(TranslatableComponent.of(
                "worldedit.selection.extend.explain.primary",
                TextComponent.of(pos.toString()),
                TextComponent.of(region.getArea())
        ));

        explainRegionAdjust(player, session);
    }

    @Override
    public void explainSecondarySelection(Actor player, LocalSession session, BlockVector3 pos) {
        player.printInfo(TranslatableComponent.of(
                "worldedit.selection.extend.explain.secondary",
                TextComponent.of(pos.toString()),
                TextComponent.of(region.getArea())
        ));

        explainRegionAdjust(player, session);
    }

}
