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

package com.sk89q.worldedit.util.formatting.component;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.util.adventure.text.Component;
import com.sk89q.worldedit.util.adventure.text.TextComponent;
import com.sk89q.worldedit.util.adventure.text.event.ClickEvent;
import com.sk89q.worldedit.util.adventure.text.event.HoverEvent;
import com.sk89q.worldedit.util.adventure.text.format.NamedTextColor;
import com.sk89q.worldedit.util.formatting.LegacyTextHelper;

import java.util.List;

public class CommandListBox extends PaginationBox {

    private final List<CommandEntry> commands = Lists.newArrayList();
    private final String helpCommand;
    private boolean hideHelp;

    /**
     * Create a new box.
     *
     * @param title the title
     */
    public CommandListBox(String title, String pageCommand, String helpCommand) {
        super(title, pageCommand);
        this.helpCommand = helpCommand;
    }

    @Override
    public Component component(int number) {
        return commands.get(number).createComponent(hideHelp);
    }

    @Override
    public int getComponentsSize() {
        return commands.size();
    }

    public void appendCommand(String alias, Component description) {
        appendCommand(alias, description, null);
    }

    @Deprecated
    public void appendCommand(String alias, String description, String insertion) {
        appendCommand(alias, Component.text(description), insertion);
    }

    @Deprecated
    public void appendCommand(String alias, com.sk89q.worldedit.util.formatting.text.Component description, String insertion) {
        appendCommand(alias, LegacyTextHelper.adapt(description), insertion);
    }

    public void appendCommand(String alias, Component description, String insertion) {
        commands.add(new CommandEntry(alias, description, insertion));
    }

    public boolean isHidingHelp() {
        return hideHelp;
    }

    public void setHidingHelp(boolean hideHelp) {
        this.hideHelp = hideHelp;
    }

    private class CommandEntry {
        private final String alias;
        private final Component description;
        private final String insertion;

        CommandEntry(String alias, Component description, String insertion) {
            this.alias = alias;
            this.description = description;
            this.insertion = insertion;
        }

        Component createComponent(boolean hideHelp) {
            TextComponent.Builder line = Component.text();
            if (!hideHelp) {
                line.append(Component.text("? ")
                        .clickEvent(ClickEvent.runCommand(CommandListBox.this.helpCommand + " " + insertion))
                        .hoverEvent(HoverEvent.showText(Component.text("Additional Help"))));
            }
            TextComponent command = Component.text(alias, NamedTextColor.GOLD);
            if (insertion == null) {
                line.append(command);
            } else {
                line.append(command
                        .clickEvent(ClickEvent.suggestCommand(insertion))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to select"))));
            }
            return line.append(Component.text(": ")).append(description).build();
        }
    }
}
