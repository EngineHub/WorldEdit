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

package com.sk89q.worldedit.masks;

import java.util.ArrayList;
import java.util.List;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;

public class CombinedMask extends AbstractMask {
    private final List<Mask> masks = new ArrayList<Mask>();

    public CombinedMask() {
    }

    public CombinedMask(Mask mask) {
        masks.add(mask);
    }

    public CombinedMask(List<Mask> masks) {
        this.masks.addAll(masks);
    }

    public void add(Mask mask) {
        masks.add(mask);
    }

    public boolean remove(Mask mask) {
        return masks.remove(mask);
    }

    public boolean has(Mask mask) {
        return masks.contains(mask);
    }

    @Override
    public void prepare(LocalSession session, LocalPlayer player, Vector target) {
        for (Mask mask : masks) {
            mask.prepare(session, player, target);
        }
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        for (Mask mask : masks) {
            if (!mask.matches(editSession, pos)) {
                return false;
            }
        }

        return true;
    }
}
