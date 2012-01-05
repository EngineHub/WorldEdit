//$Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.minecraft.util.commands;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates how the affected blocks should be hinted at in the log.
 *
 * @author sk89q
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Logging {
    public enum LogMode {
        POSITION, // Player position
        REGION, // Region selection
        ORIENTATION_REGION, // player orientation and Region selection 
        PLACEMENT, // Either the player position or pos1, depending on the placeAtPos1 flag
        ALL // Log all information available
    }

    /**
     * Log mode. Can be either POSITION, REGION, ORIENTATION_REGION, PLACEMENT or ALL.
     */
    LogMode value();
}
