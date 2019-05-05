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

public class TextComponentProducer {

    private TextComponent.Builder builder = TextComponent.builder().content("");

    public TextComponent.Builder getBuilder() {
        return builder;
    }

    /**
     * Adds a component as a child to this Producer.
     *
     * @param component The component
     * @return The producer, for chaining
     */
    public TextComponentProducer append(Component component) {
        getBuilder().append(component);
        return this;
    }

    /**
     * Adds a string as a child to this Producer.
     *
     * @param string The text
     * @return The producer, for chaining
     */
    public TextComponentProducer append(String string) {
        getBuilder().append(TextComponent.of(string));
        return this;
    }

    /**
     * Adds a newline as a child to this Producer.
     *
     * @return The producer, for chaining
     */
    public TextComponentProducer newline() {
        getBuilder().append(TextComponent.newline());
        return this;
    }

    /**
     * Create a TextComponent from this producer.
     *
     * @return The component
     */
    public TextComponent create() {
        return builder.build();
    }

    /**
     * Reset the producer to a clean slate.
     *
     * @return The producer, for chaining
     */
    public TextComponentProducer reset() {
        builder = TextComponent.builder().content("");
        return this;
    }
}
