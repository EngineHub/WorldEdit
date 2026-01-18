/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.internal.schematic.backends;

import com.sk89q.worldedit.internal.schematic.SchematicsManager;

import java.nio.file.Path;
import java.util.Set;

/**
 * {@link SchematicsManager} backend interface.
 */
public interface SchematicsBackend {

    /**
     * Initialize the backend.
     */
    void init();

    /**
     * Uninitialize the backend.
     */
    void uninit();

    /**
     * Gets the set of known schematic paths.
     *
     * @return the set of known schematics
     */
    Set<Path> getPaths();

    /**
     * Tells the backend that there are changes it should take into account.
     */
    void update();
}