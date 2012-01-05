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

package com.sk89q.worldedit.blocks;

/**
 * Represents a block that stores items.
 * 
 * @author sk89q
 */
public interface ContainerBlock {
    /**
     * Get the list of items.
     *
     * @return
     */
    public BaseItemStack[] getItems();

    /**
     * Set the list of items.
     * 
     * @param items 
     */
    public void setItems(BaseItemStack[] items);
}
