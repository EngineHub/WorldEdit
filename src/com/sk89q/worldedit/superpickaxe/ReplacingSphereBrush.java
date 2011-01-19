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
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 * Builds a sphere at the place being looked at.
 * 
 * @author sk89q
 */
public class ReplacingSphereBrush implements SuperPickaxeMode {
    private BaseBlock targetBlock;
    private int radius;
    
    public ReplacingSphereBrush(BaseBlock targetBlock, int radius) {
        this.targetBlock = targetBlock;
        this.radius = radius;
    }
    
    @Override
    public boolean act(ServerInterface server, LocalConfiguration config,
            LocalPlayer player, LocalSession session, WorldVector clicked) {
        WorldVector target = player.getBlockTrace(500);
        
        if (target == null) {
            player.printError("No block in sight!");
            return true;
        }
        
        BlockBag bag = session.getBlockBag(player);
        
        ReplacingExistingEditSession editSession =
                new ReplacingExistingEditSession(server, target.getWorld(),
                session.getBlockChangeLimit(), bag);
        
        editSession.enableReplacing();
        
        try {
            editSession.makeSphere(target, targetBlock, radius, true);
        } catch (MaxChangedBlocksException e) {
            player.printError("Max blocks change limit reached.");
        } finally {
            if (bag != null) {
                bag.flushChanges();
            }
            editSession.enableReplacing();
            session.remember(editSession);
        }
        
        return true;
    }

}
