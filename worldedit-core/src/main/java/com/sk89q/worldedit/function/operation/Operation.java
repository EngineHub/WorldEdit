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

package com.sk89q.worldedit.function.operation;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An task that may be split into multiple steps to be run sequentially
 * immediately or at a varying or fixed interval. Operations should attempt
 * to break apart tasks into smaller tasks that can be completed in quicker
 * successions.
 */
public interface Operation {

    /**
     * Complete the next step. If this method returns true, then the method may
     * be called again in the future, or possibly never. If this method
     * returns false, then this method should not be called again.
     *
     * @param run describes information about the current run
     * @return another operation to run that operation again, or null to stop
     * @throws WorldEditException an error
     */
    Operation resume(RunContext run) throws WorldEditException;

    /**
     * Abort the current task. After the this method is called,
     * {@link #resume(RunContext)} should not be called at any point in the
     * future. This method should not be called after successful completion of
     * the operation. This method must be called if the operation is
     * interrupted before completion.
     */
    void cancel();

    /**
     * Add messages to the provided list that describe the current status
     * of the operation.
     *
     * @param messages The list to add messages to
     * @deprecated Will be removed in WorldEdit 8.0 - use the Component variant
     */
    @Deprecated
    default void addStatusMessages(List<String> messages) {
    }

    /**
     * This is an internal field, and should not be touched.
     */
    Set<String> warnedDeprecatedClasses = new HashSet<>();

    /**
     * Gets an iterable of messages that describe the current status of the
     * operation.
     *
     * @return The status messages
     */
    default Iterable<Component> getStatusMessages() {
        // TODO Remove legacy code WorldEdit 8.0.0
        List<String> oldMessages = new ArrayList<>();
        addStatusMessages(oldMessages);
        if (oldMessages.size() > 0) {
            String className = getClass().getName();
            if (!warnedDeprecatedClasses.contains(className)) {
                WorldEdit.logger.warn("An operation is using the old status message API. This will be removed in WorldEdit 8. Class: " + className);
                warnedDeprecatedClasses.add(className);
            }
        }
        return oldMessages.stream().map(TextComponent::of).collect(Collectors.toList());
    }
}
