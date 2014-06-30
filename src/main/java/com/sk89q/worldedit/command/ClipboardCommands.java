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
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.annotation.Selection;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.util.command.binding.Switch;
import com.sk89q.worldedit.util.command.parametric.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.PLACEMENT;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.REGION;

/**
 * Clipboard commands.
 */
public class ClipboardCommands {

    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     */
    public ClipboardCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @Command(
        aliases = { "/copy" },
        flags = "e",
        desc = "Copy the selection to the clipboard",
        help = "Copy the selection to the clipboard\n" +
                "Flags:\n" +
                "  -e controls whether entities are copied\n" +
                "WARNING: Pasting entities cannot yet be undone!",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.clipboard.copy")
    public void copy(Player player, LocalSession session, EditSession editSession,
                     @Selection Region region, @Switch('e') boolean copyEntities) throws WorldEditException {

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOffset(region.getMinimumPoint().subtract(session.getPlacementPosition(player)));
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        Operations.completeLegacy(copy);
        session.setClipboard(clipboard);

        player.print(region.getArea() + " block(s) were copied.");
    }

    @Command(
        aliases = { "/cut" },
        usage = "[leave-id]",
        desc = "Cut the selection to the clipboard",
        help = "Copy the selection to the clipboard\n" +
                "Flags:\n" +
                "  -e controls whether entities are copied\n" +
                "WARNING: Cutting and pasting entities cannot yet be undone!",
        flags = "e",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.clipboard.cut")
    @Logging(REGION)
    public void cut(Player player, LocalSession session, EditSession editSession,
                    @Selection Region region, @Optional("air") Pattern leavePattern, @Switch('e') boolean copyEntities) throws WorldEditException {

        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
        clipboard.setOffset(region.getMinimumPoint().subtract(session.getPlacementPosition(player)));
        ForwardExtentCopy copy = new ForwardExtentCopy(editSession, region, clipboard, region.getMinimumPoint());
        copy.setSourceFunction(new BlockReplace(editSession, leavePattern));
        Operations.completeLegacy(copy);
        session.setClipboard(clipboard);

        player.print(region.getArea() + " block(s) were copied.");
    }

    @Command(
        aliases = { "/paste" },
        usage = "",
        flags = "sao",
        desc = "Paste the clipboard's contents",
        help =
            "Pastes the clipboard's contents.\n" +
            "Flags:\n" +
            "  -a skips air blocks\n" +
            "  -o pastes at the original position\n" +
            "  -s selects the region after pasting",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.clipboard.paste")
    @Logging(PLACEMENT)
    public void paste(Player player, LocalSession session, EditSession editSession,
                      @Switch('a') boolean ignoreAirBlocks, @Switch('o') boolean atOrigin,
                      @Switch('s') boolean selectPasted) throws WorldEditException {

        Clipboard clipboard = session.getClipboard();
        Vector to = atOrigin ? clipboard.getRegion().getMinimumPoint(): session.getPlacementPosition(player).add(clipboard.getOffset());
        ForwardExtentCopy copy = new ForwardExtentCopy(clipboard, clipboard.getRegion(), editSession, to);
        if (ignoreAirBlocks) {
            copy.setSourceMask(new ExistingBlockMask(clipboard));
        }
        Operations.completeLegacy(copy);

        if (selectPasted) {
            Region region = clipboard.getRegion();
            Vector max = to.add(region.getMaximumPoint().subtract(region.getMinimumPoint()));
            RegionSelector selector = new CuboidRegionSelector(player.getWorld(), to, max);
            session.setRegionSelector(player.getWorld(), selector);
            selector.learnChanges();
            selector.explainRegionAdjust(player, session);
        }

        player.print("The clipboard has been pasted at " + to.add(clipboard.getRegion().getMinimumPoint()));
    }

    @Command(
        aliases = { "/rotate" },
        usage = "<angle-in-degrees>",
        desc = "Rotate the contents of the clipboard",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.clipboard.rotate")
    public void rotate(Player player, LocalSession session, EditSession editSession, CommandContext args) throws CommandException {
        // TODO: Update for new clipboard
        throw new CommandException("Needs to be re-written again");
    }

    @Command(
        aliases = { "/flip" },
        usage = "[dir]",
        flags = "p",
        desc = "Flip the contents of the clipboard.",
        help =
            "Flips the contents of the clipboard.\n" +
            "The -p flag flips the selection around the player,\n" +
            "instead of the selections center.",
        min = 0,
        max = 1
    )
    @CommandPermissions("worldedit.clipboard.flip")
    public void flip(Player player, LocalSession session, EditSession editSession, CommandContext args) throws CommandException {
        // TODO: Update for new clipboard
        throw new CommandException("Needs to be re-written again");
    }

    @Command(
        aliases = { "/load" },
        usage = "<filename>",
        desc = "Load a schematic into your clipboard",
        min = 0,
        max = 1
    )
    @Deprecated
    @CommandPermissions("worldedit.clipboard.load")
    public void load(Actor actor) {
        actor.printError("This command is no longer used. See //schematic load.");
    }

    @Command(
        aliases = { "/save" },
        usage = "<filename>",
        desc = "Save a schematic into your clipboard",
        min = 0,
        max = 1
    )
    @Deprecated
    @CommandPermissions("worldedit.clipboard.save")
    public void save(Actor actor) {
        actor.printError("This command is no longer used. See //schematic save.");
    }

    @Command(
        aliases = { "clearclipboard" },
        usage = "",
        desc = "Clear your clipboard",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.clipboard.clear")
    public void clearClipboard(Player player, LocalSession session, EditSession editSession) throws WorldEditException {
        session.setClipboard(null);
        player.print("Clipboard cleared.");
    }
}
