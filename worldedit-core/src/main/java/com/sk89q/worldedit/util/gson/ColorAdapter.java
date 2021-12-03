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

package com.sk89q.worldedit.util.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Deserializes hexadecimal {@link Integer}s from {@link String}s.
 */
public class ColorAdapter implements JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String hexString = json.getAsJsonPrimitive().getAsString();

        if (!hexString.startsWith("#")) {
            hexString = "#" + hexString;
        }
        if (hexString.length() != 7) {
            throw new JsonParseException("String does not have length 6");
        }

        System.out.println("String: " + hexString);

        int hex;
        try {
            hex = Integer.decode(hexString);
        } catch (NumberFormatException e) {
            throw new JsonParseException("String does not contain parseable integer", e);
        }

        return hex;
    }
}
