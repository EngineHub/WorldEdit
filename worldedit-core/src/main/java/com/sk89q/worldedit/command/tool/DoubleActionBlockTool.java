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

import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.internal.util.NonAbstractForCompatibility;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;

import javax.annotation.Nullable;

/**
 * Represents a block tool that also has a secondary/primary function.
 */
public interface DoubleActionBlockTool extends BlockTool {

    /**
     * Perform the secondary action of this block tool.
     *
     * @return true to cancel the original event which triggered this action (if possible)
     * @deprecated New subclasses must override
     * {@link #actSecondary(Platform, LocalConfiguration, Player, LocalSession, Location, Direction)}
     *      instead
     */
    @Deprecated
    default boolean actSecondary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked) {
        return actSecondary(server, config, player, session, clicked, null);
    }

    /**
     * Perform the secondary action of this block tool.
     *
     * @param server The platform
     * @param config The config instance
     * @param player The player
     * @param session The local session
     * @param clicked The location that was clicked
     * @param face The face that was clicked
     * @return true to cancel the original event which triggered this action (if possible)
     * @apiNote This must be overridden by new subclasses. See {@link NonAbstractForCompatibility}
     *          for details
     */
    @NonAbstractForCompatibility(
        delegateName = "actSecondary",
        delegateParams = { Platform.class, LocalConfiguration.class, Player.class, LocalSession.class, Location.class }
    )
    default boolean actSecondary(Platform server, LocalConfiguration config, Player player, LocalSession session, Location clicked, @Nullable Direction face) {
        DeprecationUtil.checkDelegatingOverride(getClass());
        return actSecondary(server, config, player, session, clicked);
    }

}
