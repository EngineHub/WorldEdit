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

import com.sk89q.worldedit.internal.helper.MCDirections;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.world.entity.EntityType;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.enginehub.linbus.tree.LinNumberTag;

public class Pre13HangingCompatibilityHandler implements EntityNBTCompatibilityHandler {

    @Override
    public LinCompoundTag updateNbt(EntityType type, LinCompoundTag tag) {
        if (!type.getId().startsWith("minecraft:")) {
            return tag;
        }
        Direction newDirection;
        if (tag.value().get("Dir") instanceof LinNumberTag<?> legacyDir) {
            newDirection = MCDirections.fromPre13Hanging(MCDirections.fromLegacyHanging(legacyDir.value().byteValue()));
        } else if (tag.value().get("Direction") instanceof LinNumberTag<?> legacyDirection) {
            newDirection = MCDirections.fromPre13Hanging(legacyDirection.value().intValue());
        } else if (tag.value().get("Facing") instanceof LinNumberTag<?> legacyFacing) {
            newDirection = MCDirections.fromPre13Hanging(legacyFacing.value().intValue());
        } else {
            return tag;
        }
        byte hangingByte = (byte) MCDirections.toHanging(newDirection);
        var builder = tag.toBuilder();
        builder.putByte("Facing", hangingByte);
        return builder.build();
    }

}
