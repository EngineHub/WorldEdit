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
import com.sk89q.worldedit.function.mask.MaskIntersection;

import java.util.ArrayList;
import java.util.List;

/**
 * @deprecated See {@link MaskIntersection}
 */
@Deprecated
public class CombinedMask extends AbstractMask {
    private final List<Mask> masks = new ArrayList<Mask>();

    public CombinedMask() {
    }

    public CombinedMask(Mask mask) {
        add(mask);
    }

    public CombinedMask(Mask ...mask) {
        for (Mask m : mask) {
            add(m);
        }
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
