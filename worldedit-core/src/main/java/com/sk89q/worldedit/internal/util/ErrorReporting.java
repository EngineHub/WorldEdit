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

package com.sk89q.worldedit.internal.util;

import com.google.common.base.Throwables;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;

/**
 * Simple class for handling error reporting to users.
 */
public class ErrorReporting {
    public static void trigger(Actor actor, Throwable error) {
        actor.printError(TranslatableComponent.of("worldedit.command.error.report"));
        actor.print(
            TextComponent.builder(error.getClass().getName() + ": " + error.getMessage())
                .hoverEvent(HoverEvent.showText(TextComponent.of(Throwables.getStackTraceAsString(error))))
                .build()
        );
    }

    private ErrorReporting() {
    }
}
