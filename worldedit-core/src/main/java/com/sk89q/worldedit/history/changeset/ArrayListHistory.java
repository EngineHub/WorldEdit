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

package com.sk89q.worldedit.history.changeset;

import com.google.common.collect.Lists;
import com.sk89q.worldedit.history.change.Change;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Stores all {@link Change}s in an {@link ArrayList}.
 */
public class ArrayListHistory implements ChangeSet {

    private final List<Change> changes = new ArrayList<>();

    private boolean recordChanges = true;

    @Override
    public void add(Change change) {
        checkNotNull(change);
        if (recordChanges) {
            changes.add(change);
        }
    }

    @Override
    public boolean isRecordingChanges() {
        return recordChanges;
    }

    @Override
    public void setRecordChanges(boolean recordChanges) {
        this.recordChanges = recordChanges;
    }

    @Override
    public Iterator<Change> backwardIterator() {
        return Lists.reverse(changes).iterator();
    }

    @Override
    public Iterator<Change> forwardIterator() {
        return changes.iterator();
    }

    @Override
    public int size() {
        return changes.size();
    }

}
