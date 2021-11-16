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
