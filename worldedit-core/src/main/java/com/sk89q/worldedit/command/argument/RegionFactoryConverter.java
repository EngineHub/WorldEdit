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

package com.sk89q.worldedit.command.argument;

import com.google.common.collect.ImmutableSetMultimap;
import com.sk89q.worldedit.regions.factory.CuboidRegionFactory;
import com.sk89q.worldedit.regions.factory.CylinderRegionFactory;
import com.sk89q.worldedit.regions.factory.RegionFactory;
import com.sk89q.worldedit.regions.factory.SphereRegionFactory;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.MultiKeyConverter;
import org.enginehub.piston.inject.Key;

public class RegionFactoryConverter {

    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(RegionFactory.class),
            MultiKeyConverter.builder(
                ImmutableSetMultimap.<RegionFactory, String>builder()
                    .put(new CuboidRegionFactory(), "cuboid")
                    .put(new SphereRegionFactory(), "sphere")
                    .putAll(new CylinderRegionFactory(1), "cyl", "cylinder")
                    .build()
            )
                .errorMessage(arg -> "Not a known region type: " + arg)
                .build()
        );
    }

    private RegionFactoryConverter() {
    }
}
