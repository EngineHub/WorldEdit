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
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.command.tool.*;
import com.sk89q.worldedit.util.TreeGenerator;

public class ToolCommands {
    private final WorldEdit we;

    public ToolCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        aliases = { "none" },
        usage = "",
        desc = "Unbind a bound tool from your current item",
        min = 0,
        max = 0
    )
    public void none(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        session.setTool(player.getItemInHand(), null);
        player.print("Tool unbound from your current item.");
    }

    @Command(
        aliases = { "info" },
        usage = "",
        desc = "Block information tool",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.tool.info")
    public void info(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        session.setTool(player.getItemInHand(), new QueryTool());
        player.print("Info tool bound to "
                + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
        aliases = { "tree" },
        usage = "[type]",
        desc = "Tree generator tool",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.tool.tree")
    @SuppressWarnings("deprecation")
    public void tree(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

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
        aliases = { "repl" },
        usage = "<block>",
        desc = "Block replacer tool",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.tool.replacer")
    public void repl(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        BaseBlock targetBlock = we.getBlock(player, args.getString(0));
        session.setTool(player.getItemInHand(), new BlockReplacer(targetBlock));
        player.print("Block replacer tool bound to "
                + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
        aliases = { "cycler" },
        usage = "",
        desc = "Block data cycler tool",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.tool.data-cycler")
    public void cycler(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        session.setTool(player.getItemInHand(), new BlockDataCyler());
        player.print("Block data cycler tool bound to "
                + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
        aliases = { "floodfill", "flood" },
        usage = "<pattern> <range>",
        desc = "Flood fill tool",
        min = 2,
        max = 2
    )
    @CommandPermissions("worldedit.tool.flood-fill")
    public void floodFill(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();
        int range = args.getInteger(1);

        if (range > config.maxSuperPickaxeSize) {
            player.printError("Maximum range: " + config.maxSuperPickaxeSize);
            return;
        }

        Pattern pattern = we.getBlockPattern(player, args.getString(0));
        session.setTool(player.getItemInHand(), new FloodFillTool(range, pattern));
        player.print("Block flood fill tool bound to "
                + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
        aliases = { "brush", "br" },
        desc = "Brush tool"
    )
    @NestedCommand(BrushCommands.class)
    public void brush(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
    }

    @Command(
            aliases = { "deltree" },
            usage = "",
            desc = "Floating tree remover tool",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.tool.deltree")
    public void deltree(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

    session.setTool(player.getItemInHand(), new FloatingTreeRemover());
    player.print("Floating tree remover tool bound to "
            + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
            aliases = { "farwand" },
            usage = "",
            desc = "Wand at a distance tool",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.tool.farwand")
    public void farwand(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        session.setTool(player.getItemInHand(), new DistanceWand());
        player.print("Far wand tool bound to " + ItemType.toHeldName(player.getItemInHand()) + ".");
    }

    @Command(
            aliases = { "lrbuild", "/lrbuild" },
            usage = "<leftclick block> <rightclick block>",
            desc = "Long-range building tool",
            min = 2,
            max = 2
    )
    @CommandPermissions("worldedit.tool.lrbuild")
    public void longrangebuildtool(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        BaseBlock secondary = we.getBlock(player, args.getString(0));
        BaseBlock primary = we.getBlock(player, args.getString(1));
        session.setTool(player.getItemInHand(), new LongRangeBuildTool(primary, secondary));
        player.print("Long-range building tool bound to " + ItemType.toHeldName(player.getItemInHand()) + ".");
        player.print("Left-click set to " + ItemType.toName(secondary.getType()) + "; right-click set to "
                + ItemType.toName(primary.getType()) + ".");
    }
}
