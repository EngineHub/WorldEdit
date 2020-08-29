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

package com.sk89q.worldedit.command.util;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;

import java.util.concurrent.Callable;
import javax.annotation.Nullable;

/**
 * For internal WorldEdit use only.
 */
public final class WorldEditAsyncCommandBuilder {
    private WorldEditAsyncCommandBuilder() {
    }

    @Deprecated
    public static void createAndSendMessage(Actor actor, Callable<Component> task, @Nullable String desc) {
        createAndSendMessage(actor, task, desc != null ? TextComponent.of(desc) : null);
    }

    public static void createAndSendMessage(Actor actor, Callable<Component> task, @Nullable Component desc) {
        final AsyncCommandBuilder<Component> builder = AsyncCommandBuilder.wrap(task, actor);
        if (desc != null) {
            builder.setDelayMessage(desc);
        }
        builder
                .onSuccess((String) null, actor::printInfo)
                .onFailure((String) null, WorldEdit.getInstance().getPlatformManager().getPlatformCommandManager().getExceptionConverter())
                .buildAndExec(WorldEdit.getInstance().getExecutorService());
    }
}
