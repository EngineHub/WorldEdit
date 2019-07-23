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

package com.sk89q.worldedit.action;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Block placement side-effect flags.
 */
public enum SideEffect {
    /**
     * Notify neighbors of the block placement.
     */
    NOTIFY_NEIGHBORS,
    /**
     * Re-light the block after placement.
     */
    LIGHT;

    private static final ImmutableSet<SideEffect> DEFAULT
        = Sets.immutableEnumSet(Arrays.asList(values()));

    /**
     * Get the default set of side effects. Currently, this includes every side-effect.
     *
     * @return the default side effects
     */
    public static ImmutableSet<SideEffect> getDefault() {
        return DEFAULT;
    }

    private static final ImmutableSet<SideEffect> NO_NOTIFY_AND_LIGHT;
    static {
        EnumSet<SideEffect> noNotifyAndLight = EnumSet.allOf(SideEffect.class);
        noNotifyAndLight.removeAll(EnumSet.of(NOTIFY_NEIGHBORS, LIGHT));
        NO_NOTIFY_AND_LIGHT = Sets.immutableEnumSet(noNotifyAndLight);
    }

    public static ImmutableSet<SideEffect> getNoNotifyAndLight() {
        return NO_NOTIFY_AND_LIGHT;
    }
}
