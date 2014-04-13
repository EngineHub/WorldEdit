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

package com.sk89q.worldedit.extension.registry;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.*;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.masks.BiomeTypeMask;
import com.sk89q.worldedit.math.noise.RandomNoise;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.session.request.RequestSelection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Parses mask input strings.
 */
class DefaultMaskParser extends InputParser<Mask> {

    protected DefaultMaskParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
        List<Mask> masks = new ArrayList<Mask>();

        for (String component : input.split(" ")) {
            if (component.length() == 0) {
                continue;
            }

            Mask current = getBlockMaskComponent(masks, component, context);

            masks.add(current);
        }

        switch (masks.size()) {
            case 0:
                return null;

            case 1:
                return masks.get(0);

            default:
                return new MaskIntersection(masks);
        }
    }

    private Mask getBlockMaskComponent(List<Mask> masks, String component, ParserContext context) throws InputParseException {
        Extent extent = Request.request().getEditSession();

        final char firstChar = component.charAt(0);
        switch (firstChar) {
            case '#':
                if (component.equalsIgnoreCase("#existing")) {
                    return new ExistingBlockMask(extent);
                } else if (component.equalsIgnoreCase("#solid")) {
                    return new SolidBlockMask(extent);
                } else if (component.equalsIgnoreCase("#dregion")
                        || component.equalsIgnoreCase("#dselection")
                        || component.equalsIgnoreCase("#dsel")) {
                    return new RegionMask(new RequestSelection());
                } else if (component.equalsIgnoreCase("#selection")
                        || component.equalsIgnoreCase("#region")
                        || component.equalsIgnoreCase("#sel")) {
                    try {
                        return new RegionMask(context.requireSession().getSelection(context.requireWorld()).clone());
                    } catch (IncompleteRegionException e) {
                        throw new InputParseException("Please make a selection first.");
                    }
                } else {
                    throw new NoMatchException("Unrecognized mask '" + component + "'");
                }

            case '>':
            case '<':
                Mask submask;
                if (component.length() > 1) {
                    submask = getBlockMaskComponent(masks, component.substring(1), context);
                } else {
                    submask = new ExistingBlockMask(extent);
                }
                OffsetMask offsetMask = new OffsetMask(submask, new Vector(0, firstChar == '>' ? -1 : 1, 0));
                return new MaskIntersection(offsetMask, Masks.negate(submask));

            case '$':
                Set<BiomeType> biomes = new HashSet<BiomeType>();
                String[] biomesList = component.substring(1).split(",");
                for (String biomeName : biomesList) {
                    try {
                        BiomeType biome = worldEdit.getServer().getBiomes().get(biomeName);
                        biomes.add(biome);
                    } catch (UnknownBiomeTypeException e) {
                        throw new InputParseException("Unknown biome '" + biomeName + "'");
                    }
                }

                return Masks.wrap(new BiomeTypeMask(biomes));

            case '%':
                int i = Integer.parseInt(component.substring(1));
                return new NoiseFilter(new RandomNoise(), ((double) i) / 100);

            case '!':
                if (component.length() > 1) {
                    return Masks.negate(getBlockMaskComponent(masks, component.substring(1), context));
                }

            default:
                return new BlockMask(extent, worldEdit.getBlockRegistry().parseFromListInput(component, context));
        }
    }

}
