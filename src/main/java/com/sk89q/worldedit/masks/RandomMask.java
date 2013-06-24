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

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;

/**
 * Randomly returns whether a block matches this mask or not.
 */
public class RandomMask implements Mask {

    private final double ratio;

    /**
     * Create a new instance with the given ratio.
     * 
     * @param ratio a number between 0 and 1, inclusive, where 1 is always true
     */
    public RandomMask(double ratio) {
        this.ratio = ratio;
    }

    /**
     * Get the ratio.
     * 
     * @return a number between 0 and 1, inclusive, where 1 is always true
     */
    public double getRatio() {
        return ratio;
    }

    @Override
    public void prepare(LocalSession session, LocalPlayer player, Vector target) {
    }

    @Override
    public boolean matches(EditSession editSession, Vector pos) {
        return Math.random() < ratio;
    }

    @Override
    public String toString() {
        return String.format("RandomMask(ratio=%g)", ratio);
    }
    
}
