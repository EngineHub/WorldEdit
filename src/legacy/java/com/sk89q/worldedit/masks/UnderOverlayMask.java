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

package com.sk89q.worldedit.masks;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.mask.OffsetMask;

import java.util.Set;

/**
 * @deprecated Use {@link OffsetMask} with {@link MaskIntersection} and {@link Masks#negate(com.sk89q.worldedit.function.mask.Mask)}
 */
@Deprecated
public class UnderOverlayMask extends AbstractMask {
    private final int yMod;
    private Mask mask;

    @Deprecated
    public UnderOverlayMask(Set<Integer> ids, boolean overlay) {
        this(new BlockTypeMask(ids), overlay);
    }
    
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
