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

package com.sk89q.worldedit.util.logging;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Adds a WorldEdit prefix to WorldEdit's logger messages using a handler.
 */
public final class WorldEditPrefixHandler extends Handler {

    private WorldEditPrefixHandler() {
    }

    @Override
    public void publish(LogRecord record) {
        String message = record.getMessage();
        if (!message.startsWith("WorldEdit: ") && !message.startsWith("[WorldEdit] ")) {
            record.setMessage("[WorldEdit] " + message);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
    }

    /**
     * Add the handler to the following logger name.
     *
     * @param name the logger name
     */
    public static void register(String name) {
        Logger.getLogger(name).addHandler(new WorldEditPrefixHandler());
    }

}
