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

package com.sk89q.worldedit.util;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;

/**
 * Tree generator.
 *
 * @author sk89q
 */
public class TreeGenerator {
    public enum TreeType {
        TREE("Regular tree", "tree", "regular"),
        BIG_TREE("Big tree", "big", "bigtree"),
        REDWOOD("Redwood", "redwood", "sequoia", "sequoioideae"),
        TALL_REDWOOD("Tall redwood", "tallredwood", "tallsequoia", "tallsequoioideae"),
        BIRCH("Birch", "birch", "white", "whitebark"),
        PINE("Pine", "pine") {
            public boolean generate(EditSession editSession, Vector pos) throws MaxChangedBlocksException {
                makePineTree(editSession, pos);
                return true;
            }
        },
        RANDOM_REDWOOD("Random redwood",  "randredwood", "randomredwood", "anyredwood") {
            public boolean generate(EditSession editSession, Vector pos) throws MaxChangedBlocksException {
                TreeType[] choices = new TreeType[] {
                        TreeType.REDWOOD, TreeType.TALL_REDWOOD
                };
                return choices[rand.nextInt(choices.length)].generate(editSession, pos);
            }
        },
        JUNGLE("Jungle", "jungle"),
        SMALL_JUNGLE("Small jungle", "shortjungle", "smalljungle"),
        SHORT_JUNGLE("Short jungle") {
            public boolean generate(EditSession editSession, Vector pos) throws MaxChangedBlocksException {
                return SMALL_JUNGLE.generate(editSession, pos);
            }
        },
        JUNGLE_BUSH("Jungle bush", "junglebush", "jungleshrub"),
        RED_MUSHROOM("Red Mushroom", "redmushroom", "redgiantmushroom"),
        BROWN_MUSHROOM("Brown Mushroom", "brownmushroom", "browngiantmushroom"),
        SWAMP("Swamp", "swamp", "swamptree"),
        ACACIA("Acacia", "acacia"),
        DARK_OAK("Dark Oak", "darkoak"),
        MEGA_REDWOOD("Mega Redwood", "megaredwood"),
        TALL_BIRCH("Tall Birch", "tallbirch"),
        RANDOM("Random", "rand", "random") {
            public boolean generate(EditSession editSession, Vector pos) throws MaxChangedBlocksException {
                TreeType[] choices = new TreeType[] {
                        TreeType.TREE, TreeType.BIG_TREE, TreeType.BIRCH,
                        TreeType.REDWOOD, TreeType.TALL_REDWOOD, TreeType.PINE
                };
                return choices[rand.nextInt(choices.length)].generate(editSession, pos);
            }
        };

        /**
         * Stores a map of the names for fast access.
         */
        private static final Map<String, TreeType> lookup = new HashMap<String, TreeType>();

        private final String name;
        private final String[] lookupKeys;

        static {
            for (TreeType type : EnumSet.allOf(TreeType.class)) {
                for (String key : type.lookupKeys) {
                    lookup.put(key, type);
                }
            }
        }

        TreeType(String name, String... lookupKeys) {
            this.name = name;
            this.lookupKeys = lookupKeys;
        }

        public boolean generate(EditSession editSession, Vector pos) throws MaxChangedBlocksException {
            return editSession.getWorld().generateTree(this, editSession, pos);
        }

        /**
         * Get user-friendly tree type name.
         *
         * @return
         */
        public String getName() {
            return name;
        }

        /**
         * Return type from name. May return null.
         *
         * @param name
         * @return
         */
        public static TreeType lookup(String name) {
            return lookup.get(name.toLowerCase());
        }
    };

    private static Random rand = new Random();

    private TreeType type;

    /**
     * Construct the tree generator with a tree type.
     *
     * @param type
     */
    @Deprecated
    public TreeGenerator(TreeType type) {
        this.type = type;
    }

    /**
     * Generate a tree.
     *
     * @param editSession
     * @param pos
     * @return
     * @throws MaxChangedBlocksException
     */
    public boolean generate(EditSession editSession, Vector pos)
            throws MaxChangedBlocksException {
        return type.generate(editSession, pos);
    }

     /**
     * Makes a terrible looking pine tree.
     *
     * @param basePos
     */
    private static void makePineTree(EditSession editSession, Vector basePos)
            throws MaxChangedBlocksException {
        int trunkHeight = (int) Math.floor(Math.random() * 2) + 3;
        int height = (int) Math.floor(Math.random() * 5) + 8;

        BaseBlock logBlock = new BaseBlock(BlockID.LOG);
        BaseBlock leavesBlock = new BaseBlock(BlockID.LEAVES);

        // Create trunk
        for (int i = 0; i < trunkHeight; ++i) {
            if (!editSession.setBlockIfAir(basePos.add(0, i, 0), logBlock)) {
                return;
            }
        }

        // Move up
        basePos = basePos.add(0, trunkHeight, 0);

        // Create tree + leaves
        for (int i = 0; i < height; ++i) {
            editSession.setBlockIfAir(basePos.add(0, i, 0), logBlock);

            // Less leaves at these levels
            double chance = ((i == 0 || i == height - 1) ? 0.6 : 1);

            // Inner leaves
            editSession.setChanceBlockIfAir(basePos.add(-1, i, 0), leavesBlock, chance);
            editSession.setChanceBlockIfAir(basePos.add(1, i, 0), leavesBlock, chance);
            editSession.setChanceBlockIfAir(basePos.add(0, i, -1), leavesBlock, chance);
            editSession.setChanceBlockIfAir(basePos.add(0, i, 1), leavesBlock, chance);
            editSession.setChanceBlockIfAir(basePos.add(1, i, 1), leavesBlock, chance);
            editSession.setChanceBlockIfAir(basePos.add(-1, i, 1), leavesBlock, chance);
            editSession.setChanceBlockIfAir(basePos.add(1, i, -1), leavesBlock, chance);
            editSession.setChanceBlockIfAir(basePos.add(-1, i, -1), leavesBlock, chance);

            if (!(i == 0 || i == height - 1)) {
                for (int j = -2; j <= 2; ++j) {
                    editSession.setChanceBlockIfAir(basePos.add(-2, i, j), leavesBlock, 0.6);
                }
                for (int j = -2; j <= 2; ++j) {
                    editSession.setChanceBlockIfAir(basePos.add(2, i, j), leavesBlock, 0.6);
                }
                for (int j = -2; j <= 2; ++j) {
                    editSession.setChanceBlockIfAir(basePos.add(j, i, -2), leavesBlock, 0.6);
                }
                for (int j = -2; j <= 2; ++j) {
                    editSession.setChanceBlockIfAir(basePos.add(j, i, 2), leavesBlock, 0.6);
                }
            }
        }

        editSession.setBlockIfAir(basePos.add(0, height, 0), leavesBlock);
    }

    /**
     * Looks up a tree type. May return null if a tree type by that
     * name is not found.
     *
     * @param type
     * @return
     */
    public static TreeType lookup(String type) {
        return TreeType.lookup(type);
    }
}
