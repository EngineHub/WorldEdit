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

package com.sk89q.worldedit.sponge;

import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.serializer.gson.GsonComponentSerializer;

import java.util.Locale;

public class SpongeTextAdapter {

    public static net.kyori.adventure.text.Component convert(Component component, Locale locale) {
        component = WorldEditText.format(component, locale);
        return net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson()
            .deserialize(GsonComponentSerializer.INSTANCE.serialize(component));
    }

    public static Component convert(net.kyori.adventure.text.Component component) {
        return GsonComponentSerializer.INSTANCE.deserialize(
            net.kyori.adventure.text.serializer.gson.GsonComponentSerializer.gson()
                .serialize(component)
        );
    }

    private SpongeTextAdapter() {
    }
}
