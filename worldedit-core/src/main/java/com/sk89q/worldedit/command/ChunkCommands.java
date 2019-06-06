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

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.command.util.CommandPermissions;
import com.sk89q.worldedit.command.util.CommandPermissionsConditionGenerator;
import com.sk89q.worldedit.command.util.Logging;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.MathUtils;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.component.PaginationBox;
import com.sk89q.worldedit.world.storage.LegacyChunkStore;
import com.sk89q.worldedit.world.storage.McRegionChunkStore;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;
import org.enginehub.piston.annotation.param.ArgFlag;
import org.enginehub.piston.annotation.param.Switch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.command.util.Logging.LogMode.REGION;

/**
 * Commands for working with chunks.
 */
@CommandContainer(superTypes = CommandPermissionsConditionGenerator.Registration.class)
public class ChunkCommands {

    private final WorldEdit worldEdit;

    public ChunkCommands(WorldEdit worldEdit) {
        checkNotNull(worldEdit);
        this.worldEdit = worldEdit;
    }

    @Command(
        name = "chunkinfo",
        desc = "Get information about the chunk you're inside"
    )
    @CommandPermissions("worldedit.chunkinfo")
    public void chunkInfo(Player player) {
        Location pos = player.getBlockIn();
        int chunkX = (int) Math.floor(pos.getBlockX() / 16.0);
        int chunkZ = (int) Math.floor(pos.getBlockZ() / 16.0);

        final BlockVector2 chunkPos = BlockVector2.at(chunkX, chunkZ);
        player.print("Chunk: " + chunkX + ", " + chunkZ);
        player.print("Old format: " + LegacyChunkStore.getFilename(chunkPos));
        player.print("McRegion: region/" + McRegionChunkStore.getFilename(chunkPos));
    }

    @Command(
        name = "listchunks",
        desc = "List chunks that your selection includes"
    )
    @CommandPermissions("worldedit.listchunks")
    public void listChunks(Player player, LocalSession session,
            @ArgFlag(name = 'p', desc = "Page number.", def = "1") int page) throws WorldEditException {
        Set<BlockVector2> chunks = session.getSelection(player.getWorld()).getChunks();

        PaginationBox paginationBox = PaginationBox.fromStrings("Selected Chunks", "/listchunks -p %page%",
                chunks.stream().map(BlockVector2::toString).collect(Collectors.toList()));
        player.print(paginationBox.create(page));
    }

    @Command(
        name = "delchunks",
        desc = "Delete chunks that your selection includes"
    )
    @CommandPermissions("worldedit.delchunks")
    @Logging(REGION)
    public void deleteChunks(Player player, LocalSession session,
                             @Switch(name = 'f', desc = "Don't backup region files.")
                                boolean skipBackup,
                             @Switch(name = 's', desc = "Run on next startup.")
                                boolean startup) throws WorldEditException {
        LocalConfiguration config = worldEdit.getConfiguration();

        String worldName = player.getWorld().getName();
        Set<BlockVector2> chunks = session.getSelection(player.getWorld()).getChunks();

        String classPath = ChunkCommands.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String chunkFileName = (startup ? "startup_" : "") + "delete_chunks.txt";
        File chunkFile = worldEdit.getWorkingDirectoryFile(chunkFileName);
        try (FileOutputStream out = new FileOutputStream(chunkFile, true)) {
            OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
            for (BlockVector2 cc : chunks) {
                writer.write(worldName + "," + cc.getBlockX() + "," + cc.getBlockZ() + "\n");
            }
            writer.close();
        } catch (IOException e) {
            player.printError("Failed to write chunk list: " + e.getMessage());
            return;
        }
        if (startup) {
            player.print(String.format("%d chunk(s) have been marked for deletion and will be deleted the next time the server starts.", chunks.size()));
            return;
        }

        if (!"bat".equals(config.shellSaveType) && !"bash".equals(config.shellSaveType)) {
            player.printError("Shell script type must be configured: 'bat' or 'bash' expected.");
            return;
        }

        final String javaCmd = "java -classpath \"" + classPath + "\" com.sk89q.worldedit.internal.util.ChunkDeleter \""
                + chunkFile.getPath() + "\" " + (skipBackup ? "false" : "true");
        if (config.shellSaveType.equalsIgnoreCase("bat")) {
            try (FileOutputStream out = new FileOutputStream("worldedit-delchunks.bat")) {
                OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                writer.write("@ECHO off\r\n");
                writer.write("ECHO Running WorldEdit chunk deleter.\r\n");
                writer.write(javaCmd + "\r\n");
                writer.close();

                player.print("worldedit-delchunks.bat written. Run it when the server is shutdown.");
            } catch (IOException e) {
                player.printError("Error occurred writing script: " + e.getMessage());
            }
        } else if (config.shellSaveType.equalsIgnoreCase("bash")) {
            try (FileOutputStream out = new FileOutputStream("worldedit-delchunks.sh")) {
                OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
                writer.write("#!/bin/bash\n");
                writer.write("echo Running WorldEdit chunk deleter.\n");
                writer.write(javaCmd + "\n");
                writer.close();

                player.print("worldedit-delchunks.sh written. Run it when the server is shutdown.");
            } catch (IOException e) {
                player.printError("Error occurred writing script: " + e.getMessage());
                return;
            }
            if (!new File("worldedit-delchunks.sh").setExecutable(true)) {
                player.print("You may have to set the file as executable (chmod +x) to run it.");
            }
        }
    }

}
