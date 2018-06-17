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
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.command.parametric.Optional;

/**
 * Tool commands.
 */
public class ToolUtilCommands {
    private final WorldEdit we;

    public ToolUtilCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        aliases = { "/", "," },
        usage = "[on|off]",
        desc = "Toggle the super pickaxe function",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.superpickaxe")
    public void togglePickaxe(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

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
        aliases = { "mask" },
        usage = "[mask]",
        desc = "Set the brush mask",
        min = 0,
        max = -1
    )
    @CommandPermissions("worldedit.brush.options.mask")
    public void mask(Player player, LocalSession session, EditSession editSession, @Optional Mask mask) throws WorldEditException {
        if (mask == null) {
            session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType()).setMask(null);
            player.print("Brush mask disabled.");
        } else {
            session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType()).setMask(mask);
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
    public void material(Player player, LocalSession session, EditSession editSession, Pattern pattern) throws WorldEditException {
        session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType()).setFill(pattern);
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
    public void range(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        int range = args.getInteger(0);
        session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType()).setRange(range);
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
    public void size(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        int radius = args.getInteger(0);
        we.checkMaxBrushRadius(radius);

        session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType()).setSize(radius);
        player.print("Brush size set.");
    }
}
