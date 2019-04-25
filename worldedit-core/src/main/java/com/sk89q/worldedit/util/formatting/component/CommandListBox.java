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

import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;

public class CommandListBox extends MessageBox {

    private boolean first = true;

    /**
     * Create a new box.
     *
     * @param title the title
     */
    public CommandListBox(String title) {
        super(title, new TextComponentProducer());
    }

    public CommandListBox appendCommand(String alias, String description) {
        return appendCommand(alias, description, null);
    }

    public CommandListBox appendCommand(String alias, String description, String insertion) {
        if (!first) {
            getContents().newline();
        }
        TextComponent commandName = TextComponent.of(alias, TextColor.GOLD);
        if (insertion != null) {
            commandName = commandName
                    .clickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, insertion))
                    .hoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to select")));
        }
        getContents().append(commandName.append(TextComponent.of(": ")));
        getContents().append(description);
        first = false;
        return this;
    }

}
