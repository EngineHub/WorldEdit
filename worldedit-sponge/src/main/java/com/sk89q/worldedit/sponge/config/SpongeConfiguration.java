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

package com.sk89q.worldedit.sponge.config;

import com.sk89q.worldedit.sponge.SpongeWorldEdit;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SpongeConfiguration extends ConfigurateConfiguration {

    public boolean creativeEnable = false;
    public boolean cheatMode = false;

    public SpongeConfiguration(ConfigurationLoader<CommentedConfigurationNode> config, Logger logger) {
        super(config, logger);
    }

    @Override
    public void load() {
        super.load();

        creativeEnable = node.getNode("use-in-creative").getBoolean(false);
        cheatMode = node.getNode("cheat-mode").getBoolean(false);

        try {
            config.save(node);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error loading WorldEdit configuration", e);
        }
    }

    @Override
    public File getWorkingDirectory() {
        return SpongeWorldEdit.inst().getWorkingDir();
    }
}