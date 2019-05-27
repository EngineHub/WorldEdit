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
