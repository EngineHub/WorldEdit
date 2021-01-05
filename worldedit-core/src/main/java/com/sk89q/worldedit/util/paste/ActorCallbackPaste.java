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

package com.sk89q.worldedit.util.paste;

import com.sk89q.worldedit.command.util.AsyncCommandBuilder;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.task.Supervisor;

import java.net.URL;
import java.util.concurrent.Callable;

public final class ActorCallbackPaste {

    private static final Paster paster = new EngineHubPaste();

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
     * @deprecated Use the Component-based version
     */
    @Deprecated
    public static void pastebin(Supervisor supervisor, final Actor sender, String content, final String successMessage) {
        Callable<URL> task = paster.paste(content);

        AsyncCommandBuilder.wrap(task, sender)
                .registerWithSupervisor(supervisor, "Submitting content to a pastebin service.")
                .sendMessageAfterDelay("(Please wait... sending output to pastebin...)")
                .onSuccess((String) null, url -> sender.print(String.format(successMessage, url)))
                .onFailure("Failed to submit paste", null)
                .buildAndExec(Pasters.getExecutor());
    }

    /**
     * Submit data to a pastebin service and inform the sender of
     * success or failure.
     *
     * @param supervisor The supervisor instance
     * @param sender The sender
     * @param content The content
     * @param successMessage The message builder, given the URL as an arg
     */
    public static void pastebin(Supervisor supervisor, final Actor sender, String content, final TranslatableComponent.Builder successMessage) {
        Callable<URL> task = paster.paste(content);

        AsyncCommandBuilder.wrap(task, sender)
                .registerWithSupervisor(supervisor, "Submitting content to a pastebin service.")
                .setDelayMessage(TranslatableComponent.of("worldedit.pastebin.uploading"))
                .onSuccess((String) null, url -> sender.printInfo(successMessage.args(TextComponent.of(url.toString())).build()))
                .onFailure("Failed to submit paste", null)
                .buildAndExec(Pasters.getExecutor());
    }


    /**
     * Submit data to a pastebin service and inform the sender of
     * success or failure.
     *
     * @param supervisor The supervisor instance
     * @param sender The sender
     * @param content The content
     * @param pasteMetadata The paste metadata
     * @param successMessage The message builder, given the URL as an arg
     */
    public static void pastebin(Supervisor supervisor, final Actor sender, String content, PasteMetadata pasteMetadata, final TranslatableComponent.Builder successMessage) {
        Callable<URL> task = paster.paste(content, pasteMetadata);

        AsyncCommandBuilder.wrap(task, sender)
            .registerWithSupervisor(supervisor, "Submitting content to a pastebin service.")
            .setDelayMessage(TranslatableComponent.of("worldedit.pastebin.uploading"))
            .onSuccess((String) null, url -> sender.printInfo(successMessage.args(TextComponent.of(url.toString())).build()))
            .onFailure("Failed to submit paste", null)
            .buildAndExec(Pasters.getExecutor());
    }

}
