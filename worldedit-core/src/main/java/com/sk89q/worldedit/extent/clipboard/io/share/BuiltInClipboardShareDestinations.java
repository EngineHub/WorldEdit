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
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.paste.EngineHubPaste;
import com.sk89q.worldedit.util.paste.PasteMetadata;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.function.Consumer;

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
        public Consumer<Actor> share(ClipboardShareMetadata metadata, ShareOutputProvider serializer) throws Exception {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            serializer.writeTo(outputStream);

            PasteMetadata pasteMetadata = new PasteMetadata();
            pasteMetadata.author = metadata.author();
            pasteMetadata.extension = "schem";
            pasteMetadata.name = metadata.name();
            EngineHubPaste pasteService = new EngineHubPaste();

            URL url = pasteService.paste(new String(Base64.getEncoder().encode(outputStream.toByteArray()), StandardCharsets.UTF_8), pasteMetadata).call();
            String urlString = url.toExternalForm() + ".schem";
            return actor -> actor.printInfo(TextComponent.of(urlString).clickEvent(ClickEvent.openUrl(urlString)));
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
