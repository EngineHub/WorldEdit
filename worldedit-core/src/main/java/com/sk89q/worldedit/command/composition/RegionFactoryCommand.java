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

package com.sk89q.worldedit.command.composition;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.regions.factory.CuboidRegionFactory;
import com.sk89q.worldedit.regions.factory.CylinderRegionFactory;
import com.sk89q.worldedit.regions.factory.RegionFactory;
import com.sk89q.worldedit.regions.factory.SphereRegionFactory;
import com.sk89q.worldedit.util.command.CommandExecutor;
import com.sk89q.worldedit.util.command.argument.CommandArgs;

public class RegionFactoryCommand extends CommandExecutor<RegionFactory> {

    @Override
    public RegionFactory call(CommandArgs args, CommandLocals locals, String[] parentCommands) throws CommandException {
        String type = args.next();

        if (type.equals("cuboid")) {
            return new CuboidRegionFactory();
        } else if (type.equals("sphere")) {
            return new SphereRegionFactory();
        } else if (type.equals("cyl") || type.equals("cylinder")) {
            return new CylinderRegionFactory(1); // TODO: Adjustable height
        } else {
            throw new CommandException("Unknown shape type: " + type);
        }
    }

}
