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

package com.sk89q.worldedit.extent.clipboard.io.legacycompat;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.CompoundTagBuilder;
import com.sk89q.worldedit.internal.helper.MCDirections;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.entity.EntityType;

public class Pre13HangingCompatibilityHandler implements EntityNBTCompatibilityHandler {

    @Override
    public boolean isAffectedEntity(EntityType type, CompoundTag tag) {
        if (!type.getId().startsWith("minecraft:")) {
            return false;
        }
        boolean hasLegacyDirection = tag.containsKey("Dir") || tag.containsKey("Direction");
        boolean hasFacing = tag.containsKey("Facing");
        return hasLegacyDirection || hasFacing;
    }

    @Override
    public CompoundTag updateNBT(EntityType type, CompoundTag tag) {
        boolean hasLegacyDir = tag.containsKey("Dir");
        boolean hasLegacyDirection = tag.containsKey("Direction");
        boolean hasPre113Facing = tag.containsKey("Facing");
        Direction newDirection;
        if (hasLegacyDir) {
            newDirection = MCDirections.fromPre13Hanging(MCDirections.fromLegacyHanging((byte) tag.asInt("Dir")));
        } else if (hasLegacyDirection) {
            newDirection = MCDirections.fromPre13Hanging(tag.asInt("Direction"));
        } else if (hasPre113Facing) {
            newDirection = MCDirections.fromPre13Hanging(tag.asInt("Facing"));
        } else {
            return tag;
        }
        byte hangingByte = (byte) MCDirections.toHanging(newDirection);
        CompoundTagBuilder builder = tag.createBuilder();
        builder.putByte("Facing", hangingByte);
        return builder.build();
    }

}
