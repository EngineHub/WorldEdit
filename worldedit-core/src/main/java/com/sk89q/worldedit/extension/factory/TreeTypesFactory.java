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

package com.sk89q.worldedit.extension.factory;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.internal.registry.AbstractFactory;
import com.sk89q.worldedit.util.TreeTypes;

/**
 * A registry of known {@link TreeTypes}s. Provides methods to instantiate
 * new TreeTypes from input.
 *
 * <p>Instances of this class can be taken from
 * {@link WorldEdit#getTreeTypesFactory()}.</p>
 */
public final class TreeTypesFactory extends AbstractFactory<TreeTypes> {

    /**
     * Create a new instance.
     *
     * @param worldEdit the WorldEdit instance
     */
    public TreeTypesFactory(WorldEdit worldEdit) {
        super(worldEdit);

        parsers.add(new TreeTypesParser(worldEdit));
    }

}
