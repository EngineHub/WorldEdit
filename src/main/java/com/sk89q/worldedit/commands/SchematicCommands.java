/*
 * WorldEdit
 * Copyright (C) 2012 sk89q <http://www.sk89q.com> and contributors
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

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

import java.io.File;
import java.io.IOException;

/**
 * Commands related to schematics
 *
 * @see com.sk89q.worldedit.commands.ClipboardCommands#schematic()
 */
public class SchematicCommands {
    private final WorldEdit we;

    public SchematicCommands(WorldEdit we) {
        this.we = we;
    }

    @Command(
            aliases = { "load" },
            usage = "<format> <filename>",
            desc = "Load a schematic into your clipboard",
            min = 2,
            max = 2
    )
    @CommandPermissions("worldedit.clipboard.load")
    public void load(CommandContext args, LocalSession session, LocalPlayer player,
                     EditSession editSession) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();
        SchematicFormat format = SchematicFormat.getFormat(args.getString(0));
        if (format == null) {
            player.printError("Unknown schematic format: " + args.getString(0));
            return;
        }

        String filename = args.getString(1);
        File dir = we.getWorkingDirectoryFile(config.saveDir);
        File f = we.getSafeOpenFile(player, dir, filename, "schematic", "schematic");

        try {
            String filePath = f.getCanonicalPath();
            String dirPath = dir.getCanonicalPath();

            if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                player.printError("Schematic could not read or it does not exist.");
            } else {
                session.setClipboard(format.load(f));
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
            aliases = { "save" },
            usage = "<format> <filename>",
            desc = "Save a schematic into your clipboard",
            min = 2,
            max = 2
    )
    @CommandPermissions("worldedit.clipboard.save")
    public void save(CommandContext args, LocalSession session, LocalPlayer player,
                     EditSession editSession) throws WorldEditException {

        LocalConfiguration config = we.getConfiguration();
        SchematicFormat format = SchematicFormat.getFormat(args.getString(0));
        if (format == null) {
            player.printError("Unknown schematic format: " + args.getString(0));
            return;
        }

        String filename = args.getString(1);

        File dir = we.getWorkingDirectoryFile(config.saveDir);
        File f = we.getSafeSaveFile(player, dir, filename, "schematic", "schematic");

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

            format.save(session.getClipboard(), f);
            WorldEdit.logger.info(player.getName() + " saved " + f.getCanonicalPath());
            player.print(filename + " saved.");
        } catch (DataException se) {
            player.printError("Save error: " + se.getMessage());
        } catch (IOException e) {
            player.printError("Schematic could not written: " + e.getMessage());
        }
    }

    @Command(
            aliases = {"formats", "listformats"},
            desc = "List available schematic formats",
            max = 0
    )
    public void formats(CommandContext args, LocalSession session, LocalPlayer player,
                     EditSession editSession) throws WorldEditException {
        player.print("Available schematic formats (Name: Lookup names)");
        StringBuilder builder;
        boolean first = true;
        for (SchematicFormat format : SchematicFormat.getFormats()) {
            builder = new StringBuilder();
            builder.append(format.getName()).append(": ");
            for (String lookupName : format.getLookupNames()) {
                if (!first) {
                    builder.append(", ");
                }
                builder.append(lookupName);
                first = false;
            }
            first = true;
            player.print(builder.toString());
        }
    }
}
