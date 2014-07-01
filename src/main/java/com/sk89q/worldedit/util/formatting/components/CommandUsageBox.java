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

package com.sk89q.worldedit.util.formatting.components;

import com.sk89q.worldedit.util.command.Description;
import com.sk89q.worldedit.util.formatting.Style;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A box to describe usage of a command.
 */
public class CommandUsageBox extends MessageBox {

    /**
     * Create a new box.
     *
     * @param description the command to describe
     * @param title the title
     */
    public CommandUsageBox(Description description, String title) {
        super(title);
        checkNotNull(description);
        attachCommandUsage(description);
    }

    /**
     * Create a new box.
     *
     * @param description the command to describe
     */
    public CommandUsageBox(Description description) {
        super("Usage Help");
        checkNotNull(description);
        attachCommandUsage(description);
    }

    private void attachCommandUsage(Description description) {
        if (description.getUsage() != null) {
            getContents().append(new Label().append("Usage: "));
            getContents().append(description.getUsage());
        } else {
            getContents().append(new Subtle().append("Usage information is not available."));
        }

        getContents().newLine();

        if (description.getHelp() != null) {
            getContents().createFragment(Style.YELLOW_DARK).append(description.getHelp());
        } else if (description.getShortDescription() != null) {
            getContents().createFragment(Style.YELLOW_DARK).append(description.getShortDescription());
        } else {
            getContents().append(new Subtle().append("No further help is available."));
        }
    }

}
