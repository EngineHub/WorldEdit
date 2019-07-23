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
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.World;

import java.util.Collection;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Performs side-effects at a block location.
 */
public final class PerformSideEffects implements SideEffectWorldAction {

    public static PerformSideEffects create(BlockVector3 location) {
        return create(location, SideEffect.getDefault());
    }

    public static PerformSideEffects create(BlockVector3 location, Collection<SideEffect> sideEffects) {
        return new PerformSideEffects(location, sideEffects);
    }

    private final BlockVector3 position;
    private final ImmutableSet<SideEffect> sideEffects;

    private PerformSideEffects(BlockVector3 position, Collection<SideEffect> sideEffects) {
        this.position = checkNotNull(position);
        this.sideEffects = Sets.immutableEnumSet(sideEffects);
    }

    @Override
    public BlockVector3 getPosition() {
        return position;
    }

    @Override
    public ImmutableSet<SideEffect> getSideEffects() {
        return sideEffects;
    }

    @Override
    public PerformSideEffects withSideEffects(Collection<SideEffect> sideEffects) {
        return new PerformSideEffects(position, sideEffects);
    }

    @Override
    public void apply(World world) throws WorldEditException {
        world.applySideEffects(position, world.getBlock(position), sideEffects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, sideEffects);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        PerformSideEffects p = (PerformSideEffects) obj;
        return Objects.equals(position, p.position)
            && Objects.equals(sideEffects, p.sideEffects);
    }

}
