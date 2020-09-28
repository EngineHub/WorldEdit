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

package com.sk89q.worldedit.extension.platform;

import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.format.NamedTextColor;

import java.util.function.Consumer;

import static com.sk89q.worldedit.util.formatting.text.Component.text;

/**
 * Base class for implementing an actor. Provides reasonable defaults.
 */
public abstract class AbstractActor implements Actor {

    private final Consumer<Component> sendMessage;

    protected AbstractActor(Consumer<Component> sendMessage) {
        this.sendMessage = sendMessage;
    }

    @Override
    @Deprecated
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            print(text(part));
        }
    }

    @Override
    @Deprecated
    public void print(String msg) {
        for (String part : msg.split("\n")) {
            print(text(part, NamedTextColor.LIGHT_PURPLE));
        }
    }

    @Override
    @Deprecated
    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            print(text(part, NamedTextColor.GRAY));
        }
    }

    @Override
    @Deprecated
    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            print(text(part, NamedTextColor.RED));
        }
    }

    @Override
    public void print(Component component) {
        sendMessage.accept(WorldEditText.format(component, getLocale()));
    }

}
