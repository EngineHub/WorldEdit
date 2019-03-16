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

package com.sk89q.worldedit.extension.factory.parser.mask;

import com.google.common.base.Splitter;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.function.mask.BiomeMask2D;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.session.request.RequestExtent;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.Biomes;
import com.sk89q.worldedit.world.registry.BiomeRegistry;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class BiomeMaskParser extends InputParser<Mask> {

    public BiomeMaskParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
        if (!input.startsWith("$")) {
            return null;
        }

        Set<BiomeType> biomes = new HashSet<>();
        BiomeRegistry biomeRegistry = worldEdit.getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getBiomeRegistry();
        Collection<BiomeType> knownBiomes = BiomeType.REGISTRY.values();
        for (String biomeName : Splitter.on(",").split(input.substring(1))) {
            BiomeType biome = Biomes.findBiomeByName(knownBiomes, biomeName, biomeRegistry);
            if (biome == null) {
                throw new InputParseException("Unknown biome '" + biomeName + '\'');
            }
            biomes.add(biome);
        }

        return Masks.asMask(new BiomeMask2D(new RequestExtent(), biomes));
    }
}
