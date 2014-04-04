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

package com.sk89q.worldedit.command.tool;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.regions.RegionSelector;

/**
 * A wand that can be used at a distance.
 * 
 * @author wizjany
 */
public class DistanceWand extends BrushTool implements DoubleActionTraceTool {

    public DistanceWand() {
        super("worldedit.wand");
    }

    @Override
    public boolean canUse(LocalPlayer player) {
        return player.hasPermission("worldedit.wand");
    }

    public boolean actSecondary(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session) {
        if (session.isToolControlEnabled() && player.hasPermission("worldedit.selection.pos")) {
            WorldVector target = getTarget(player);
            if (target == null) return true;

            RegionSelector selector = session.getRegionSelector(player.getWorld());
            if (selector.selectPrimary(target)) {
                selector.explainPrimarySelection(player, session, target);
            }
            return true;

        }
        return false;

    }

    @Override
    public boolean actPrimary(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session) {
        if (session.isToolControlEnabled() && player.hasPermission("worldedit.selection.pos")) {
            WorldVector target = getTarget(player);
            if (target == null) return true;

            RegionSelector selector = session.getRegionSelector(player.getWorld());
            if (selector.selectSecondary(target)) {
                selector.explainSecondarySelection(player, session, target);
            }
            return true;

        }
        return false;
    }

    public WorldVector getTarget(LocalPlayer player) {
        WorldVector target = null;
        if (this.range > -1) {
            target = player.getBlockTrace(getRange(), true);
        } else {
            target = player.getBlockTrace(MAX_RANGE);
        }

        if (target == null) {
            player.printError("No block in sight!");
            return null;
        }

        return target;
    }
}
