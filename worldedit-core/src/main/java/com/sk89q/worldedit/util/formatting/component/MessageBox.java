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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;

/**
 * Makes for a box with a border above and below.
 */
public class MessageBox extends TextComponentProducer {

    private static final int GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH = 47;

    private final TextComponentProducer contents;

    /**
     * Create a new box.
     */
    public MessageBox(String title, TextComponentProducer contents) {
        checkNotNull(title);

        int leftOver = GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH - title.length() - 2;
        int leftSide = (int) Math.floor(leftOver * 1.0/3);
        int rightSide = (int) Math.floor(leftOver * 2.0/3);
        if (leftSide > 0) {
            append(TextComponent.of(createBorder(leftSide), TextColor.YELLOW));
        }
        append(Component.space());
        append(title);
        append(Component.space());
        if (rightSide > 0) {
            append(TextComponent.of(createBorder(rightSide), TextColor.YELLOW));
        }
        newline();
        this.contents = contents;
    }

    private String createBorder(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append("-");
        }
        return builder.toString();
    }

    /**
     * Gets the message box contents producer.
     *
     * @return The contents producer
     */
    public TextComponentProducer getContents() {
        return contents;
    }

    @Override
    public TextComponent create() {
        append(contents.create());
        return super.create();
    }
}
