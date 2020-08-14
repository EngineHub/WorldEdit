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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.Map;

public class SignCompatibilityHandler implements NBTCompatibilityHandler {

    @Override
    public <B extends BlockStateHolder<B>> boolean isAffectedBlock(B block) {
        return DeprecationUtil.isSign(block.getBlockType());
    }

    @Override
    public <B extends BlockStateHolder<B>> BlockStateHolder<?> updateNBT(B block, Map<String, Tag> values) {
        for (int i = 0; i < 4; ++i) {
            String key = "Text" + (i + 1);
            Tag value = values.get(key);
            if (value instanceof StringTag) {
                String storedString = ((StringTag) value).getValue();
                JsonElement jsonElement = null;
                if (storedString != null && storedString.startsWith("{")) {
                    try {
                        jsonElement = new JsonParser().parse(storedString);
                    } catch (JsonSyntaxException ex) {
                        // ignore: jsonElement will be null in the next check
                    }
                }
                if (jsonElement == null) {
                    jsonElement = new JsonPrimitive(storedString == null ? "" : storedString);
                }
                if (jsonElement.isJsonObject()) {
                    continue;
                }

                if (jsonElement.isJsonNull()) {
                    jsonElement = new JsonPrimitive("");
                }

                JsonObject jsonTextObject = new JsonObject();
                jsonTextObject.add("text", jsonElement);
                values.put("Text" + (i + 1), new StringTag(jsonTextObject.toString()));
            }
        }
        return block;
    }
}
