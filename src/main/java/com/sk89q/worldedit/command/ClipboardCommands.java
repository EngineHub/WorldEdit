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

import com.sk89q.minecraft.util.commands.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;
import com.sk89q.worldedit.world.World;

import static com.sk89q.minecraft.util.commands.Logging.LogMode.PLACEMENT;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.REGION;

/**
 * Clipboard commands.
 *
 * @author sk89q
 */
public class ClipboardCommands {
    private final WorldEdit we;

    public ClipboardCommands(WorldEdit we) {
        this.we = we;
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
    public void copy(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        Region region = session.getSelection(player.getWorld());
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();
        Vector pos = session.getPlacementPosition(player);

        CuboidClipboard clipboard = new CuboidClipboard(
                max.subtract(min).add(Vector.ONE),
                min, min.subtract(pos));

        if (region instanceof CuboidRegion) {
            clipboard.copy(editSession);
        } else {
            clipboard.copy(editSession, region);
        }

        if (args.hasFlag('e')) {
            for (LocalEntity entity : player.getWorld().getEntities(region)) {
                clipboard.storeEntity(entity);
            }
        }
        session.setClipboard(clipboard);

        player.print("Block(s) copied.");
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
    public void cut(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        BaseBlock block = new BaseBlock(BlockID.AIR);
        World world = player.getWorld();

        if (args.argsLength() > 0) {
            block = we.getBlock(player, args.getString(0));
        }

        Region region = session.getSelection(world);
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();
        Vector pos = session.getPlacementPosition(player);

        CuboidClipboard clipboard = new CuboidClipboard(
                max.subtract(min).add(Vector.ONE),
                min, min.subtract(pos));

        if (region instanceof CuboidRegion) {
            clipboard.copy(editSession);
        } else {
            clipboard.copy(editSession, region);
        }

        if (args.hasFlag('e')) {
            LocalEntity[] entities = world.getEntities(region);
            for (LocalEntity entity : entities) {
                clipboard.storeEntity(entity);
            }
            world.killEntities(entities);
        }
        session.setClipboard(clipboard);

        editSession.setBlocks(region, block);
        player.print("Block(s) cut.");
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
    public void paste(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        boolean atOrigin = args.hasFlag('o');
        boolean pasteNoAir = args.hasFlag('a');

        CuboidClipboard clipboard = session.getClipboard();

        Vector pos = atOrigin ? session.getClipboard().getOrigin()
                : session.getPlacementPosition(player);

        if (atOrigin) {
            clipboard.place(editSession, pos, pasteNoAir);
            clipboard.pasteEntities(pos);
            player.findFreePosition();
            player.print("Pasted to copy origin. Undo with //undo");
        } else {
            clipboard.paste(editSession, pos, pasteNoAir, true);
            player.findFreePosition();
            player.print("Pasted relative to you. Undo with //undo");
        }

        if (args.hasFlag('s')) {
            World world = player.getWorld();
            Vector pos2 = pos.add(clipboard.getSize().subtract(1, 1, 1));
            if (!atOrigin) {
                pos2 = pos2.add(clipboard.getOffset());
                pos = pos.add(clipboard.getOffset());
            }
            session.setRegionSelector(world, new CuboidRegionSelector(world, pos, pos2));
            session.getRegionSelector(world).learnChanges();
            session.getRegionSelector(world).explainRegionAdjust(player, session);
        }
    }

    @Command(
        aliases = { "/rotate" },
        usage = "<angle-in-degrees>",
        desc = "Rotate the contents of the clipboard",
        min = 1,
        max = 1
    )
    @CommandPermissions("worldedit.clipboard.rotate")
    public void rotate(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        int angle = args.getInteger(0);

        if (angle % 90 == 0) {
            CuboidClipboard clipboard = session.getClipboard();
            clipboard.rotate2D(angle);
            player.print("Clipboard rotated by " + angle + " degrees.");
        } else {
            player.printError("Angles must be divisible by 90 degrees.");
        }
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
    public void flip(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        CuboidClipboard.FlipDirection dir = we.getFlipDirection(player,
                args.argsLength() > 0 ? args.getString(0).toLowerCase() : "me");

        CuboidClipboard clipboard = session.getClipboard();
        clipboard.flip(dir, args.hasFlag('p'));
        player.print("Clipboard flipped.");
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
    public void load(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        player.printError("This command is no longer used. See //schematic load.");
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
    public void save(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {
        player.printError("This command is no longer used. See //schematic save.");
    }

    @Command(
            aliases = { "/schematic", "/schem"},
            desc = "Schematic-related commands"
    )
    @NestedCommand(SchematicCommands.class)
    public void schematic() {}

    @Command(
        aliases = { "clearclipboard" },
        usage = "",
        desc = "Clear your clipboard",
        min = 0,
        max = 0
    )
    @CommandPermissions("worldedit.clipboard.clear")
    public void clearClipboard(CommandContext args, LocalSession session, LocalPlayer player,
            EditSession editSession) throws WorldEditException {

        session.setClipboard(null);
        player.print("Clipboard cleared.");
    }
}
