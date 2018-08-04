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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.generator.ForestGenerator;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.util.command.argument.ArgumentUtils;
import com.sk89q.worldedit.util.command.argument.CommandArgs;
import com.sk89q.worldedit.util.command.argument.MissingArgumentException;
import com.sk89q.worldedit.util.command.composition.CommandExecutor;

import java.util.Arrays;
import java.util.List;

public class TreeGeneratorParser implements CommandExecutor<Contextual<ForestGenerator>> {

    private final String name;

    public TreeGeneratorParser(String name) {
        this.name = name;
    }

    private String getOptionsList() {
        return Joiner.on(" | ").join(Arrays.asList(TreeType.values()));
    }

    @Override
    public Contextual<ForestGenerator> call(CommandArgs args, CommandLocals locals) throws CommandException {
        try {
            String input = args.next();
            TreeType type = TreeGenerator.lookup(input);
            if (type != null) {
                return new GeneratorFactory(type);
            } else {
                throw new CommandException("Unknown value for <" + name + "> (try one of " + getOptionsList() + ").");
            }
        } catch (MissingArgumentException e) {
            throw new CommandException("Missing value for <" + name + "> (try one of " + getOptionsList() + ").");
        }
    }

    @Override
    public List<String> getSuggestions(CommandArgs args, CommandLocals locals) throws MissingArgumentException {
        String s = args.next();
        return s.isEmpty() ? Lists.newArrayList(TreeType.getPrimaryAliases()) : ArgumentUtils.getMatchingSuggestions(TreeType.getAliases(), s);
    }

    @Override
    public String getUsage() {
        return "<" + name + ">";
    }

    @Override
    public String getDescription() {
        return "Choose a tree generator";
    }

    @Override
    public boolean testPermission(CommandLocals locals) {
        return true;
    }

    private static final class GeneratorFactory implements Contextual<ForestGenerator> {
        private final TreeType type;

        private GeneratorFactory(TreeType type) {
            this.type = type;
        }

        @Override
        public ForestGenerator createFromContext(EditContext input) {
            return new ForestGenerator((EditSession) input.getDestination(), type);
        }

        @Override
        public String toString() {
            return "tree of type " + type;
        }
    }

}
