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

package com.sk89q.worldedit.cli;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.FileDialogUtil;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.serializer.plain.PlainComponentSerializer;
import org.slf4j.Logger;

import java.io.File;
import java.util.UUID;

public class CLICommandSender implements Actor {

    /**
     * One time generated ID.
     */
    private static final UUID DEFAULT_ID = UUID.fromString("a233eb4b-4cab-42cd-9fd9-7e7b9a3f74be");

    private final CLIWorldEdit app;
    private final Logger sender;

    public CLICommandSender(CLIWorldEdit app, Logger sender) {
        checkNotNull(app);
        checkNotNull(sender);

        this.app = app;
        this.sender = sender;
    }

    @Override
    public UUID getUniqueId() {
        return DEFAULT_ID;
    }

    @Override
    public String getName() {
        return "Console";
    }

    @Override
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            sender.info(part);
        }
    }

    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RESET = "\u001B[0m";

    @Override
    public void print(String msg) {
        for (String part : msg.split("\n")) {
            sender.info(ANSI_PURPLE + part + ANSI_RESET);
        }
    }

    @Override
    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            sender.debug(ANSI_GREEN + part + ANSI_RESET);
        }
    }

    @Override
    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            sender.error(ANSI_RED + part + ANSI_RESET);
        }
    }

    @Override
    public void print(Component component) {
        print(PlainComponentSerializer.INSTANCE.serialize(WorldEditText.format(component)));
    }

    @Override
    public boolean canDestroyBedrock() {
        return true;
    }

    @Override
    public String[] getGroups() {
        return new String[0];
    }

    @Override
    public boolean hasPermission(String perm) {
        return true;
    }

    @Override
    public void checkPermission(String permission) throws AuthorizationException {
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public File openFileOpenDialog(String[] extensions) {
        return FileDialogUtil.showOpenDialog(extensions);
    }

    @Override
    public File openFileSaveDialog(String[] extensions) {
        return FileDialogUtil.showSaveDialog(extensions);
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
    }

    @Override
    public SessionKey getSessionKey() {
        return new SessionKey() {
            @Override
            public String getName() {
                return "Console";
            }

            @Override
            public boolean isActive() {
                return true;
            }

            @Override
            public boolean isPersistent() {
                return true;
            }

            @Override
            public UUID getUniqueId() {
                return DEFAULT_ID;
            }
        };
    }
}
