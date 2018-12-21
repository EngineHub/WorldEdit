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

package com.sk89q.worldedit.util.paste;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.sk89q.worldedit.command.util.AsyncCommandHelper;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.command.parametric.ExceptionConverter;
import com.sk89q.worldedit.util.task.Supervisor;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActorCallbackPaste {

    private static final Logger LOGGER = Logger.getLogger(ActorCallbackPaste.class.getSimpleName());

    private ActorCallbackPaste() {
    }

    /**
     * Submit data to a pastebin service and inform the sender of
     * success or failure.
     *
     * @param supervisor The supervisor instance
     * @param sender The sender
     * @param content The content
     * @param successMessage The message, formatted with {@link String#format(String, Object...)} on success
     */
    public static void pastebin(Supervisor supervisor, final Actor sender, String content, final String successMessage, final ExceptionConverter exceptionConverter) {
        ListenableFuture<URL> future = new EngineHubPaste().paste(content);

        AsyncCommandHelper.wrap(future, supervisor, sender, exceptionConverter)
                .registerWithSupervisor("Submitting content to a pastebin service...")
                .sendMessageAfterDelay("(Please wait... sending output to pastebin...)");

        Futures.addCallback(future, new FutureCallback<URL>() {
            @Override
            public void onSuccess(URL url) {
                sender.print(String.format(successMessage, url));
            }

            @Override
            public void onFailure(Throwable throwable) {
                LOGGER.log(Level.WARNING, "Failed to submit pastebin", throwable);
                sender.printError("Failed to submit to a pastebin. Please see console for the error.");
            }
        });
    }

}
