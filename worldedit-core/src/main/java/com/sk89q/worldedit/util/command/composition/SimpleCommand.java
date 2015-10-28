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

package com.sk89q.worldedit.util.command.composition;

import com.google.common.collect.Lists;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.util.command.argument.CommandArgs;
import com.sk89q.worldedit.util.command.argument.MissingArgumentException;

import java.util.List;

public abstract class SimpleCommand<T> extends ParameterCommand<T> {

    @Override
    public final List<String> getSuggestions(CommandArgs args, CommandLocals locals) throws MissingArgumentException {
        List<String> suggestions = Lists.newArrayList();
        boolean seenParameter = false;
        for (CommandExecutor<?> parameter : getParameters()) {
            try {
                suggestions = parameter.getSuggestions(args, locals);
                seenParameter = true;
            } catch (MissingArgumentException e) {
                if (seenParameter) {
                    return suggestions;
                } else {
                    throw e;
                }
            }

            // There's nothing more anyway
            if (args.position() == args.size()) {
                return suggestions;
            }
        }
        return suggestions;
    }
}
