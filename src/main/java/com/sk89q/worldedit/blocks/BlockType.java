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
import com.sk89q.worldedit.PlayerDirection;

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
    STONE_BRICK_STAIRS(BlockID.STONE_BRICK_STAIRS, "Stone brick stairs", "stonebrickstairs", "smoothstonebrickstairs"),
    MYCELIUM(BlockID.MYCELIUM, "Mycelium", "fungus" ,"mycel"),
    LILY_PAD(BlockID.LILY_PAD, "Lily pad", "lilypad", "waterlily"),
    NETHER_BRICK(BlockID.NETHER_BRICK, "Nether brick", "netherbrick"),
    NETHER_BRICK_FENCE(BlockID.NETHER_BRICK_FENCE, "Nether brick fence", "netherbrickfence", "netherfence"),
    NETHER_BRICK_STAIRS(BlockID.NETHER_BRICK_STAIRS, "Nether brick stairs", "netherbrickstairs", "netherbricksteps", "netherstairs", "nethersteps"),
    NETHER_WART(BlockID.NETHER_WART, "Nether wart", "netherwart", "netherstalk"),
    ENCHANTMENT_TABLE(BlockID.ENCHANTMENT_TABLE, "Enchantment table", "enchantmenttable", "enchanttable"),
    BREWING_STAND(BlockID.BREWING_STAND, "Brewing Stand", "brewingstand"),
    CAULDRON(BlockID.CAULDRON, "Cauldron"),
    END_PORTAL(BlockID.END_PORTAL, "End Portal", "endportal", "blackstuff", "airportal", "weirdblackstuff"),
    END_PORTAL_FRAME(BlockID.END_PORTAL_FRAME, "End Portal Frame", "endportalframe", "airportalframe", "crystalblock"),
    END_STONE(BlockID.END_STONE, "End Stone", "endstone", "enderstone", "endersand");

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
        this.lookupKeys = new String[] { lookupKey };
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
        shouldPlaceLast.add(BlockID.LILY_PAD);
        shouldPlaceLast.add(BlockID.NETHER_WART);
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
        canPassThrough.add(BlockID.PUMPKIN_STEM);
        canPassThrough.add(BlockID.MELON_STEM);
        canPassThrough.add(BlockID.VINE);
        canPassThrough.add(BlockID.LILY_PAD);
        canPassThrough.add(BlockID.NETHER_WART);
        canPassThrough.add(BlockID.END_PORTAL);
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
     * Checks whether a block can be passed through.
     *
     * @return
     */
    public boolean canPassThrough() {
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
        usesData.add(BlockID.FIRE);
        usesData.add(BlockID.WOODEN_STAIRS);
        usesData.add(BlockID.CHEST);
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
        usesData.add(BlockID.REED);
        usesData.add(BlockID.JUKEBOX);
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
        usesData.add(BlockID.NETHER_BRICK_STAIRS);
        usesData.add(BlockID.NETHER_WART);
        usesData.add(BlockID.ENCHANTMENT_TABLE);
        usesData.add(BlockID.BREWING_STAND);
        usesData.add(BlockID.CAULDRON);
        usesData.add(BlockID.END_PORTAL_FRAME);
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
     * Returns true if the block uses its data value.
     *
     * @return
     */
    public boolean usesData() {
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
        isContainerBlock.add(BlockID.BREWING_STAND);
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
     * Returns true if the block is a container block.
     *
     * @return
     */
    public boolean isContainerBlock() {
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
     * Returns true if a block uses redstone in some way.
     *
     * @return
     */
    public boolean isRedstoneBlock() {
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
     * Returns true if a block can transfer redstone.
     * Made this since isRedstoneBlock was getting big.
     *
     * @return
     */
    public boolean canTransferRedstone() {
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
     * Yay for convenience methods.
     *
     * @return
     */
    public boolean isRedstoneSource() {
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
     * Checks if the id is that of one of the rail types
     *
     * @return
     */
    public boolean isRailBlock() {
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
        isNaturalTerrainBlock.add(BlockID.MYCELIUM);

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
     * Checks if the block type is naturally occuring
     *
     * @return
     */
    public boolean isNaturalTerrainBlock() {
        return isNaturalTerrainBlock.contains(id);
    }

    /**
     * HashSet for emitsLight.
     */
    private static final Set<Integer> emitsLight = new HashSet<Integer>();
    static {
        emitsLight.add(BlockID.LAVA);
        emitsLight.add(BlockID.STATIONARY_LAVA);
        emitsLight.add(BlockID.BROWN_MUSHROOM);
        emitsLight.add(BlockID.RED_MUSHROOM);
        emitsLight.add(BlockID.TORCH);
        emitsLight.add(BlockID.FIRE);
        emitsLight.add(BlockID.BURNING_FURNACE);
        emitsLight.add(BlockID.GLOWING_REDSTONE_ORE);
        emitsLight.add(BlockID.REDSTONE_TORCH_ON);
        emitsLight.add(BlockID.LIGHTSTONE);
        emitsLight.add(BlockID.PORTAL);
        emitsLight.add(BlockID.JACKOLANTERN);
        emitsLight.add(BlockID.REDSTONE_REPEATER_ON);
        emitsLight.add(BlockID.LOCKED_CHEST);
        emitsLight.add(BlockID.BROWN_MUSHROOM_CAP);
        emitsLight.add(BlockID.RED_MUSHROOM_CAP);
        emitsLight.add(BlockID.END_PORTAL);
    }

    /**
     * Checks if the block type emits light
     *
     * @param id
     * @return
     */
    public static boolean emitsLight(int id) {
        return emitsLight.contains(id);
    }

    /**
     * HashSet for isTranslucent.
     */
    private static final Set<Integer> isTranslucent = new HashSet<Integer>();
    static {
        isTranslucent.add(BlockID.AIR);
        isTranslucent.add(BlockID.SAPLING);
        isTranslucent.add(BlockID.WATER);
        isTranslucent.add(BlockID.STATIONARY_WATER);
        //isTranslucent.add(BlockID.LEAVES);
        isTranslucent.add(BlockID.GLASS);
        isTranslucent.add(BlockID.BED);
        isTranslucent.add(BlockID.POWERED_RAIL);
        isTranslucent.add(BlockID.DETECTOR_RAIL);
        //isTranslucent.add(BlockID.PISTON_STICKY_BASE);
        isTranslucent.add(BlockID.WEB);
        isTranslucent.add(BlockID.LONG_GRASS);
        isTranslucent.add(BlockID.DEAD_BUSH);
        //isTranslucent.add(BlockID.PISTON_BASE);
        //isTranslucent.add(BlockID.PISTON_EXTENSION);
        //isTranslucent.add(BlockID.PISTON_MOVING_PIECE);
        isTranslucent.add(BlockID.YELLOW_FLOWER);
        isTranslucent.add(BlockID.RED_FLOWER);
        isTranslucent.add(BlockID.BROWN_MUSHROOM);
        isTranslucent.add(BlockID.RED_MUSHROOM);
        isTranslucent.add(BlockID.TORCH);
        isTranslucent.add(BlockID.FIRE);
        //isTranslucent.add(BlockID.MOB_SPAWNER);
        //isTranslucent.add(BlockID.WOODEN_STAIRS);
        isTranslucent.add(BlockID.REDSTONE_WIRE);
        isTranslucent.add(BlockID.CROPS);
        isTranslucent.add(BlockID.SIGN_POST);
        isTranslucent.add(BlockID.WOODEN_DOOR);
        isTranslucent.add(BlockID.LADDER);
        isTranslucent.add(BlockID.MINECART_TRACKS);
        //isTranslucent.add(BlockID.COBBLESTONE_STAIRS);
        isTranslucent.add(BlockID.WALL_SIGN);
        isTranslucent.add(BlockID.LEVER);
        isTranslucent.add(BlockID.STONE_PRESSURE_PLATE);
        isTranslucent.add(BlockID.IRON_DOOR);
        isTranslucent.add(BlockID.WOODEN_PRESSURE_PLATE);
        isTranslucent.add(BlockID.REDSTONE_TORCH_OFF);
        isTranslucent.add(BlockID.REDSTONE_TORCH_ON);
        isTranslucent.add(BlockID.STONE_BUTTON);
        isTranslucent.add(BlockID.SNOW);
        isTranslucent.add(BlockID.ICE);
        //isTranslucent.add(BlockID.CACTUS);
        isTranslucent.add(BlockID.REED);
        isTranslucent.add(BlockID.FENCE);
        isTranslucent.add(BlockID.PORTAL);
        isTranslucent.add(BlockID.CAKE_BLOCK);
        isTranslucent.add(BlockID.REDSTONE_REPEATER_OFF);
        isTranslucent.add(BlockID.REDSTONE_REPEATER_ON);
        isTranslucent.add(BlockID.TRAP_DOOR);
        isTranslucent.add(BlockID.IRON_BARS);
        isTranslucent.add(BlockID.GLASS_PANE);
        isTranslucent.add(BlockID.PUMPKIN_STEM);
        isTranslucent.add(BlockID.MELON_STEM);
        isTranslucent.add(BlockID.VINE);
        isTranslucent.add(BlockID.FENCE_GATE);
        isTranslucent.add(BlockID.BRICK_STAIRS);
        isTranslucent.add(BlockID.STONE_BRICK_STAIRS);
        isTranslucent.add(BlockID.LILY_PAD);
        isTranslucent.add(BlockID.NETHER_BRICK_FENCE);
        isTranslucent.add(BlockID.NETHER_BRICK_STAIRS);
        isTranslucent.add(BlockID.NETHER_WART);
        isTranslucent.add(BlockID.ENCHANTMENT_TABLE);
        isTranslucent.add(BlockID.BREWING_STAND);
        isTranslucent.add(BlockID.CAULDRON);
    }

    /**
     * Checks if the block type lets light through
     *
     * @param id
     * @return
     */
    public static boolean isTranslucent(int id) {
        return isTranslucent.contains(id);
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
        blockDrops.put(BlockID.BROWN_MUSHROOM_CAP, BlockID.BROWN_MUSHROOM_CAP);
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
        blockDrops.put(BlockID.MYCELIUM, BlockID.DIRT);
        blockDrops.put(BlockID.LILY_PAD, BlockID.LILY_PAD);
        blockDrops.put(BlockID.NETHER_BRICK, BlockID.NETHER_BRICK);
        blockDrops.put(BlockID.NETHER_BRICK_FENCE, BlockID.NETHER_BRICK_FENCE);
        blockDrops.put(BlockID.NETHER_BRICK_STAIRS, BlockID.NETHER_BRICK);
        blockDrops.put(BlockID.NETHER_WART, ItemID.NETHER_WART_SEED);
        blockDrops.put(BlockID.ENCHANTMENT_TABLE, BlockID.ENCHANTMENT_TABLE);
        blockDrops.put(BlockID.BREWING_STAND, ItemID.BREWING_STAND);
        blockDrops.put(BlockID.CAULDRON, ItemID.CAULDRON);
        blockDrops.put(BlockID.END_PORTAL, -1);
        blockDrops.put(BlockID.END_PORTAL_FRAME, -1);
        blockDrops.put(BlockID.END_STONE, BlockID.END_STONE);
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

    public BaseItemStack getBlockDrop(short data) {
        return getBlockDrop(id, data);
    }

    private static final Random random = new Random();
    public static BaseItemStack getBlockDrop(int id, short data) {
        int store;
        switch (id) {
        case BlockID.STONE:
            return new BaseItemStack(BlockID.COBBLESTONE);

        case BlockID.GRASS:
            return new BaseItemStack(BlockID.DIRT);

        case BlockID.GRAVEL:
            if (random.nextInt(10) == 0) {
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
            if (data == 7) return new BaseItemStack(ItemID.WHEAT);
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

        case BlockID.BROWN_MUSHROOM_CAP:
            store = random.nextInt(10);
            if (store == 0) {
                return new BaseItemStack(BlockID.BROWN_MUSHROOM, 2);
            } else if (store == 1) {
                return new BaseItemStack(BlockID.BROWN_MUSHROOM);
            } else {
                return null;
            }

        case BlockID.RED_MUSHROOM_CAP:
            store = random.nextInt(10);
            if (store == 0) {
                return new BaseItemStack(BlockID.RED_MUSHROOM, 2);
            } else if (store == 1) {
                return new BaseItemStack(BlockID.RED_MUSHROOM);
            } else {
                return null;
            }

        case BlockID.MELON_BLOCK:
            return new BaseItemStack(ItemID.MELON, (random.nextInt(5) + 3));

        case BlockID.PUMPKIN_STEM:
            return new BaseItemStack(ItemID.PUMPKIN_SEEDS);

        case BlockID.MELON_STEM:
            return new BaseItemStack(ItemID.MELON_SEEDS);

        case BlockID.BRICK_STAIRS:
            return new BaseItemStack(BlockID.BRICK);

        case BlockID.STONE_BRICK_STAIRS:
            return new BaseItemStack(BlockID.STONE_BRICK);

        case BlockID.MYCELIUM:
            return new BaseItemStack(BlockID.DIRT);

        case BlockID.LILY_PAD:
            return new BaseItemStack(BlockID.LILY_PAD);

        case BlockID.NETHER_BRICK_STAIRS:
            return new BaseItemStack(BlockID.NETHER_BRICK);

        case BlockID.NETHER_WART:
            return new BaseItemStack(ItemID.NETHER_WART_SEED, random.nextInt(3) + 1);

        case BlockID.BREWING_STAND:
            return new BaseItemStack(ItemID.BREWING_STAND);

        case BlockID.CAULDRON:
            return new BaseItemStack(ItemID.CAULDRON);

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
        case BlockID.END_PORTAL:
        case BlockID.END_PORTAL_FRAME:
            return null;
        }

        if (usesData(id)) {
            return new BaseItemStack(id, 1, data);
        } else {
            return new BaseItemStack(id);
        }
    }

    private static final Map<Integer, PlayerDirection> dataAttachments = new HashMap<Integer, PlayerDirection>();
    private static final Map<Integer, PlayerDirection> nonDataAttachments = new HashMap<Integer, PlayerDirection>();
    static {
        nonDataAttachments.put(BlockID.SAPLING, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.POWERED_RAIL, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.DETECTOR_RAIL, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.LONG_GRASS, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.DEAD_BUSH, PlayerDirection.DOWN);
        for (int offset = 0; offset <= 8; offset += 8) {
            dataAttachments.put(attachmentKey(BlockID.PISTON_EXTENSION, offset + 0), PlayerDirection.UP);
            dataAttachments.put(attachmentKey(BlockID.PISTON_EXTENSION, offset + 1), PlayerDirection.DOWN);
            addCardinals(BlockID.PISTON_EXTENSION, offset + 2, offset + 5, offset + 3, offset + 4);
        }
        nonDataAttachments.put(BlockID.YELLOW_FLOWER, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.RED_FLOWER, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.BROWN_MUSHROOM, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.RED_MUSHROOM, PlayerDirection.DOWN);
        for (int blockId : new int[] { BlockID.TORCH, BlockID.REDSTONE_TORCH_ON, BlockID.REDSTONE_TORCH_OFF }) {
            dataAttachments.put(attachmentKey(blockId, 5), PlayerDirection.DOWN);
            addCardinals(blockId, 4, 1, 3, 2);
        }
        nonDataAttachments.put(BlockID.REDSTONE_WIRE, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.CROPS, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.SIGN_POST, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.WOODEN_DOOR, PlayerDirection.DOWN);
        addCardinals(BlockID.LADDER, 2, 5, 3, 4);
        nonDataAttachments.put(BlockID.MINECART_TRACKS, PlayerDirection.DOWN);
        addCardinals(BlockID.WALL_SIGN, 2, 5, 3, 4);
        for (int offset = 0; offset <= 8; offset += 8) {
            addCardinals(BlockID.LEVER, offset + 4, offset + 1, offset + 3, offset + 2);
            dataAttachments.put(attachmentKey(BlockID.LEVER, offset + 5), PlayerDirection.DOWN);
            dataAttachments.put(attachmentKey(BlockID.LEVER, offset + 6), PlayerDirection.DOWN);
        }
        nonDataAttachments.put(BlockID.STONE_PRESSURE_PLATE, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.IRON_DOOR, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.WOODEN_PRESSURE_PLATE, PlayerDirection.DOWN);
        // redstone torches: see torches
        for (int offset = 0; offset <= 8; offset += 8) {
            addCardinals(BlockID.STONE_BUTTON, offset + 4, offset + 1, offset + 3, offset + 2);
        }
        nonDataAttachments.put(BlockID.CACTUS, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.REED, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.CAKE_BLOCK, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.REDSTONE_REPEATER_OFF, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.REDSTONE_REPEATER_ON, PlayerDirection.DOWN);
        for (int offset = 0; offset <= 4; offset += 4) {
            addCardinals(BlockID.TRAP_DOOR, offset + 0, offset + 3, offset + 1, offset + 2);
        }
        nonDataAttachments.put(BlockID.PUMPKIN_STEM, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.MELON_STEM, PlayerDirection.DOWN);
        // vines are complicated, but I'll list the single-attachment variants anyway
        dataAttachments.put(attachmentKey(BlockID.VINE, 0), PlayerDirection.UP);
        addCardinals(BlockID.VINE, 1, 2, 4, 8);
        nonDataAttachments.put(BlockID.NETHER_WART, PlayerDirection.DOWN);
    }

    /**
     * Returns the direction to the block(B) this block(A) is attached to.
     * Attached means that if block B is destroyed, block A will pop off. 
     *
     * @param type The block id of block A
     * @param data The data value of block A
     * @return direction to block B
     */
    public static PlayerDirection getAttachment(int type, int data) {
        PlayerDirection direction = nonDataAttachments.get(type);
        if (direction != null) return direction;

        return dataAttachments.get(attachmentKey(type, data));
    }

    private static int attachmentKey(int type, int data) {
        return (type << 4) | (data & 0xf);
    }

    private static void addCardinals(int type, int west, int north, int east, int south) {
        dataAttachments.put(attachmentKey(type, west), PlayerDirection.WEST);
        dataAttachments.put(attachmentKey(type, north), PlayerDirection.NORTH);
        dataAttachments.put(attachmentKey(type, east), PlayerDirection.EAST);
        dataAttachments.put(attachmentKey(type, south), PlayerDirection.SOUTH);
    }
}
