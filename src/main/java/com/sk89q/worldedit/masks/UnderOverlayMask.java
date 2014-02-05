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

package com.sk89q.worldedit.masks;

import java.util.Set;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;

/**
 *
 * @author 1337
 */
public class UnderOverlayMask extends AbstractMask {
    private final int yMod;
    private Mask mask;

    public UnderOverlayMask(Mask mask, boolean overlay) {
        this.yMod = overlay ? -1 : 1;
        this.mask = mask;
    }

    @Deprecated
    public void addAll(Set<Integer> ids) {
        if (mask instanceof BlockMask) {
            final BlockMask blockTypeMask = (BlockMask) mask;
            for (Integer id : ids) {
                blockTypeMask.add(new BaseBlock(id));
            }
        } else if (mask instanceof ExistingBlockMask) {
            final BlockMask blockMask = new BlockMask();
            for (int type : ids) {
                blockMask.add(new BaseBlock(type));
            }
            mask = blockMask;
        }
    }

    @Override
    public void prepare(LocalSession session, LocalPlayer player, Vector target) {
        mask.prepare(session, player, target);
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        return !mask.matches(editSession, pos) && mask.matches(editSession, pos.add(0, yMod, 0));
    }
}
