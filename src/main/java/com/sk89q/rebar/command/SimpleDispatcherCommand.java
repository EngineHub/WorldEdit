// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.rebar.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;

/**
 * A combination {@link Dispatcher} and {@link CommandCallable} that is backed by
 * a {@link SimpleDispatcher}.
 * 
 * <p>The primary use of this is to make nested commands.</p>
 */
public class SimpleDispatcherCommand extends SimpleDispatcher implements CommandCallable {
    
    private final SimpleDescription description = new SimpleDescription();

    @Override
    public Set<Character> getValueFlags() {
        return Collections.emptySet();
    }

    @Override
    public void call(CommandContext context) throws CommandException {
        if (context.argsLength() >= 1) {
            super.call(context.getRemainingString(0), context.getLocals());
        } else {
            Set<String> aliases = getPrimaryAliases();
            
            if (aliases.size() == 0) {
                throw new InvalidUsageException(
                        "This command is supposed to have sub-commands, " +
                        "but it has no sub-commands.", 
                        getDescription());
            }
            
            StringBuilder builder = new StringBuilder();
            for (String alias : getPrimaryAliases()) {
                builder.append("\n- ").append(alias);
            }
            
            if (aliases.size() == 1) {
                builder.append(" (there is only one)");
            }
            
            throw new InvalidUsageException(
                    "Select one of these subcommand(s):" + builder.toString(), 
                    getDescription());
        }
    }

    @Override
    public SimpleDescription getDescription() {
        return description;
    }

    @Override
    public Collection<String> getSuggestions(CommandContext context) throws CommandException {
        if (context.argsLength() == 0) {
            return super.getAllAliases();
        } else if (context.argsLength() == 1 && 
                context.getSuggestionContext().forLastValue()) {
            String prefix = context.getString(0).toLowerCase();
            List<String> suggestions = new ArrayList<String>();
            for (String alias : super.getAllAliases()) {
                if (alias.startsWith(prefix)) {
                    suggestions.add(alias);
                }
            }
            return suggestions;
        }
        
        return super.getSuggestions(
                context.argsLength() > 1 ? context.getRemainingString(1) : "");
    }

}
