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

package com.sk89q.worldedit.command.util;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.util.concurrent.FutureCallback;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.command.parametric.ExceptionConverter;

import javax.annotation.Nullable;

public class MessageFutureCallback<V> implements FutureCallback<V> {

    private final ExceptionConverter exceptionConverter;
    private final Actor sender;
    @Nullable
    private final String success;
    @Nullable
    private final String failure;

    private MessageFutureCallback(ExceptionConverter exceptionConverter, Actor sender, @Nullable String success, @Nullable String failure) {
        this.exceptionConverter = exceptionConverter;
        this.sender = sender;
        this.success = success;
        this.failure = failure;
    }

    @Override
    public void onSuccess(@Nullable V v) {
        if (success != null) {
            sender.print(success);
        }
    }

    @Override
    public void onFailure(@Nullable Throwable throwable) {
        try {
            exceptionConverter.convert(throwable);
        } catch (CommandException e) {
            String failure = this.failure != null ? this.failure : "An error occurred";
            String message = e.getMessage() != null ? e.getMessage() : "An unknown error occurred. Please see the console!";
            sender.printError(failure + ": " + message);
        }
    }

    public static class Builder {
        private final Actor sender;
        @Nullable
        private String success;
        @Nullable
        private String failure;
        private ExceptionConverter exceptionConverter;

        public Builder(Actor sender) {
            checkNotNull(sender);

            this.sender = sender;
        }

        public Builder exceptionConverter(ExceptionConverter exceptionConverter) {
            this.exceptionConverter = exceptionConverter;
            return this;
        }

        public Builder onSuccess(@Nullable String message) {
            this.success = message;
            return this;
        }

        public Builder onFailure(@Nullable String message) {
            this.failure = message;
            return this;
        }

        public <V> MessageFutureCallback<V> build() {
            checkNotNull(exceptionConverter);
            return new MessageFutureCallback<>(exceptionConverter, sender, success, failure);
        }
    }

    public static <V> MessageFutureCallback<V> createRegionLoadCallback(ExceptionConverter exceptionConverter, Actor sender) {
        return new Builder(sender)
                .exceptionConverter(exceptionConverter)
                .onSuccess("Successfully load the region data.")
                .build();
    }

    public static <V> MessageFutureCallback<V> createRegionSaveCallback(ExceptionConverter exceptionConverter, Actor sender) {
        return new Builder(sender)
                .exceptionConverter(exceptionConverter)
                .onSuccess("Successfully saved the region data.")
                .build();
    }

}
