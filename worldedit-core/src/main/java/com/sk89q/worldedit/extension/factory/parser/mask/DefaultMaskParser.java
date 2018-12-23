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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.BiomeMask2D;
import com.sk89q.worldedit.function.mask.BlockMask;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.mask.ExpressionMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.mask.OffsetMask;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.shape.WorldEditExpressionEnvironment;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.world.biome.BaseBiome;
import com.sk89q.worldedit.world.biome.Biomes;
import com.sk89q.worldedit.world.registry.BiomeRegistry;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parses mask input strings.
 */
public class DefaultMaskParser extends InputParser<Mask> {

    public DefaultMaskParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    public Mask parseFromInput(String component, ParserContext context) throws InputParseException {
        Extent extent = Request.request().getEditSession();

        final char firstChar = component.charAt(0);
        switch (firstChar) {
            case '>':
            case '<':
                Mask submask;
                if (component.length() > 1) {
                    submask = worldEdit.getMaskFactory().parseFromInput(component.substring(1), context);
                } else {
                    submask = new ExistingBlockMask(extent);
                }
                OffsetMask offsetMask = new OffsetMask(submask, BlockVector3.at(0, firstChar == '>' ? -1 : 1, 0));
                return new MaskIntersection(offsetMask, Masks.negate(submask));

            case '$':
                Set<BaseBiome> biomes = new HashSet<>();
                String[] biomesList = component.substring(1).split(",");
                BiomeRegistry biomeRegistry = worldEdit.getPlatformManager().queryCapability(Capability.GAME_HOOKS).getRegistries().getBiomeRegistry();
                List<BaseBiome> knownBiomes = biomeRegistry.getBiomes();
                for (String biomeName : biomesList) {
                    BaseBiome biome = Biomes.findBiomeByName(knownBiomes, biomeName, biomeRegistry);
                    if (biome == null) {
                        throw new InputParseException("Unknown biome '" + biomeName + '\'');
                    }
                    biomes.add(biome);
                }

                return Masks.asMask(new BiomeMask2D(context.requireExtent(), biomes));

            case '=':
                try {
                    Expression exp = Expression.compile(component.substring(1), "x", "y", "z");
                    WorldEditExpressionEnvironment env = new WorldEditExpressionEnvironment(
                            Request.request().getEditSession(), Vector3.ONE, Vector3.ZERO);
                    exp.setEnvironment(env);
                    return new ExpressionMask(exp);
                } catch (ExpressionException e) {
                    throw new InputParseException("Invalid expression: " + e.getMessage());
                }

            default:
                ParserContext tempContext = new ParserContext(context);
                tempContext.setRestricted(false);
                tempContext.setPreferringWildcard(true);
                return new BlockMask(extent, worldEdit.getBlockFactory().parseFromListInput(component, tempContext));
        }
    }

}
