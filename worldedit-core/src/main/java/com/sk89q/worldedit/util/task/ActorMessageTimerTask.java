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

package com.sk89q.worldedit.util.task;

import com.sk89q.worldedit.extension.platform.Actor;

import javax.annotation.Nullable;
import java.util.TimerTask;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A timer task to inform an actor.
 */
class ActorMessageTimerTask extends TimerTask {

    @Nullable
    private final Actor actor;
    private final String message;

    /**
     * Create a new instance.
     *
     * @param actor an actor, which can be null to have this instance do nothing
     * @param message the message
     */
    ActorMessageTimerTask(@Nullable Actor actor, String message) {
        checkNotNull(message);
        this.actor = actor;
        this.message = message;
    }

    @Override
    public void run() {
        if (actor != null) {
            actor.print(message);
        }
    }

}
