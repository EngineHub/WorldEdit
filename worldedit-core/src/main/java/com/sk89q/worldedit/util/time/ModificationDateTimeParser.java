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

package com.sk89q.worldedit.util.time;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ModificationDateTimeParser implements SnapshotDateTimeParser {

    private static final ModificationDateTimeParser INSTANCE = new ModificationDateTimeParser();

    public static ModificationDateTimeParser getInstance() {
        return INSTANCE;
    }

    private ModificationDateTimeParser() {
    }

    @Override
    public ZonedDateTime detectDateTime(Path path) {
        if (!Files.exists(path)) {
            return null;
        }
        try {
            return Files.getLastModifiedTime(path).toInstant().atZone(ZoneId.systemDefault());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
