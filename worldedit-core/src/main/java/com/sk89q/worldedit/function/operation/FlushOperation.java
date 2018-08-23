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

package com.sk89q.worldedit.function.operation;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extension.platform.Actor;

import java.util.List;

public class FlushOperation implements Operation {

    private Actor actor;
    private EditSession editSession;

    public FlushOperation(Actor actor, EditSession editSession) {
        this.actor = actor;
        this.editSession = editSession;
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(actor);
        session.remember(editSession);
        editSession.flushQueue();

        WorldEdit.getInstance().flushBlockBag(actor, editSession);

        return null;
    }

    @Override
    public void cancel() {

    }

    @Override
    public void addStatusMessages(List<String> messages) {

    }
}
