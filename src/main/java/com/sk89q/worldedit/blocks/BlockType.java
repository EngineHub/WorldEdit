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

import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import com.sk89q.util.StringUtil;

/**
 * Block types.
 *
 * @author sk89q
 */
public enum BlockType {
    AIR(BlockID.AIR, "Air", "air"),
    STONE(BlockID.STONE, "Stone", "stone", "rock"),
    GRASS(BlockID.GRASS, "Grass", "grass"),
    DIRT(BlockID.DIRT, "Dirt", "dirt"),
    COBBLESTONE(BlockID.COBBLESTONE, "Cobblestone", "cobblestone", "cobble"),
    WOOD(BlockID.WOOD, "Wood", "wood", "woodplank", "plank", "woodplanks", "planks"),
    SAPLING(BlockID.SAPLING, "Sapling", "sapling", "seedling"),
    BEDROCK(BlockID.BEDROCK, "Bedrock", "adminium", "bedrock"),
    WATER(BlockID.WATER, "Water", "watermoving", "movingwater", "flowingwater", "waterflowing"),
    STATIONARY_WATER(BlockID.STATIONARY_WATER, "Water (stationary)", "water", "waterstationary", "stationarywater", "stillwater"),
    LAVA(BlockID.LAVA, "Lava", "lavamoving", "movinglava", "flowinglava", "lavaflowing"),
    STATIONARY_LAVA(BlockID.STATIONARY_LAVA, "Lava (stationary)", "lava", "lavastationary", "stationarylava", "stilllava"),
    SAND(BlockID.SAND, "Sand", "sand"),
    GRAVEL(BlockID.GRAVEL, "Gravel", "gravel"),
    GOLD_ORE(BlockID.GOLD_ORE, "Gold ore", "goldore"),
    IRON_ORE(BlockID.IRON_ORE, "Iron ore", "ironore"),
    COAL_ORE(BlockID.COAL_ORE, "Coal ore", "coalore"),
    LOG(BlockID.LOG, "Log", "log", "tree", "pine", "oak", "birch", "redwood"),
    LEAVES(BlockID.LEAVES, "Leaves", "leaves", "leaf"),
    SPONGE(BlockID.SPONGE, "Sponge", "sponge"),
    GLASS(BlockID.GLASS, "Glass", "glass"),
    LAPIS_LAZULI_ORE(BlockID.LAPIS_LAZULI_ORE, "Lapis lazuli ore", "lapislazuliore", "blueore", "lapisore"),
    LAPIS_LAZULI(BlockID.LAPIS_LAZULI_BLOCK, "Lapis lazuli", "lapislazuli", "lapislazuliblock", "bluerock"),
    DISPENSER(BlockID.DISPENSER, "Dispenser", "dispenser"),
    SANDSTONE(BlockID.SANDSTONE, "Sandstone", "sandstone"),
    NOTE_BLOCK(BlockID.NOTE_BLOCK, "Note block", "musicblock", "noteblock", "note", "music", "instrument"),
    BED(BlockID.BED, "Bed", "bed"),
    POWERED_RAIL(BlockID.POWERED_RAIL, "Powered Rail", "poweredrail", "boosterrail", "poweredtrack", "boostertrack", "booster"),
    DETECTOR_RAIL(BlockID.DETECTOR_RAIL, "Detector Rail", "detectorrail", "detector"),
    PISTON_STICKY_BASE(BlockID.PISTON_STICKY_BASE, "Sticky Piston", "stickypiston"),
    WEB(BlockID.WEB, "Web", "web", "spiderweb"),
    LONG_GRASS(BlockID.LONG_GRASS, "Long grass", "longgrass", "tallgrass"),
    DEAD_BUSH(BlockID.DEAD_BUSH, "Shrub", "deadbush", "shrub", "deadshrub", "tumbleweed"),
    PISTON_BASE(BlockID.PISTON_BASE, "Piston", "piston"),
    PISTON_EXTENSION(BlockID.PISTON_EXTENSION, "Piston extension", "pistonextendsion", "pistonhead"),
    CLOTH(BlockID.CLOTH, "Wool", "cloth", "wool"),
    PISTON_MOVING_PIECE(BlockID.PISTON_MOVING_PIECE, "Piston moving piece", "movingpiston"),
    YELLOW_FLOWER(BlockID.YELLOW_FLOWER, "Yellow flower", "yellowflower", "flower"),
    RED_FLOWER(BlockID.RED_FLOWER, "Red rose", "redflower", "redrose", "rose"),
    BROWN_MUSHROOM(BlockID.BROWN_MUSHROOM, "Brown mushroom", "brownmushroom", "mushroom"),
    RED_MUSHROOM(BlockID.RED_MUSHROOM, "Red mushroom", "redmushroom"),
    GOLD_BLOCK(BlockID.GOLD_BLOCK, "Gold block", "gold", "goldblock"),
    IRON_BLOCK(BlockID.IRON_BLOCK, "Iron block", "iron", "ironblock"),
    DOUBLE_STEP(BlockID.DOUBLE_STEP, "Double step", "doubleslab", "doublestoneslab", "doublestep"),
    STEP(BlockID.STEP, "Step", "slab", "stoneslab", "step", "halfstep"),
    BRICK(BlockID.BRICK, "Brick", "brick", "brickblock"),
    TNT(BlockID.TNT, "TNT", "tnt", "c4", "explosive"),
    BOOKCASE(BlockID.BOOKCASE, "Bookcase", "bookshelf", "bookshelves", "bookcase", "bookcases"),
    MOSSY_COBBLESTONE(BlockID.MOSSY_COBBLESTONE, "Cobblestone (mossy)", "mossycobblestone", "mossstone", "mossystone", "mosscobble", "mossycobble", "moss", "mossy", "sossymobblecone"),
    OBSIDIAN(BlockID.OBSIDIAN, "Obsidian", "obsidian"),
    TORCH(BlockID.TORCH, "Torch", "torch", "light", "candle"),
    FIRE(BlockID.FIRE, "Fire", "fire", "flame", "flames"),
    MOB_SPAWNER(BlockID.MOB_SPAWNER, "Mob spawner", "mobspawner", "spawner"),
    WOODEN_STAIRS(BlockID.WOODEN_STAIRS, "Wooden stairs", "woodstair", "woodstairs", "woodenstair", "woodenstairs"),
    CHEST(BlockID.CHEST, "Chest", "chest", "storage", "storagechest"),
    REDSTONE_WIRE(BlockID.REDSTONE_WIRE, "Redstone wire", "redstone", "redstoneblock"),
    DIAMOND_ORE(BlockID.DIAMOND_ORE, "Diamond ore", "diamondore"),
    DIAMOND_BLOCK(BlockID.DIAMOND_BLOCK, "Diamond block", "diamond", "diamondblock"),
    WORKBENCH(BlockID.WORKBENCH, "Workbench", "workbench", "table", "craftingtable", "crafting"),
    CROPS(BlockID.CROPS, "Crops", "crops", "crop", "plant", "plants"),
    SOIL(BlockID.SOIL, "Soil", "soil", "farmland"),
    FURNACE(BlockID.FURNACE, "Furnace", "furnace"),
    BURNING_FURNACE(BlockID.BURNING_FURNACE, "Furnace (burning)", "burningfurnace", "litfurnace"),
    SIGN_POST(BlockID.SIGN_POST, "Sign post", "sign", "signpost"),
    WOODEN_DOOR(BlockID.WOODEN_DOOR, "Wooden door", "wooddoor", "woodendoor", "door"),
    LADDER(BlockID.LADDER, "Ladder", "ladder"),
    MINECART_TRACKS(BlockID.MINECART_TRACKS, "Minecart tracks", "track", "tracks", "minecrattrack", "minecarttracks", "rails", "rail"),
    COBBLESTONE_STAIRS(BlockID.COBBLESTONE_STAIRS, "Cobblestone stairs", "cobblestonestair", "cobblestonestairs", "cobblestair", "cobblestairs"),
    WALL_SIGN(BlockID.WALL_SIGN, "Wall sign", "wallsign"),
    LEVER(BlockID.LEVER, "Lever", "lever", "switch", "stonelever", "stoneswitch"),
    STONE_PRESSURE_PLATE(BlockID.STONE_PRESSURE_PLATE, "Stone pressure plate", "stonepressureplate", "stoneplate"),
    IRON_DOOR(BlockID.IRON_DOOR, "Iron Door", "irondoor"),
    WOODEN_PRESSURE_PLATE(BlockID.WOODEN_PRESSURE_PLATE, "Wooden pressure plate", "woodpressureplate", "woodplate", "woodenpressureplate", "woodenplate", "plate", "pressureplate"),
    REDSTONE_ORE(BlockID.REDSTONE_ORE, "Redstone ore", "redstoneore"),
    GLOWING_REDSTONE_ORE(BlockID.GLOWING_REDSTONE_ORE, "Glowing redstone ore", "glowingredstoneore"),
    REDSTONE_TORCH_OFF(BlockID.REDSTONE_TORCH_OFF, "Redstone torch (off)", "redstonetorchoff", "rstorchoff"),
    REDSTONE_TORCH_ON(BlockID.REDSTONE_TORCH_ON, "Redstone torch (on)", "redstonetorch", "redstonetorchon", "rstorchon", "redtorch"),
    STONE_BUTTON(BlockID.STONE_BUTTON, "Stone Button", "stonebutton", "button"),
    SNOW(BlockID.SNOW, "Snow", "snow"),
    ICE(BlockID.ICE, "Ice", "ice"),
    SNOW_BLOCK(BlockID.SNOW_BLOCK, "Snow block", "snowblock"),
    CACTUS(BlockID.CACTUS, "Cactus", "cactus", "cacti"),
    CLAY(BlockID.CLAY, "Clay", "clay"),
    SUGAR_CANE(BlockID.REED, "Reed", "reed", "cane", "sugarcane", "sugarcanes", "vine", "vines"),
    JUKEBOX(BlockID.JUKEBOX, "Jukebox", "jukebox", "stereo", "recordplayer"),
    FENCE(BlockID.FENCE, "Fence", "fence"),
    PUMPKIN(BlockID.PUMPKIN, "Pumpkin", "pumpkin"),
    NETHERRACK(BlockID.NETHERRACK, "Netherrack", "redmossycobblestone", "redcobblestone", "redmosstone", "redcobble", "netherstone", "netherrack", "nether", "hellstone"),
    SOUL_SAND(BlockID.SLOW_SAND, "Soul sand", "slowmud", "mud", "soulsand", "hellmud"),
    GLOWSTONE(BlockID.LIGHTSTONE, "Glowstone", "brittlegold", "glowstone", "lightstone", "brimstone", "australium"),
    PORTAL(BlockID.PORTAL, "Portal", "portal"),
    JACK_O_LANTERN(BlockID.JACKOLANTERN, "Pumpkin (on)", "pumpkinlighted", "pumpkinon", "litpumpkin", "jackolantern"),
    CAKE(BlockID.CAKE_BLOCK, "Cake", "cake", "cakeblock"),
    REDSTONE_REPEATER_OFF(BlockID.REDSTONE_REPEATER_OFF, "Redstone repeater (off)", "diodeoff", "redstonerepeater", "repeateroff", "delayeroff"),
    REDSTONE_REPEATER_ON(BlockID.REDSTONE_REPEATER_ON, "Redstone repeater (on)", "diodeon", "redstonerepeateron", "repeateron", "delayeron"),
    LOCKED_CHEST(BlockID.LOCKED_CHEST, "Locked chest", "lockedchest", "steveco", "supplycrate", "valveneedstoworkonep3nottf2kthx"),
    TRAP_DOOR(BlockID.TRAP_DOOR, "Trap door", "trapdoor", "hatch", "floordoor"),
    SILVERFISH_BLOCK(BlockID.SILVERFISH_BLOCK, "Silverfish block", "silverfish", "silver"),
    STONE_BRICK(BlockID.STONE_BRICK, "Stone brick", "stonebrick", "sbrick", "smoothstonebrick"),
    RED_MUSHROOM_CAP(BlockID.RED_MUSHROOM_CAP, "Red mushroom cap", "giantmushroomred", "redgiantmushroom", "redmushroomcap"),
    BROWN_MUSHROOM_CAP(BlockID.BROWN_MUSHROOM_CAP, "Brown mushroom cap", "giantmushroombrown", "browngiantmushoom", "brownmushroomcap"),
    IRON_BARS(BlockID.IRON_BARS, "Iron bars", "ironbars", "ironfence"),
    GLASS_PANE(BlockID.GLASS_PANE, "Glass pane", "window", "glasspane", "glasswindow"),
    MELON_BLOCK(BlockID.MELON_BLOCK, "Melon (block)", "melonblock"),
    PUMPKIN_STEM(BlockID.PUMPKIN_STEM, "Pumpkin stem", "pumpkinstem"),
    MELON_STEM(BlockID.MELON_STEM, "Melon stem", "melonstem"),
    VINE(BlockID.VINE, "Vine", "vine", "vines", "creepers"),
    FENCE_GATE(BlockID.FENCE_GATE, "Fence gate", "fencegate", "gate"),
    BRICK_STAIRS(BlockID.BRICK_STAIRS, "Brick stairs", "brickstairs", "bricksteps"),
    STONE_BRICK_STAIRS(BlockID.STONE_BRICK_STAIRS, "Stone brick stairs", "stonebrickstairs", "smoothstonebrickstairs");

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
     * HashSet for shouldPlaceLast.
     */
    private static final Set<Integer> shouldPlaceLast = new HashSet<Integer>();
    static {
        shouldPlaceLast.add(BlockID.SAPLING);
        shouldPlaceLast.add(BlockID.BED);
        shouldPlaceLast.add(BlockID.POWERED_RAIL);
        shouldPlaceLast.add(BlockID.DETECTOR_RAIL);
        shouldPlaceLast.add(BlockID.LONG_GRASS);
        shouldPlaceLast.add(BlockID.DEAD_BUSH);
        shouldPlaceLast.add(BlockID.PISTON_EXTENSION);
        shouldPlaceLast.add(BlockID.YELLOW_FLOWER);
        shouldPlaceLast.add(BlockID.RED_FLOWER);
        shouldPlaceLast.add(BlockID.BROWN_MUSHROOM);
        shouldPlaceLast.add(BlockID.RED_MUSHROOM);
        shouldPlaceLast.add(BlockID.TORCH);
        shouldPlaceLast.add(BlockID.FIRE);
        shouldPlaceLast.add(BlockID.REDSTONE_WIRE);
        shouldPlaceLast.add(BlockID.CROPS);
        shouldPlaceLast.add(BlockID.SIGN_POST);
        shouldPlaceLast.add(BlockID.WOODEN_DOOR);
        shouldPlaceLast.add(BlockID.LADDER);
        shouldPlaceLast.add(BlockID.MINECART_TRACKS);
        shouldPlaceLast.add(BlockID.WALL_SIGN);
        shouldPlaceLast.add(BlockID.LEVER);
        shouldPlaceLast.add(BlockID.STONE_PRESSURE_PLATE);
        shouldPlaceLast.add(BlockID.IRON_DOOR);
        shouldPlaceLast.add(BlockID.WOODEN_PRESSURE_PLATE);
        shouldPlaceLast.add(BlockID.REDSTONE_TORCH_OFF);
        shouldPlaceLast.add(BlockID.REDSTONE_TORCH_ON);
        shouldPlaceLast.add(BlockID.STONE_BUTTON);
        shouldPlaceLast.add(BlockID.SNOW);
        shouldPlaceLast.add(BlockID.CACTUS);
        shouldPlaceLast.add(BlockID.REED);
        shouldPlaceLast.add(BlockID.PORTAL);
        shouldPlaceLast.add(BlockID.CAKE_BLOCK);
        shouldPlaceLast.add(BlockID.REDSTONE_REPEATER_OFF);
        shouldPlaceLast.add(BlockID.REDSTONE_REPEATER_ON);
        shouldPlaceLast.add(BlockID.TRAP_DOOR);
        shouldPlaceLast.add(BlockID.VINE);
    }

    /**
     * Checks to see whether a block should be placed last.
     *
     * @param id
     * @return
     */
    public static boolean shouldPlaceLast(int id) {
        return shouldPlaceLast.contains(id);
    }

    /**
     * Checks to see whether this block should be placed last.
     *
     * @return
     */
    public boolean shouldPlaceLast() {
        return shouldPlaceLast.contains(id);
    }

    /**
     * HashSet for canPassThrough.
     */
    private static final Set<Integer> canPassThrough = new HashSet<Integer>();
    static {
        canPassThrough.add(BlockID.AIR);
        canPassThrough.add(BlockID.WATER);
        canPassThrough.add(BlockID.STATIONARY_WATER);
        canPassThrough.add(BlockID.SAPLING);
        canPassThrough.add(BlockID.POWERED_RAIL);
        canPassThrough.add(BlockID.DETECTOR_RAIL);
        canPassThrough.add(BlockID.WEB);
        canPassThrough.add(BlockID.LONG_GRASS);
        canPassThrough.add(BlockID.DEAD_BUSH);
        canPassThrough.add(BlockID.YELLOW_FLOWER);
        canPassThrough.add(BlockID.RED_FLOWER);
        canPassThrough.add(BlockID.BROWN_MUSHROOM);
        canPassThrough.add(BlockID.RED_MUSHROOM);
        canPassThrough.add(BlockID.TORCH);
        canPassThrough.add(BlockID.FIRE);
        canPassThrough.add(BlockID.REDSTONE_WIRE);
        canPassThrough.add(BlockID.CROPS);
        canPassThrough.add(BlockID.SIGN_POST);
        canPassThrough.add(BlockID.LADDER);
        canPassThrough.add(BlockID.MINECART_TRACKS);
        canPassThrough.add(BlockID.WALL_SIGN);
        canPassThrough.add(BlockID.LEVER);
        canPassThrough.add(BlockID.STONE_PRESSURE_PLATE);
        canPassThrough.add(BlockID.WOODEN_PRESSURE_PLATE);
        canPassThrough.add(BlockID.REDSTONE_TORCH_OFF);
        canPassThrough.add(BlockID.REDSTONE_TORCH_ON);
        canPassThrough.add(BlockID.STONE_BUTTON);
        canPassThrough.add(BlockID.SNOW);
        canPassThrough.add(BlockID.REED);
        canPassThrough.add(BlockID.PORTAL);
        canPassThrough.add(BlockID.REDSTONE_REPEATER_OFF);
        canPassThrough.add(BlockID.REDSTONE_REPEATER_ON);
        canPassThrough.add(BlockID.VINE);
    }

    /**
     * Checks whether a block can be passed through.
     *
     * @param id
     * @return
     */
    public static boolean canPassThrough(int id) {
        return canPassThrough.contains(id);

    }

    /**
     * HashSet for usesData.
     */
    private static final Set<Integer> usesData = new HashSet<Integer>();
    static {
        usesData.add(BlockID.SAPLING);
        usesData.add(BlockID.WATER);
        usesData.add(BlockID.STATIONARY_WATER);
        usesData.add(BlockID.LAVA);
        usesData.add(BlockID.STATIONARY_LAVA);
        usesData.add(BlockID.LOG);
        usesData.add(BlockID.LEAVES);
        usesData.add(BlockID.DISPENSER);
        usesData.add(BlockID.NOTE_BLOCK);
        usesData.add(BlockID.BED);
        usesData.add(BlockID.POWERED_RAIL);
        usesData.add(BlockID.DETECTOR_RAIL);
        usesData.add(BlockID.PISTON_STICKY_BASE);
        usesData.add(BlockID.LONG_GRASS);
        usesData.add(BlockID.PISTON_BASE);
        usesData.add(BlockID.PISTON_EXTENSION);
        usesData.add(BlockID.CLOTH);
        usesData.add(BlockID.DOUBLE_STEP);
        usesData.add(BlockID.STEP);
        usesData.add(BlockID.TORCH);
        usesData.add(BlockID.WOODEN_STAIRS);
        usesData.add(BlockID.REDSTONE_WIRE);
        usesData.add(BlockID.CROPS);
        usesData.add(BlockID.SOIL);
        usesData.add(BlockID.FURNACE);
        usesData.add(BlockID.BURNING_FURNACE);
        usesData.add(BlockID.SIGN_POST);
        usesData.add(BlockID.WOODEN_DOOR);
        usesData.add(BlockID.LADDER);
        usesData.add(BlockID.MINECART_TRACKS);
        usesData.add(BlockID.COBBLESTONE_STAIRS);
        usesData.add(BlockID.WALL_SIGN);
        usesData.add(BlockID.LEVER);
        usesData.add(BlockID.STONE_PRESSURE_PLATE);
        usesData.add(BlockID.IRON_DOOR);
        usesData.add(BlockID.WOODEN_PRESSURE_PLATE);
        usesData.add(BlockID.REDSTONE_TORCH_OFF);
        usesData.add(BlockID.REDSTONE_TORCH_ON);
        usesData.add(BlockID.STONE_BUTTON);
        usesData.add(BlockID.SNOW);
        usesData.add(BlockID.CACTUS);
        usesData.add(BlockID.PUMPKIN);
        usesData.add(BlockID.JACKOLANTERN);
        usesData.add(BlockID.CAKE_BLOCK);
        usesData.add(BlockID.REDSTONE_REPEATER_OFF);
        usesData.add(BlockID.REDSTONE_REPEATER_ON);
        usesData.add(BlockID.TRAP_DOOR);
        usesData.add(BlockID.SILVERFISH_BLOCK);
        usesData.add(BlockID.STONE_BRICK);
        usesData.add(BlockID.RED_MUSHROOM_CAP);
        usesData.add(BlockID.BROWN_MUSHROOM_CAP);
        usesData.add(BlockID.PUMPKIN_STEM);
        usesData.add(BlockID.MELON_STEM);
        usesData.add(BlockID.VINE);
        usesData.add(BlockID.FENCE_GATE);
        usesData.add(BlockID.BRICK_STAIRS);
        usesData.add(BlockID.STONE_BRICK_STAIRS);
    }

    /**
     * Returns true if the block uses its data value.
     *
     * @param id
     * @return
     */
    public static boolean usesData(int id) {
        return usesData.contains(id);
    }

    /**
     * HashSet for isContainerBlock.
     */
    private static final Set<Integer> isContainerBlock = new HashSet<Integer>();
    static {
        isContainerBlock.add(BlockID.DISPENSER);
        isContainerBlock.add(BlockID.FURNACE);
        isContainerBlock.add(BlockID.BURNING_FURNACE);
        isContainerBlock.add(BlockID.CHEST);
    }

    /**
     * Returns true if the block is a container block.
     *
     * @param id
     * @return
     */
    public static boolean isContainerBlock(int id) {
        return isContainerBlock.contains(id);
    }

    /**
     * HashSet for isRedstoneBlock.
     */
    private static final Set<Integer> isRedstoneBlock = new HashSet<Integer>();
    static {
        isRedstoneBlock.add(BlockID.POWERED_RAIL);
        isRedstoneBlock.add(BlockID.DETECTOR_RAIL);
        isRedstoneBlock.add(BlockID.PISTON_STICKY_BASE);
        isRedstoneBlock.add(BlockID.PISTON_BASE);
        isRedstoneBlock.add(BlockID.LEVER);
        isRedstoneBlock.add(BlockID.STONE_PRESSURE_PLATE);
        isRedstoneBlock.add(BlockID.WOODEN_PRESSURE_PLATE);
        isRedstoneBlock.add(BlockID.REDSTONE_TORCH_OFF);
        isRedstoneBlock.add(BlockID.REDSTONE_TORCH_ON);
        isRedstoneBlock.add(BlockID.STONE_BUTTON);
        isRedstoneBlock.add(BlockID.REDSTONE_WIRE);
        isRedstoneBlock.add(BlockID.WOODEN_DOOR);
        isRedstoneBlock.add(BlockID.IRON_DOOR);
        isRedstoneBlock.add(BlockID.TNT);
        isRedstoneBlock.add(BlockID.DISPENSER);
        isRedstoneBlock.add(BlockID.NOTE_BLOCK);
        isRedstoneBlock.add(BlockID.REDSTONE_REPEATER_OFF);
        isRedstoneBlock.add(BlockID.REDSTONE_REPEATER_ON);
    }

    /**
     * Returns true if a block uses redstone in some way.
     *
     * @param id
     * @return
     */
    public static boolean isRedstoneBlock(int id) {
        return isRedstoneBlock.contains(id);
    }

    /**
     * HashSet for canTransferRedstone.
     */
    private static final Set<Integer> canTransferRedstone = new HashSet<Integer>();
    static {
        canTransferRedstone.add(BlockID.REDSTONE_TORCH_OFF);
        canTransferRedstone.add(BlockID.REDSTONE_TORCH_ON);
        canTransferRedstone.add(BlockID.REDSTONE_WIRE);
        canTransferRedstone.add(BlockID.REDSTONE_REPEATER_OFF);
        canTransferRedstone.add(BlockID.REDSTONE_REPEATER_ON);
    }

    /**
     * Returns true if a block can transfer redstone.
     * Made this since isRedstoneBlock was getting big.
     *
     * @param id
     * @return
     */
    public static boolean canTransferRedstone(int id) {
        return canTransferRedstone.contains(id);
    }

    /**
     * HashSet for isRedstoneSource.
     */
    private static final Set<Integer> isRedstoneSource = new HashSet<Integer>();
    static {
        isRedstoneSource.add(BlockID.DETECTOR_RAIL);
        isRedstoneSource.add(BlockID.REDSTONE_TORCH_OFF);
        isRedstoneSource.add(BlockID.REDSTONE_TORCH_ON);
        isRedstoneSource.add(BlockID.LEVER);
        isRedstoneSource.add(BlockID.STONE_PRESSURE_PLATE);
        isRedstoneSource.add(BlockID.WOODEN_PRESSURE_PLATE);
        isRedstoneSource.add(BlockID.STONE_BUTTON);
    }

    /**
     * Yay for convenience methods.
     *
     * @param id
     * @return
     */
    public static boolean isRedstoneSource(int id) {
        return isRedstoneSource.contains(id);
    }

    /**
     * HashSet for isRailBlock.
     */
    private static final Set<Integer> isRailBlock = new HashSet<Integer>();
    static {
        isRailBlock.add(BlockID.POWERED_RAIL);
        isRailBlock.add(BlockID.DETECTOR_RAIL);
        isRailBlock.add(BlockID.MINECART_TRACKS);
    }

    /**
     * Checks if the id is that of one of the rail types
     *
     * @param id
     * @return
     */
    public static boolean isRailBlock(int id) {
        return isRailBlock.contains(id);
    }

    /**
     * HashSet for isNaturalBlock.
     */
    private static final Set<Integer> isNaturalTerrainBlock = new HashSet<Integer>();
    static {
        isNaturalTerrainBlock.add(BlockID.STONE);
        isNaturalTerrainBlock.add(BlockID.GRASS);
        isNaturalTerrainBlock.add(BlockID.DIRT);
        // isNaturalBlock.add(BlockID.COBBLESTONE); // technically can occur next to water and lava
        isNaturalTerrainBlock.add(BlockID.BEDROCK);
        isNaturalTerrainBlock.add(BlockID.SAND);
        isNaturalTerrainBlock.add(BlockID.GRAVEL);
        isNaturalTerrainBlock.add(BlockID.CLAY);

        // hell
        isNaturalTerrainBlock.add(BlockID.NETHERSTONE);
        isNaturalTerrainBlock.add(BlockID.SLOW_SAND);
        isNaturalTerrainBlock.add(BlockID.LIGHTSTONE);

        // ores
        isNaturalTerrainBlock.add(BlockID.COAL_ORE);
        isNaturalTerrainBlock.add(BlockID.IRON_ORE);
        isNaturalTerrainBlock.add(BlockID.GOLD_ORE);
        isNaturalTerrainBlock.add(BlockID.LAPIS_LAZULI_ORE);
        isNaturalTerrainBlock.add(BlockID.DIAMOND_ORE);
        isNaturalTerrainBlock.add(BlockID.REDSTONE_ORE);
        isNaturalTerrainBlock.add(BlockID.GLOWING_REDSTONE_ORE);
    }

    /**
     * Checks if the block type is naturally occuring
     *
     * @param id
     * @return
     */
    public static boolean isNaturalTerrainBlock(int id) {
        return isNaturalTerrainBlock.contains(id);
    }

    /**
     * HashMap for getDroppedBlock.
     */
    private static final Map<Integer, Integer> blockDrops = new HashMap<Integer, Integer>();
    static {
        blockDrops.put(BlockID.STONE, BlockID.COBBLESTONE);
        blockDrops.put(BlockID.GRASS, BlockID.DIRT);
        blockDrops.put(BlockID.DIRT, BlockID.DIRT);
        blockDrops.put(BlockID.COBBLESTONE, BlockID.COBBLESTONE);
        blockDrops.put(BlockID.WOOD, BlockID.WOOD);
        blockDrops.put(BlockID.SAPLING, BlockID.SAPLING);
        blockDrops.put(BlockID.BEDROCK, -1);
        blockDrops.put(BlockID.SAND, BlockID.SAND);
        blockDrops.put(BlockID.GRAVEL, BlockID.GRAVEL);
        blockDrops.put(BlockID.GOLD_ORE, BlockID.GOLD_ORE);
        blockDrops.put(BlockID.IRON_ORE, BlockID.IRON_ORE);
        blockDrops.put(BlockID.COAL_ORE, BlockID.COAL_ORE);
        blockDrops.put(BlockID.LOG, BlockID.LOG);
        blockDrops.put(BlockID.LEAVES, BlockID.LEAVES);
        blockDrops.put(BlockID.SPONGE, BlockID.SPONGE);
        blockDrops.put(BlockID.GLASS, BlockID.GLASS); // Have to drop glass for //undo
        blockDrops.put(BlockID.LAPIS_LAZULI_ORE, BlockID.LAPIS_LAZULI_ORE); // Block damage drops not implemented
        blockDrops.put(BlockID.LAPIS_LAZULI_BLOCK, BlockID.LAPIS_LAZULI_BLOCK);
        blockDrops.put(BlockID.DISPENSER, BlockID.DISPENSER);
        blockDrops.put(BlockID.SANDSTONE, BlockID.SANDSTONE);
        blockDrops.put(BlockID.NOTE_BLOCK, BlockID.NOTE_BLOCK);
        blockDrops.put(BlockID.BED, ItemID.BED_ITEM);
        blockDrops.put(BlockID.POWERED_RAIL, BlockID.POWERED_RAIL);
        blockDrops.put(BlockID.DETECTOR_RAIL, BlockID.DETECTOR_RAIL);
        blockDrops.put(BlockID.WEB, BlockID.WEB);
        blockDrops.put(BlockID.PISTON_EXTENSION, -1);
        blockDrops.put(BlockID.CLOTH, BlockID.CLOTH);
        blockDrops.put(BlockID.PISTON_MOVING_PIECE, -1);
        blockDrops.put(BlockID.YELLOW_FLOWER, BlockID.YELLOW_FLOWER);
        blockDrops.put(BlockID.RED_FLOWER, BlockID.RED_FLOWER);
        blockDrops.put(BlockID.BROWN_MUSHROOM, BlockID.BROWN_MUSHROOM);
        blockDrops.put(BlockID.RED_MUSHROOM, BlockID.RED_MUSHROOM);
        blockDrops.put(BlockID.GOLD_BLOCK, BlockID.GOLD_BLOCK);
        blockDrops.put(BlockID.IRON_BLOCK, BlockID.IRON_BLOCK);
        blockDrops.put(BlockID.DOUBLE_STEP, BlockID.DOUBLE_STEP);
        blockDrops.put(BlockID.STEP, BlockID.STEP);
        blockDrops.put(BlockID.BRICK, BlockID.BRICK);
        blockDrops.put(BlockID.BOOKCASE, BlockID.BOOKCASE);
        blockDrops.put(BlockID.MOSSY_COBBLESTONE, BlockID.MOSSY_COBBLESTONE);
        blockDrops.put(BlockID.OBSIDIAN, BlockID.OBSIDIAN);
        blockDrops.put(BlockID.TORCH, BlockID.TORCH);
        blockDrops.put(BlockID.WOODEN_STAIRS, BlockID.WOODEN_STAIRS);
        blockDrops.put(BlockID.CHEST, BlockID.CHEST);
        blockDrops.put(BlockID.REDSTONE_WIRE, ItemID.REDSTONE_DUST);
        blockDrops.put(BlockID.DIAMOND_ORE, ItemID.DIAMOND);
        blockDrops.put(BlockID.DIAMOND_BLOCK, BlockID.DIAMOND_BLOCK);
        blockDrops.put(BlockID.WORKBENCH, BlockID.WORKBENCH);
        blockDrops.put(BlockID.CROPS, ItemID.SEEDS);
        blockDrops.put(BlockID.SOIL, BlockID.SOIL);
        blockDrops.put(BlockID.FURNACE, BlockID.FURNACE);
        blockDrops.put(BlockID.BURNING_FURNACE, BlockID.FURNACE);
        blockDrops.put(BlockID.SIGN_POST, ItemID.SIGN);
        blockDrops.put(BlockID.WOODEN_DOOR, ItemID.WOODEN_DOOR_ITEM);
        blockDrops.put(BlockID.LADDER, BlockID.LADDER);
        blockDrops.put(BlockID.MINECART_TRACKS, BlockID.MINECART_TRACKS);
        blockDrops.put(BlockID.COBBLESTONE_STAIRS, BlockID.COBBLESTONE_STAIRS);
        blockDrops.put(BlockID.WALL_SIGN, ItemID.SIGN);
        blockDrops.put(BlockID.LEVER, BlockID.LEVER);
        blockDrops.put(BlockID.STONE_PRESSURE_PLATE, BlockID.STONE_PRESSURE_PLATE);
        blockDrops.put(BlockID.IRON_DOOR, ItemID.IRON_DOOR_ITEM);
        blockDrops.put(BlockID.WOODEN_PRESSURE_PLATE, BlockID.WOODEN_PRESSURE_PLATE);
        blockDrops.put(BlockID.REDSTONE_ORE, ItemID.REDSTONE_DUST);
        blockDrops.put(BlockID.GLOWING_REDSTONE_ORE, ItemID.REDSTONE_DUST);
        blockDrops.put(BlockID.REDSTONE_TORCH_OFF, BlockID.REDSTONE_TORCH_ON);
        blockDrops.put(BlockID.REDSTONE_TORCH_ON, BlockID.REDSTONE_TORCH_ON);
        blockDrops.put(BlockID.STONE_BUTTON, BlockID.STONE_BUTTON);
        blockDrops.put(BlockID.SNOW, ItemID.SNOWBALL);
        blockDrops.put(BlockID.ICE, BlockID.ICE);
        blockDrops.put(BlockID.SNOW_BLOCK, BlockID.SNOW_BLOCK);
        blockDrops.put(BlockID.CLAY, BlockID.CLAY);
        blockDrops.put(BlockID.REED, ItemID.SUGAR_CANE_ITEM);
        blockDrops.put(BlockID.JUKEBOX, BlockID.JUKEBOX);
        blockDrops.put(BlockID.FENCE, BlockID.FENCE);
        blockDrops.put(BlockID.PUMPKIN, BlockID.PUMPKIN);
        blockDrops.put(BlockID.NETHERRACK, BlockID.NETHERRACK);
        blockDrops.put(BlockID.SLOW_SAND, BlockID.SLOW_SAND);
        blockDrops.put(BlockID.LIGHTSTONE, ItemID.LIGHTSTONE_DUST);
        blockDrops.put(BlockID.JACKOLANTERN, BlockID.JACKOLANTERN);
        blockDrops.put(BlockID.CAKE_BLOCK, ItemID.CAKE_ITEM);
        blockDrops.put(BlockID.REDSTONE_REPEATER_OFF, ItemID.REDSTONE_REPEATER);
        blockDrops.put(BlockID.REDSTONE_REPEATER_ON, ItemID.REDSTONE_REPEATER);
        blockDrops.put(BlockID.LOCKED_CHEST, BlockID.LOCKED_CHEST);
        blockDrops.put(BlockID.TRAP_DOOR, BlockID.TRAP_DOOR);
        blockDrops.put(BlockID.SILVERFISH_BLOCK, -1);
        blockDrops.put(BlockID.STONE_BRICK, BlockID.STONE_BRICK);
        blockDrops.put(BlockID.BROWN_MUSHROOM_CAP, BlockID.BROWN_MUSHROOM_CAP); // the wiki has the 2 mushroom caps the other way round
        blockDrops.put(BlockID.RED_MUSHROOM_CAP, BlockID.RED_MUSHROOM_CAP);
        blockDrops.put(BlockID.IRON_BARS, BlockID.IRON_BARS);
        blockDrops.put(BlockID.GLASS_PANE, BlockID.GLASS_PANE);
        blockDrops.put(BlockID.MELON_BLOCK, BlockID.MELON_BLOCK);
        blockDrops.put(BlockID.PUMPKIN_STEM, BlockID.PUMPKIN_STEM);
        blockDrops.put(BlockID.MELON_STEM, BlockID.MELON_STEM);
        blockDrops.put(BlockID.VINE, -1);
        blockDrops.put(BlockID.FENCE_GATE, BlockID.FENCE_GATE);
        blockDrops.put(BlockID.BRICK_STAIRS, BlockID.BRICK);
        blockDrops.put(BlockID.STONE_BRICK_STAIRS, BlockID.STONE_BRICK);
    }

    /**
     * Get the block or item that would have been dropped. If nothing is
     * dropped, 0 will be returned. If the block should not be destroyed
     * (i.e. bedrock), -1 will be returned.
     *
     * @param id
     * @return
     * @deprecated This function ignores the data value.
     */
    @Deprecated
    public static int getDroppedBlock(int id) {
        Integer dropped = blockDrops.get(id);
        if (dropped == null) {
            return BlockID.AIR;
        }
        return dropped;
    }

    private static final Random random = new Random();
    public static BaseItemStack getBlockDrop(int id, short data) {
        switch (id) {
        case BlockID.STONE:
            return new BaseItemStack(BlockID.COBBLESTONE);

        case BlockID.GRASS:
            return new BaseItemStack(BlockID.DIRT);

        case BlockID.GRAVEL:
            if (random.nextDouble() >= 0.9) {
                return new BaseItemStack(ItemID.FLINT);
            } else {
                return new BaseItemStack(BlockID.GRAVEL);
            }

        case BlockID.COAL_ORE:
            return new BaseItemStack(ItemID.COAL);

        case BlockID.LEAVES:
            if (random.nextDouble() > 0.95) {
                return new BaseItemStack(BlockID.SAPLING, 1, data);
            } else {
                return null;
            }

        case BlockID.LAPIS_LAZULI_ORE:
            return new BaseItemStack(ItemID.INK_SACK, random.nextInt(5) + 4, (short) 4);

        case BlockID.BED:
            return new BaseItemStack(ItemID.BED_ITEM);

        case BlockID.LONG_GRASS:
            if (random.nextInt(8) == 0) {
                return new BaseItemStack(ItemID.SEEDS);
            } else {
                return null;
            }

        case BlockID.DOUBLE_STEP:
            return new BaseItemStack(BlockID.STEP, 2, data);

        case BlockID.WOODEN_STAIRS:
            return new BaseItemStack(BlockID.WOOD);

        case BlockID.REDSTONE_WIRE:
            return new BaseItemStack(ItemID.REDSTONE_DUST);

        case BlockID.DIAMOND_ORE:
            return new BaseItemStack(ItemID.DIAMOND);

        case BlockID.CROPS:
            return new BaseItemStack(ItemID.SEEDS);

        case BlockID.SOIL:
            return new BaseItemStack(BlockID.DIRT);

        case BlockID.BURNING_FURNACE:
            return new BaseItemStack(BlockID.FURNACE);

        case BlockID.SIGN_POST:
            return new BaseItemStack(ItemID.SIGN);

        case BlockID.WOODEN_DOOR:
            return new BaseItemStack(ItemID.WOODEN_DOOR_ITEM);

        case BlockID.COBBLESTONE_STAIRS:
            return new BaseItemStack(BlockID.COBBLESTONE);

        case BlockID.WALL_SIGN:
            return new BaseItemStack(ItemID.SIGN);

        case BlockID.IRON_DOOR:
            return new BaseItemStack(ItemID.IRON_DOOR_ITEM);

        case BlockID.REDSTONE_ORE:
        case BlockID.GLOWING_REDSTONE_ORE:
            return new BaseItemStack(ItemID.REDSTONE_DUST, (random.nextInt(2) + 4));

        case BlockID.REDSTONE_TORCH_OFF:
            return new BaseItemStack(BlockID.REDSTONE_TORCH_ON);

        case BlockID.CLAY:
            return new BaseItemStack(ItemID.CLAY_BALL, 4);

        case BlockID.REED:
            return new BaseItemStack(ItemID.SUGAR_CANE_ITEM);

        case BlockID.LIGHTSTONE:
            return new BaseItemStack(ItemID.LIGHTSTONE_DUST, (random.nextInt(3) + 2));

        case BlockID.REDSTONE_REPEATER_OFF:
        case BlockID.REDSTONE_REPEATER_ON:
            return new BaseItemStack(ItemID.REDSTONE_REPEATER);

        case BlockID.MELON_BLOCK:
            return new BaseItemStack(ItemID.MELON, (random.nextInt(5) + 3));

        case BlockID.PUMPKIN_STEM:
            return new BaseItemStack(ItemID.PUMPKIN_SEEDS);

        case BlockID.MELON_STEM:
            return new BaseItemStack(ItemID.MELON_SEEDS);

        case BlockID.BEDROCK:
        case BlockID.WATER:
        case BlockID.STATIONARY_WATER:
        case BlockID.LAVA:
        case BlockID.STATIONARY_LAVA:
        case BlockID.GLASS:
        case BlockID.PISTON_EXTENSION:
        case BlockID.BOOKCASE:
        case BlockID.FIRE:
        case BlockID.MOB_SPAWNER:
        case BlockID.SNOW:
        case BlockID.ICE:
        case BlockID.PORTAL:
        case BlockID.AIR:
        case BlockID.LOCKED_CHEST:
        case BlockID.SILVERFISH_BLOCK:
        case BlockID.VINE:
            return null;
        }
        if (usesData(id)) {
            return new BaseItemStack(id, 1, data);
        } else {
            return new BaseItemStack(id);
        }
    }

}
