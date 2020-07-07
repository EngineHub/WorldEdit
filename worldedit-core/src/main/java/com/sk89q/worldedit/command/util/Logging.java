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

//$Id$


package com.sk89q.worldedit.command.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates how the affected blocks should be hinted at in the log.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Logging {

    enum LogMode {
        /**
         * Player position
         */
        POSITION,

        /**
         * Region selection
         */
        REGION,

        /**
         * Player orientation and region selection
         */
        ORIENTATION_REGION,

        /**
         * Either the player position or pos1, depending on the placeAtPos1 flag
         */
        PLACEMENT,

        /**
         * Log all information available
         */
        ALL
    }

    /**
     * Log mode.
     *
     * @return either POSITION, REGION, ORIENTATION_REGION, PLACEMENT or ALL
     */
    LogMode value();

}
