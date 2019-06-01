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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.util.formatting.text.Component;

import javax.annotation.Nullable;
import java.util.concurrent.Callable;

/**
 * For internal WorldEdit use only.
 */
public final class WorldEditAsyncCommandBuilder {
    private WorldEditAsyncCommandBuilder() {
    }

    public static void createAndSendMessage(Actor actor, Callable<Component> task, @Nullable String desc) {
        final AsyncCommandBuilder<Component> builder = AsyncCommandBuilder.wrap(task, actor);
        if (desc != null) {
            builder.sendMessageAfterDelay(desc);
        }
        builder
                .onSuccess((String) null, actor::print)
                .onFailure((String) null, WorldEdit.getInstance().getPlatformManager().getPlatformCommandManager().getExceptionConverter())
                .buildAndExec(WorldEdit.getInstance().getExecutorService());
    }
}
