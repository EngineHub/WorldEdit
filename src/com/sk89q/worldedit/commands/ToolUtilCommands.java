// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.commands;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.masks.Mask;

/**
 * Tool commands.
 * 
 * @author sk89q
 */
public class ToolUtilCommands {
    @Command(
        aliases = {"/", ","},
        usage = "",
        desc = "Toggle the super pickaxe pickaxe function",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.superpickaxe"})
    public static void togglePickaxe(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        if (session.toggleSuperPickAxe()) {
            player.print("Super pick axe enabled.");
        } else {
            player.print("Super pick axe disabled.");
        }
    }

    @Command(
        aliases = {"pickaxe", "pa", "spa"},
        desc = "Select super pickaxe mode"
    )
    @NestedCommand({SuperPickaxeCommands.class})
    public static void pickaxe(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
    }

    @Command(
        aliases = {"tool", "t"},
        desc = "Select a tool to bind"
    )
    @NestedCommand({ToolCommands.class})
    public static void tool(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
    }

    @Command(
        aliases = {"mask"},
        usage = "[mask]",
        desc = "Set the brush mask",
        min = 0,
        max = 1
    )
    public static void mask(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        if (args.argsLength() == 0) {
            session.getBrushTool(player.getItemInHand()).setMask(null);
            player.print("Brush mask disabled.");
        } else {
            Mask mask = we.getBlockMask(player, args.getString(0));
            session.getBrushTool(player.getItemInHand()).setMask(mask);
            player.print("Brush mask set.");
        }
    }
}
