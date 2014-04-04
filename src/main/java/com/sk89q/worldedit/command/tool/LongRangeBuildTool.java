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
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * A tool that can place (or remove) blocks at a distance.
 *
 * @author wizjany
 */
public class LongRangeBuildTool extends BrushTool implements DoubleActionTraceTool {

    BaseBlock primary;
    BaseBlock secondary;

    public LongRangeBuildTool(BaseBlock primary, BaseBlock secondary) {
        super("worldedit.tool.lrbuild");
        this.primary = primary;
        this.secondary = secondary;
    }

    @Override
    public boolean canUse(LocalPlayer player) {
        return player.hasPermission("worldedit.tool.lrbuild");
    }

    public boolean actSecondary(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session) {

        WorldVectorFace pos = getTargetFace(player);
        if (pos == null) return false;
        EditSession eS = session.createEditSession(player);
        try {
            if (secondary.getType() == BlockID.AIR) {
                eS.setBlock(pos, secondary);
            } else {
                eS.setBlock(pos.getFaceVector(), secondary);
            }
            return true;
        } catch (MaxChangedBlocksException e) {
            // one block? eat it
        }
        return false;

    }

    @Override
    public boolean actPrimary(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session) {

        WorldVectorFace pos = getTargetFace(player);
        if (pos == null) return false;
        EditSession eS = session.createEditSession(player);
        try {
            if (primary.getType() == BlockID.AIR) {
                eS.setBlock(pos, primary);
            } else {
                eS.setBlock(pos.getFaceVector(), primary);
            }
            return true;
        } catch (MaxChangedBlocksException e) {
            // one block? eat it
        }
        return false;
    }

    public WorldVectorFace getTargetFace(LocalPlayer player) {
        WorldVectorFace target = null;
        target = player.getBlockTraceFace(getRange(), true);

        if (target == null) {
            player.printError("No block in sight!");
            return null;
        }

        return target;
    }
}
