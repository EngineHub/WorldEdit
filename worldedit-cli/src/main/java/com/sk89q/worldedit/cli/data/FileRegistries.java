/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.cli.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.cli.CLIWorldEdit;
import com.sk89q.worldedit.util.io.Closer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileRegistries {

    private static final int CLI_DATA_VERSION = 1;
    private static final String DATA_FILE_DOWNLOAD_URL = "https://services.enginehub.org/cassette-deck/we-cli-data/";

    private final CLIWorldEdit app;
    private final Gson gson = new GsonBuilder().create();

    private DataFile dataFile;

    public FileRegistries(CLIWorldEdit app) {
        this.app = app;
    }

    public void loadDataFiles() {
        Path outputFolder = WorldEdit.getInstance().getWorkingDirectoryPath("cli-data");
        Path checkPath = outputFolder.resolve(app.getPlatform().getDataVersion() + "_" + CLI_DATA_VERSION + ".json");

        try {
            Files.createDirectories(outputFolder);

            if (!Files.exists(checkPath)) {
                URL url = URI.create(DATA_FILE_DOWNLOAD_URL + app.getPlatform().getDataVersion() + "/" + CLI_DATA_VERSION).toURL();

                try (var stream = url.openStream()) {
                    Files.copy(stream, checkPath);
                }
            }

            this.dataFile = gson.fromJson(Files.readString(checkPath), DataFile.class);
        } catch (IOException e) {
            throw new RuntimeException("The provided file is not compatible with this version of WorldEdit-CLI. Please update or report this.", e);
        }
    }

    public DataFile getDataFile() {
        return this.dataFile;
    }

}
