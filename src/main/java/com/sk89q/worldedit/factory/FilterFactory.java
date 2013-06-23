// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sk89q.worldedit.BiomeType;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.DisallowedItemException;
import com.sk89q.worldedit.EmptyClipboardException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.UnknownItemException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.masks.BiomeTypeMask;
import com.sk89q.worldedit.masks.BlockMask;
import com.sk89q.worldedit.masks.CombinedMask;
import com.sk89q.worldedit.masks.DynamicRegionMask;
import com.sk89q.worldedit.masks.ExistingBlockMask;
import com.sk89q.worldedit.masks.InvertedMask;
import com.sk89q.worldedit.masks.Mask;
import com.sk89q.worldedit.masks.RandomMask;
import com.sk89q.worldedit.masks.RegionMask;
import com.sk89q.worldedit.masks.UnderOverlayMask;
import com.sk89q.worldedit.patterns.BlockChance;
import com.sk89q.worldedit.patterns.ClipboardPattern;
import com.sk89q.worldedit.patterns.Pattern;
import com.sk89q.worldedit.patterns.RandomFillPattern;
import com.sk89q.worldedit.patterns.SingleBlockPattern;

/**
 * Creates {@link Pattern}s and {@link com.sk89q.worldedit.masks.Mask}s.
 */
public class FilterFactory extends AbstractFactory {

    private MaterialFactory materialFactory;

    /**
     * Create a new instance.
     *
     * @param worldEdit a WorldEdit instance
     * @param materialFactory the material factory
     */
    public FilterFactory(WorldEdit worldEdit, MaterialFactory materialFactory) {
        super(worldEdit);
        this.materialFactory = materialFactory;
    }

    /**
     * Match a {@link com.sk89q.worldedit.patterns.Pattern} from user input.
     *
     * @param player the player
     * @param input the string describing a block
     * @return a pattern
     * @throws com.sk89q.worldedit.UnknownItemException if an item cannot be matched
     * @throws com.sk89q.worldedit.DisallowedItemException if a disallowed item has been matched
     */
    public Pattern matchPattern(LocalPlayer player, String input)
            throws UnknownItemException, DisallowedItemException {

        String[] items = input.split(",");

        // Handle special block pattern types
        if (input.charAt(0) == '#') {
            if (input.equals("#clipboard") || input.equals("#copy")) {
                LocalSession session = getWorldEdit().getSessions().get(player);
                CuboidClipboard clipboard;

                try {
                    clipboard = session.getClipboard();
                } catch (EmptyClipboardException e) {
                    player.printError("Copy a selection first with //copy.");
                    throw new UnknownItemException("#clipboard");
                }

                return new ClipboardPattern(clipboard);
            } else {
                throw new UnknownItemException(input);
            }
        }

        // If it's only one block, then just return that single one
        if (items.length == 1) {
            return new SingleBlockPattern(materialFactory.matchBlock(player, items[0]));
        }

        List<BlockChance> blockChances = new ArrayList<BlockChance>();

        for (String s : items) {
            BaseBlock block;

            double chance;

            // Parse special percentage syntax
            if (s.matches("[0-9]+(\\.[0-9]*)?%.*")) {
                String[] p = s.split("%");
                chance = Double.parseDouble(p[0]);
                block = materialFactory.matchBlock(player, p[1]);
            } else {
                chance = 1;
                block = materialFactory.matchBlock(player, s);
            }

            blockChances.add(new BlockChance(block, chance));
        }

        return new RandomFillPattern(blockChances);
    }


    /**
     * Match a {@link Mask} from user input.
     *
     * @param player the player
     * @param input the user input
     * @return a mask
     * @throws WorldEditException on an error
     */
    public Mask matchMask(LocalPlayer player, String input) throws WorldEditException {
        LocalSession session = getWorldEdit().getSessions().get(player);
        List<Mask> masks = new ArrayList<Mask>();

        for (String component : input.split(" ")) {
            if (component.length() == 0) {
                continue;
            }

            Mask current = getBlockMaskComponent(player, session, masks, component);

            masks.add(current);
        }

        switch (masks.size()) {
            case 0:
                return null;

            case 1:
                return masks.get(0);

            default:
                return new CombinedMask(masks);
        }
    }

    /**
     * Internal method to get a block mask component.
     *
     * @param player the player
     * @param session the session
     * @param masks a list of masks
     * @param component the component
     * @return a mask component
     * @throws WorldEditException on error
     */
    private Mask getBlockMaskComponent(LocalPlayer player, LocalSession session,
                                       List<Mask> masks, String component)
            throws WorldEditException {
        final char firstChar = component.charAt(0);
        switch (firstChar) {
            case '#':
                if (component.equalsIgnoreCase("#existing")) {
                    return new ExistingBlockMask();
                } else if (component.equalsIgnoreCase("#dregion")
                        || component.equalsIgnoreCase("#dselection")
                        || component.equalsIgnoreCase("#dsel")) {
                    return new DynamicRegionMask();
                } else if (component.equalsIgnoreCase("#selection")
                        || component.equalsIgnoreCase("#region")
                        || component.equalsIgnoreCase("#sel")) {
                    return new RegionMask(session.getSelection(player.getWorld()));
                } else {
                    throw new UnknownItemException(component);
                }

            case '>':
            case '<':
                Mask submask;
                if (component.length() > 1) {
                    submask = getBlockMaskComponent(player, session, masks, component.substring(1));
                } else {
                    submask = new ExistingBlockMask();
                }
                return new UnderOverlayMask(submask, firstChar == '>');

            case '$':
                Set<BiomeType> biomes = new HashSet<BiomeType>();
                String[] biomesList = component.substring(1).split(",");
                for (String biomeName : biomesList) {
                    BiomeType biome = getServer().getBiomes().get(biomeName);
                    biomes.add(biome);
                }
                return new BiomeTypeMask(biomes);

            case '%':
                int i = Integer.parseInt(component.substring(1));
                return new RandomMask(((double) i) / 100);

            case '!':
                if (component.length() > 1) {
                    return new InvertedMask(getBlockMaskComponent(player, session, masks, component.substring(1)));
                }

            default:
                return new BlockMask(
                        materialFactory.matchBlocks(player, component, true, true));
        }
    }

}
