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

package com.sk89q.worldedit.reorder.arrange;

import com.sk89q.worldedit.action.WorldAction;

import java.util.List;
import java.util.function.Supplier;

public interface ArrangerContext {

    /**
     * Get the number of actions in the current group.
     *
     * @return the number of actions
     */
    int getActionCount();

    /**
     * Get the action at the index within the current group.
     *
     * @param i the index
     * @return the action at that index
     */
    WorldAction getAction(int i);

    /**
     * Returns the list for writing changes.
     *
     * <p>
     * DO NOT call this for simple reads, re-groupings, etc.
     * It can have a large impact on WorldEdit's memory footprint.
     * </p>
     *
     * @return a list for adding actions
     */
    List<WorldAction> getActionWriteList();

    /**
     * Mark a range of actions as a single group for the next arranger.
     *
     * <p>
     * This will cause the next arranger to be triggered with this group of data
     * as its input for re-arranging.
     * </p>
     *
     * @param start the start of the group, inclusive
     * @param end the end of the group, exclusive
     */
    void markGroup(int start, int end);

}
