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

import com.sk89q.worldedit.regions.factory.CuboidRegionFactory;
import com.sk89q.worldedit.regions.factory.CylinderRegionFactory;
import com.sk89q.worldedit.regions.factory.RegionFactory;
import com.sk89q.worldedit.regions.factory.SphereRegionFactory;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.converter.SuccessfulConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

public class RegionFactoryConverter implements ArgumentConverter<RegionFactory> {

    public static void register(CommandManager commandManager) {
        commandManager.registerConverter(Key.of(RegionFactory.class), new RegionFactoryConverter());
    }

    private RegionFactoryConverter() {
    }

    @Override
    public Component describeAcceptableArguments() {
        return TextComponent.of("cuboid|sphere|cyl");
    }

    @Override
    public ConversionResult<RegionFactory> convert(String argument, InjectedValueAccess context) {
        try {
            return SuccessfulConversion.fromSingle(parse(argument));
        } catch (Exception e) {
            return FailedConversion.from(e);
        }
    }

    private RegionFactory parse(String argument) {
        switch (argument) {
            case "cuboid":
                return new CuboidRegionFactory();
            case "sphere":
                return new SphereRegionFactory();
            case "cyl":
            case "cylinder":
                return new CylinderRegionFactory(1); // TODO: Adjustable height
            default:
                throw new IllegalArgumentException("Not a known region type: " + argument);
        }
    }
}
