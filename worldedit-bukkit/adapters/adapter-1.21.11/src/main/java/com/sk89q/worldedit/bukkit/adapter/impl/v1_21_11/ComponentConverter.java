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

package com.sk89q.worldedit.bukkit.adapter.impl.v1_21_11;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.Strictness;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;

import java.io.StringReader;
import javax.annotation.Nullable;

public class ComponentConverter {

    public static class Serializer {
        private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

        private Serializer() {
        }

        static MutableComponent deserialize(JsonElement json, HolderLookup.Provider registries) {
            return (MutableComponent) ComponentSerialization.CODEC.parse(registries.createSerializationContext(JsonOps.INSTANCE), json).getOrThrow(JsonParseException::new);
        }

        static JsonElement serialize(Component text, HolderLookup.Provider registries) {
            return ComponentSerialization.CODEC.encodeStart(registries.createSerializationContext(JsonOps.INSTANCE), text).getOrThrow(JsonParseException::new);
        }

        public static String toJson(Component text, HolderLookup.Provider registries) {
            return GSON.toJson(serialize(text, registries));
        }

        @Nullable
        public static MutableComponent fromJson(String json, HolderLookup.Provider registries) {
            JsonElement jsonelement = JsonParser.parseString(json);
            return jsonelement == null ? null : deserialize(jsonelement, registries);
        }

        @Nullable
        public static MutableComponent fromJson(@Nullable JsonElement json, HolderLookup.Provider registries) {
            return json == null ? null : deserialize(json, registries);
        }

        @Nullable
        public static MutableComponent fromJsonLenient(String json, HolderLookup.Provider registries) {
            JsonReader jsonreader = new JsonReader(new StringReader(json));
            jsonreader.setStrictness(Strictness.LENIENT);
            JsonElement jsonelement = JsonParser.parseReader(jsonreader);
            return jsonelement == null ? null : deserialize(jsonelement, registries);
        }
    }
}
