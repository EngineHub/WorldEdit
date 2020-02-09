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

import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SideEffectApplier {

    private static final Collection<SideEffect> CONFIGURABLE = Arrays.stream(SideEffect.values()).filter(SideEffect::isConfigurable).collect(Collectors.toList());
    public static final SideEffectApplier ALL = new SideEffectApplier(CONFIGURABLE);
    public static final SideEffectApplier NONE = new SideEffectApplier(EnumSet.noneOf(SideEffect.class));

    private final Set<SideEffect> sideEffects;
    private boolean requiresCleanup = false;
    private boolean isAll = false;

    public SideEffectApplier(Collection<SideEffect> sideEffects) {
        this.sideEffects = Sets.immutableEnumSet(sideEffects);
        updateSideEffects();
    }

    private void updateSideEffects() {
        requiresCleanup = sideEffects.stream().anyMatch(SideEffect::requiresCleanup);
        isAll = sideEffects.stream().filter(SideEffect::isConfigurable).count() == CONFIGURABLE.size();
    }

    public SideEffectApplier with(Collection<SideEffect> newSideEffects) {
        List<SideEffect> entries = new ArrayList<>(newSideEffects);
        entries.addAll(this.sideEffects);
        return new SideEffectApplier(entries);
    }

    public SideEffectApplier without(Collection<SideEffect> removedSideEffects) {
        List<SideEffect> entries = new ArrayList<>(this.sideEffects);
        entries.removeAll(removedSideEffects);
        return new SideEffectApplier(entries);
    }

    public boolean doesRequireCleanup() {
        return this.requiresCleanup;
    }

    public boolean shouldApply(SideEffect effect) {
        return sideEffects.contains(effect);
    }

    public boolean isNone() {
        return sideEffects.isEmpty();
    }

    public boolean isAll() {
        return this.isAll;
    }

    public SideEffectApplier withAll() {
        return with(CONFIGURABLE);
    }

    public SideEffectApplier withoutAll() {
        return without(CONFIGURABLE);
    }
}
