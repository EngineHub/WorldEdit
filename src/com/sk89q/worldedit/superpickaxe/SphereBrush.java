// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.worldedit.superpickaxe;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * Builds a sphere at the place being looked at.
 * 
 * @author sk89q
 */
public class SphereBrush implements SuperPickaxeMode {
    private BaseBlock targetBlock;
    private int radius;
    private boolean nonReplacing;
    
    public SphereBrush(BaseBlock targetBlock, int radius, boolean nonReplacing) {
        this.targetBlock = targetBlock;
        this.radius = radius;
        this.nonReplacing = nonReplacing;
    }
    
    @Override
    public boolean act(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session, WorldVector clicked) {
        WorldVector target = player.getBlockTrace(500);
        
        if (target == null) {
            player.printError("No block in sight!");
            return true;
        }

        ReplacingEditSession editSession = new ReplacingEditSession(server, target.getWorld(),
                session.getBlockChangeLimit(), session.getBlockBag(player));
        
        if (nonReplacing) {
            editSession.disableReplacing();
        }
        
        try {
            editSession.makeSphere(target, targetBlock, radius, true);
        } catch (MaxChangedBlocksException e) {
            player.printError("Max blocks change limit reached.");
        } finally {
            editSession.enableReplacing();
            session.remember(editSession);
        }
        
        return true;
    }

}
