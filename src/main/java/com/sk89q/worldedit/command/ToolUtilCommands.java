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
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.patterns.Pattern;

/**
 * Tool commands.
 * 
 * @author sk89q
 */
public class ToolUtilCommands {
    private final WorldEdit we;

    public ToolUtilCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        aliases = { "/", "," },
        usage = "[on|off]",
        desc = "Toggle the super pickaxe pickaxe function",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.superpickaxe")
    public void togglePickaxe(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        String newState = args.getString(0, null);
        if (session.hasSuperPickAxe()) {
            if ("on".equals(newState)) {
                player.printError("Super pick axe already enabled.");
                return;
            }

            session.disableSuperPickAxe();
            player.print("Super pick axe disabled.");
        } else {
            if ("off".equals(newState)) {
                player.printError("Super pick axe already disabled.");
                return;
            }
            session.enableSuperPickAxe();
            player.print("Super pick axe enabled.");
        }

    }

    @Command(
        aliases = { "superpickaxe", "pickaxe", "sp" },
        desc = "Select super pickaxe mode"
    )
    @NestedCommand(SuperPickaxeCommands.class)
    public void pickaxe(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
    }

    @Command(
        aliases = {"tool"},
        desc = "Select a tool to bind"
    )
    @NestedCommand(ToolCommands.class)
    public void tool(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
    }

    @Command(
        aliases = { "mask" },
        usage = "[mask]",
        desc = "Set the brush mask",
        min = 0,
        max = -1
    )
    @CommandPermissions("worldedit.brush.options.mask")
    public void mask(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        if (args.argsLength() == 0) {
            session.getBrushTool(player.getItemInHand()).setMask(null);
            player.print("Brush mask disabled.");
        } else {
            Mask mask = we.getBlockMask(player, session, args.getJoinedStrings(0));
            session.getBrushTool(player.getItemInHand()).setMask(mask);
            player.print("Brush mask set.");
        }
    }

    @Command(
        aliases = { "mat", "material" },
        usage = "[pattern]",
        desc = "Set the brush material",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.brush.options.material")
    public void material(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        Pattern pattern = we.getBlockPattern(player, args.getString(0));
        session.getBrushTool(player.getItemInHand()).setFill(pattern);
        player.print("Brush material set.");
    }

    @Command(
            aliases = { "range" },
            usage = "[pattern]",
            desc = "Set the brush range",
            min = 1,
            max = 1
        )
    @CommandPermissions("worldedit.brush.options.range")
    public void range(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        int range = args.getInteger(0);
        session.getBrushTool(player.getItemInHand()).setRange(range);
        player.print("Brush range set.");
    }

    @Command(
        aliases = { "size" },
        usage = "[pattern]",
        desc = "Set the brush size",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.brush.options.size")
    public void size(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        int radius = args.getInteger(0);
        we.checkMaxBrushRadius(radius);

        session.getBrushTool(player.getItemInHand()).setSize(radius);
        player.print("Brush size set.");
    }
}
