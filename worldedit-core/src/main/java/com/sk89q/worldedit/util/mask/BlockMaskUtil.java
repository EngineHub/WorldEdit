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

package com.sk89q.worldedit.util.mask;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.AllowedBlocksMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;

public class BlockMaskUtil {

    /**
     * Utility function to expand the masks of copying operations by the disallowed blocks list
     *
     * @param actor the actor issuing the copying operation
     * @param mask a potential mask already set by the actor
     * @param extent the extent
     */
    public static Mask checkForAllowedBlocksMask(Actor actor, Mask mask, Extent extent) {
        WorldEdit worldEdit = WorldEdit.getInstance();

        if (worldEdit.getConfiguration().disableDisallowedBlockCopying
                && actor != null
                && !actor.hasPermission("worldedit.anyblock")) {
            Mask allowedBlocksMask = new AllowedBlocksMask(worldEdit, extent);

            return (mask == null)
                    ? allowedBlocksMask
                    : new MaskIntersection(allowedBlocksMask, mask);
        }

        return mask;
    }

}