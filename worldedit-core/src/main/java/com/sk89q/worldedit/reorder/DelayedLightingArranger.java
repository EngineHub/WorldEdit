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

package com.sk89q.worldedit.reorder;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.action.PerformSideEffects;
import com.sk89q.worldedit.action.SideEffect;
import com.sk89q.worldedit.action.SideEffectWorldAction;
import com.sk89q.worldedit.action.WorldAction;
import com.sk89q.worldedit.reorder.arrange.Arranger;
import com.sk89q.worldedit.reorder.arrange.ArrangerContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Delays all lighting side-effects until after changes have been made.
 */
public final class DelayedLightingArranger implements Arranger {

    private static final Set<SideEffect> LIGHT = ImmutableSet.of(SideEffect.LIGHT);

    @Override
    public void rearrange(ArrangerContext context) {
        int initialCount = context.getActionCount();
        for (int i = 0; i < initialCount; i++) {
            WorldAction action = context.getAction(i);
            if (action instanceof SideEffectWorldAction) {
                SideEffectWorldAction se = (SideEffectWorldAction) action;
                if (se.getSideEffects().contains(SideEffect.LIGHT)) {
                    List<WorldAction> actions = context.getActionWriteList();
                    actions.set(i, se.withSideEffects(
                        Sets.difference(se.getSideEffects(), LIGHT)
                    ));
                    actions.add(PerformSideEffects.create(se.getPosition(), LIGHT));
                }
            }
        }
        context.markGroup(0, context.getActionCount());
    }

}
