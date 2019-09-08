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

package com.sk89q.worldedit.command.util;

import com.google.common.collect.ImmutableSet;
import org.enginehub.piston.Command;
import org.enginehub.piston.inject.InjectedValueAccess;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class SubCommandPermissionCondition extends PermissionCondition {

    private final Command.Condition aggregate;

    private SubCommandPermissionCondition(Set<String> perms, Command.Condition aggregate) {
        super(perms);
        this.aggregate = aggregate;
    }

    @Override
    public boolean satisfied(InjectedValueAccess context) {
        return aggregate.satisfied(context);
    }

    public static class Generator {
        private final List<Command> subCommands;

        public Generator(List<Command> subCommands) {
            this.subCommands = subCommands;
        }

        public Command.Condition build() {
            final List<Command.Condition> conditions = subCommands.stream().map(Command::getCondition).collect(Collectors.toList());
            final List<Optional<PermissionCondition>> permConds = conditions.stream().map(c -> c.as(PermissionCondition.class)).collect(Collectors.toList());
            if (permConds.stream().anyMatch(o -> !o.isPresent())) {
                // if any sub-command doesn't require permissions, then this command doesn't require permissions
                return new PermissionCondition(ImmutableSet.of());
            }
            // otherwise, this command requires any one subcommand to be available
            final Set<String> perms = permConds.stream().map(Optional::get).flatMap(cond -> cond.getPermissions().stream()).collect(Collectors.toSet());
            final Command.Condition aggregate = permConds.stream().map(Optional::get)
                    .map(c -> (Command.Condition) c)
                    .reduce(Command.Condition::or).orElse(TRUE);
            return new SubCommandPermissionCondition(perms, aggregate);
        }
    }
}
