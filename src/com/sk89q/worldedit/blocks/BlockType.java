// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.blocks;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;

/**
 * Block types.
 *
 * @author sk89q
 */
public enum BlockType {
    AIR(0, "Air", "air"),
    STONE(1, "Stone", new String[]{"stone", "rock"}),
    GRASS(2, "Grass", "grass"),
    DIRT(3, "Dirt", "dirt"),
    COBBLESTONE(4, "Cobblestone", "cobblestone"),
    WOOD(5, "Wood", "wood"),
    SAPLING(6, "Sapling", "sapling"),
    BEDROCK(7, "Bedrock", "bedrock"),
    WATER(8, "Water", "watermoving"),
    STATIONARY_WATER(9, "Water (stationary)", "water"),
    LAVA(10, "Lava", "lavamoving"),
    STATIONARY_LAVA(11, "Lava (stationary)", "lava"),
    SAND(12,    "Sand", "sand"),
    GRAVEL(13, "Gravel", "gravel"),
    GOLD_ORE(14, "Gold ore", "goldore"),
    IRON_ORE(15, "Iron ore", "ironore"),
    COAL_ORE(16, "Coal ore", "coalore"),
    LOG(17, "Log", "log"),
    LEAVES(18, "Leaves", "leaves"),
    SPONGE(19, "Sponge", "sponge"),
    GLASS(20, "Glass", "glass"),
    CLOTH(35, "Cloth", "cloth"),
    YELLOW_FLOWER(37, "Yellow flower", "yellowflower"),
    RED_FLOWER(38, "Red rose", new String[]{"redflower", "redrose"}),
    BROWN_MUSHROOM(39, "Brown mushroom", "brownmushroom"),
    RED_MUSHROOM(40, "Red mushroom", "redmushroom"),
    GOLD_BLOCK(41, "Gold block", new String[]{"gold", "goldblock"}),
    IRON_BLOCK(42, "Iron block", new String[]{"iron", "ironblock"}),
    DOUBLE_STEP(43, "Double step", "doublestep"),
    STEP(44, "Step", "step"),
    BRICK(45, "Brick", "brick"),
    TNT(46, "TNT", "tnt"),
    BOOKCASE(47, "Bookcase", new String[]{"bookshelf", "bookshelf"}),
    MOSSY_COBBLESTONE(48, "Cobblestone (mossy)", "mossycobblestone"),
    OBSIDIAN(49, "Obsidian", "obsidian"),
    TORCH(50, "Torch", "torch"),
    FIRE(51, "Fire", "fire"),
    MOB_SPAWNER(52, "Mob spawner", "mobspawner"),
    WOODEN_STAIRS(53, "Wooden stairs", "woodstairs"),
    CHEST(54, "Chest", "chest"),
    REDSTONE_WIRE(55, "Redstone wire", "redstone"),
    DIAMOND_ORE(56, "Diamond ore", "diamondore"),
    DIAMOND_BLOCK(57, "Diamond block", new String[]{"diamond", "diamondblock"}),
    WORKBENCH(58, "Workbench", "workbench"),
    CROPS(59, "Crops", "crops"),
    SOIL(60, "Soil", "soil"),
    FURNACE(61, "Furnace", "furnace"),
    BURNING_FURNACE(62, "Furnace (burning)", "burningfurnace"),
    SIGN_POST(63, "Sign post", new String[]{"sign", "signpost"}),
    WOODEN_DOOR(64, "Wooden door", "wooddoor"),
    LADDER(65, "Ladder", "ladder"),
    MINECART_TRACKS(66, "Minecart tracks", new String[]{"track", "tracks"}),
    COBBLESTONE_STAIRS(67, "Cobblestone stairs", "cobblestonestairs"),
    WALL_SIGN(68, "Wall sign", "wallsign"),
    LEVER(69, "Lever", "level"),
    STONE_PRESSURE_PLATE(70, "Stone pressure plate", "stonepressureplate"),
    IRON_DOOR(71, "Iron Door", "irondoor"),
    WOODEN_PRESSURE_PLATE(72, "Wooden pressure plate", "woodpressureplate"),
    REDSTONE_ORE(73, "Redstone ore", "redstoneore"),
    GLOWING_REDSTONE_ORE(74, "Glowing redstone ore", "glowingredstoneore"),
    REDSTONE_TORCH_OFF(75, "Redstone torch (off)",
            new String[]{"redstonetorch"," redstonetorchon"}),
    REDSTONE_TORCH_ON(76, "Redstone torch (on)", "redstonetorchon"),
    STONE_BUTTON(77, "Stone Button", "stonebutton"),
    SNOW(78, "Snow", "snow"),
    ICE(79, "Ice", "ice"),
    SNOW_BLOCK(80, "Snow block", "snowblock"),
    CACTUS(81, "Cactus", "cactus"),
    CLAY(82, "Clay", "clay"),
    REED(83, "Reed", "reed"),
    JUKEBOX(84, "Jukebox", "jukebox"),
    FENCE(85, "Fence", "fence"),
    PUMPKIN(86, "Pumpkin", "pumpkin"),
    RED_BLOCK(87, "Cobblestone (red mossy)", new String[]{"redmossycobblestone", "redcobblestone"}),
    HELL_DIRT(88, "Mud", "mud"),
    HELL_GOLD(89, "Brittle gold", "brittlegold"),
    PORTAL(90, "Portal", "portal"),
    LIGHTED_PUMPKIN(91, "Pumpkin (on)", new String[]{"pumpkinlighted", "pumpkinon", "litpumpkin"});

    /**
     * Stores a map of the IDs for fast access.
     */
    private static final Map<Integer,BlockType> ids = new HashMap<Integer,BlockType>();
    /**
     * Stores a map of the names for fast access.
     */
    private static final Map<String,BlockType> lookup = new HashMap<String,BlockType>();

    private final int id;
    private final String name;
    private final String[] lookupKeys;

    static {
        for(BlockType type : EnumSet.allOf(BlockType.class)) {
            ids.put(type.id, type);
            for (String key : type.lookupKeys) {
                lookup.put(key, type);
            }
        }
    }


    /**
     * Construct the type.
     *
     * @param id
     * @param name
     */
    BlockType(int id, String name, String lookupKey) {
        this.id = id;
        this.name = name;
        this.lookupKeys = new String[]{lookupKey};
    }

    /**
     * Construct the type.
     *
     * @param id
     * @param name
     */
    BlockType(int id, String name, String[] lookupKeys) {
        this.id = id;
        this.name = name;
        this.lookupKeys = lookupKeys;
    }

    /**
     * Return type from ID. May return null.
     *
     * @param id
     * @return
     */
    public static BlockType fromID(int id) {
        return ids.get(id);
    }

    /**
     * Return type from name. May return null.
     *
     * @param name
     * @return
     */
    public static BlockType lookup(String name) {
        return lookup.get(name.toLowerCase());
    }

    /**
     * Get block numeric ID.
     *
     * @return
     */
    public int getID() {
        return id;
    }

    /**
     * Get user-friendly block name.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Checks to see whether a block should be placed last.
     *
     * @param id
     * @return
     */
    public boolean shouldPlaceLast() {
        return shouldPlaceLast(id);
    }

    /**
     * Checks to see whether a block should be placed last.
     * 
     * @param id
     * @return
     */
    public static boolean shouldPlaceLast(int id) {
        return id == 6 // Saplings
                || id == 37 // Yellow flower
                || id == 38 // Red flower
                || id == 39 // Brown mushroom
                || id == 40 // Red mush room
                || id == 50 // Torch
                || id == 51 // Fire
                || id == 55 // Redstone wire
                || id == 59 // Crops
                || id == 63 // Sign post
                || id == 64 // Wooden door
                || id == 65 // Ladder
                || id == 66 // Minecart tracks
                || id == 68 // Wall sign
                || id == 69 // Lever
                || id == 70 // Stone pressure plate
                || id == 71 // Iron door
                || id == 72 // Wooden pressure plate
                || id == 75 // Redstone torch (off)
                || id == 76 // Redstone torch (on)
                || id == 77 // Stone button
                || id == 78 // Snow
                || id == 81 // Cactus
                || id == 83 // Reed
                || id == 90; // Portal
    }

    /**
     * Checks whether a block can be passed through.
     *
     * @param id
     * @return
     */
    public static boolean canPassThrough(int id) {
        return id == 0 // Air
                || id == 6 // Saplings
                || id == 37 // Yellow flower
                || id == 38 // Red flower
                || id == 39 // Brown mushroom
                || id == 40 // Red mush room
                || id == 50 // Torch
                || id == 51 // Fire
                || id == 55 // Redstone wire
                || id == 59 // Crops
                || id == 63 // Sign post
                || id == 65 // Ladder
                || id == 66 // Minecart tracks
                || id == 68 // Wall sign
                || id == 69 // Lever
                || id == 70 // Stone pressure plate
                || id == 72 // Wooden pressure plate
                || id == 75 // Redstone torch (off)
                || id == 76 // Redstone torch (on)
                || id == 77 // Stone button
                || id == 78 // Snow
                || id == 83 // Reed
                || id == 90; // Portal
    }
}