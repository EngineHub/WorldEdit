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

package com.sk89q.worldedit.util.formatting.component;

import com.google.common.collect.Multimap;
import com.google.common.io.Files;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;

import java.io.File;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

public class SchematicPaginationBox extends PaginationBox {
    private final String prefix;
    private final File[] files;

    public SchematicPaginationBox(String rootDir, File[] files, String pageCommand) {
        super("Available schematics", pageCommand);
        this.prefix = rootDir == null ? "" : rootDir;
        this.files = files;
    }

    @Override
    public Component getComponent(int number) {
        checkArgument(number < files.length && number >= 0);
        File file = files[number];
        Multimap<String, ClipboardFormat> exts = ClipboardFormats.getFileExtensionMap();
        String format = exts.get(Files.getFileExtension(file.getName()))
                .stream().findFirst().map(ClipboardFormat::getName).orElse("Unknown");
        boolean inRoot = file.getParentFile().getName().equals(prefix);

        String path = inRoot ? file.getName() : file.getPath().split(Pattern.quote(prefix + File.separator))[1];

        return TextComponent.builder()
                .content("")
                .append(TextComponent.of("[L]")
                        .color(TextColor.GOLD)
                        .clickEvent(ClickEvent.of(ClickEvent.Action.RUN_COMMAND, "/schem load " + path))
                        .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to load"))))
                .append(TextComponent.space())
                .append(TextComponent.of(path)
                        .color(TextColor.DARK_GREEN)
                        .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of(format))))
                .build();
    }

    @Override
    public int getComponentsSize() {
        return files.length;
    }
}
