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
import java.util.Map.Entry;
import com.sk89q.util.StringUtil;

/**
 * Block types.
 *
 * @author sk89q
 */
public enum BlockType {
    AIR(0, "Air", "air"),
    STONE(1, "Stone", "stone", "rock"),
    GRASS(2, "Grass", "grass"),
    DIRT(3, "Dirt", "dirt"),
    COBBLESTONE(4, "Cobblestone", "cobblestone", "cobble"),
    WOOD(5, "Wood", "wood", "woodplank", "plank", "woodplanks", "planks"),
    SAPLING(6, "Sapling", "sapling", "seedling"),
    BEDROCK(7, "Bedrock", "adminium", "bedrock"),
    WATER(8, "Water", "watermoving", "movingwater", "flowingwater", "waterflowing"),
    STATIONARY_WATER(9, "Water (stationary)", "water", "waterstationary", "stationarywater", "stillwater"),
    LAVA(10, "Lava", "lavamoving", "movinglava", "flowinglava", "lavaflowing"),
    STATIONARY_LAVA(11, "Lava (stationary)", "lava", "lavastationary", "stationarylava", "stilllava"),
    SAND(12, "Sand", "sand"),
    GRAVEL(13, "Gravel", "gravel"),
    GOLD_ORE(14, "Gold ore", "goldore"),
    IRON_ORE(15, "Iron ore", "ironore"),
    COAL_ORE(16, "Coal ore", "coalore"),
    LOG(17, "Log", "log", "tree", "pine", "oak", "birch", "redwood"),
    LEAVES(18, "Leaves", "leaves", "leaf"),
    SPONGE(19, "Sponge", "sponge"),
    GLASS(20, "Glass", "glass"),
    LAPIS_LAZULI_ORE(21, "Lapis lazuli ore", "lapislazuliore", "blueore", "lapisore"),
    LAPIS_LAZULI(22, "Lapis lazuli", "lapislazuli", "lapislazuliblock", "bluerock"),
    DISPENSER(23, "Dispenser", "dispenser"),
    SANDSTONE(24, "Sandstone", "sandstone"),
    NOTE_BLOCK(25, "Note block", "musicblock", "noteblock", "note", "music", "instrument"),
    BED(26, "Bed", "bed"),
    POWERED_RAIL(27, "Powered Rail", "poweredrail", "boosterrail", "poweredtrack", "boostertrack", "booster"),
    DETECTOR_RAIL(28, "Detector Rail", "detectorrail", "detector"),
    PISTON_STICKY_BASE(29, "Sticky Piston", "stickypiston"),
    WEB(30, "Web", "web", "spiderweb"),
    LONG_GRASS(31, "Long grass", "longgrass", "tallgrass"),
    DEAD_BUSH(32, "Shrub", "deadbush", "shrub", "deadshrub", "tumbleweed"),
    PISTON_BASE(33, "Piston", "piston"),
    PISTON_EXTENSION(34, "Piston extension", "pistonextendsion", "pistonhead"),
    CLOTH(35, "Wool", "cloth", "wool"),
    PISTON_MOVING_PIECE(36, "Piston moving piece", "movingpiston"),
    YELLOW_FLOWER(37, "Yellow flower", "yellowflower", "flower"),
    RED_FLOWER(38, "Red rose", "redflower", "redrose", "rose"),
    BROWN_MUSHROOM(39, "Brown mushroom", "brownmushroom", "mushroom"),
    RED_MUSHROOM(40, "Red mushroom", "redmushroom"),
    GOLD_BLOCK(41, "Gold block", "gold", "goldblock"),
    IRON_BLOCK(42, "Iron block", "iron", "ironblock"),
    DOUBLE_STEP(43, "Double step", "doubleslab", "doublestoneslab", "doublestep"),
    STEP(44, "Step", "slab", "stoneslab", "step", "halfstep"),
    BRICK(45, "Brick", "brick", "brickblock"),
    TNT(46, "TNT", "tnt", "c4", "explosive"),
    BOOKCASE(47, "Bookcase", "bookshelf", "bookshelves", "bookcase", "bookcases"),
    MOSSY_COBBLESTONE(48, "Cobblestone (mossy)", "mossycobblestone", "mossstone", "mossystone", "mosscobble", "mossycobble", "moss", "mossy", "sossymobblecone"),
    OBSIDIAN(49, "Obsidian", "obsidian"),
    TORCH(50, "Torch", "torch", "light", "candle"),
    FIRE(51, "Fire", "fire", "flame", "flames"),
    MOB_SPAWNER(52, "Mob spawner", "mobspawner", "spawner"),
    WOODEN_STAIRS(53, "Wooden stairs", "woodstair", "woodstairs", "woodenstair", "woodenstairs"),
    CHEST(54, "Chest", "chest", "storage", "storagechest"),
    REDSTONE_WIRE(55, "Redstone wire", "redstone", "redstoneblock"),
    DIAMOND_ORE(56, "Diamond ore", "diamondore"),
    DIAMOND_BLOCK(57, "Diamond block", "diamond", "diamondblock"),
    WORKBENCH(58, "Workbench", "workbench", "table", "craftingtable", "crafting"),
    CROPS(59, "Crops", "crops", "crop", "plant", "plants"),
    SOIL(60, "Soil", "soil", "farmland"),
    FURNACE(61, "Furnace", "furnace"),
    BURNING_FURNACE(62, "Furnace (burning)", "burningfurnace", "litfurnace"),
    SIGN_POST(63, "Sign post", "sign", "signpost"),
    WOODEN_DOOR(64, "Wooden door", "wooddoor", "woodendoor", "door"),
    LADDER(65, "Ladder", "ladder"),
    MINECART_TRACKS(66, "Minecart tracks", "track", "tracks", "minecrattrack", "minecarttracks", "rails", "rail"),
    COBBLESTONE_STAIRS(67, "Cobblestone stairs", "cobblestonestair", "cobblestonestairs", "cobblestair", "cobblestairs"),
    WALL_SIGN(68, "Wall sign", "wallsign"),
    LEVER(69, "Lever", "lever", "switch", "stonelever", "stoneswitch"),
    STONE_PRESSURE_PLATE(70, "Stone pressure plate", "stonepressureplate", "stoneplate"),
    IRON_DOOR(71, "Iron Door", "irondoor"),
    WOODEN_PRESSURE_PLATE(72, "Wooden pressure plate", "woodpressureplate", "woodplate", "woodenpressureplate", "woodenplate", "plate", "pressureplate"),
    REDSTONE_ORE(73, "Redstone ore", "redstoneore"),
    GLOWING_REDSTONE_ORE(74, "Glowing redstone ore", "glowingredstoneore"),
    REDSTONE_TORCH_OFF(75, "Redstone torch (off)", "redstonetorchoff", "rstorchoff"),
    REDSTONE_TORCH_ON(76, "Redstone torch (on)", "redstonetorch", "redstonetorchon", "rstorchon", "redtorch"),
    STONE_BUTTON(77, "Stone Button", "stonebutton", "button"),
    SNOW(78, "Snow", "snow"),
    ICE(79, "Ice", "ice"),
    SNOW_BLOCK(80, "Snow block", "snowblock"),
    CACTUS(81, "Cactus", "cactus", "cacti"),
    CLAY(82, "Clay", "clay"),
    SUGAR_CANE(83, "Reed", "reed", "cane", "sugarcane", "sugarcanes", "vine", "vines"),
    JUKEBOX(84, "Jukebox", "jukebox", "stereo", "recordplayer"),
    FENCE(85, "Fence", "fence"),
    PUMPKIN(86, "Pumpkin", "pumpkin"),
    NETHERRACK(87, "Netherrack", "redmossycobblestone", "redcobblestone", "redmosstone", "redcobble", "netherstone", "netherrack", "nether", "hellstone"),
    SOUL_SAND(88, "Soul sand", "slowmud", "mud", "soulsand", "hellmud"),
    GLOWSTONE(89, "Glowstone", "brittlegold", "glowstone", "lightstone", "brimstone", "australium"),
    PORTAL(90, "Portal", "portal"),
    JACK_O_LANTERN(91, "Pumpkin (on)", "pumpkinlighted", "pumpkinon", "litpumpkin", "jackolantern"),
    CAKE(92, "Cake", "cake", "cakeblock"),
    REDSTONE_REPEATER_OFF(93, "Redstone repeater (off)", "diodeoff", "redstonerepeater", "repeater", "delayer"),
    REDSTONE_REPEATER_ON(94, "Redstone repeater (on)", "diode", "diodeon", "redstonerepeateron", "repeateron", "delayeron"),
    LOCKED_CHEST(95, "Locked chest", "lockedchest", "steveco", "supplycrate", "valveneedstoworkonep3nottf2kthx"),
    TRAP_DOOR(96, "Trap door", "trapdoor", "hatch", "floordoor");

    /**
     * Stores a list of dropped blocks for blocks.
     */
    private static final Map<Integer,Integer> blockDrops = new HashMap<Integer,Integer>();

    /**
     * Static constructor.
     */
    static {
        blockDrops.put(1, 4);
        blockDrops.put(2, 3);
        blockDrops.put(3, 3);
        blockDrops.put(4, 4);
        blockDrops.put(5, 5);
        blockDrops.put(6, 6);
        blockDrops.put(7, -1);
        blockDrops.put(12, 12);
        blockDrops.put(13, 13);
        blockDrops.put(14, 14);
        blockDrops.put(15, 15);
        blockDrops.put(16, 16);
        blockDrops.put(17, 17);
        blockDrops.put(18, 18);
        blockDrops.put(19, 19);
        blockDrops.put(20, 20); // Have to drop glass for //undo
        blockDrops.put(21, 21); // Block damage drops not implemented
        blockDrops.put(22, 22);
        blockDrops.put(23, 23);
        blockDrops.put(24, 24);
        blockDrops.put(25, 25);
        blockDrops.put(26, 355);
        blockDrops.put(27, 27);
        blockDrops.put(28, 28);
        blockDrops.put(30, 30);
        blockDrops.put(34, -1);
        blockDrops.put(35, 35);
        blockDrops.put(36, -1);
        blockDrops.put(37, 37);
        blockDrops.put(38, 38);
        blockDrops.put(39, 39);
        blockDrops.put(40, 40);
        blockDrops.put(41, 41);
        blockDrops.put(42, 42);
        blockDrops.put(43, 43);
        blockDrops.put(44, 44);
        blockDrops.put(45, 45);
        blockDrops.put(47, 47);
        blockDrops.put(48, 48);
        blockDrops.put(49, 49);
        blockDrops.put(50, 50);
        blockDrops.put(53, 53);
        blockDrops.put(54, 54);
        blockDrops.put(55, 331);
        blockDrops.put(56, 264);
        blockDrops.put(57, 57);
        blockDrops.put(58, 58);
        blockDrops.put(59, 295);
        blockDrops.put(60, 60);
        blockDrops.put(61, 61);
        blockDrops.put(62, 61);
        blockDrops.put(63, 323);
        blockDrops.put(64, 324);
        blockDrops.put(65, 65);
        blockDrops.put(66, 66);
        blockDrops.put(67, 67);
        blockDrops.put(68, 323);
        blockDrops.put(69, 69);
        blockDrops.put(70, 70);
        blockDrops.put(71, 330);
        blockDrops.put(72, 72);
        blockDrops.put(73, 331);
        blockDrops.put(74, 331);
        blockDrops.put(75, 76);
        blockDrops.put(76, 76);
        blockDrops.put(77, 77);
        blockDrops.put(78, 332);
        blockDrops.put(80, 80);
        blockDrops.put(81, 81);
        blockDrops.put(82, 82);
        blockDrops.put(83, 338);
        blockDrops.put(84, 84);
        blockDrops.put(85, 85);
        blockDrops.put(86, 86);
        blockDrops.put(87, 87);
        blockDrops.put(88, 88);
        blockDrops.put(89, 348);
        blockDrops.put(91, 91);
        blockDrops.put(92, 354);
        blockDrops.put(93, 356);
        blockDrops.put(94, 356);
        blockDrops.put(95, 95);
        blockDrops.put(96, 96);
    }

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
    BlockType(int id, String name, String ... lookupKeys) {
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
        return lookup(name, true);
    }

    /**
     * Return type from name. May return null.
     *
     * @param name
     * @param fuzzy
     * @return
     */
    public static BlockType lookup(String name, boolean fuzzy) {
        String testName = name.replace(" ", "").toLowerCase();
        
        BlockType type = lookup.get(testName);
        
        if (type != null) {
            return type;
        }
        
        if (!fuzzy) {
            return null;
        }
        
        int minDist = -1;
        
        for (Entry<String, BlockType> entry : lookup.entrySet()) {
            if (entry.getKey().charAt(0) != testName.charAt(0)) {
                continue;
            }
            
            int dist = StringUtil.getLevenshteinDistance(entry.getKey(), testName);
            
            if ((dist < minDist || minDist == -1) && dist < 2) {
                minDist = dist;
                type = entry.getValue();
            }
        }
        
        return type;
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
                || id == 26 // Beds
                || id == 27 // Powered rails
                || id == 28 // Detector rails
                || id == 31 // Long grass
                || id == 32 // Shrub
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
                || id == 90 // Portal
                || id == 92 // Cake
                || id == 93 // Repeater (off)
                || id == 94 // Repeater (on)
                || id == 96; // Trap door
    }

    /**
     * Checks whether a block can be passed through.
     *
     * @param id
     * @return
     */
    public static boolean canPassThrough(int id) {
        return id == 0 // Air
                || id == 8 // Water
                || id == 9 // Water
                || id == 6 // Saplings
                || id == 27 // Powered rails
                || id == 28 // Detector rails
                || id == 30 // Web <- someone will hate me for this
                || id == 31 // Long grass
                || id == 32 // Shrub
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
                || id == 90 // Portal
                || id == 93 // Diode (off)
                || id == 94; // Diode (on)
    }

    /**
     * Returns true if the block uses its data value.
     * 
     * @param id
     * @return
     */
    public static boolean usesData(int id) {
        return id == 6 // Saplings
                || id == 8 // Water
                || id == 9 // Water
                || id == 10 // Lava
                || id == 11 // Lava
                || id == 17 // Wood
                || id == 18 // Leaves
                || id == 23 // Dispenser
                || id == 25 // Note Block
                || id == 26 // Bed
                || id == 27 // Powered rails
                || id == 28 // Detector rails
                || id == 29 // Sticky piston
                || id == 31 // Tall grass
                || id == 33 // Piston
                || id == 34 // Piston extension
                || id == 35 // Wool
                || id == 43 // Double slab
                || id == 44 // Slab
                || id == 50 // Torch
                || id == 53 // Wooden stairs
                || id == 55 // Redstone wire
                || id == 59 // Crops
                || id == 60 // Soil
                || id == 61 // Furnace
                || id == 62 // Furnace
                || id == 63 // Sign post
                || id == 64 // Wooden door
                || id == 65 // Ladder
                || id == 66 // Minecart track
                || id == 67 // Cobblestone stairs
                || id == 68 // Wall sign
                || id == 69 // Lever
                || id == 70 // Stone pressure plate
                || id == 71 // Iron door
                || id == 72 // Wooden pressure plate
                || id == 75 // Redstone torch (off)
                || id == 76 // Redstone torch (on)
                || id == 77 // Stone button
                || id == 78 // Snow tile
                || id == 81 // Cactus
                || id == 86 // Pumpkin
                || id == 91 // Jack-o-lantern
                || id == 92 // Cake
                || id == 93 // Redstone repeater (off)
                || id == 94 // Redstone repeater (on)
                || id == 96; // Trap door
    }
    
    /**
     * Returns true if the block is a container block.
     * 
     * @param id
     * @return
     */
    public static boolean isContainerBlock(int id) {
        return id == 23 // Dispenser
                || id == 61 // Furnace
                || id == 62 // Furnace
                || id == 54; // Chest
    }

    /**
     * Returns true if a block uses redstone in some way.
     *
     * @param id
     * @return
     */
    public static boolean isRedstoneBlock(int id) {
        return id == 27 // Powered rail
                || id == 28 // Detector rail
                || id == 29 // Sticky piston
                || id == 33 // Piston
                || id == 69 // Lever
                || id == 70 // Stone pressure plate
                || id == 72 // Wood pressure plate
                || id == 76 // Redstone torch
                || id == 75 // Redstone torch
                || id == 77 // Stone button
                || id == 55 // Redstone wire
                || id == 64 // Wooden door
                || id == 71 // Iron door
                || id == 46 // TNT
                || id == 23 // Dispenser
                || id == 25 // Note block
                || id == 93 // Diode (off)
                || id == 94; // Diode (on)
    }

   /**
     * Returns true if a block can transfer redstone.
     * Made this since isRedstoneBlock was getting big.
     *
     * @param id
     * @return
     */
    public static boolean canTransferRedstone(int id) {
        return id == 27 // Powered rail
                || id == 75 // Redstone torch (off)
                || id == 76 // Redstone torch (on)
                || id == 55 // Redstone wire
                || id == 93 // Diode (off)
                || id == 94; // Diode (on)
    }

    /**
     * Yay for convenience methods.
     *
     * @param id
     * @return
     */
    public static boolean isRedstoneSource(int id) {
        return id == 28 // Detector rail
                || id == 75 // Redstone torch (off)
                || id == 76 // Redstone torch (on)
                || id == 69 // Lever
                || id == 70 // Stone plate
                || id == 72 // Wood plate
                || id == 77; // Button
    }

    /**
     * Checks if the id is that of one of the rail types
     *
     * @param id
     * @return
     */
    public static boolean isRailBlock(int id) {
       return id == 27 // Powered rail
               || id == 28 // Detector rail
               || id == 66; // Normal rail
    }

    /**
     * Get the block or item that would have been dropped. If nothing is
     * dropped, 0 will be returned. If the block should not be destroyed
     * (i.e. bedrock), -1 will be returned.
     * 
     * @param id
     * @return
     */
    public static int getDroppedBlock(int id) {
        Integer dropped = blockDrops.get(id);
        if (dropped == null) {
            return 0;
        }
        return dropped;
    }
}
