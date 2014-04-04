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
import com.sk89q.minecraft.util.commands.Console;
import com.sk89q.minecraft.util.commands.NestedCommand;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.masks.Mask;

/**
 * General WorldEdit commands.
 * 
 * @author sk89q
 */
public class GeneralCommands {
    private final WorldEdit we;

    public GeneralCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
        aliases = { "/limit" },
        usage = "<limit>",
        desc = "Modify block change limit",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.limit")
    public void limit(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        int limit = Math.max(-1, args.getInteger(0));
        if (!player.hasPermission("worldedit.limit.unrestricted")
                && config.maxChangeLimit > -1) {
            if (limit > config.maxChangeLimit) {
                player.printError("Your maximum allowable limit is "
                        + config.maxChangeLimit + ".");
                return;
            }
        }

        session.setBlockChangeLimit(limit);
        player.print("Block change limit set to " + limit + ".");
    }

    @Command(
        aliases = { "/fast" },
        usage = "[on|off]",
        desc = "Toggle fast mode",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.fast")
    public void fast(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        String newState = args.getString(0, null);
        if (session.hasFastMode()) {
            if ("on".equals(newState)) {
                player.printError("Fast mode already enabled.");
                return;
            }

            session.setFastMode(false);
            player.print("Fast mode disabled.");
        } else {
            if ("off".equals(newState)) {
                player.printError("Fast mode already disabled.");
                return;
            }

            session.setFastMode(true);
            player.print("Fast mode enabled. Lighting in the affected chunks may be wrong and/or you may need to rejoin to see changes.");
        }
    }

    @Command(
        aliases = { "/gmask", "gmask" },
        usage = "[mask]",
        desc = "Set the global mask",
        min = 0,
        max = -1
    )
    @CommandPermissions("worldedit.global-mask")
    public void gmask(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        if (args.argsLength() == 0) {
            session.setMask(null);
            player.print("Global mask disabled.");
        } else {
            Mask mask = we.getBlockMask(player, session, args.getJoinedStrings(0));
            session.setMask(mask);
            player.print("Global mask set.");
        }
    }

    @Command(
        aliases = { "/toggleplace", "toggleplace" },
        usage = "",
        desc = "Switch between your position and pos1 for placement",
        min = 0,
        max = 0
    )
    public void togglePlace(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        if (session.togglePlacementPosition()) {
            player.print("Now placing at pos #1.");
        } else {
            player.print("Now placing at the block you stand in.");
        }
    }

    @Command(
        aliases = { "/searchitem", "/l", "/search", "searchitem" },
        usage = "<query>",
        flags = "bi",
        desc = "Search for an item",
        help =
            "Searches for an item.\n" +
            "Flags:\n" +
            "  -b only search for blocks\n" +
            "  -i only search for items",
        min = 1,
        max = 1
    )
    @Console
    public void searchItem(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        
        String query = args.getString(0).trim().toLowerCase();
        boolean blocksOnly = args.hasFlag('b');
        boolean itemsOnly = args.hasFlag('i');

        try {
            int id = Integer.parseInt(query);

            ItemType type = ItemType.fromID(id);

            if (type != null) {
                player.print("#" + type.getID() + " (" + type.getName() + ")");
            } else {
                player.printError("No item found by ID " + id);
            }

            return;
        } catch (NumberFormatException e) {
        }

        if (query.length() <= 2) {
            player.printError("Enter a longer search string (len > 2).");
            return;
        }

        if (!blocksOnly && !itemsOnly) {
            player.print("Searching for: " + query);
        } else if (blocksOnly && itemsOnly) {
            player.printError("You cannot use both the 'b' and 'i' flags simultaneously.");
            return;
        } else if (blocksOnly) {
            player.print("Searching for blocks: " + query);
        } else {
            player.print("Searching for items: " + query);
        }

        int found = 0;

        for (ItemType type : ItemType.values()) {
            if (found >= 15) {
                player.print("Too many results!");
                break;
            }

            if (blocksOnly && type.getID() > 255) {
                continue;
            }

            if (itemsOnly && type.getID() <= 255) {
                continue;
            }

            for (String alias : type.getAliases()) {
                if (alias.contains(query)) {
                    player.print("#" + type.getID() + " (" + type.getName() + ")");
                    ++found;
                    break;
                }
            }
        }

        if (found == 0) {
            player.printError("No items found.");
        }
    }

    @Command(
        aliases = { "we", "worldedit" },
        desc = "WorldEdit commands"
    )
    @NestedCommand(WorldEditCommands.class)
    @Console
    public void we(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
    }
}
