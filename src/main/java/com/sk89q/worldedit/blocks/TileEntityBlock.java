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

import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.data.*;

import java.util.Map;

/**
 * A class implementing this interface has extra TileEntityBlock data to store.
 *
 * @author sk89q
 */
public interface TileEntityBlock {

    /**
     * Return the name of the title entity ID.
     *
     * @return tile entity ID
     */
    public abstract String getTileEntityID();

    /**
     * Store additional tile entity data.
     *
     * @return map of values
     * @throws DataException When invalid data is encountered
     */
    public abstract Map<String, Tag> toTileEntityNBT()
            throws DataException;

    /**
     * Get additional information from the tile entity data.
     *
     * @param values map of data
     * @throws DataException When invalid data is encountered
     */
    public abstract void fromTileEntityNBT(Map<String, Tag> values)
            throws DataException;


}
