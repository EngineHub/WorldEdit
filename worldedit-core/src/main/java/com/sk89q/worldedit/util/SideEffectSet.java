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

import com.google.common.base.Verify;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.EnumSet;
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

    static {
        Verify.verify(
            SideEffect.State.values().length == 3,
            "Implementation requires specifically 3 values in the SideEffect.State enum"
        );
        int maxEffects = Integer.SIZE / 2;
        Verify.verify(
            SideEffect.values().length <= maxEffects,
            "Implementation requires less than " + maxEffects + " side effects"
        );
        Verify.verify(
            SideEffect.State.OFF.ordinal() == 0,
            "Implementation requires SideEffect.State.OFF to be the first value"
        );
    }

    private static int shift(SideEffect effect) {
        return effect.ordinal() * 2;
    }

    private static int computeSideEffectsBitmap(Map<SideEffect, SideEffect.State> sideEffects) {
        int sideEffectsBitmap = 0;
        for (SideEffect effect : SideEffect.values()) {
            SideEffect.State state = sideEffects.getOrDefault(effect, effect.getDefaultValue());
            sideEffectsBitmap |= (state.ordinal() << shift(effect));
        }
        return sideEffectsBitmap;
    }

    /**
     * Side-effects and state are encoded into this field, 2 bits per side-effect. Least-significant bit is first.
     */
    private final int sideEffectsBitmap;
    private final Set<SideEffect> sideEffectsToApply;

    public SideEffectSet(Map<SideEffect, SideEffect.State> sideEffects) {
        this(computeSideEffectsBitmap(sideEffects));
    }

    private SideEffectSet(int sideEffectsBitmap) {
        this.sideEffectsBitmap = sideEffectsBitmap;
        var sideEffectsToApply = EnumSet.noneOf(SideEffect.class);
        for (SideEffect effect : SideEffect.values()) {
            if (shouldApply(effect)) {
                sideEffectsToApply.add(effect);
            }
        }
        this.sideEffectsToApply = Sets.immutableEnumSet(sideEffectsToApply);
    }

    public SideEffectSet with(SideEffect sideEffect, SideEffect.State state) {
        int mask = 0b11 << shift(sideEffect);
        int newState = (state.ordinal() << shift(sideEffect)) & mask;
        int newBitmap = (sideEffectsBitmap & ~mask) | newState;
        return new SideEffectSet(newBitmap);
    }

    public boolean doesApplyAny() {
        return sideEffectsBitmap != 0;
    }

    public SideEffect.State getState(SideEffect effect) {
        return SideEffect.State.values()[getRawState(effect)];
    }

    private int getRawState(SideEffect effect) {
        return (sideEffectsBitmap >> shift(effect)) & 0b11;
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
        return getRawState(effect) != 0;
    }

    public Set<SideEffect> getSideEffectsToApply() {
        return sideEffectsToApply;
    }

    public static SideEffectSet defaults() {
        return DEFAULT;
    }

    public static SideEffectSet none() {
        return NONE;
    }
}
