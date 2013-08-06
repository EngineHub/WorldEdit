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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;

/**
 * Alternative selector for cuboids.
 *
 * @author sk89q
 */
public class ExtendingCuboidRegionSelector extends CuboidRegionSelector {
    public ExtendingCuboidRegionSelector(LocalWorld world) {
        super(world);
    }

    public ExtendingCuboidRegionSelector(RegionSelector oldSelector) {
        super(oldSelector);

        if (pos1 == null || pos2 == null) {
            return;
        }

        pos1 = region.getMinimumPoint().toBlockVector();
        pos2 = region.getMaximumPoint().toBlockVector();
        region.setPos1(pos1);
        region.setPos2(pos2);
    }

    public ExtendingCuboidRegionSelector(LocalWorld world, Vector pos1, Vector pos2) {
        this(world);
        pos1 = Vector.getMinimum(pos1,  pos2);
        pos2 = Vector.getMaximum(pos1,  pos2);
        region.setPos1(pos1);
        region.setPos2(pos2);
    }

    @Override
    public boolean selectPrimary(Vector pos) {
        if (pos1 != null && pos2 != null && pos.compareTo(pos1) == 0 && pos.compareTo(pos2) == 0) {
            return false;
        }

        pos1 = pos2 = pos.toBlockVector();
        region.setPos1(pos1);
        region.setPos2(pos2);
        return true;
    }

    @Override
    public boolean selectSecondary(Vector pos) {
        if (pos1 == null || pos2 == null) {
            return selectPrimary(pos);
        }

        if (region.contains(pos)) {
            return false;
        }

        double x1 = Math.min(pos.getX(), pos1.getX());
        double y1 = Math.min(pos.getY(), pos1.getY());
        double z1 = Math.min(pos.getZ(), pos1.getZ());

        double x2 = Math.max(pos.getX(), pos2.getX());
        double y2 = Math.max(pos.getY(), pos2.getY());
        double z2 = Math.max(pos.getZ(), pos2.getZ());

        final BlockVector o1 = pos1;
        final BlockVector o2 = pos2;
        pos1 = new BlockVector(x1, y1, z1);
        pos2 = new BlockVector(x2, y2, z2);
        region.setPos1(pos1);
        region.setPos2(pos2);

        assert(region.contains(o1));
        assert(region.contains(o2));
        assert(region.contains(pos));

        return true;
    }

    @Override
    public void explainPrimarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        player.print("Started selection at " + pos + " (" + region.getArea() + ").");

        explainRegionAdjust(player, session);
    }

    @Override
    public void explainSecondarySelection(LocalPlayer player, LocalSession session, Vector pos) {
        player.print("Extended selection to encompass " + pos + " (" + region.getArea() + ").");

        explainRegionAdjust(player, session);
    }
}
