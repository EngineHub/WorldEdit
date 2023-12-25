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
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.world.block.BaseBlock;
import org.enginehub.linbus.tree.LinStringTag;
import org.enginehub.linbus.tree.LinTagType;

public class SignCompatibilityHandler implements NBTCompatibilityHandler {

    @Override
    public BaseBlock updateNbt(BaseBlock block) {
        if (!DeprecationUtil.isSign(block.getBlockType())) {
            return block;
        }
        var tag = block.getNbt();
        if (tag == null) {
            return block;
        }
        var newTag = tag.toBuilder();
        for (int i = 0; i < 4; ++i) {
            String key = "Text" + (i + 1);
            var value = tag.findTag(key, LinTagType.stringTag());
            if (value == null) {
                continue;
            }
            String storedString = value.value();
            JsonElement jsonElement = null;
            if (storedString.startsWith("{")) {
                try {
                    jsonElement = JsonParser.parseString(storedString);
                } catch (JsonSyntaxException ex) {
                    // ignore: jsonElement will be null in the next check
                }
            }
            if (jsonElement == null) {
                jsonElement = new JsonPrimitive(storedString);
            }
            if (jsonElement.isJsonObject()) {
                continue;
            }

            if (jsonElement.isJsonNull()) {
                jsonElement = new JsonPrimitive("");
            }

            JsonObject jsonTextObject = new JsonObject();
            jsonTextObject.add("text", jsonElement);
            newTag.put("Text" + (i + 1), LinStringTag.of(jsonTextObject.toString()));
        }
        block = block.toBaseBlock(LazyReference.computed(newTag.build()));
        return block;
    }
}
