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

package com.sk89q.worldedit.internal;

import com.sk89q.worldedit.event.platform.ConfigurationLoadEvent;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SchematicsEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchematicsEventListener.class);

    @Subscribe
    public void onConfigLoad(ConfigurationLoadEvent event) {
        Path config = event.getConfiguration().getWorkingDirectory().toPath();
        try {
            Files.createDirectories(config.resolve(event.getConfiguration().saveDir));
        } catch (FileAlreadyExistsException e) {
            LOGGER.debug("Schematic directory exists as file. Possible symlink.", e);
        } catch (IOException e) {
            LOGGER.warn("Failed to create schematics directory", e);
        }
    }
}
