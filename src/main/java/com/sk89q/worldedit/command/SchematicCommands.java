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
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.world.DataException;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Commands that work with schematic files.
 */
public class SchematicCommands {

    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit reference to WorldEdit
     */
    public SchematicCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @Command(
            aliases = { "load", "l" },
            usage = "[format] <filename>",
            desc = "Load a file into your clipboard",
            help = "Load a schematic file into your clipboard\n" +
                    "Format is a format from \"//schematic formats\"\n" +
                    "If the format is not provided, WorldEdit will\n" +
                    "attempt to automatically detect the format of the schematic",
            flags = "f",
            min = 1,
            max = 2
    )
    @CommandPermissions({"worldedit.clipboard.load", "worldedit.schematic.load"}) // TODO: Remove 'clipboard' perm
    public void load(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        LocalConfiguration config = worldEdit.getConfiguration();
        String fileName;
        String formatName;

        if (args.argsLength() == 1) {
            formatName = null;
            fileName = args.getString(0);
        } else {
            formatName = args.getString(0);
            fileName = args.getString(1);
        }
        File dir = worldEdit.getWorkingDirectoryFile(config.saveDir);
        File f = worldEdit.getSafeOpenFile(player, dir, fileName, "schematic", "schematic");

        if (!f.exists()) {
            player.printError("Schematic " + fileName + " does not exist!");
            return;
        }

        SchematicFormat format = formatName == null ? null : SchematicFormat.getFormat(formatName);
        if (format == null) {
            format = SchematicFormat.getFormat(f);
        }

        if (format == null) {
            player.printError("Unknown schematic format: " + formatName);
            return;
        }

        if (!format.isOfFormat(f) && !args.hasFlag('f')) {
            player.printError(fileName + " is not of the " + format.getName() + " schematic format!");
            return;
        }

        try {
            String filePath = f.getCanonicalPath();
            String dirPath = dir.getCanonicalPath();

            if (!filePath.substring(0, dirPath.length()).equals(dirPath)) {
                player.printError("Schematic could not read or it does not exist.");
            } else {
                session.setClipboard(format.load(f));
                WorldEdit.logger.info(player.getName() + " loaded " + filePath);
                player.print(fileName + " loaded. Paste it with //paste");
            }
        } catch (DataException e) {
            player.printError("Load error: " + e.getMessage());
        } catch (IOException e) {
            player.printError("Schematic could not read or it does not exist: " + e.getMessage());
        }
    }

    @Command(
            aliases = { "save", "s" },
            usage = "[format] <filename>",
            desc = "Save your clipboard to file",
            help = "Save your clipboard to file\n" +
                    "Format is a format from \"//schematic formats\"\n",
            min = 1,
            max = 2
    )
    @CommandPermissions({"worldedit.clipboard.save", "worldedit.schematic.save"}) // TODO: Remove 'clipboard' perm
    public void save(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException, CommandException {

        LocalConfiguration config = worldEdit.getConfiguration();
        SchematicFormat format;
        if (args.argsLength() == 1) {
            if (SchematicFormat.getFormats().size() == 1) {
                format = SchematicFormat.getFormats().iterator().next();
            } else {
                player.printError("More than one schematic format is available. Please provide the desired format");
                return;
            }
        } else {
            format = SchematicFormat.getFormat(args.getString(0));
            if (format == null) {
                player.printError("Unknown schematic format: " + args.getString(0));
                return;
            }
        }

        String filename = args.getString(args.argsLength() - 1);

        File dir = worldEdit.getWorkingDirectoryFile(config.saveDir);
        File f = worldEdit.getSafeSaveFile(player, dir, filename, "schematic", "schematic");

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
                if (!parent.mkdirs()) {
                    throw new CommandException("Could not create folder for schematics!");
                }
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
            aliases = { "delete", "d" },
            usage = "<filename>",
            desc = "Delete a saved schematic",
            help = "Delete a schematic from the schematic list",
            min = 1,
            max = 1
    )
    @CommandPermissions("worldedit.schematic.delete")
    public void delete(Player player, LocalSession session, EditSession editSession, CommandContext args) throws WorldEditException {

        LocalConfiguration config = worldEdit.getConfiguration();
        String filename = args.getString(0);

        File dir = worldEdit.getWorkingDirectoryFile(config.saveDir);
        File f = worldEdit.getSafeSaveFile(player, dir, filename, "schematic", "schematic");

        if (!f.exists()) {
            player.printError("Schematic " + filename + " does not exist!");
            return;
        }

        if (!f.delete()) {
            player.printError("Deletion of " + filename + " failed! Maybe it is read-only.");
            return;
        }

        player.print(filename + " has been deleted.");
    }

    @Command(
            aliases = {"formats", "listformats", "f"},
            desc = "List available formats",
            max = 0
    )
    @CommandPermissions("worldedit.schematic.formats")
    public void formats(Actor actor) throws WorldEditException {
        actor.print("Available schematic formats (Name: Lookup names)");
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
            actor.print(builder.toString());
        }
    }

    @Command(
            aliases = {"list", "all", "ls"},
            desc = "List saved schematics",
            max = 0,
            flags = "dn",
            help = "List all schematics in the schematics directory\n" +
                    " -d sorts by date, oldest first\n" +
                    " -n sorts by date, newest first\n"
    )
    @CommandPermissions("worldedit.schematic.list")
    public void list(Actor actor, CommandContext args) throws WorldEditException {
        File dir = worldEdit.getWorkingDirectoryFile(worldEdit.getConfiguration().saveDir);
        File[] files = dir.listFiles(new FileFilter(){
            @Override
            public boolean accept(File file) {
                // sort out directories from the schematic list
                // if WE supports sub-directories in the future,
                // this will have to be changed
                return file.isFile();
            }
        });
        if (files == null) {
            throw new FilenameResolutionException(dir.getPath(), "Schematics directory invalid or not found.");
        }

        final int sortType = args.hasFlag('d') ? -1 : args.hasFlag('n') ? 1 : 0;
        // cleanup file list
        Arrays.sort(files, new Comparator<File>(){
            @Override
            public int compare(File f1, File f2) {
                // this should no longer happen, as directory-ness is checked before
                // however, if a directory slips through, this will break the contract
                // of comparator transitivity
                if (!f1.isFile() || !f2.isFile()) return -1;
                // http://stackoverflow.com/questions/203030/best-way-to-list-files-in-java-sorted-by-date-modified
                int result = sortType == 0 ? f1.getName().compareToIgnoreCase(f2.getName()) : // use name by default
                    Long.valueOf(f1.lastModified()).compareTo(f2.lastModified()); // use date if there is a flag
                if (sortType == 1) result = -result; // flip date for newest first instead of oldest first
                return result;
            }
        });

        actor.print("Available schematics (Filename (Format)):");
        actor.print(listFiles("", files));
    }

    private String listFiles(String prefix, File[] files) {
        StringBuilder build = new StringBuilder();
        for (File file : files) {
            if (file.isDirectory()) {
                build.append(listFiles(prefix + file.getName() + "/", file.listFiles()));
                continue;
            }

            if (!file.isFile()) {
                continue;
            }

            build.append("\n\u00a79");
            SchematicFormat format = SchematicFormat.getFormat(file);
            build.append(prefix).append(file.getName())
               .append(": ").append(format == null ? "Unknown" : format.getName());
        }
        return build.toString();
    }
}
