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

import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.cli.CLIWorldEdit;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.util.io.ResourceLoader;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class FileRegistries {

    private final CLIWorldEdit app;
    private final Gson gson = new GsonBuilder().create();

    private DataFile dataFile;

    public FileRegistries(CLIWorldEdit app) {
        this.app = app;
    }

    public void loadDataFiles() {
        ResourceLoader resourceLoader = WorldEdit.getInstance().getPlatformManager().queryCapability(Capability.CONFIGURATION).getResourceLoader();
        try {
            URL url = resourceLoader.getResource(FileRegistries.class, app.getPlatform().getDataVersion() + ".json");
            this.dataFile = gson.fromJson(Resources.toString(url, StandardCharsets.UTF_8), DataFile.class);
        } catch (IOException e) {
            throw new RuntimeException("The provided file is not compatible with this version of WorldEdit-CLI. Please update or report this.");
        }
    }

    public DataFile getDataFile() {
        return this.dataFile;
    }

    public static class BlockManifest {
        public String defaultstate;
        public Map<String, BlockProperty> properties;
    }

    public static class BlockProperty {
        public List<String> values;
        public String type;
    }

    public static class DataFile {
        public Map<String, List<String>> itemtags;
        public Map<String, List<String>> blocktags;
        public Map<String, List<String>> entitytags;
        public List<String> items;
        public List<String> entities;
        public List<String> biomes;
        public Map<String, BlockManifest> blocks;
    }
}
