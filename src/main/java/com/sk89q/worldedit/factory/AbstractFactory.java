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

package com.sk89q.worldedit.factory;

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.WorldEdit;

/**
 * Base class for factories.
 */
abstract class AbstractFactory {

    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit a WorldEdit instance
     */
    protected AbstractFactory(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    /**
     * Get WorldEdit.
     *
     * @return the WorldEdit
     */
    protected WorldEdit getWorldEdit() {
        return worldEdit;
    }

    /**
     * Get the configuration.
     *
     * @return the configuration
     */
    protected LocalConfiguration getConfiguration() {
        return getWorldEdit().getConfiguration();
    }

    /**
     * Get the server interface.
     *
     * @return the server interface
     */
    protected ServerInterface getServer() {
        return getWorldEdit().getServer();
    }

}
