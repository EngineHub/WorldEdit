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

import com.google.common.util.concurrent.FutureCallback;
import com.sk89q.worldedit.extension.platform.Actor;

import javax.annotation.Nullable;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles thrown errors from an operation and logs them.
 */
class ErrorHandlerCallback implements FutureCallback<Object> {

    private static final Logger log = Logger.getLogger(ErrorHandlerCallback.class.getCanonicalName());
    private final Actor actor;

    ErrorHandlerCallback(@Nullable Actor actor) {
        this.actor = actor;
    }

    @Override
    public void onSuccess(@Nullable Object result) {
    }

    @Override
    public void onFailure(@Nullable Throwable t) {
        if (t != null) {
            // TODO: Handle WorldEditException with friendly messages

            if (actor != null) {
                if (t instanceof CancellationException) {
                    actor.printError("Your operation was cancelled.");
                } else {
                    actor.printError("An error has occurred while executing a WorldEdit operation: " + t.getMessage() + " (see console for details)");
                }
            }

            if (!(t instanceof CancellationException)) {
                log.log(Level.SEVERE, "An error occurred while executing a WorldEdit operation", t);
            }
        } else {
            if (actor != null) {
                actor.printError("An unknown error has occurred while executing a WorldEdit operation");
            }

            log.log(Level.SEVERE, "An unknown error occurred while executing a WorldEdit operation");
        }
    }

}
