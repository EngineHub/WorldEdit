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

import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;

import java.util.List;
import java.util.Locale;

public class TextUtils {

    private TextUtils() {
    }

    /**
     * Join an array of components with a joiner component.
     *
     * @param components The components to join
     * @param joiner The joiner component
     * @return The joined component
     */
    public static Component join(List<Component> components, Component joiner) {
        TextComponent.Builder builder = TextComponent.builder();
        for (int i = 0; i < components.size(); i++) {
            builder.append(components.get(i));
            if (i < components.size() - 1) {
                builder.append(joiner);
            }
        }
        return builder.build();
    }

    /**
     * Gets a Java Locale object by the Minecraft locale tag.
     *
     * @param locale The Minecraft locale tag
     * @return A Java locale
     */
    public static Locale getLocaleByMinecraftTag(String locale) {
        return Locale.forLanguageTag(locale.replace('_', '-'));
    }
}
