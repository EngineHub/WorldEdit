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

package com.sk89q.worldedit.extension.factory;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.factory.parser.pattern.BlockCategoryPatternParser;
import com.sk89q.worldedit.extension.factory.parser.pattern.ClipboardPatternParser;
import com.sk89q.worldedit.extension.factory.parser.pattern.RandomPatternParser;
import com.sk89q.worldedit.extension.factory.parser.pattern.RandomStatePatternParser;
import com.sk89q.worldedit.extension.factory.parser.pattern.SingleBlockPatternParser;
import com.sk89q.worldedit.extension.factory.parser.pattern.TypeOrStateApplyingPatternParser;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.internal.registry.AbstractFactory;

/**
 * A registry of known {@link Pattern}s. Provides methods to instantiate
 * new patterns from input.
 *
 * <p>Instances of this class can be taken from
 * {@link WorldEdit#getPatternFactory()}.</p>
 */
public final class PatternFactory extends AbstractFactory<Pattern> {

    /**
     * Create a new instance.
     *
     * @param worldEdit the WorldEdit instance
     */
    public PatternFactory(WorldEdit worldEdit) {
        super(worldEdit, new SingleBlockPatternParser(worldEdit));

        // split and parse each sub-pattern
        register(new RandomPatternParser(worldEdit));

        // individual patterns
        register(new ClipboardPatternParser(worldEdit));
        register(new TypeOrStateApplyingPatternParser(worldEdit));
        register(new RandomStatePatternParser(worldEdit));
        register(new BlockCategoryPatternParser(worldEdit));
    }

}
