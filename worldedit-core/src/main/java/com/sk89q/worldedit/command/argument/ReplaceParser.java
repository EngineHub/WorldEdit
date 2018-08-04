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

import static com.sk89q.worldedit.util.GuavaUtil.firstNonNull;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.command.argument.CommandArgs;
import com.sk89q.worldedit.util.command.composition.SimpleCommand;


public class ReplaceParser extends SimpleCommand<Contextual<? extends RegionFunction>> {

    private final PatternParser fillArg = addParameter(new PatternParser("fillPattern"));

    @Override
    public Contextual<RegionFunction> call(CommandArgs args, CommandLocals locals) throws CommandException {
        Pattern fill = fillArg.call(args, locals);
        return new ReplaceFactory(fill);
    }

    @Override
    public String getDescription() {
        return "Replaces blocks";
    }

    @Override
    protected boolean testPermission0(CommandLocals locals) {
        return true;
    }

    private static class ReplaceFactory implements Contextual<RegionFunction> {
        private final Pattern fill;

        private ReplaceFactory(Pattern fill) {
            this.fill = fill;
        }

        @Override
        public RegionFunction createFromContext(EditContext context) {
            return new BlockReplace(
                    firstNonNull(context.getDestination(), new NullExtent()),
                    firstNonNull(context.getFill(), fill));
        }

        @Override
        public String toString() {
            return "replace blocks";
        }
    }

}
