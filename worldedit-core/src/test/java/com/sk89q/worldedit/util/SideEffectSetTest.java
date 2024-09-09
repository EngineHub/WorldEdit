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

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

import java.util.Map;

public class SideEffectSetTest {
    @Property
    public boolean stateOrDefaultIsCorrect(
        @ForAll Map<SideEffect, SideEffect.State> stateMap,
        @ForAll SideEffect effectToTest
    ) {
        SideEffectSet set = new SideEffectSet(stateMap);
        return set.getState(effectToTest) == stateMap.getOrDefault(effectToTest, effectToTest.getDefaultValue());
    }

    @Property
    public boolean shouldApplyUnlessOff(
        @ForAll Map<SideEffect, SideEffect.State> stateMap,
        @ForAll SideEffect effectToTest
    ) {
        SideEffectSet set = new SideEffectSet(stateMap);
        return set.shouldApply(effectToTest)
            == (stateMap.getOrDefault(effectToTest, effectToTest.getDefaultValue()) != SideEffect.State.OFF);
    }

    @Property
    public boolean withChangesState(
        @ForAll Map<SideEffect, SideEffect.State> stateMap,
        @ForAll SideEffect effectToTest,
        @ForAll SideEffect.State stateToSet
    ) {
        SideEffectSet set = new SideEffectSet(stateMap).with(effectToTest, stateToSet);
        return set.getState(effectToTest) == stateToSet;
    }

    @Property
    public boolean anyShouldApplyEqualsDoesApplyAny(@ForAll Map<SideEffect, SideEffect.State> stateMap) {
        SideEffectSet set = new SideEffectSet(stateMap);
        boolean anyShouldApply = false;
        for (SideEffect effect : SideEffect.values()) {
            if (set.shouldApply(effect)) {
                anyShouldApply = true;
                break;
            }
        }
        return anyShouldApply == set.doesApplyAny();
    }

    @Property
    public boolean shouldApplyEqualsApplySetContains(
        @ForAll Map<SideEffect, SideEffect.State> stateMap,
        @ForAll SideEffect effectToTest
    ) {
        SideEffectSet set = new SideEffectSet(stateMap);
        return set.shouldApply(effectToTest) == set.getSideEffectsToApply().contains(effectToTest);
    }
}
