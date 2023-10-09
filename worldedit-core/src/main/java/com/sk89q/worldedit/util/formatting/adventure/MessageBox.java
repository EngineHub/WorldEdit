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

import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.sk89q.worldedit.util.adventure.text.Component;
import com.sk89q.worldedit.util.adventure.text.TextComponent;
import com.sk89q.worldedit.util.adventure.text.format.NamedTextColor;
import com.sk89q.worldedit.util.adventure.text.format.TextColor;
import com.sk89q.worldedit.util.adventure.text.format.TextDecoration;
import com.sk89q.worldedit.util.adventure.text.serializer.plain.PlainTextComponentSerializer;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Makes for a box with a border above and below.
 */
public class MessageBox {

    private static final int GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH = 47;

    private final TextComponent.Builder builder = Component.text();
    private final TextColor borderColor;

    public MessageBox(String title, Component contents) {
        this(title, contents, NamedTextColor.YELLOW);
    }

    public MessageBox(String title, Component contents, NamedTextColor borderColor) {
        checkNotNull(title);
        this.borderColor = borderColor;

        this.builder.append(centerAndBorder(Component.text(title))).append(Component.newline());
        this.builder.append(contents);
    }

    protected Component centerAndBorder(TextComponent text) {
        TextComponent.Builder line = Component.text();
        int leftOver = GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH - getLength(text);
        int side = (int) Math.floor(leftOver / 2.0);
        if (side > 0) {
            if (side > 1) {
                line.append(createBorder(side - 1));
            }
            line.append(Component.space());
        }
        line.append(text);
        if (side > 0) {
            line.append(Component.space());
            if (side > 1) {
                line.append(createBorder(side - 1));
            }
        }
        return line.build();
    }

    private static int getLength(TextComponent text) {
        return PlainTextComponentSerializer.plainText().serialize(text).length();
    }

    private TextComponent createBorder(int count) {
        return Component.text(Strings.repeat("-", count),
                borderColor, Sets.newHashSet(TextDecoration.STRIKETHROUGH));
    }

    public Component build() {
        return builder.build();
    }

    public TextComponent.Builder builder() {
        return builder;
    }
}
