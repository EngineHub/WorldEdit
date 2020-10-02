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

package com.sk89q.worldedit.util.asset;

import com.sk89q.worldedit.command.util.AsyncCommandBuilder;

import java.util.concurrent.Callable;

/**
 * A Callable to load an asset by name.
 *
 * <p>
 * This is intended to be used with {@link AsyncCommandBuilder} for loading assets in commands.
 * </p>
 *
 * @param <T> The asset type
 */
public class AssetLoadTask<T> implements Callable<T> {

    private final String assetName;
    private final AssetLoader<T> loader;

    /**
     * Creates an asset load task with the given loader and asset name.
     *
     * @param loader The asset loader
     * @param assetName The asset name
     */
    public AssetLoadTask(AssetLoader<T> loader, String assetName) {
        this.loader = loader;
        this.assetName = assetName;
    }

    @Override
    public T call() {
        return this.loader.getAsset(this.assetName);
    }
}
