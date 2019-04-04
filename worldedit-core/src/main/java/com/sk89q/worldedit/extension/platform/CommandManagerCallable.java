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

package com.sk89q.worldedit.extension.platform;

import com.google.common.base.Splitter;
import com.google.inject.Key;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.util.command.CommandCallable;
import com.sk89q.worldedit.util.command.Description;
import org.enginehub.piston.CommandManager;

import java.util.Collections;
import java.util.List;

/**
 * Hack to get {@link CommandManager} working under {@link CommandCallable}.
 */
public class CommandManagerCallable implements CommandCallable {

    private final WorldEdit worldEdit;
    private final CommandManager manager;
    private final Description description;

    public CommandManagerCallable(WorldEdit worldEdit, CommandManager manager, Description description) {
        this.worldEdit = worldEdit;
        this.manager = manager;
        this.description = description;
    }

    @Override
    public Object call(String arguments, CommandLocals locals, String[] parentCommands) throws CommandException {
        manager.injectValue(Key.get(Actor.class), () -> locals.get(Actor.class));
        manager.injectValue(Key.get(Player.class), () -> getPlayer(locals));
        manager.injectValue(Key.get(LocalSession.class), () -> {
            Player sender = getPlayer(locals);
            return worldEdit.getSessionManager().get(sender);
        });
        manager.injectValue(Key.get(EditSession.class), () -> {
            Player sender = getPlayer(locals);
            LocalSession session = worldEdit.getSessionManager().get(sender);
            EditSession editSession = session.createEditSession(sender);
            editSession.enableStandardMode();
            session.tellVersion(sender);
            return editSession;
        });
        return manager.execute(Splitter.on(' ').splitToList(arguments));
    }

    private Player getPlayer(CommandLocals locals) {
        Actor actor = locals.get(Actor.class);
        return actor instanceof Player ? (Player) actor : null;
    }

    @Override
    public Description getDescription() {
        return description;
    }

    @Override
    public boolean testPermission(CommandLocals locals) {
        return true;
    }

    @Override
    public List<String> getSuggestions(String arguments, CommandLocals locals) throws CommandException {
        return Collections.emptyList();
    }
}
