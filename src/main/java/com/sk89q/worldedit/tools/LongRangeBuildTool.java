// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.worldedit.tools;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.patterns.Pattern;

/**
 * A tool that can place (or remove) blocks at a distance.
 *
 * @author wizjany
 */
public class LongRangeBuildTool extends BrushTool implements DoubleActionTraceTool {

    Pattern primary;
    Pattern secondary;

    public LongRangeBuildTool(Pattern primary, Pattern secondary) {
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
            final BaseBlock secondaryBlock = secondary.next(pos);
            if (secondaryBlock.getType() == BlockID.AIR) {
                eS.setBlock(pos, secondaryBlock);
            } else {
                eS.setBlock(pos.getFaceVector(), secondaryBlock);
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
            final BaseBlock primaryBlock = primary.next(pos);
            if (primaryBlock.getType() == BlockID.AIR) {
                eS.setBlock(pos, primaryBlock);
            } else {
                eS.setBlock(pos.getFaceVector(), primaryBlock);
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
