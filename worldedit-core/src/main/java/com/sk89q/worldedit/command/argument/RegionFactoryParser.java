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

import com.google.common.collect.Lists;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.regions.factory.CuboidRegionFactory;
import com.sk89q.worldedit.regions.factory.CylinderRegionFactory;
import com.sk89q.worldedit.regions.factory.RegionFactory;
import com.sk89q.worldedit.regions.factory.SphereRegionFactory;
import com.sk89q.worldedit.util.command.argument.ArgumentUtils;
import com.sk89q.worldedit.util.command.argument.CommandArgs;
import com.sk89q.worldedit.util.command.argument.MissingArgumentException;
import com.sk89q.worldedit.util.command.composition.CommandExecutor;

import java.util.List;

public class RegionFactoryParser implements CommandExecutor<RegionFactory> {

    @Override
    public RegionFactory call(CommandArgs args, CommandLocals locals) throws CommandException {
        try {
            String type = args.next();

            switch (type) {
                case "cuboid":
                    return new CuboidRegionFactory();
                case "sphere":
                    return new SphereRegionFactory();
                case "cyl":
                case "cylinder":
                    return new CylinderRegionFactory(1); // TODO: Adjustable height

                default:
                    throw new CommandException("Unknown shape type: " + type + " (try one of " + getUsage() + ")");
            }
        } catch (MissingArgumentException e) {
            throw new CommandException("Missing shape type (try one of " + getUsage() + ")");

        }
    }

    @Override
    public List<String> getSuggestions(CommandArgs args, CommandLocals locals) throws MissingArgumentException {
        return ArgumentUtils.getMatchingSuggestions(Lists.newArrayList("cuboid", "sphere", "cyl"), args.next());
    }

    @Override
    public String getUsage() {
        return "(cuboid | sphere | cyl)";
    }

    @Override
    public String getDescription() {
        return "Defines a region";
    }

    @Override
    public boolean testPermission(CommandLocals locals) {
        return true;
    }

}
