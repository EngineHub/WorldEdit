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

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.command.argument.CommandArgs;
import com.sk89q.worldedit.util.command.composition.SimpleCommand;
import com.sk89q.worldedit.world.World;

public class ItemUseParser extends SimpleCommand<Contextual<RegionFunction>> {

    private final ItemParser itemParser = addParameter(new ItemParser("item", "minecraft:dye:15"));

    @Override
    public Contextual<RegionFunction> call(CommandArgs args, CommandLocals locals) throws CommandException {
        BaseItem item = itemParser.call(args, locals);
        return new ItemUseFactory(item);
    }

    @Override
    public String getDescription() {
        return "Applies an item";
    }

    @Override
    protected boolean testPermission0(CommandLocals locals) {
        return true;
    }

    private static final class ItemUseFactory implements Contextual<RegionFunction> {
        private final BaseItem item;

        private ItemUseFactory(BaseItem item) {
            this.item = item;
        }

        @Override
        public RegionFunction createFromContext(EditContext input) {
            World world = ((EditSession) input.getDestination()).getWorld();
            return new ItemUseFunction(world, item);
        }

        @Override
        public String toString() {
            return "application of the item " + item.getType() + ":" + item.getNbtData();
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
        public boolean apply(BlockVector3 position) throws WorldEditException {
            return world.useItem(position, item, Direction.UP);
        }
    }

}
