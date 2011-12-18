// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

import com.sk89q.worldedit.LocalPlayer;

/**
 * Represents a tool. This interface alone defines nothing. A tool also
 * has to implement <code>BlockTool</code> or <code>TraceTool</code>.
 *
 * @author sk89q
 */
public abstract interface Tool {

    /**
     * Checks to see if the player can still be using this tool (considering
     * permissions and such).
     * 
     * @param player
     * @return
     */
    public boolean canUse(LocalPlayer player);

}
