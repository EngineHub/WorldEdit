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

package com.sk89q.worldedit.command;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.tool.AreaPickaxe;
import com.sk89q.worldedit.command.tool.RecursivePickaxe;
import com.sk89q.worldedit.command.tool.SinglePickaxe;
import com.sk89q.worldedit.entity.Player;

public class SuperPickaxeCommands {
    private final WorldEdit we;

    public SuperPickaxeCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        aliases = { "single" },
        usage = "",
        desc = "Enable the single block super pickaxe mode",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.superpickaxe")
    public void single(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        session.setSuperPickaxe(new SinglePickaxe());
        session.enableSuperPickAxe();
        player.print("Mode changed. Left click with a pickaxe. // to disable.");
    }

    @Command(
        aliases = { "area" },
        usage = "<radius>",
        desc = "Enable the area super pickaxe pickaxe mode",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.superpickaxe.area")
    public void area(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();
        int range = args.getInteger(0);

        if (range > config.maxSuperPickaxeSize) {
            player.printError("Maximum range: " + config.maxSuperPickaxeSize);
            return;
        }

        session.setSuperPickaxe(new AreaPickaxe(range));
        session.enableSuperPickAxe();
        player.print("Mode changed. Left click with a pickaxe. // to disable.");
    }

    @Command(
        aliases = { "recur", "recursive" },
        usage = "<radius>",
        desc = "Enable the recursive super pickaxe pickaxe mode",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.superpickaxe.recursive")
    public void recursive(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();
        double range = args.getDouble(0);

        if (range > config.maxSuperPickaxeSize) {
            player.printError("Maximum range: " + config.maxSuperPickaxeSize);
            return;
        }

        session.setSuperPickaxe(new RecursivePickaxe(range));
        session.enableSuperPickAxe();
        player.print("Mode changed. Left click with a pickaxe. // to disable.");
    }
}
