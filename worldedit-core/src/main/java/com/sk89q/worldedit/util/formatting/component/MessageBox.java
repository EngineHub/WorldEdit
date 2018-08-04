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

import com.sk89q.worldedit.util.formatting.ColorCodeBuilder;
import com.sk89q.worldedit.util.formatting.Style;
import com.sk89q.worldedit.util.formatting.StyledFragment;

/**
 * Makes for a box with a border above and below.
 */
public class MessageBox extends StyledFragment {

    private final StyledFragment contents = new StyledFragment();

    /**
     * Create a new box.
     */
    public MessageBox(String title) {
        checkNotNull(title);

        int leftOver = ColorCodeBuilder.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH - title.length() - 2;
        int leftSide = (int) Math.floor(leftOver * 1.0/3);
        int rightSide = (int) Math.floor(leftOver * 2.0/3);
        if (leftSide > 0) {
            createFragment(Style.YELLOW).append(createBorder(leftSide));
        }
        append(" ");
        append(title);
        append(" ");
        if (rightSide > 0) {
            createFragment(Style.YELLOW).append(createBorder(rightSide));
        }
        newLine();
        append(contents);
    }

    private String createBorder(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            builder.append("-");
        }
        return builder.toString();
    }

    /**
     * Get the internal contents.
     * 
     * @return the contents
     */
    public StyledFragment getContents() {
        return contents;
    }

}
