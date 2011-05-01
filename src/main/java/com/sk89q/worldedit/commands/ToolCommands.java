// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.tools.*;
import com.sk89q.worldedit.util.TreeGenerator;

public class ToolCommands {
    @Command(
        aliases = {"none"},
        usage = "",
        desc = "Turn off all superpickaxe alternate modes",
        min = 0,
        max = 0
    )
    public static void none(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        session.setTool(player.getItemInHand(), null);
        player.print("Tool unbound from your current item.");
    }

    @Command(
        aliases = {"info"},
        usage = "",
        desc = "Block information tool",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.tool.info"})
    public static void info(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        session.setTool(player.getItemInHand(), new QueryTool());
        player.print("Info tool bound to "
                + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
        aliases = {"tree"},
        usage = "[type]",
        desc = "Tree generator tool",
        min = 0,
        max = 1
    )
    @CommandPermissions({"worldedit.tool.tree"})
    public static void tree(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        TreeGenerator.TreeType type = args.argsLength() > 0 ?
                type = TreeGenerator.lookup(args.getString(0))
                : TreeGenerator.TreeType.TREE;

        if (type == null) {
            player.printError("Tree type '" + args.getString(0) + "' is unknown.");
            return;
        }

        session.setTool(player.getItemInHand(), new TreePlanter(new TreeGenerator(type)));
        player.print("Tree tool bound to "
                + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
        aliases = {"repl"},
        usage = "<block>",
        desc = "Block replacer tool",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.tool.replacer"})
    public static void repl(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        BaseBlock targetBlock = we.getBlock(player, args.getString(0));
        session.setTool(player.getItemInHand(), new BlockReplacer(targetBlock));
        player.print("Block replacer tool bound to "
                + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
        aliases = {"cycler"},
        usage = "",
        desc = "Block data cycler tool",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.tool.data-cycler"})
    public static void cycler(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        session.setTool(player.getItemInHand(), new BlockDataCyler());
        player.print("Block data cycler tool bound to "
                + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
        aliases = {"brush", "br"},
        desc = "Brush tool"
    )
    @NestedCommand({BrushCommands.class})
    public static void brush(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
    }
}
