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

package com.sk89q.worldedit.extension.factory;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.factory.parser.mask.BlockCategoryMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.DefaultMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.ExistingMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.LazyRegionMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.NegateMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.NoiseMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.RegionMaskParser;
import com.sk89q.worldedit.extension.factory.parser.mask.SolidMaskParser;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.internal.registry.AbstractFactory;
import com.sk89q.worldedit.internal.registry.InputParser;

import java.util.ArrayList;
import java.util.List;

/**
 * A registry of known {@link Mask}s. Provides methods to instantiate
 * new masks from input.
 *
 * <p>Instances of this class can be taken from
 * {@link WorldEdit#getMaskFactory()}.</p>
 */
public final class MaskFactory extends AbstractFactory<Mask> {

    /**
     * Create a new mask registry.
     *
     * @param worldEdit the WorldEdit instance
     */
    public MaskFactory(WorldEdit worldEdit) {
        super(worldEdit);

        register(new ExistingMaskParser(worldEdit));
        register(new SolidMaskParser(worldEdit));
        register(new LazyRegionMaskParser(worldEdit));
        register(new RegionMaskParser(worldEdit));
        register(new BlockCategoryMaskParser(worldEdit));
        register(new NoiseMaskParser(worldEdit));
        register(new NegateMaskParser(worldEdit));
        register(new DefaultMaskParser(worldEdit));
    }

    @Override
    public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
        List<Mask> masks = new ArrayList<>();

        for (String component : input.split(" ")) {
            if (component.isEmpty()) {
                continue;
            }

            for (InputParser<Mask> parser : getParsers()) {
                Mask match = parser.parseFromInput(component, context);

                if (match != null) {
                    masks.add(match);
                }
            }
        }

        switch (masks.size()) {
            case 0:
                throw new NoMatchException("No match for '" + input + "'");
            case 1:
                return masks.get(0);
            default:
                return new MaskIntersection(masks);
        }
    }

}
