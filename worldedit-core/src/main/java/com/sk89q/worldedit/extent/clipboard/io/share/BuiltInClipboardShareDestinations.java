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

import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.ClipboardTransformFuser;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.io.Closer;
import com.sk89q.worldedit.util.paste.EngineHubPaste;
import com.sk89q.worldedit.util.paste.PasteMetadata;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * A collection of natively supported clipboard share destinations.
 */
public enum BuiltInClipboardShareDestinations implements ClipboardShareDestination {

    /**
     * The EngineHub pastebin service, at https://paste.enginehub.org/
     */
    ENGINEHUB_PASTEBIN("enginehub_paste", "ehpaste") {
        @Override
        public String getName() {
            return "EngineHub Paste";
        }

        @Override
        public URL share(ClipboardHolder holder, ClipboardFormat format, PasteMetadata metadata) throws Exception {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Clipboard clipboard = holder.getClipboard();
            Transform transform = holder.getTransform();
            Clipboard target;

            // If we have a transform, bake it into the copy
            if (transform.isIdentity()) {
                target = clipboard;
            } else {
                ClipboardTransformFuser result = ClipboardTransformFuser.transform(clipboard, transform);
                target = new BlockArrayClipboard(result.getTransformedRegion());
                target.setOrigin(clipboard.getOrigin());
                Operations.completeLegacy(result.copyTo(target));
            }

            try (Closer closer = Closer.create()) {
                OutputStream stream = closer.register(outputStream);
                BufferedOutputStream bos = closer.register(new BufferedOutputStream(stream));
                ClipboardWriter writer = closer.register(format.getWriter(bos));
                writer.write(target);
            }

            EngineHubPaste pasteService = new EngineHubPaste();
            return pasteService.paste(new String(Base64.getEncoder().encode(outputStream.toByteArray()), StandardCharsets.UTF_8), metadata).call();
        }

        @Override
        public ClipboardFormat getDefaultFormat() {
            return BuiltInClipboardFormat.SPONGE_SCHEMATIC;
        }

        @Override
        public boolean supportsFormat(ClipboardFormat format) {
            return format == getDefaultFormat();
        }
    };

    private final ImmutableSet<String> aliases;

    BuiltInClipboardShareDestinations(String... aliases) {
        this.aliases = ImmutableSet.copyOf(aliases);
    }

    @Override
    public ImmutableSet<String> getAliases() {
        return this.aliases;
    }

    @Override
    public String getName() {
        return name();
    }
}
