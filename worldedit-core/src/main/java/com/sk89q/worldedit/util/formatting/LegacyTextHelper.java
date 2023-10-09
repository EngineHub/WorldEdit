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

package com.sk89q.worldedit.util.formatting;

import com.sk89q.worldedit.util.adventure.text.Component;
import com.sk89q.worldedit.util.adventure.text.serializer.gson.GsonComponentSerializer;

/**
 * Converts adventure text components to kyori text components.
 * @deprecated
 */
@Deprecated
public class LegacyTextHelper {
    public static Component adapt(com.sk89q.worldedit.util.formatting.text.Component toAdapt) {
        return GsonComponentSerializer.gson().deserialize(
                com.sk89q.worldedit.util.formatting.text.serializer.legacy.LegacyComponentSerializer.legacy().serialize(toAdapt)
        );
    }

    public static com.sk89q.worldedit.util.formatting.text.Component adapt(Component toAdapt) {
        return com.sk89q.worldedit.util.formatting.text.serializer.legacy.LegacyComponentSerializer.legacy().deserialize(
                GsonComponentSerializer.gson().serialize(toAdapt)
        );
    }
}
