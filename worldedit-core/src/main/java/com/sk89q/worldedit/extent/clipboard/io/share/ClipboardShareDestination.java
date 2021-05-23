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

package com.sk89q.worldedit.extent.clipboard.io.share;

import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;

import java.util.Set;
import java.util.function.Consumer;

public interface ClipboardShareDestination {

    /**
     * Gets the name of this share destination.
     *
     * @return The name
     */
    String getName();

    /**
     * Get a set of aliases.
     *
     * @return a set of aliases
     */
    Set<String> getAliases();

    /**
     * Share a clipboard output stream and return a URL.
     *
     * <p>
     * The serialized schematic can be retrieved by providing an {@link java.io.OutputStream} to {@code serializer}.
     * </p>
     *
     * @param metadata The clipboard metadata
     * @param serializer A function taking the {@link java.io.OutputStream}
     * @return A consumer to provide the actor with the share results
     * @throws Exception if it failed to share
     */
    Consumer<Actor> share(ClipboardShareMetadata metadata, ShareOutputProvider serializer) throws Exception;

    /**
     * Gets the default clipboard format for this share destination.
     *
     * @return The default format
     */
    ClipboardFormat getDefaultFormat();

    /**
     * Gets whether the share destination supports the given format.
     *
     * @param format The format
     * @return If it's supported
     */
    boolean supportsFormat(ClipboardFormat format);
}
