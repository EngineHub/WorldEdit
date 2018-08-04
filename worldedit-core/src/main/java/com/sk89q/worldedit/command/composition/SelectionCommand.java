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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.command.argument.CommandArgs;
import com.sk89q.worldedit.util.command.composition.CommandExecutor;
import com.sk89q.worldedit.util.command.composition.SimpleCommand;

import java.util.List;

public class SelectionCommand extends SimpleCommand<Operation> {

    private final CommandExecutor<Contextual<? extends Operation>> delegate;
    private final String permission;

    public SelectionCommand(CommandExecutor<Contextual<? extends Operation>> delegate, String permission) {
        checkNotNull(delegate, "delegate");
        checkNotNull(permission, "permission");
        this.delegate = delegate;
        this.permission = permission;
        addParameter(delegate);
    }

    @Override
    public Operation call(CommandArgs args, CommandLocals locals) throws CommandException {
        if (!testPermission(locals)) {
            throw new CommandPermissionsException();
        }

        Contextual<? extends Operation> operationFactory = delegate.call(args, locals);

        Actor actor = locals.get(Actor.class);
        if (actor instanceof Player) {
            try {
                Player player = (Player) actor;
                LocalSession session = WorldEdit.getInstance().getSessionManager().get(player);
                Region selection = session.getSelection(player.getWorld());

                EditSession editSession = session.createEditSession(player);
                editSession.enableQueue();
                locals.put(EditSession.class, editSession);
                session.tellVersion(player);

                EditContext editContext = new EditContext();
                editContext.setDestination(locals.get(EditSession.class));
                editContext.setRegion(selection);

                Operation operation = operationFactory.createFromContext(editContext);
                Operations.completeBlindly(operation);

                List<String> messages = Lists.newArrayList();
                operation.addStatusMessages(messages);
                if (messages.isEmpty()) {
                    actor.print("Operation completed.");
                } else {
                    actor.print("Operation completed (" + Joiner.on(", ").join(messages) + ").");
                }

                return operation;
            } catch (IncompleteRegionException e) {
                WorldEdit.getInstance().getPlatformManager().getCommandManager().getExceptionConverter().convert(e);
                return null;
            }
        } else {
            throw new CommandException("This command can only be used by players.");
        }
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    protected boolean testPermission0(CommandLocals locals) {
        return locals.get(Actor.class).hasPermission(permission);
    }

}
