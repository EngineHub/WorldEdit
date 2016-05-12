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

package com.sk89q.worldedit.extension.factory.delegate.block;

import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.extension.factory.DelegateParser;
import com.sk89q.worldedit.extension.input.InputParseException;

import java.util.HashMap;
import java.util.Map;

public class SkullParser implements DelegateParser<CompoundTag> {
    @Override
    public CompoundTag createFromArguments(String[] blockAndExtraData) throws InputParseException {
        // allow setting type/player/rotation
        byte rot = 0;
        byte skullType = 0;
        String type = "";

        if (blockAndExtraData.length > 1) {
            try {
                rot = Byte.parseByte(blockAndExtraData[1]);
            } catch (NumberFormatException e) {
                type = blockAndExtraData[1];
                if (blockAndExtraData.length > 2) {
                    try {
                        rot = Byte.parseByte(blockAndExtraData[2]);
                    } catch (NumberFormatException e2) {
                        throw new InputParseException("Second part of skull metadata should be a number.");
                    }
                }
            }
            // type is either the mob type or the player name
            // sorry for the four minecraft accounts named "skeleton", "wither", "zombie", or "creeper"
            if (!type.isEmpty()) {
                if (type.equalsIgnoreCase("skeleton")) skullType = 0;
                else if (type.equalsIgnoreCase("wither")) skullType = 1;
                else if (type.equalsIgnoreCase("zombie")) skullType = 2;
                else if (type.equalsIgnoreCase("creeper")) skullType = 4;
                else {
                    skullType = 3;
                    type = type.replace(" ", "_");
                }
            }
        }

        Map<String, Tag> values = new HashMap<String, Tag>();
        values.put("SkullType", new ByteTag(skullType));
        if (type == null) type = "";
        values.put("ExtraType", new StringTag(type));
        values.put("Rot", new ByteTag(rot));
        return new CompoundTag(values);
    }
}
