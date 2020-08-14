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

package com.sk89q.worldedit.command.tool;

import com.sk89q.worldedit.extension.platform.Actor;

/**
 * Represents a tool. This interface alone defines nothing. A tool also
 * has to implement {@code BlockTool} or {@code TraceTool}.
 */
public interface Tool {

    /**
     * Checks to see if the player can still be using this tool (considering
     * permissions and such).
     *
     * @param actor the actor
     * @return true if use is permitted
     */
    boolean canUse(Actor actor);

}
