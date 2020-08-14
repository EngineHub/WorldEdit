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

package com.sk89q.worldedit.event.platform;

import com.sk89q.worldedit.event.Event;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.util.Substring;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Posted when suggestions for auto-completion are requested for command input.
 */
public class CommandSuggestionEvent extends Event {

    private final Actor actor;
    private final String arguments;
    private List<Substring> suggestions = Collections.emptyList();

    /**
     * Create a new instance.
     *
     * @param actor the player
     * @param arguments the arguments
     */
    public CommandSuggestionEvent(Actor actor, String arguments) {
        checkNotNull(actor);
        checkNotNull(arguments);

        this.actor = actor;
        this.arguments = arguments;
    }

    /**
     * Get the actor that issued the command.
     *
     * @return the actor that issued the command
     */
    public Actor getActor() {
        return actor;
    }

    /**
     * Get the arguments.
     *
     * @return the arguments
     */
    public String getArguments() {
        return arguments;
    }

    /**
     * Get the list of suggestions that are to be presented.
     *
     * <p>
     *     Each Substring holds the replacement as the substring,
     *     and the replacement range as the original substring range.
     * </p>
     *
     * @return the list of suggestions
     */
    public List<Substring> getSuggestions() {
        return suggestions;
    }

    /**
     * Set the list of suggestions that are to be presented.
     *
     * @param suggestions the list of suggestions
     */
    public void setSuggestions(List<Substring> suggestions) {
        checkNotNull(suggestions);
        this.suggestions = suggestions;
    }

}
