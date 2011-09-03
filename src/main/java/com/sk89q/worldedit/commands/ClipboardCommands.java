// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

import java.io.File;
import java.io.IOException;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.Logging;
import static com.sk89q.minecraft.util.commands.Logging.LogMode.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.regions.Region;

/**
 * Clipboard commands.
 * 
 * @author sk89q
 */
public class ClipboardCommands {
    @Command(
        aliases = {"/copy"},
        usage = "",
        desc = "Copy the selection to the clipboard",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.clipboard.copy"})
    public static void copy(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
            
        Region region = session.getSelection(player.getWorld());
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();
        Vector pos = session.getPlacementPosition(player);

        CuboidClipboard clipboard = new CuboidClipboard(
                max.subtract(min).add(new Vector(1, 1, 1)),
                min, min.subtract(pos));
        clipboard.copy(editSession);
        session.setClipboard(clipboard);

        player.print("Block(s) copied.");
    }

    @Command(
        aliases = {"/cut"},
        usage = "[leave-id]",
        desc = "Cut the selection to the clipboard",
        min = 0,
        max = 1
    )
    @CommandPermissions({"worldedit.clipboard.cut"})
    @Logging(REGION)
    public static void cut(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        BaseBlock block = new BaseBlock(BlockID.AIR);

        if (args.argsLength() > 0) {
            block = we.getBlock(player, args.getString(0));
        }
            
        Region region = session.getSelection(player.getWorld());
        Vector min = region.getMinimumPoint();
        Vector max = region.getMaximumPoint();
        Vector pos = session.getPlacementPosition(player);

        CuboidClipboard clipboard = new CuboidClipboard(
                max.subtract(min).add(new Vector(1, 1, 1)),
                min, min.subtract(pos));
        clipboard.copy(editSession);
        session.setClipboard(clipboard);

        editSession.setBlocks(session.getSelection(player.getWorld()), block);
        player.print("Block(s) cut.");
    }
    
    @Command(
        aliases = {"/paste"},
        usage = "",
        flags = "ao",
        desc = "Paste the clipboard's contents",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.clipboard.paste"})
    @Logging(PLACEMENT)
    public static void paste(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        boolean atOrigin = args.hasFlag('o');
        boolean pasteNoAir = args.hasFlag('a');
                
        if (atOrigin) {
            Vector pos = session.getClipboard().getOrigin();
            session.getClipboard().place(editSession, pos, pasteNoAir);
            player.findFreePosition();
            player.print("Pasted to copy origin. Undo with //undo");
        } else {
            Vector pos = session.getPlacementPosition(player);
            session.getClipboard().paste(editSession, pos, pasteNoAir);
            player.findFreePosition();
            player.print("Pasted relative to you. Undo with //undo");
        }
    }

    @Command(
        aliases = {"/rotate"},
        usage = "<angle-in-degrees>",
        desc = "Rotate the contents of the clipboard",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.clipboard.rotate"})
    public static void rotate(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
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
        aliases = {"/flip"},
        usage = "[dir]",
        flags = "p",
        desc = "Flip the contents of the clipboard. To flip it around yourself, use the -p flag.",
        min = 0,
        max = 1
    )
    @CommandPermissions({"worldedit.clipboard.flip"})
    public static void flip(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        CuboidClipboard.FlipDirection dir = we.getFlipDirection(player,
                args.argsLength() > 0 ? args.getString(0).toLowerCase() : "me");

        CuboidClipboard clipboard = session.getClipboard();
        clipboard.flip(dir, args.hasFlag('p'));
        player.print("Clipboard flipped.");
    }
    
    @Command(
        aliases = {"/load"},
        usage = "<filename>",
        desc = "Load a schematic into your clipboard",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.clipboard.load"})
    public static void load(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        String filename = args.getString(0);
        File dir = we.getWorkingDirectoryFile(config.saveDir);
        File f = we.getSafeOpenFile(player, dir, filename, "schematic",
                new String[] {"schematic"});

        try {
            String filePath = f.getCanonicalPath();
            String dirPath = dir.getCanonicalPath();

            if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                player.printError("Schematic could not read or it does not exist.");
            } else {
                session.setClipboard(CuboidClipboard.loadSchematic(f));
                WorldEdit.logger.info(player.getName() + " loaded " + filePath);
                player.print(filename + " loaded. Paste it with //paste");
            }
        } catch (DataException e) {
            player.printError("Load error: " + e.getMessage());
        } catch (IOException e) {
            player.printError("Schematic could not read or it does not exist: " + e.getMessage());
        }
    }
    
    @Command(
        aliases = {"/save"},
        usage = "<filename>",
        desc = "Save a schematic into your clipboard",
        min = 1,
        max = 1
    )
    @CommandPermissions({"worldedit.clipboard.save"})
    public static void save(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {
        
        LocalConfiguration config = we.getConfiguration();

        String filename = args.getString(0);

        File dir = we.getWorkingDirectoryFile(config.saveDir);
        File f = we.getSafeSaveFile(player, dir, filename, "schematic",
                new String[] {"schematic"});

        if (!dir.exists()) {
            if (!dir.mkdir()) {
                player.printError("The storage folder could not be created.");
                return;
            }
        }

        try {
            // Create parent directories
            File parent = f.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            session.getClipboard().saveSchematic(f);
            WorldEdit.logger.info(player.getName() + " saved " + f.getCanonicalPath());
            player.print(filename + " saved.");
        } catch (DataException se) {
            player.printError("Save error: " + se.getMessage());
        } catch (IOException e) {
            player.printError("Schematic could not written: " + e.getMessage());
        }
    }
    
    @Command(
        aliases = {"clearclipboard"},
        usage = "",
        desc = "Clear your clipboard",
        min = 0,
        max = 0
    )
    @CommandPermissions({"worldedit.clipboard.clear"})
    public static void clearClipboard(CommandContext args, WorldEdit we,
            LocalSession session, LocalPlayer player, EditSession editSession)
            throws WorldEditException {

        session.setClipboard(null);
        player.print("Clipboard cleared.");
    }
}
