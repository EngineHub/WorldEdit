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

import java.nio.file.Path;
import java.time.ZonedDateTime;
import javax.annotation.Nullable;

/**
 * Instances of this interface try to determine an {@link ZonedDateTime} from a given
 * {@link Path}.
 */
public interface SnapshotDateTimeParser {

    /**
     * Attempt to detect an ZonedDateTime from a path.
     *
     * <p>
     * The path is not guaranteed to exist.
     * </p>
     *
     * @param path the path
     * @return date-time, if it can be parsed
     */
    @Nullable
    ZonedDateTime detectDateTime(Path path);

}
