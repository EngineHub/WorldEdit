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

package org.enginehub.command.parametric;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.enginehub.command.Command;
import org.enginehub.command.CommandContext;
import org.enginehub.command.CommandException;
import org.enginehub.command.Suggestion;
import org.enginehub.command.SuggestionProvider;

/**
 * An implementation of a command bound to a {@link java.lang.reflect.Method} that
 * identifies the parameters to pass to the method automatically.
 */
public abstract class ParametricCommand implements Command, SuggestionProvider {

    private String name;
    private String[] aliases;
    private String description;
    private String help;
    private String usage;
    private ParameterResolver<?>[] resolvers;

    @Override
    public boolean execute(CommandContext context) throws CommandException {
        List<Object> args = new ArrayList<Object>();

        for (ParameterResolver<?> resolver : resolvers) {
            Object object = resolver.resolve(context);
            args.add(object);
        }

        execute(args); // Handled by sub-class
        
        return true;
    }
    
    /**
     * Execute the command using the list of arguments (that have been automatically
     * parsed using relevant {@link ParameterResolver}s).
     * 
     * @param arguments the list of arguments
     * @throws CommandException on a command exception
     */
    protected abstract void execute(List<Object> arguments) throws CommandException;

    @Override
    public Set<Suggestion> getProposals(CommandContext context) {
        return new HashSet<Suggestion>(); // @TODO: Actually generate proposals
    }

    @Override
    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    @Override
    public String[] getAliases() {
        return aliases;
    }

    void setAliases(String[] aliases) {
        this.aliases = aliases;
    }

    @Override
    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getHelp() {
        return help;
    }

    void setHelp(String help) {
        this.help = help;
    }

    @Override
    public String getUsage() {
        return usage;
    }

    void setUsage(String usage) {
        this.usage = usage;
    }

    public ParameterResolver<?>[] getResolvers() {
        return resolvers;
    }

    void setResolvers(ParameterResolver<?>[] resolvers) {
        this.resolvers = resolvers;
    }
}
