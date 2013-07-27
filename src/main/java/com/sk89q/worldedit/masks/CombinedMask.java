// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.masks;

import java.util.ArrayList;
import java.util.List;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;

/**
 * A mask that requires that all provided masks are matched.
 */
public class CombinedMask implements Mask {

    private final List<Mask> masks = new ArrayList<Mask>();

    /**
     * Create a new instance.
     */
    public CombinedMask() {
    }

    /**
     * Create a new instance with just one given mask.
     * 
     * @param mask the mask
     */
    public CombinedMask(Mask mask) {
        masks.add(mask);
    }

    /**
     * Create a new instance with a list of masks.
     * 
     * @param masks a list of masks
     */
    public CombinedMask(List<Mask> masks) {
        this.masks.addAll(masks);
    }

    /**
     * Get the list of masks.
     * 
     * @return the list of masks
     */
    public List<Mask> getMasks() {
        return masks;
    }

    /**
     * Add a given mask to the list of masks.
     * 
     * @param mask the mask
     */
    public void add(Mask mask) {
        masks.add(mask);
    }

    /**
     * Remove a list from the list of masks.
     * 
     * @param mask the mask
     * @return the mask that was removed, or null
     */
    public boolean remove(Mask mask) {
        return masks.remove(mask);
    }

    /**
     * Return whether this combined mask contains the given mask.
     * 
     * @param mask the mask
     * @return true if the mask is contained
     */
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

    @Override
    public String toString() {
        return String.format("{%s}", masks);
    }
    
}
