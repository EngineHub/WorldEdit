/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.command;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.Arg;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Commands to undo, redo, and clear history.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class HistoryCommands {

    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     */
    public HistoryCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @Command(
        name = "undo",
        aliases = { "/undo" },
        desc = "Undoes the last action (from history)"
    )
    @CommandPermissions({"worldedit.history.undo", "worldedit.history.undo.self"})
    public void undo(Actor actor, LocalSession session,
                     @Arg(desc = "Number of undoes to perform", def = "1")
                         int times,
                     @Arg(name = "player", desc = "Undo this player's operations", def = "")
                         String playerName) throws WorldEditException {
        times = Math.max(1, times);
        LocalSession undoSession = session;
        if (playerName != null) {
            actor.checkPermission("worldedit.history.undo.other");
            undoSession = worldEdit.getSessionManager().findByName(playerName);
            if (undoSession == null) {
                actor.printError(TranslatableComponent.of("worldedit.session.cant-find-session", TextComponent.of(playerName)));
                return;
            }
        }
        int timesUndone = 0;
        for (int i = 0; i < times; ++i) {
            BlockBag blockBag = actor instanceof Player ? undoSession.getBlockBag((Player) actor) : null;
            EditSession undone = undoSession.undo(blockBag, actor);
            if (undone != null) {
                timesUndone++;
                worldEdit.flushBlockBag(actor, undone);
            } else {
                break;
            }
        }
        if (timesUndone > 0) {
            actor.printInfo(TranslatableComponent.of("worldedit.undo.undone", TextComponent.of(timesUndone)));
        } else {
            actor.printError(TranslatableComponent.of("worldedit.undo.none"));
        }
    }

    @Command(
        name = "redo",
        aliases = { "/redo" },
        desc = "Redoes the last action (from history)"
    )
    @CommandPermissions({"worldedit.history.redo", "worldedit.history.redo.self"})
    public void redo(Actor actor, LocalSession session,
                     @Arg(desc = "Number of redoes to perform", def = "1")
                         int times,
                     @Arg(name = "player", desc = "Redo this player's operations", def = "")
                         String playerName) throws WorldEditException {
        times = Math.max(1, times);
        LocalSession redoSession = session;
        if (playerName != null) {
            actor.checkPermission("worldedit.history.redo.other");
            redoSession = worldEdit.getSessionManager().findByName(playerName);
            if (redoSession == null) {
                actor.printError(TranslatableComponent.of("worldedit.session.cant-find-session", TextComponent.of(playerName)));
                return;
            }
        }
        int timesRedone = 0;
        for (int i = 0; i < times; ++i) {
            BlockBag blockBag = actor instanceof Player ? redoSession.getBlockBag((Player) actor) : null;
            EditSession redone = redoSession.redo(blockBag, actor);
            if (redone != null) {
                timesRedone++;
                worldEdit.flushBlockBag(actor, redone);
            } else {
                break;
            }
        }
        if (timesRedone > 0) {
            actor.printInfo(TranslatableComponent.of("worldedit.redo.redone", TextComponent.of(timesRedone)));
        } else {
            actor.printError(TranslatableComponent.of("worldedit.redo.none"));
        }
    }

    @Command(
        name = "clearhistory",
        aliases = { "/clearhistory" },
        desc = "Clear your history"
    )
    @CommandPermissions("worldedit.history.clear")
    public void clearHistory(Actor actor, LocalSession session) {
        session.clearHistory();
        actor.printInfo(TranslatableComponent.of("worldedit.clearhistory.cleared"));
    }

}
