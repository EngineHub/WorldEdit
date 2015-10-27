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

import com.google.common.base.Function;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.command.CommandExecutor;
import com.sk89q.worldedit.util.command.argument.CommandArgs;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;

public class ItemUseCommand extends CommandExecutor<Function<EditContext, RegionFunction>> {

    @Override
    public Function<EditContext, RegionFunction> call(CommandArgs args, CommandLocals locals, String[] parentCommands) throws CommandException {
        BaseItem item = new ItemCommand().call(args, locals, parentCommands);
        return new ItemUseFactory(item);
    }

    private static final class ItemUseFactory implements Function<EditContext, RegionFunction> {
        private final BaseItem item;

        private ItemUseFactory(BaseItem item) {
            this.item = item;
        }

        @Nullable
        @Override
        public RegionFunction apply(EditContext input) {
            World world = ((EditSession) input.getDestination()).getWorld();
            return new ItemUseFunction(world, item);
        }

        @Override
        public String toString() {
            return "application of the item " + item.getType() + ":" + item.getData();
        }
    }

    private static final class ItemUseFunction implements RegionFunction {
        private final World world;
        private final BaseItem item;

        private ItemUseFunction(World world, BaseItem item) {
            this.world = world;
            this.item = item;
        }

        @Override
        public boolean apply(Vector position) throws WorldEditException {
            return world.useItem(position, item, Direction.UP);
        }
    }

}
