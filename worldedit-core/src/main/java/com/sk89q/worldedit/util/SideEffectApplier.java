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

package com.sk89q.worldedit.util;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SideEffectApplier {
    private static final SideEffectApplier DEFAULT = new SideEffectApplier();
    private static final SideEffectApplier NONE = new SideEffectApplier(
            Arrays.stream(SideEffect.values()).collect(Collectors.toMap(Function.identity(), SideEffect::getDefaultValue))
    );

    private final Map<SideEffect, SideEffect.State> sideEffects;
    private boolean requiresCleanup = false;
    private boolean appliesAny = false;

    private SideEffectApplier() {
        this.sideEffects = ImmutableMap.of();
        updateSideEffects();
    }

    public SideEffectApplier(Map<SideEffect, SideEffect.State> sideEffects) {
        this.sideEffects = Maps.immutableEnumMap(sideEffects);
        updateSideEffects();
    }

    private void updateSideEffects() {
        requiresCleanup = sideEffects.keySet().stream().anyMatch(SideEffect::requiresCleanup);
        appliesAny = sideEffects.values().stream().anyMatch(state -> state != SideEffect.State.OFF);
    }

    public SideEffectApplier with(SideEffect sideEffect, SideEffect.State state) {
        Map<SideEffect, SideEffect.State> entries = this.sideEffects.isEmpty() ? Maps.newEnumMap(SideEffect.class) : new EnumMap<>(this.sideEffects);
        entries.put(sideEffect, state);
        return new SideEffectApplier(entries);
    }

    public boolean doesApplyAny() {
        return this.appliesAny;
    }

    public boolean doesRequireCleanup() {
        return this.requiresCleanup;
    }

    public SideEffect.State getState(SideEffect effect) {
        return sideEffects.getOrDefault(effect, effect.getDefaultValue());
    }

    /**
     * Gets whether this side effect is not off.
     *
     * This returns whether it is either delayed or on.
     *
     * @param effect The side effect
     * @return Whether it should apply
     */
    public boolean shouldApply(SideEffect effect) {
        return getState(effect) != SideEffect.State.OFF;
    }

    public static SideEffectApplier defaults() {
        return DEFAULT;
    }

    public static SideEffectApplier none() {
        return NONE;
    }
}
