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

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.extension.factory.DelegateParser;

import java.util.HashMap;
import java.util.Map;

public class WallSignParser implements DelegateParser<CompoundTag> {
    @Override
    public CompoundTag createFromArguments(String[] blockAndExtraData) {
        String[] text = new String[4];
        text[0] = blockAndExtraData.length > 1 ? blockAndExtraData[1] : "";
        text[1] = blockAndExtraData.length > 2 ? blockAndExtraData[2] : "";
        text[2] = blockAndExtraData.length > 3 ? blockAndExtraData[3] : "";
        text[3] = blockAndExtraData.length > 4 ? blockAndExtraData[4] : "";

        Map<String, Tag> values = new HashMap<String, Tag>();
        for (int i = 0; i < text.length; ++i) {
            JsonObject jsonTextObject = new JsonObject();
            jsonTextObject.add("text", new JsonPrimitive(text[i]));
            values.put("Text" + (i + 1), new StringTag(jsonTextObject.toString()));
        }

        return new CompoundTag(values);
    }
}
