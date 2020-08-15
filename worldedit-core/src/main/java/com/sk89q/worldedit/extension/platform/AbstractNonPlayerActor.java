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

import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.util.collection.SetWithDefault;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.io.file.FileSelectionAbortedException;
import com.sk89q.worldedit.util.io.file.FileType;
import com.sk89q.worldedit.util.io.file.PathRequestType;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public abstract class AbstractNonPlayerActor implements Actor {

    @Override
    public boolean canDestroyBedrock() {
        return true;
    }

    @Override
    public boolean isPlayer() {
        return false;
    }

    @Override
    public CompletableFuture<Path> requestPath(PathRequestType type, SetWithDefault<FileType> fileTypes) {
        CompletableFuture<Path> cf = new CompletableFuture<>();
        cf.completeExceptionally(new FileSelectionAbortedException(TranslatableComponent.of(
            "worldedit.platform.no-file-dialog"
        )));
        return cf;
    }

    @Override
    public void dispatchCUIEvent(CUIEvent event) {
    }
}
