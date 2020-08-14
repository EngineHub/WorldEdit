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

package com.sk89q.worldedit.extension.factory.parser.mask;

import com.google.common.base.Splitter;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.BiomeMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.biome.BiomeType;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BiomeMaskParser extends InputParser<Mask> {

    public BiomeMaskParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public Stream<String> getSuggestions(String input) {
        if (input.isEmpty()) {
            return Stream.of("$");
        }
        if (input.charAt(0) == '$') {
            input = input.substring(1);
            final int lastTermIdx = input.lastIndexOf(',');
            if (lastTermIdx <= 0) {
                return SuggestionHelper.getNamespacedRegistrySuggestions(BiomeType.REGISTRY, input).map(s -> "$" + s);
            }
            String prev = input.substring(0, lastTermIdx) + ",";
            Set<String> prevBiomes = Arrays.stream(prev.split(",", 0)).collect(Collectors.toSet());
            String search = input.substring(lastTermIdx + 1);
            return SuggestionHelper.getNamespacedRegistrySuggestions(BiomeType.REGISTRY, search)
                    .filter(s -> !prevBiomes.contains(s)).map(s -> "$" + prev + s);
        }
        return Stream.empty();
    }

    @Override
    public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
        if (!input.startsWith("$")) {
            return null;
        }

        Set<BiomeType> biomes = new HashSet<>();
        for (String biomeName : Splitter.on(",").split(input.substring(1))) {
            BiomeType biome = BiomeType.REGISTRY.get(biomeName);
            if (biome == null) {
                throw new NoMatchException(TranslatableComponent.of("worldedit.error.unknown-biome", TextComponent.of(biomeName)));
            }
            biomes.add(biome);
        }

        return new BiomeMask(context.requireExtent(), biomes);
    }
}
