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

package com.sk89q.worldedit.util.formatting.component;

import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectApplier;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class SideEffectBox extends PaginationBox {

    private static final List<SideEffect> sideEffects = Arrays
            .stream(SideEffect.values())
            .filter(SideEffect::isConfigurable)
            .sorted(Comparator.comparing(Enum::name))
            .collect(Collectors.toList());

    private SideEffectApplier sideEffectApplier;

    public SideEffectBox(SideEffectApplier sideEffectApplier) {
        super("Side Effects");

        this.sideEffectApplier = sideEffectApplier;
    }

    @Override
    public Component getComponent(int number) {
        SideEffect effect = sideEffects.get(number);
        SideEffect.State state = this.sideEffectApplier.getState(effect);

        TextComponent.Builder builder = TextComponent.builder();
        builder = builder.append(TranslatableComponent.of(effect.getDisplayName(), TextColor.YELLOW)
                .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TranslatableComponent.of(effect.getDescription()))));
        for (SideEffect.State uiState : SideEffect.State.values()) {
            builder = builder.append(TextComponent.space());
            builder = builder.append(TranslatableComponent.of(uiState.getDisplayName(), uiState == state ? TextColor.WHITE : TextColor.GRAY)
                    .clickEvent(ClickEvent.runCommand("//fast -h " + effect.name().toLowerCase(Locale.US) + " " + uiState.name().toLowerCase(Locale.US)))
                    .hoverEvent(HoverEvent.showText(uiState == state ?
                     TextComponent.of("Current") : TextComponent.of("Click to set to ").append(TranslatableComponent.of(uiState.getDisplayName()))))
            );
        }

        return builder.build();
    }

    @Override
    public int getComponentsSize() {
        return sideEffects.size();
    }
}
