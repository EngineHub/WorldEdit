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
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.command.tool.BlockDataCyler;
import com.sk89q.worldedit.command.tool.BlockReplacer;
import com.sk89q.worldedit.command.tool.DistanceWand;
import com.sk89q.worldedit.command.tool.FloatingTreeRemover;
import com.sk89q.worldedit.command.tool.FloodFillTool;
import com.sk89q.worldedit.command.tool.LongRangeBuildTool;
import com.sk89q.worldedit.command.tool.QueryTool;
import com.sk89q.worldedit.command.tool.TreePlanter;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.block.BlockStateHolder;

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
    public void none(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        session.setTool(player.getItemInHand(HandSide.MAIN_HAND).getType(), null);
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
    public void info(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        BaseItemStack itemStack = player.getItemInHand(HandSide.MAIN_HAND);
        session.setTool(itemStack.getType(), new QueryTool());
        player.print("Info tool bound to "
                + itemStack.getType().getName() + ".");
    }

    @Command(
        aliases = { "tree" },
        usage = "[type]",
        desc = "Tree generator tool",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.tool.tree")
    public void tree(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        TreeGenerator.TreeType type = args.argsLength() > 0
                ? TreeGenerator.lookup(args.getString(0))
                : TreeGenerator.TreeType.TREE;

        if (type == null) {
            player.printError("Tree type '" + args.getString(0) + "' is unknown.");
            return;
        }

        BaseItemStack itemStack = player.getItemInHand(HandSide.MAIN_HAND);
        session.setTool(itemStack.getType(), new TreePlanter(type));
        player.print("Tree tool bound to " + itemStack.getType().getName() + ".");
    }

    @Command(
        aliases = { "repl" },
        usage = "<block>",
        desc = "Block replacer tool",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.tool.replacer")
    public void repl(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        ParserContext context = new ParserContext();
        context.setActor(player);
        context.setWorld(player.getWorld());
        context.setSession(session);
        context.setRestricted(true);
        context.setPreferringWildcard(false);

        BlockStateHolder targetBlock = we.getBlockFactory().parseFromInput(args.getString(0), context);
        BaseItemStack itemStack = player.getItemInHand(HandSide.MAIN_HAND);
        session.setTool(itemStack.getType(), new BlockReplacer(targetBlock));
        player.print("Block replacer tool bound to " + itemStack.getType().getName() + ".");
    }

    @Command(
        aliases = { "cycler" },
        usage = "",
        desc = "Block data cycler tool",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.tool.data-cycler")
    public void cycler(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        BaseItemStack itemStack = player.getItemInHand(HandSide.MAIN_HAND);
        session.setTool(itemStack.getType(), new BlockDataCyler());
        player.print("Block data cycler tool bound to " + itemStack.getType().getName() + ".");
    }

    @Command(
        aliases = { "floodfill", "flood" },
        usage = "<pattern> <range>",
        desc = "Flood fill tool",
        min = 2,
        max = 2
    )
    @CommandPermissions("worldedit.tool.flood-fill")
    public void floodFill(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();
        int range = args.getInteger(1);

        if (range > config.maxSuperPickaxeSize) {
            player.printError("Maximum range: " + config.maxSuperPickaxeSize);
            return;
        }

        ParserContext context = new ParserContext();
        context.setActor(player);
        context.setWorld(player.getWorld());
        context.setSession(session);
        Pattern pattern = we.getPatternFactory().parseFromInput(args.getString(0), context);

        BaseItemStack itemStack = player.getItemInHand(HandSide.MAIN_HAND);
        session.setTool(itemStack.getType(), new FloodFillTool(range, pattern));
        player.print("Block flood fill tool bound to " + itemStack.getType().getName() + ".");
    }

    @Command(
            aliases = { "deltree" },
            usage = "",
            desc = "Floating tree remover tool",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.tool.deltree")
    public void deltree(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        BaseItemStack itemStack = player.getItemInHand(HandSide.MAIN_HAND);
        session.setTool(itemStack.getType(), new FloatingTreeRemover());
        player.print("Floating tree remover tool bound to "
                + itemStack.getType().getName() + ".");
    }

    @Command(
            aliases = { "farwand" },
            usage = "",
            desc = "Wand at a distance tool",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.tool.farwand")
    public void farwand(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        BaseItemStack itemStack = player.getItemInHand(HandSide.MAIN_HAND);
        session.setTool(itemStack.getType(), new DistanceWand());
        player.print("Far wand tool bound to " + itemStack.getType().getName() + ".");
    }

    @Command(
            aliases = { "lrbuild", "/lrbuild" },
            usage = "<leftclick block> <rightclick block>",
            desc = "Long-range building tool",
            min = 2,
            max = 2
    )
    @CommandPermissions("worldedit.tool.lrbuild")
    public void longrangebuildtool(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        ParserContext context = new ParserContext();
        context.setActor(player);
        context.setWorld(player.getWorld());
        context.setSession(session);
        context.setRestricted(true);
        context.setPreferringWildcard(false);

        BlockStateHolder secondary = we.getBlockFactory().parseFromInput(args.getString(0), context);
        BlockStateHolder primary = we.getBlockFactory().parseFromInput(args.getString(1), context);

        BaseItemStack itemStack = player.getItemInHand(HandSide.MAIN_HAND);

        session.setTool(itemStack.getType(), new LongRangeBuildTool(primary, secondary));
        player.print("Long-range building tool bound to " + itemStack.getType().getName() + ".");
        player.print("Left-click set to " + secondary.getBlockType().getName() + "; right-click set to "
                + primary.getBlockType().getName() + ".");
    }
}
