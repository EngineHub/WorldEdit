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

package com.sk89q.worldedit.extent.clipboard.io.sponge;

import com.google.common.collect.ImmutableList;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.CompoundTagBuilder;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.util.Location;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class WriterUtil {
    static ListTag encodeEntities(Clipboard clipboard, boolean positionIsRelative) {
        List<CompoundTag> entities = clipboard.getEntities().stream().map(e -> {
            BaseEntity state = e.getState();
            if (state == null) {
                return null;
            }
            CompoundTagBuilder fullTagBuilder = CompoundTagBuilder.create();
            CompoundTagBuilder dataTagBuilder = CompoundTagBuilder.create();
            CompoundTag rawData = state.getNbtData();
            if (rawData != null) {
                dataTagBuilder.putAll(rawData.getValue());
                dataTagBuilder.remove("id");
            }
            final Location location = e.getLocation();
            Vector3 pos = location.toVector();
            dataTagBuilder.put("Rotation", encodeRotation(location));
            if (positionIsRelative) {
                pos = pos.subtract(clipboard.getMinimumPoint().toVector3());

                fullTagBuilder.put("Data", dataTagBuilder.build());
            } else {
                fullTagBuilder.putAll(dataTagBuilder.build().getValue());
            }
            fullTagBuilder.putString("Id", state.getType().getId());
            fullTagBuilder.put("Pos", encodeVector(pos));

            return fullTagBuilder.build();
        }).filter(Objects::nonNull).collect(Collectors.toList());
        if (entities.isEmpty()) {
            return null;
        }
        return new ListTag(CompoundTag.class, entities);
    }

    static Tag encodeVector(Vector3 vector) {
        return new ListTag(DoubleTag.class, ImmutableList.of(
            new DoubleTag(vector.getX()),
            new DoubleTag(vector.getY()),
            new DoubleTag(vector.getZ())
        ));
    }

    static Tag encodeRotation(Location location) {
        return new ListTag(FloatTag.class, ImmutableList.of(
            new FloatTag(location.getYaw()),
            new FloatTag(location.getPitch())
        ));
    }
}
