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

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SideEffectSetTest {
    private static void assertAppliesWithState(Map<SideEffect, SideEffect.State> expected, SideEffectSet set) {
        Preconditions.checkArgument(
            expected.keySet().containsAll(EnumSet.allOf(SideEffect.class)),
            "Expected map must contain all side effects"
        );

        Set<SideEffect> appliedSet = expected.entrySet().stream()
            .filter(e -> e.getValue() != SideEffect.State.OFF)
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        assertEquals(appliedSet, set.getSideEffectsToApply());
        assertEquals(!appliedSet.isEmpty(), set.doesApplyAny());
        for (SideEffect effect : SideEffect.values()) {
            assertEquals(
                appliedSet.contains(effect), set.shouldApply(effect), "Does not apply expected effect: " + effect
            );
            assertEquals(
                expected.get(effect), set.getState(effect),
                "Does not have expected state for effect: " + effect
            );
        }
    }

    private static Map<SideEffect, SideEffect.State> initStateMap(Function<SideEffect, SideEffect.State> stateFunction) {
        return Arrays.stream(SideEffect.values()).collect(Collectors.toMap(Function.identity(), stateFunction));
    }

    @Test
    public void defaults() {
        assertAppliesWithState(
            initStateMap(SideEffect::getDefaultValue),
            SideEffectSet.defaults()
        );
    }

    @Test
    public void noneExposed() {
        assertAppliesWithState(
            initStateMap(effect -> {
                if (effect.isExposed()) {
                    return SideEffect.State.OFF;
                } else {
                    return effect.getDefaultValue();
                }
            }),
            SideEffectSet.none()
        );
    }

    @Test
    public void allOn() {
        Map<SideEffect, SideEffect.State> expected = initStateMap(effect -> SideEffect.State.ON);
        assertAppliesWithState(
            expected,
            new SideEffectSet(expected)
        );
    }

    @Test
    public void allDelayed() {
        Map<SideEffect, SideEffect.State> expected = initStateMap(effect -> SideEffect.State.DELAYED);
        assertAppliesWithState(
            expected,
            new SideEffectSet(expected)
        );
    }

    @Test
    public void allOff() {
        Map<SideEffect, SideEffect.State> expected = initStateMap(effect -> SideEffect.State.OFF);
        assertAppliesWithState(
            expected,
            new SideEffectSet(expected)
        );
    }

    @Test
    public void with() {
        Map<SideEffect, SideEffect.State> expected = initStateMap(SideEffect::getDefaultValue);
        SideEffectSet set = SideEffectSet.defaults();

        for (SideEffect effect : SideEffect.values()) {
            for (SideEffect.State state : SideEffect.State.values()) {
                expected = Maps.transformEntries(expected, (e, s) -> e == effect ? state : s);
                set = set.with(effect, state);
                assertAppliesWithState(expected, set);
            }
        }
    }
}
