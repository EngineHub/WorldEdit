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

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.Sets;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.DisallowedUsageException;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.util.command.parametric.Optional;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;

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
        usage = "[limit]",
        desc = "Modify block change limit",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.limit")
    public void limit(Player player, LocalSession session, CommandContext args) throws WorldEditException {
        
        LocalConfiguration config = worldEdit.getConfiguration();
        boolean mayDisable = player.hasPermission("worldedit.limit.unrestricted");

        int limit = args.argsLength() == 0 ? config.defaultChangeLimit : Math.max(-1, args.getInteger(0));
        if (!mayDisable && config.maxChangeLimit > -1) {
            if (limit > config.maxChangeLimit) {
                player.printError("Your maximum allowable limit is " + config.maxChangeLimit + ".");
                return;
            }
        }

        session.setBlockChangeLimit(limit);

        if (limit != config.defaultChangeLimit) {
            player.print("Block change limit set to " + limit + ". (Use //limit to go back to the default.)");
        } else {
            player.print("Block change limit set to " + limit + ".");
        }
    }

    @Command(
            aliases = { "/timeout" },
            usage = "[time]",
            desc = "Modify evaluation timeout time.",
            min = 0,
            max = 1
    )
    @CommandPermissions("worldedit.timeout")
    public void timeout(Player player, LocalSession session, CommandContext args) throws WorldEditException {

        LocalConfiguration config = worldEdit.getConfiguration();
        boolean mayDisable = player.hasPermission("worldedit.timeout.unrestricted");

        int limit = args.argsLength() == 0 ? config.calculationTimeout : Math.max(-1, args.getInteger(0));
        if (!mayDisable && config.maxCalculationTimeout > -1) {
            if (limit > config.maxCalculationTimeout) {
                player.printError("Your maximum allowable timeout is " + config.maxCalculationTimeout + " ms.");
                return;
            }
        }

        session.setTimeout(limit);

        if (limit != config.calculationTimeout) {
            player.print("Timeout time set to " + limit + " ms. (Use //timeout to go back to the default.)");
        } else {
            player.print("Timeout time set to " + limit + " ms.");
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
    public void fast(Player player, LocalSession session, CommandContext args) throws WorldEditException {

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
            aliases = { "/reorder" },
            usage = "[multi|fast|none]",
            desc = "Sets the reorder mode of WorldEdit",
            min = 0,
            max = 1
    )
    @CommandPermissions("worldedit.reorder")
    public void reorderMode(Player player, LocalSession session, CommandContext args) throws WorldEditException {
        String newState = args.getString(0, null);
        if (newState == null) {
            player.print("The reorder mode is " + session.getReorderMode().getDisplayName());
        } else {
            java.util.Optional<EditSession.ReorderMode> reorderModeOptional = EditSession.ReorderMode.getFromDisplayName(newState);
            if (!reorderModeOptional.isPresent()) {
                player.printError("Unknown reorder mode!");
                return;
            }

            EditSession.ReorderMode reorderMode = reorderModeOptional.get();
            session.setReorderMode(reorderMode);
            player.print("The reorder mode is now " + session.getReorderMode().getDisplayName());
        }
    }

    @Command(
            aliases = { "/drawsel" },
            usage = "[on|off]",
            desc = "Toggle drawing the current selection",
            min = 0,
            max = 1
    )
    @CommandPermissions("worldedit.drawsel")
    public void drawSelection(Player player, LocalSession session, CommandContext args) throws WorldEditException {

        if (!WorldEdit.getInstance().getConfiguration().serverSideCUI) {
            throw new DisallowedUsageException("This functionality is disabled in the configuration!");
        }
        String newState = args.getString(0, null);
        if (session.shouldUseServerCUI()) {
            if ("on".equals(newState)) {
                player.printError("Server CUI already enabled.");
                return;
            }

            session.setUseServerCUI(false);
            session.updateServerCUI(player);
            player.print("Server CUI disabled.");
        } else {
            if ("off".equals(newState)) {
                player.printError("Server CUI already disabled.");
                return;
            }

            session.setUseServerCUI(true);
            session.updateServerCUI(player);
            player.print("Server CUI enabled. This only supports cuboid regions, with a maximum size of 32x32x32.");
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
    public void gmask(Player player, LocalSession session, @Optional Mask mask) throws WorldEditException {
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
    public void togglePlace(Player player, LocalSession session) throws WorldEditException {

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

        ItemType type = ItemTypes.get(query);

        if (type != null) {
            actor.print(type.getId() + " (" + type.getName() + ")");
        } else {
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

            for (ItemType searchType : ItemType.REGISTRY) {
                if (found >= 15) {
                    actor.print("Too many results!");
                    break;
                }

                if (blocksOnly && !searchType.hasBlockType()) {
                    continue;
                }

                if (itemsOnly && searchType.hasBlockType()) {
                    continue;
                }

                for (String alias : Sets.newHashSet(searchType.getId(), searchType.getName())) {
                    if (alias.contains(query)) {
                        actor.print(searchType.getId() + " (" + searchType.getName() + ")");
                        ++found;
                        break;
                    }
                }
            }

            if (found == 0) {
                actor.printError("No items found.");
            }
        }
    }

}
