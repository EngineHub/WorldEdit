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

package com.sk89q.worldedit.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SideEffectSet {
    private static final SideEffectSet DEFAULT = new SideEffectSet(ImmutableMap.of());
    private static final SideEffectSet NONE = new SideEffectSet(
        Arrays.stream(SideEffect.values())
            .filter(SideEffect::isExposed)
            .collect(Collectors.toMap(Function.identity(), state -> SideEffect.State.OFF))
    );

    private final Map<SideEffect, SideEffect.State> sideEffects;
    private final Set<SideEffect> appliedSideEffects;
    private final boolean appliesAny;

    public SideEffectSet(Map<SideEffect, SideEffect.State> sideEffects) {
        this.sideEffects = Maps.immutableEnumMap(sideEffects);

        appliedSideEffects = sideEffects.entrySet()
                .stream()
                .filter(entry -> entry.getValue() != SideEffect.State.OFF)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
        appliesAny = !appliedSideEffects.isEmpty();
    }

    public SideEffectSet with(SideEffect sideEffect, SideEffect.State state) {
        Map<SideEffect, SideEffect.State> entries = this.sideEffects.isEmpty() ? Maps.newEnumMap(SideEffect.class) : new EnumMap<>(this.sideEffects);
        entries.put(sideEffect, state);
        return new SideEffectSet(entries);
    }

    public boolean doesApplyAny() {
        return this.appliesAny;
    }

    public SideEffect.State getState(SideEffect effect) {
        return sideEffects.getOrDefault(effect, effect.getDefaultValue());
    }

    /**
     * Gets whether this side effect is not off.
     *
     * <p>
     * This returns whether it is either delayed or on.
     * </p>
     *
     * @param effect The side effect
     * @return Whether it should apply
     */
    public boolean shouldApply(SideEffect effect) {
        return getState(effect) != SideEffect.State.OFF;
    }

    public Set<SideEffect> getSideEffectsToApply() {
        return this.appliedSideEffects;
    }

    public static SideEffectSet defaults() {
        return DEFAULT;
    }

    public static SideEffectSet none() {
        return NONE;
    }

    public static class GsonSerializer implements JsonSerializer<SideEffectSet>, JsonDeserializer<SideEffectSet> {

        @Override
        public SideEffectSet deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Map<SideEffect, SideEffect.State> sideEffects = Maps.newEnumMap(SideEffect.class);
            JsonObject obj = json.getAsJsonObject();
            for (Map.Entry<String, JsonElement> stringJsonElementEntry : obj.entrySet()) {
                SideEffect sideEffect = SideEffect.valueOf(stringJsonElementEntry.getKey());
                SideEffect.State state = SideEffect.State.valueOf(stringJsonElementEntry.getValue().getAsString());
                sideEffects.put(sideEffect, state);
            }
            return new SideEffectSet(sideEffects);
        }

        @Override
        public JsonElement serialize(SideEffectSet src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            for (Map.Entry<SideEffect, SideEffect.State> entry : src.sideEffects.entrySet()) {
                obj.add(entry.getKey().name(), new JsonPrimitive(entry.getValue().name()));
            }
            return obj;
        }
    }
}
