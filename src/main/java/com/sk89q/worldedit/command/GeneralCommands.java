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
import com.sk89q.worldedit.blocks.ItemType;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.OperationFuture;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.util.command.parametric.Optional;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * General WorldEdit commands.
 */
public class GeneralCommands {

    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     */
    public GeneralCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @Command(
        aliases = { "/limit" },
        usage = "<limit>",
        desc = "Modify block change limit",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.limit")
    public void limit(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {
        
        LocalConfiguration config = worldEdit.getConfiguration();
        boolean mayDisable = player.hasPermission("worldedit.limit.unrestricted");

        int limit = Math.max(-1, args.getInteger(0));
        if (!mayDisable && config.maxChangeLimit > -1) {
            if (limit > config.maxChangeLimit) {
                player.printError("Your maximum allowable limit is " + config.maxChangeLimit + ".");
                return;
            }
        }

        session.setBlockChangeLimit(limit);

        if (limit != -1) {
            player.print("Block change limit set to " + limit + ". (Use //limit -1 to go back to the default.)");
        } else {
            player.print("Block change limit set to " + limit + ".");
        }
    }

    @Command(
        aliases = { "/fast" },
        usage = "[on|off]",
        desc = "Toggle fast mode",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.fast")
    public void fast(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

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
    public void gmask(Player player, LocalSession session, EditSession editSession, @Optional Mask mask) throws WorldEditException {
        if (mask == null) {
            session.setMask((Mask) null);
            player.print("Global mask disabled.");
        } else {
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
    public void togglePlace(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

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
    public void searchItem(Actor actor, CommandContext args) throws WorldEditException {

        String query = args.getString(0).trim().toLowerCase();
        boolean blocksOnly = args.hasFlag('b');
        boolean itemsOnly = args.hasFlag('i');

        try {
            int id = Integer.parseInt(query);

            ItemType type = ItemType.fromID(id);

            if (type != null) {
                actor.print("#" + type.getID() + " (" + type.getName() + ")");
            } else {
                actor.printError("No item found by ID " + id);
            }

            return;
        } catch (NumberFormatException e) {
        }

        if (query.length() <= 2) {
            actor.printError("Enter a longer search string (len > 2).");
            return;
        }

        if (!blocksOnly && !itemsOnly) {
            actor.print("Searching for: " + query);
        } else if (blocksOnly && itemsOnly) {
            actor.printError("You cannot use both the 'b' and 'i' flags simultaneously.");
            return;
        } else if (blocksOnly) {
            actor.print("Searching for blocks: " + query);
        } else {
            actor.print("Searching for items: " + query);
        }

        int found = 0;

        for (ItemType type : ItemType.values()) {
            if (found >= 15) {
                actor.print("Too many results!");
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
                    actor.print("#" + type.getID() + " (" + type.getName() + ")");
                    ++found;
                    break;
                }
            }
        }

        if (found == 0) {
            actor.printError("No items found.");
        }
    }

    @Command(
            aliases = { "/abort", "abort" },
            usage = "[id]",
            desc = "Aborts a long-running action",
            min = 0,
            max = 1
    )
    @CommandPermissions("worldedit.queue.abort")
    public void abortOperation(Actor player, CommandContext args) {
        List<OperationFuture> queue = Operations.getQueueSnapshot();
        // if only one operation, don't require the id
        if (queue.size() == 1) {
            queue.get(0).cancel(true);
            return;
        }
        // otherwise, if no id given, show the list
        if (args.argsLength() == 0) {
            viewOperationQueue(player);
            return;
        }
        int hashEnd = args.getInteger(0);
        for (OperationFuture future : queue) {
            int hash = System.identityHashCode(future);
            if (hashEnd == hash % 100) {
                future.cancel(true);
            }
        }
    }

    @Command(
            aliases = { "/queue", "queue" },
            usage = "",
            desc = "View the long-running queue",
            min = 0,
            max = 0
    )
    @CommandPermissions("worldedit.queue.view")
    public void viewOperationQueue(Actor player) {
        List<OperationFuture> queue = Operations.getQueueSnapshot();
        if (queue.size() == 0) {
            player.print("No long-running tasks.");
            return;
        }

        for (OperationFuture future : queue) {
            int hash = System.identityHashCode(future);
            long duration = System.currentTimeMillis() - future.getStartTime();
            player.print(String.format("[%02d] Running for %.1f seconds", hash % 100, duration / 1000.0));
        }

        player.print("Use '//abort id' to cancel an operation.");
    }

}
