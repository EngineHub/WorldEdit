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
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.generator.ForestGenerator;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.util.command.CommandExecutor;
import com.sk89q.worldedit.util.command.argument.CommandArgs;

import javax.annotation.Nullable;
import java.util.Arrays;

public class TreeGeneratorCommand extends CommandExecutor<Function<EditContext, ForestGenerator>> {

    @Override
    public Function<EditContext, ForestGenerator> call(CommandArgs args, CommandLocals locals, String[] parentCommands) throws CommandException {
        String input = args.next();
        TreeType type = TreeGenerator.lookup(input);
        if (type != null) {
            return new GeneratorFactory(type);
        } else {
            throw new CommandException(String.format("Can't recognize tree type '%s' -- choose from: %s", input, Arrays.toString(TreeType.values())));
        }
    }

    private static class GeneratorFactory implements Function<EditContext, ForestGenerator> {
        private final TreeType type;

        private GeneratorFactory(TreeType type) {
            this.type = type;
        }

        @Nullable
        @Override
        public ForestGenerator apply(EditContext input) {
            return new ForestGenerator((EditSession) input.getDestination(), new TreeGenerator(type));
        }

        @Override
        public String toString() {
            return "tree of type " + type;
        }
    }
}
