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

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class SideEffectApplier {

    public static final SideEffectApplier ALL = new SideEffectApplier(EnumSet.allOf(SideEffect.class));
    public static final SideEffectApplier NONE = new SideEffectApplier(EnumSet.noneOf(SideEffect.class));

    private final Set<SideEffect> sideEffects;
    private boolean requiresCleanup = false;

    public SideEffectApplier(Collection<SideEffect> sideEffects) {
        this.sideEffects = Sets.immutableEnumSet(sideEffects);
        updateSideEffects();
    }

    private void updateSideEffects() {
        requiresCleanup = sideEffects.stream().anyMatch(SideEffect::requiresCleanup);
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
        return sideEffects.size() == SideEffect.values().length;
    }
}
