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

package com.sk89q.worldedit.util.formatting.adventure;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.adventure.text.Component;
import com.sk89q.worldedit.util.adventure.text.TextComponent;
import com.sk89q.worldedit.util.adventure.text.event.ClickEvent;
import com.sk89q.worldedit.util.adventure.text.event.HoverEvent;
import com.sk89q.worldedit.util.adventure.text.format.NamedTextColor;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.formatting.WorldEditText;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SideEffectBox extends PaginationBox {

    private static final LazyReference<List<SideEffect>> SIDE_EFFECTS = LazyReference.from(() ->
        WorldEdit.getInstance().getPlatformManager().getSupportedSideEffects()
            .stream()
            .filter(SideEffect::isExposed)
            .sorted(Comparator.comparing(effect ->
                WorldEditText.reduceToText(
                    Component.translatable(effect.getDisplayName()),
                    Locale.US
                )
            ))
            .toList()
    );

    private final SideEffectSet sideEffectSet;

    private static List<SideEffect> getSideEffects() {
        return SIDE_EFFECTS.getValue();
    }

    public SideEffectBox(SideEffectSet sideEffectSet) {
        super("Side Effects");

        this.sideEffectSet = sideEffectSet;
    }

    private static final SideEffect.State[] SHOWN_VALUES = {SideEffect.State.OFF, SideEffect.State.ON};

    @Override
    public Component component(int number) {
        SideEffect effect = getSideEffects().get(number);
        SideEffect.State state = this.sideEffectSet.getState(effect);

        TextComponent.Builder builder = Component.text();
        builder.append(Component.translatable(effect.getDisplayName(), NamedTextColor.YELLOW)
                .hoverEvent(HoverEvent.showText(Component.translatable(effect.getDescription()))));
        for (SideEffect.State uiState : SHOWN_VALUES) {
            builder.append(Component.space());
            builder.append(Component.translatable(uiState.getDisplayName(), uiState == state ? NamedTextColor.WHITE : NamedTextColor.GRAY)
                    .clickEvent(ClickEvent.runCommand("//perf -h " + effect.name().toLowerCase(Locale.US) + " " + uiState.name().toLowerCase(Locale.US)))
                    .hoverEvent(HoverEvent.showText(uiState == state
                            ? Component.translatable("worldedit.sideeffect.box.current")
                            : Component.translatable("worldedit.sideeffect.box.change-to", Component.translatable(uiState.getDisplayName()))
                    ))
            );
        }

        return builder.build();
    }

    @Override
    public int getComponentsSize() {
        return getSideEffects().size();
    }
}
