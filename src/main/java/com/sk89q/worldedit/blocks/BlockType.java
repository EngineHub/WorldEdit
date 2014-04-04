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

package com.sk89q.worldedit.blocks;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.PlayerDirection;

import java.util.*;

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
    WOODEN_STAIRS(BlockID.OAK_WOOD_STAIRS, "Wooden stairs", "woodstair", "woodstairs", "woodenstair", "woodenstairs"),
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
    @Deprecated LOCKED_CHEST(BlockID.LOCKED_CHEST, "thisblockisinvalidusedstainedglassinstead"),
    STAINED_GLASS(BlockID.STAINED_GLASS, "Stained Glass", "stainedglass"),
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
    MYCELIUM(BlockID.MYCELIUM, "Mycelium", "mycelium", "fungus", "mycel"),
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
    END_STONE(BlockID.END_STONE, "End Stone", "endstone", "enderstone", "endersand"),
    DRAGON_EGG(BlockID.DRAGON_EGG, "Dragon Egg", "dragonegg", "dragons"),
    REDSTONE_LAMP_OFF(BlockID.REDSTONE_LAMP_OFF, "Redstone lamp (off)", "redstonelamp", "redstonelampoff", "rslamp", "rslampoff", "rsglow", "rsglowoff"),
    REDSTONE_LAMP_ON(BlockID.REDSTONE_LAMP_ON, "Redstone lamp (on)", "redstonelampon", "rslampon", "rsglowon"),
    DOUBLE_WOODEN_STEP(BlockID.DOUBLE_WOODEN_STEP, "Double wood step", "doublewoodslab", "doublewoodstep"),
    WOODEN_STEP(BlockID.WOODEN_STEP, "Wood step", "woodenslab", "woodslab", "woodstep", "woodhalfstep"),
    COCOA_PLANT(BlockID.COCOA_PLANT, "Cocoa plant", "cocoplant", "cocoaplant"),
    SANDSTONE_STAIRS(BlockID.SANDSTONE_STAIRS, "Sandstone stairs", "sandstairs", "sandstonestairs"),
    EMERALD_ORE(BlockID.EMERALD_ORE, "Emerald ore", "emeraldore"),
    ENDER_CHEST(BlockID.ENDER_CHEST, "Ender chest", "enderchest"),
    TRIPWIRE_HOOK(BlockID.TRIPWIRE_HOOK, "Tripwire hook", "tripwirehook"),
    TRIPWIRE(BlockID.TRIPWIRE, "Tripwire", "tripwire", "string"),
    EMERALD_BLOCK(BlockID.EMERALD_BLOCK, "Emerald block", "emeraldblock", "emerald"),
    SPRUCE_WOOD_STAIRS(BlockID.SPRUCE_WOOD_STAIRS, "Spruce wood stairs", "sprucestairs", "sprucewoodstairs"),
    BIRCH_WOOD_STAIRS(BlockID.BIRCH_WOOD_STAIRS, "Birch wood stairs", "birchstairs", "birchwoodstairs"),
    JUNGLE_WOOD_STAIRS(BlockID.JUNGLE_WOOD_STAIRS, "Jungle wood stairs", "junglestairs", "junglewoodstairs"),
    COMMAND_BLOCK(BlockID.COMMAND_BLOCK, "Command block", "commandblock", "cmdblock", "command", "cmd"),
    BEACON(BlockID.BEACON, "Beacon", "beacon", "beaconblock"),
    COBBLESTONE_WALL(BlockID.COBBLESTONE_WALL, "Cobblestone wall", "cobblestonewall", "cobblewall"),
    FLOWER_POT(BlockID.FLOWER_POT, "Flower pot", "flowerpot", "plantpot", "pot"),
    CARROTS(BlockID.CARROTS, "Carrots", "carrots", "carrotsplant", "carrotsblock"),
    POTATOES(BlockID.POTATOES, "Potatoes", "potatoes", "potatoesblock"),
    WOODEN_BUTTON(BlockID.WOODEN_BUTTON, "Wooden button", "woodbutton", "woodenbutton"),
    HEAD(BlockID.HEAD, "Head", "head", "skull"),
    ANVIL(BlockID.ANVIL, "Anvil", "anvil", "blacksmith"),
    TRAPPED_CHEST(BlockID.TRAPPED_CHEST, "Trapped Chest", "trappedchest", "redstonechest"),
    PRESSURE_PLATE_LIGHT(BlockID.PRESSURE_PLATE_LIGHT, "Weighted Pressure Plate (Light)", "lightpressureplate"),
    PRESSURE_PLATE_HEAVY(BlockID.PRESSURE_PLATE_HEAVY, "Weighted Pressure Plate (Heavy)", "heavypressureplate"),
    COMPARATOR_OFF(BlockID.COMPARATOR_OFF, "Redstone Comparator (inactive)", "redstonecomparator", "comparator"),
    COMPARATOR_ON(BlockID.COMPARATOR_ON, "Redstone Comparator (active)", "redstonecomparatoron", "comparatoron"),
    DAYLIGHT_SENSOR(BlockID.DAYLIGHT_SENSOR, "Daylight Sensor", "daylightsensor", "lightsensor", "daylightdetector"),
    REDSTONE_BLOCK(BlockID.REDSTONE_BLOCK, "Block of Redstone", "redstoneblock", "blockofredstone"),
    QUARTZ_ORE(BlockID.QUARTZ_ORE, "Nether Quartz Ore", "quartzore", "netherquartzore"),
    HOPPER(BlockID.HOPPER, "Hopper", "hopper"),
    QUARTZ_BLOCK(BlockID.QUARTZ_BLOCK, "Block of Quartz", "quartzblock", "quartz"),
    QUARTZ_STAIRS(BlockID.QUARTZ_STAIRS, "Quartz Stairs", "quartzstairs"),
    ACTIVATOR_RAIL(BlockID.ACTIVATOR_RAIL, "Activator Rail", "activatorrail", "tntrail", "activatortrack"),
    DROPPER(BlockID.DROPPER, "Dropper", "dropper"),
    STAINED_CLAY(BlockID.STAINED_CLAY, "Stained Clay", "stainedclay", "stainedhardenedclay"),
    STAINED_GLASS_PANE(BlockID.STAINED_GLASS_PANE, "Stained Glass Pane", "stainedglasspane"),
    LEAVES2(BlockID.LEAVES2, "Leaves", "leaves2", "acacialeaves", "darkoakleaves"),
    LOG2(BlockID.LOG2, "Log", "log2", "acacia", "darkoak"),
    ACACIA_STAIRS(BlockID.ACACIA_STAIRS, "Acacia Wood Stairs", "acaciawoodstairs", "acaciastairs"),
    DARK_OAK_STAIRS(BlockID.DARK_OAK_STAIRS, "Dark Oak Wood Stairs", "darkoakwoodstairs", "darkoakstairs"),
    HAY_BLOCK(BlockID.HAY_BLOCK, "Hay Block", "hayblock", "haybale", "wheatbale"),
    CARPET(BlockID.CARPET, "Carpet", "carpet"),
    HARDENED_CLAY(BlockID.HARDENED_CLAY, "Hardened Clay", "hardenedclay", "hardclay"),
    COAL_BLOCK(BlockID.COAL_BLOCK, "Block of Coal", "coalblock", "blockofcoal"),
    PACKED_ICE(BlockID.PACKED_ICE, "Packed Ice", "packedice", "hardice"),
    DOUBLE_PLANT(BlockID.DOUBLE_PLANT, "Large Flowers", "largeflowers", "doubleflowers");

    /**
     * Stores a map of the IDs for fast access.
     */
    private static final Map<Integer, BlockType> ids = new HashMap<Integer, BlockType>();
    /**
     * Stores a map of the names for fast access.
     */
    private static final Map<String, BlockType> lookup = new HashMap<String, BlockType>();

    private final int id;
    private final String name;
    private final String[] lookupKeys;

    static {
        for (BlockType type : EnumSet.allOf(BlockType.class)) {
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
    BlockType(int id, String name, String... lookupKeys) {
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
        try {
            return fromID(Integer.parseInt(name));
        } catch (NumberFormatException e) {
            return StringUtil.lookup(lookup, name, fuzzy);
        }
    }

    private static Map<Integer, BaseBlock> itemBlockMapping = new HashMap<Integer, BaseBlock>();
    private static Map<Integer, BaseBlock> dataItemBlockMapping = new HashMap<Integer, BaseBlock>();
    static {
        for (int data = 0; data < 16; ++data) {
            dataItemBlockMapping.put(typeDataKey(BlockID.DIRT, data), new BaseBlock(BlockID.DIRT, data));
            dataItemBlockMapping.put(typeDataKey(BlockID.WOOD, data), new BaseBlock(BlockID.WOOD, data));
            dataItemBlockMapping.put(typeDataKey(BlockID.SAND, data), new BaseBlock(BlockID.SAND, data));
            dataItemBlockMapping.put(typeDataKey(BlockID.SANDSTONE, data), new BaseBlock(BlockID.SANDSTONE, data));
            dataItemBlockMapping.put(typeDataKey(BlockID.LONG_GRASS, data), new BaseBlock(BlockID.LONG_GRASS, data));
            dataItemBlockMapping.put(typeDataKey(BlockID.CLOTH, data), new BaseBlock(BlockID.CLOTH, data));
            dataItemBlockMapping.put(typeDataKey(BlockID.SILVERFISH_BLOCK, data), new BaseBlock(BlockID.SILVERFISH_BLOCK, data));
            dataItemBlockMapping.put(typeDataKey(BlockID.STONE_BRICK, data), new BaseBlock(BlockID.STONE_BRICK, data));
            dataItemBlockMapping.put(typeDataKey(BlockID.COBBLESTONE_WALL, data), new BaseBlock(BlockID.COBBLESTONE_WALL, data));
            dataItemBlockMapping.put(typeDataKey(BlockID.STAINED_CLAY, data), new BaseBlock(BlockID.STAINED_CLAY, data));
            dataItemBlockMapping.put(typeDataKey(BlockID.CARPET, data), new BaseBlock(BlockID.CARPET, data));
            dataItemBlockMapping.put(typeDataKey(BlockID.RED_FLOWER, data), new BaseBlock(BlockID.RED_FLOWER, data));
            dataItemBlockMapping.put(typeDataKey(BlockID.DOUBLE_PLANT, data), new BaseBlock(BlockID.DOUBLE_PLANT, data));
            dataItemBlockMapping.put(typeDataKey(BlockID.STAINED_GLASS, data), new BaseBlock(BlockID.STAINED_GLASS, data));
        }

        itemBlockMapping.put(ItemID.FLINT_AND_TINDER, new BaseBlock(BlockID.FIRE, -1));
        itemBlockMapping.put(ItemID.STRING, new BaseBlock(BlockID.TRIPWIRE, -1));
        itemBlockMapping.put(ItemID.SEEDS, new BaseBlock(BlockID.CROPS, -1));
        itemBlockMapping.put(ItemID.SIGN, new BaseBlock(BlockID.SIGN_POST, -1));
        itemBlockMapping.put(ItemID.WOODEN_DOOR_ITEM, new BaseBlock(BlockID.WOODEN_DOOR, -1));
        itemBlockMapping.put(ItemID.WATER_BUCKET, new BaseBlock(BlockID.STATIONARY_WATER, -1));
        itemBlockMapping.put(ItemID.LAVA_BUCKET, new BaseBlock(BlockID.STATIONARY_LAVA, -1));
        itemBlockMapping.put(ItemID.IRON_DOOR_ITEM, new BaseBlock(BlockID.IRON_DOOR, -1));
        itemBlockMapping.put(ItemID.REDSTONE_DUST, new BaseBlock(BlockID.REDSTONE_WIRE, -1));
        itemBlockMapping.put(ItemID.SUGAR_CANE_ITEM, new BaseBlock(BlockID.REED, -1));
        itemBlockMapping.put(ItemID.BED_ITEM, new BaseBlock(BlockID.BED, -1));
        itemBlockMapping.put(ItemID.REDSTONE_REPEATER, new BaseBlock(BlockID.REDSTONE_REPEATER_OFF, -1));
        itemBlockMapping.put(ItemID.PUMPKIN_SEEDS, new BaseBlock(BlockID.PUMPKIN_STEM, -1));
        itemBlockMapping.put(ItemID.MELON_SEEDS, new BaseBlock(BlockID.MELON_STEM, -1));
        itemBlockMapping.put(ItemID.NETHER_WART_SEED, new BaseBlock(BlockID.NETHER_WART, -1));
        itemBlockMapping.put(ItemID.BREWING_STAND, new BaseBlock(BlockID.BREWING_STAND, -1));
        itemBlockMapping.put(ItemID.CAULDRON, new BaseBlock(BlockID.CAULDRON, -1));
        itemBlockMapping.put(ItemID.FLOWER_POT, new BaseBlock(BlockID.FLOWER_POT, -1));
        itemBlockMapping.put(ItemID.CARROT, new BaseBlock(BlockID.CARROTS, -1));
        itemBlockMapping.put(ItemID.POTATO, new BaseBlock(BlockID.POTATOES, -1));
        itemBlockMapping.put(ItemID.COMPARATOR, new BaseBlock(BlockID.COMPARATOR_OFF, -1));
    }

    public static BaseBlock getBlockForItem(int typeId, int data) {
        final BaseBlock block = itemBlockMapping.get(typeId);

        if (block != null) {
            return block;
        }

        return dataItemBlockMapping.get(typeDataKey(typeId, data));
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
        shouldPlaceLast.add(BlockID.LADDER);
        shouldPlaceLast.add(BlockID.MINECART_TRACKS);
        shouldPlaceLast.add(BlockID.LEVER);
        shouldPlaceLast.add(BlockID.STONE_PRESSURE_PLATE);
        shouldPlaceLast.add(BlockID.WOODEN_PRESSURE_PLATE);
        shouldPlaceLast.add(BlockID.REDSTONE_TORCH_OFF);
        shouldPlaceLast.add(BlockID.REDSTONE_TORCH_ON);
        shouldPlaceLast.add(BlockID.STONE_BUTTON);
        shouldPlaceLast.add(BlockID.SNOW);
        shouldPlaceLast.add(BlockID.PORTAL);
        shouldPlaceLast.add(BlockID.REDSTONE_REPEATER_OFF);
        shouldPlaceLast.add(BlockID.REDSTONE_REPEATER_ON);
        shouldPlaceLast.add(BlockID.TRAP_DOOR);
        shouldPlaceLast.add(BlockID.VINE);
        shouldPlaceLast.add(BlockID.LILY_PAD);
        shouldPlaceLast.add(BlockID.NETHER_WART);
        shouldPlaceLast.add(BlockID.PISTON_BASE);
        shouldPlaceLast.add(BlockID.PISTON_STICKY_BASE);
        shouldPlaceLast.add(BlockID.PISTON_EXTENSION);
        shouldPlaceLast.add(BlockID.PISTON_MOVING_PIECE);
        shouldPlaceLast.add(BlockID.COCOA_PLANT);
        shouldPlaceLast.add(BlockID.TRIPWIRE_HOOK);
        shouldPlaceLast.add(BlockID.TRIPWIRE);
        shouldPlaceLast.add(BlockID.FLOWER_POT);
        shouldPlaceLast.add(BlockID.CARROTS);
        shouldPlaceLast.add(BlockID.POTATOES);
        shouldPlaceLast.add(BlockID.WOODEN_BUTTON);
        shouldPlaceLast.add(BlockID.ANVIL); // becomes relevant with asynchronous placement
        shouldPlaceLast.add(BlockID.PRESSURE_PLATE_LIGHT);
        shouldPlaceLast.add(BlockID.PRESSURE_PLATE_HEAVY);
        shouldPlaceLast.add(BlockID.COMPARATOR_OFF);
        shouldPlaceLast.add(BlockID.COMPARATOR_ON);
        shouldPlaceLast.add(BlockID.ACTIVATOR_RAIL);
        shouldPlaceLast.add(BlockID.CARPET);
        shouldPlaceLast.add(BlockID.DOUBLE_PLANT);
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
     * HashSet for shouldPlaceLast.
     */
    private static final Set<Integer> shouldPlaceFinal = new HashSet<Integer>();
    static {
        shouldPlaceFinal.add(BlockID.SIGN_POST);
        shouldPlaceFinal.add(BlockID.WOODEN_DOOR);
        shouldPlaceFinal.add(BlockID.WALL_SIGN);
        shouldPlaceFinal.add(BlockID.IRON_DOOR);
        shouldPlaceFinal.add(BlockID.CACTUS);
        shouldPlaceFinal.add(BlockID.REED);
        shouldPlaceFinal.add(BlockID.CAKE_BLOCK);
        shouldPlaceFinal.add(BlockID.PISTON_EXTENSION);
        shouldPlaceFinal.add(BlockID.PISTON_MOVING_PIECE);
    }

    /**
     * Checks to see whether a block should be placed in the final queue.
     *
     * This applies to blocks that can be attached to other blocks that have an attachment.
     *
     * @param id
     * @return
     */
    public static boolean shouldPlaceFinal(int id) {
        return shouldPlaceFinal.contains(id);
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
        canPassThrough.add(-16*BlockID.SNOW-0);
        canPassThrough.add(-16*BlockID.SNOW-8);
        canPassThrough.add(BlockID.REED);
        canPassThrough.add(BlockID.PORTAL);
        canPassThrough.add(BlockID.REDSTONE_REPEATER_OFF);
        canPassThrough.add(BlockID.REDSTONE_REPEATER_ON);
        canPassThrough.add(BlockID.PUMPKIN_STEM);
        canPassThrough.add(BlockID.MELON_STEM);
        canPassThrough.add(BlockID.VINE);
        canPassThrough.add(BlockID.NETHER_WART);
        canPassThrough.add(BlockID.END_PORTAL);
        canPassThrough.add(BlockID.TRIPWIRE_HOOK);
        canPassThrough.add(BlockID.TRIPWIRE);
        canPassThrough.add(BlockID.CARROTS);
        canPassThrough.add(BlockID.POTATOES);
        canPassThrough.add(BlockID.WOODEN_BUTTON);
        canPassThrough.add(BlockID.PRESSURE_PLATE_LIGHT);
        canPassThrough.add(BlockID.PRESSURE_PLATE_HEAVY);
        canPassThrough.add(BlockID.COMPARATOR_OFF);
        canPassThrough.add(BlockID.COMPARATOR_ON);
        canPassThrough.add(BlockID.ACTIVATOR_RAIL);
        canPassThrough.add(BlockID.CARPET);
        canPassThrough.add(BlockID.DOUBLE_PLANT);
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
     * @param id
     * @param data
     * @return
     */
    public static boolean canPassThrough(int id, int data) {
        return canPassThrough.contains(-16*id-data) || canPassThrough.contains(id);
    }

    /**
     * Checks whether a block can be passed through.
     *
     * @param block
     * @return
     */
    public static boolean canPassThrough(BaseBlock block) {
        return canPassThrough(block.getId(), block.getData());
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
     * HashSet for centralTopLimit.
     */
    private static final Map<Integer, Double> centralTopLimit = new HashMap<Integer, Double>();
    static {
        centralTopLimit.put(BlockID.BED, 0.5625);
        centralTopLimit.put(BlockID.BREWING_STAND, 0.875);
        centralTopLimit.put(BlockID.CAKE_BLOCK, 0.4375);
        for (int data = 6; data < 16; ++data) {
            centralTopLimit.put(-16*BlockID.CAKE_BLOCK-data, 0.0);
        }
        centralTopLimit.put(BlockID.CAULDRON, 0.3125);
        centralTopLimit.put(BlockID.COCOA_PLANT, 0.750);
        centralTopLimit.put(BlockID.ENCHANTMENT_TABLE, 0.75);
        for (int data = 0; data < 16; ++data) {
            if ((data & 4) != 0) {
                centralTopLimit.put(-16*BlockID.END_PORTAL_FRAME-data, 1.0);
            } else {
                centralTopLimit.put(-16*BlockID.END_PORTAL_FRAME-data, 0.8125);
            }
            centralTopLimit.put(-16*BlockID.HEAD-data, 0.75);
        }
        // Heads on the floor are lower
        centralTopLimit.put(-16*BlockID.HEAD-1, 0.5);
        centralTopLimit.put(-16*BlockID.HEAD-9, 0.5);
        centralTopLimit.put(BlockID.FENCE, 1.5);
        for (int data = 0; data < 8; ++data) {
            centralTopLimit.put(-16*BlockID.STEP-data, 0.5);
            centralTopLimit.put(-16*BlockID.WOODEN_STEP-data, 0.5);
            centralTopLimit.put(-16*BlockID.SNOW-data, 0.125*data);
            centralTopLimit.put(-16*BlockID.SNOW-(data+8), 0.125*data);
        }
        centralTopLimit.put(BlockID.LILY_PAD, 0.015625);
        centralTopLimit.put(BlockID.REDSTONE_REPEATER_ON, .125);
        centralTopLimit.put(BlockID.REDSTONE_REPEATER_OFF, .125);
        for (int data = 0; data < 4; ++data) {
            centralTopLimit.put(-16*BlockID.TRAP_DOOR-(data+ 0), 0.1875); // closed lower trap doors
            centralTopLimit.put(-16*BlockID.TRAP_DOOR-(data+ 4), 0.0); // opened lower trap doors
            centralTopLimit.put(-16*BlockID.TRAP_DOOR-(data+ 8), 1.0); // closed upper trap doors
            centralTopLimit.put(-16*BlockID.TRAP_DOOR-(data+12), 0.0); // opened upper trap doors

            centralTopLimit.put(-16*BlockID.FENCE_GATE-(data+ 0), 1.5);
            centralTopLimit.put(-16*BlockID.FENCE_GATE-(data+ 4), 0.0);
            centralTopLimit.put(-16*BlockID.FENCE_GATE-(data+ 8), 1.5);
            centralTopLimit.put(-16*BlockID.FENCE_GATE-(data+12), 0.0);
        }
        centralTopLimit.put(BlockID.SLOW_SAND, 0.875);
        centralTopLimit.put(BlockID.COBBLESTONE_WALL, 1.5);
        centralTopLimit.put(BlockID.FLOWER_POT, 0.375);
        centralTopLimit.put(BlockID.COMPARATOR_OFF, .125);
        centralTopLimit.put(BlockID.COMPARATOR_ON, .125);
        centralTopLimit.put(BlockID.DAYLIGHT_SENSOR, 0.375);
        centralTopLimit.put(BlockID.HOPPER, 0.625);

        // Some default values to be used if no data value is given
        centralTopLimit.put(BlockID.HEAD, 0.75);
        centralTopLimit.put(BlockID.TRAP_DOOR, 1.0);
        centralTopLimit.put(BlockID.FENCE_GATE, 1.5);
    }

    /**
     * Returns the y offset a player falls to when falling onto the top of a block at xp+0.5/zp+0.5.
     *
     * @param id
     * @param data
     * @return
     */
    public static double centralTopLimit(int id, int data) {
        if (centralTopLimit.containsKey(-16*id-data))
            return centralTopLimit.get(-16*id-data);

        if (centralTopLimit.containsKey(id))
            return centralTopLimit.get(id);

        return canPassThrough(id) ? 0 : 1;
    }

    /**
     * Returns the y offset a player falls to when falling onto the top of a block at xp+0.5/zp+0.5.
     *
     * @param block
     * @return
     */
    public static double centralTopLimit(BaseBlock block) {
        return centralTopLimit(block.getId(), block.getData());
    }

    /**
     * Returns the y offset a player falls to when falling onto the top of a block at xp+0.5/zp+0.5.
     *
     * @return
     */
    public double centralTopLimit() {
        if (centralTopLimit.containsKey(id))
            return centralTopLimit.get(id);

        return canPassThrough(id) ? 0 : 1;
    }

    /**
     * HashSet for usesData.
     */
    private static final Set<Integer> usesData = new HashSet<Integer>();
    static {
        usesData.add(BlockID.DIRT);
        usesData.add(BlockID.WOOD);
        usesData.add(BlockID.SAPLING);
        usesData.add(BlockID.WATER);
        usesData.add(BlockID.STATIONARY_WATER);
        usesData.add(BlockID.LAVA);
        usesData.add(BlockID.STATIONARY_LAVA);
        usesData.add(BlockID.SAND);
        usesData.add(BlockID.LOG);
        usesData.add(BlockID.LOG2);
        usesData.add(BlockID.LEAVES);
        usesData.add(BlockID.LEAVES2);
        usesData.add(BlockID.DISPENSER);
        usesData.add(BlockID.SANDSTONE);
        usesData.add(BlockID.BED);
        usesData.add(BlockID.POWERED_RAIL);
        usesData.add(BlockID.DETECTOR_RAIL);
        usesData.add(BlockID.PISTON_STICKY_BASE);
        usesData.add(BlockID.LONG_GRASS);
        usesData.add(BlockID.PISTON_BASE);
        usesData.add(BlockID.PISTON_EXTENSION);
        usesData.add(BlockID.CLOTH);
        usesData.add(BlockID.RED_FLOWER);
        usesData.add(BlockID.DOUBLE_STEP);
        usesData.add(BlockID.STEP);
        usesData.add(BlockID.TORCH);
        usesData.add(BlockID.FIRE);
        usesData.add(BlockID.OAK_WOOD_STAIRS);
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
        usesData.add(BlockID.BREWING_STAND);
        usesData.add(BlockID.CAULDRON);
        usesData.add(BlockID.END_PORTAL_FRAME);
        usesData.add(BlockID.DOUBLE_WOODEN_STEP);
        usesData.add(BlockID.WOODEN_STEP);
        usesData.add(BlockID.COCOA_PLANT);
        usesData.add(BlockID.SANDSTONE_STAIRS);
        usesData.add(BlockID.ENDER_CHEST);
        usesData.add(BlockID.TRIPWIRE_HOOK);
        usesData.add(BlockID.TRIPWIRE);
        usesData.add(BlockID.SPRUCE_WOOD_STAIRS);
        usesData.add(BlockID.BIRCH_WOOD_STAIRS);
        usesData.add(BlockID.JUNGLE_WOOD_STAIRS);
        usesData.add(BlockID.COBBLESTONE_WALL);
        usesData.add(BlockID.FLOWER_POT);
        usesData.add(BlockID.CARROTS);
        usesData.add(BlockID.POTATOES);
        usesData.add(BlockID.WOODEN_BUTTON);
        usesData.add(BlockID.HEAD);
        usesData.add(BlockID.ANVIL);
        usesData.add(BlockID.PRESSURE_PLATE_LIGHT);
        usesData.add(BlockID.PRESSURE_PLATE_HEAVY);
        usesData.add(BlockID.COMPARATOR_OFF);
        usesData.add(BlockID.COMPARATOR_ON);
        usesData.add(BlockID.QUARTZ_BLOCK);
        usesData.add(BlockID.QUARTZ_STAIRS);
        usesData.add(BlockID.ACTIVATOR_RAIL);
        usesData.add(BlockID.DROPPER);
        usesData.add(BlockID.HOPPER);
        usesData.add(BlockID.STAINED_CLAY);
        usesData.add(BlockID.STAINED_GLASS_PANE);
        usesData.add(BlockID.HAY_BLOCK);
        usesData.add(BlockID.CARPET);
        usesData.add(BlockID.DOUBLE_PLANT);
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
        isContainerBlock.add(BlockID.TRAPPED_CHEST);
        isContainerBlock.add(BlockID.HOPPER);
        isContainerBlock.add(BlockID.DROPPER);
        //isContainerBlock.add(BlockID.ENDER_CHEST); // ender chest has no own inventory, don't add this here
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
        isRedstoneBlock.add(BlockID.TRIPWIRE_HOOK);
        isRedstoneBlock.add(BlockID.COMMAND_BLOCK);
        isRedstoneBlock.add(BlockID.WOODEN_BUTTON);
        isRedstoneBlock.add(BlockID.TRAPPED_CHEST);
        isRedstoneBlock.add(BlockID.PRESSURE_PLATE_LIGHT);
        isRedstoneBlock.add(BlockID.PRESSURE_PLATE_HEAVY);
        isRedstoneBlock.add(BlockID.COMPARATOR_OFF);
        isRedstoneBlock.add(BlockID.COMPARATOR_ON);
        isRedstoneBlock.add(BlockID.DAYLIGHT_SENSOR);
        isRedstoneBlock.add(BlockID.REDSTONE_BLOCK);
        isRedstoneBlock.add(BlockID.HOPPER);
        isRedstoneBlock.add(BlockID.ACTIVATOR_RAIL);
        isRedstoneBlock.add(BlockID.DROPPER);
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
        canTransferRedstone.add(BlockID.COMPARATOR_OFF);
        canTransferRedstone.add(BlockID.COMPARATOR_ON);
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
        isRedstoneSource.add(BlockID.TRIPWIRE_HOOK);
        isRedstoneSource.add(BlockID.WOODEN_BUTTON);
        isRedstoneSource.add(BlockID.PRESSURE_PLATE_LIGHT);
        isRedstoneSource.add(BlockID.PRESSURE_PLATE_HEAVY);
        isRedstoneSource.add(BlockID.DAYLIGHT_SENSOR);
        isRedstoneSource.add(BlockID.REDSTONE_BLOCK);
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
        isRailBlock.add(BlockID.ACTIVATOR_RAIL);
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
        isNaturalTerrainBlock.add(BlockID.PACKED_ICE);
        isNaturalTerrainBlock.add(BlockID.STAINED_CLAY);

        // hell
        isNaturalTerrainBlock.add(BlockID.NETHERRACK);
        isNaturalTerrainBlock.add(BlockID.SLOW_SAND);
        isNaturalTerrainBlock.add(BlockID.LIGHTSTONE);
        isNaturalTerrainBlock.add(BlockID.QUARTZ_ORE);

        // ores
        isNaturalTerrainBlock.add(BlockID.COAL_ORE);
        isNaturalTerrainBlock.add(BlockID.IRON_ORE);
        isNaturalTerrainBlock.add(BlockID.GOLD_ORE);
        isNaturalTerrainBlock.add(BlockID.LAPIS_LAZULI_ORE);
        isNaturalTerrainBlock.add(BlockID.DIAMOND_ORE);
        isNaturalTerrainBlock.add(BlockID.REDSTONE_ORE);
        isNaturalTerrainBlock.add(BlockID.GLOWING_REDSTONE_ORE);
        isNaturalTerrainBlock.add(BlockID.EMERALD_ORE);
    }

    /**
     * Checks if the block type is naturally occuring
     *
     * @param id      ID of the block
     * @return true if the block type is naturally occuring
     * @deprecated Use {@link #isNaturalTerrainBlock(int, int)}
     */
    @Deprecated
    public static boolean isNaturalTerrainBlock(int id) {
        return isNaturalTerrainBlock.contains(id);
    }

    /**
     * Checks if the block type is naturally occuring
     *
     * @param id      ID of the block
     * @param data    Data value of the block
     * @return true if the block type is naturally occuring
     */
    public static boolean isNaturalTerrainBlock(int id, int data) {
        return isNaturalTerrainBlock.contains(-16*id-data) || isNaturalTerrainBlock.contains(id);
    }

    /**
     * Checks if the block type is naturally occuring
     *
     * @param block The block
     * @return true if the block type is naturally occuring
     */
    public static boolean isNaturalTerrainBlock(BaseBlock block) {
        return isNaturalTerrainBlock(block.getId(), block.getData());
    }

    /**
     * Checks if the block type is naturally occuring
     *
     * @return true if the block type is naturally occuring
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
        emitsLight.add(BlockID.BROWN_MUSHROOM_CAP);
        emitsLight.add(BlockID.RED_MUSHROOM_CAP);
        emitsLight.add(BlockID.END_PORTAL);
        emitsLight.add(BlockID.REDSTONE_LAMP_ON);
        emitsLight.add(BlockID.ENDER_CHEST);
        emitsLight.add(BlockID.BEACON);
        emitsLight.add(BlockID.REDSTONE_BLOCK);
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
        isTranslucent.add(BlockID.LEAVES);
        isTranslucent.add(BlockID.GLASS);
        isTranslucent.add(BlockID.BED);
        isTranslucent.add(BlockID.POWERED_RAIL);
        isTranslucent.add(BlockID.DETECTOR_RAIL);
        //isTranslucent.add(BlockID.PISTON_STICKY_BASE);
        isTranslucent.add(BlockID.WEB);
        isTranslucent.add(BlockID.LONG_GRASS);
        isTranslucent.add(BlockID.DEAD_BUSH);
        //isTranslucent.add(BlockID.PISTON_BASE);
        isTranslucent.add(BlockID.PISTON_EXTENSION);
        //isTranslucent.add(BlockID.PISTON_MOVING_PIECE);
        isTranslucent.add(BlockID.YELLOW_FLOWER);
        isTranslucent.add(BlockID.RED_FLOWER);
        isTranslucent.add(BlockID.BROWN_MUSHROOM);
        isTranslucent.add(BlockID.RED_MUSHROOM);
        isTranslucent.add(BlockID.TORCH);
        isTranslucent.add(BlockID.FIRE);
        isTranslucent.add(BlockID.MOB_SPAWNER);
        isTranslucent.add(BlockID.OAK_WOOD_STAIRS);
        isTranslucent.add(BlockID.CHEST);
        isTranslucent.add(BlockID.REDSTONE_WIRE);
        isTranslucent.add(BlockID.CROPS);
        isTranslucent.add(BlockID.SIGN_POST);
        isTranslucent.add(BlockID.WOODEN_DOOR);
        isTranslucent.add(BlockID.LADDER);
        isTranslucent.add(BlockID.MINECART_TRACKS);
        isTranslucent.add(BlockID.COBBLESTONE_STAIRS);
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
        isTranslucent.add(BlockID.CACTUS);
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
        isTranslucent.add(BlockID.WOODEN_STEP);
        isTranslucent.add(BlockID.COCOA_PLANT);
        isTranslucent.add(BlockID.SANDSTONE_STAIRS);
        isTranslucent.add(BlockID.ENDER_CHEST);
        isTranslucent.add(BlockID.TRIPWIRE_HOOK);
        isTranslucent.add(BlockID.TRIPWIRE);
        isTranslucent.add(BlockID.SPRUCE_WOOD_STAIRS);
        isTranslucent.add(BlockID.BIRCH_WOOD_STAIRS);
        isTranslucent.add(BlockID.JUNGLE_WOOD_STAIRS);
        isTranslucent.add(BlockID.COBBLESTONE_WALL);
        isTranslucent.add(BlockID.FLOWER_POT);
        isTranslucent.add(BlockID.CARROTS);
        isTranslucent.add(BlockID.POTATOES);
        isTranslucent.add(BlockID.WOODEN_BUTTON);
        isTranslucent.add(BlockID.HEAD);
        isTranslucent.add(BlockID.ANVIL);
        isTranslucent.add(BlockID.TRAPPED_CHEST);
        isTranslucent.add(BlockID.PRESSURE_PLATE_LIGHT);
        isTranslucent.add(BlockID.PRESSURE_PLATE_HEAVY);
        isTranslucent.add(BlockID.COMPARATOR_OFF);
        isTranslucent.add(BlockID.COMPARATOR_ON);
        isTranslucent.add(BlockID.DAYLIGHT_SENSOR);
        isTranslucent.add(BlockID.HOPPER);
        isTranslucent.add(BlockID.QUARTZ_STAIRS);
        isTranslucent.add(BlockID.ACTIVATOR_RAIL);
        isTranslucent.add(BlockID.CARPET);
        isTranslucent.add(BlockID.STAINED_GLASS_PANE);
        isTranslucent.add(BlockID.DOUBLE_PLANT);
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
     * HashMap for getBlockBagItem.
     */
    private static final Map<Integer, BaseItem> dataBlockBagItems = new HashMap<Integer, BaseItem>();
    private static final Map<Integer, BaseItem> nonDataBlockBagItems = new HashMap<Integer, BaseItem>();
    private static final BaseItem doNotDestroy = new BaseItemStack(BlockID.AIR, 0);
    static {
        /*
         * rules:
         *
         * 1. block yields itself => addIdentity
         * 2. block is part of a 2-block object => drop an appropriate item for one of the 2 blocks
         * 3. block can be placed by right-clicking an obtainable item on the ground => use that item
         * 4. block yields more than one item => addIdentities
         * 5. block yields exactly one item => use that item
         * 6. block is a liquid => drop nothing
         * 7. block is created from thin air by the game other than by the map generator => drop nothing
         */

        nonDataBlockBagItems.put(BlockID.STONE, new BaseItem(BlockID.COBBLESTONE)); // rule 5
        nonDataBlockBagItems.put(BlockID.GRASS, new BaseItem(BlockID.DIRT)); // rule 5
        addIdentities(BlockID.DIRT, 3); // rule 1
        addIdentity(BlockID.COBBLESTONE); // rule 1
        addIdentities(BlockID.WOOD, 6); // rule 1
        addIdentities(BlockID.SAPLING, 6); // rule 1
        nonDataBlockBagItems.put(BlockID.BEDROCK, doNotDestroy); // exception
        // WATER, rule 6
        // STATIONARY_WATER, rule 6
        // LAVA, rule 6
        // STATIONARY_LAVA, rule 6
        addIdentity(BlockID.SAND); // rule 1
        addIdentity(BlockID.GRAVEL); // rule 1
        addIdentity(BlockID.GOLD_ORE); // rule 1
        addIdentity(BlockID.IRON_ORE); // rule 1
        nonDataBlockBagItems.put(BlockID.COAL_ORE, new BaseItem(ItemID.COAL)); // rule 5
        addIdentities(BlockID.LOG, 4); // rule 1
        addIdentities(BlockID.LEAVES, 4); // rule 1 with shears, otherwise rule 3
        addIdentity(BlockID.SPONGE); // rule 1
        addIdentity(BlockID.GLASS); // rule 3
        addIdentity(BlockID.LAPIS_LAZULI_ORE); // rule 4
        addIdentity(BlockID.LAPIS_LAZULI_BLOCK); // rule 1
        addIdentity(BlockID.DISPENSER); // rule 1
        addIdentity(BlockID.SANDSTONE); // rule 1
        addIdentity(BlockID.NOTE_BLOCK); // rule 1
        addIdentities(BlockID.BED, 8); // rule 2
        addIdentity(BlockID.POWERED_RAIL); // rule 1
        addIdentity(BlockID.DETECTOR_RAIL); // rule 1
        addIdentity(BlockID.PISTON_STICKY_BASE);
        nonDataBlockBagItems.put(BlockID.WEB, new BaseItem(ItemID.STRING)); // rule 5
        // LONG_GRASS
        // DEAD_BUSH
        addIdentity(BlockID.PISTON_BASE);
        // PISTON_EXTENSION, rule 7
        addIdentities(BlockID.CLOTH, 16); // rule 1
        // PISTON_MOVING_PIECE, rule 7
        addIdentity(BlockID.YELLOW_FLOWER); // rule 1
        addIdentity(BlockID.RED_FLOWER); // rule 1
        addIdentity(BlockID.BROWN_MUSHROOM); // rule 1
        addIdentity(BlockID.RED_MUSHROOM); // rule 1
        addIdentity(BlockID.GOLD_BLOCK); // rule 1
        addIdentity(BlockID.IRON_BLOCK); // rule 1
        addIdentities(BlockID.DOUBLE_STEP, 7); // rule 3
        addIdentities(BlockID.STEP, 7); // rule 1
        addIdentity(BlockID.BRICK); // rule 1
        addIdentity(BlockID.TNT);
        addIdentity(BlockID.BOOKCASE); // rule 3
        addIdentity(BlockID.MOSSY_COBBLESTONE); // rule 1
        addIdentity(BlockID.OBSIDIAN); // rule 1
        addIdentity(BlockID.TORCH); // rule 1
        // FIRE
        // MOB_SPAWNER
        addIdentity(BlockID.OAK_WOOD_STAIRS); // rule 1
        addIdentity(BlockID.CHEST); // rule 1
        nonDataBlockBagItems.put(BlockID.REDSTONE_WIRE, new BaseItem(ItemID.REDSTONE_DUST)); // rule 3
        nonDataBlockBagItems.put(BlockID.DIAMOND_ORE, new BaseItem(ItemID.DIAMOND)); // rule 5
        addIdentity(BlockID.DIAMOND_BLOCK); // rule 1
        addIdentity(BlockID.WORKBENCH); // rule 1
        nonDataBlockBagItems.put(BlockID.CROPS, new BaseItem(ItemID.SEEDS)); // rule 3
        nonDataBlockBagItems.put(BlockID.SOIL, new BaseItem(BlockID.DIRT)); // rule 5
        addIdentity(BlockID.FURNACE); // rule 1
        nonDataBlockBagItems.put(BlockID.BURNING_FURNACE, new BaseItem(BlockID.FURNACE));
        nonDataBlockBagItems.put(BlockID.SIGN_POST, new BaseItem(ItemID.SIGN)); // rule 3
        addIdentities(BlockID.WOODEN_DOOR, 8); // rule 2
        addIdentity(BlockID.LADDER); // rule 1
        addIdentity(BlockID.MINECART_TRACKS); // rule 1
        addIdentity(BlockID.COBBLESTONE_STAIRS); // rule 3
        nonDataBlockBagItems.put(BlockID.WALL_SIGN, new BaseItem(ItemID.SIGN)); // rule 3
        addIdentity(BlockID.LEVER); // rule 1
        addIdentity(BlockID.STONE_PRESSURE_PLATE); // rule 1
        addIdentities(BlockID.IRON_DOOR, 8); // rule 2
        addIdentity(BlockID.WOODEN_PRESSURE_PLATE); // rule 1
        addIdentity(BlockID.REDSTONE_ORE); // rule 4
        nonDataBlockBagItems.put(BlockID.GLOWING_REDSTONE_ORE, new BaseItem(BlockID.REDSTONE_ORE)); // rule 4
        nonDataBlockBagItems.put(BlockID.REDSTONE_TORCH_OFF, new BaseItem(BlockID.REDSTONE_TORCH_ON)); // rule 3
        addIdentity(BlockID.REDSTONE_TORCH_ON); // rule 1
        addIdentity(BlockID.STONE_BUTTON); // rule 1
        addIdentity(BlockID.SNOW); // rule 1
        addIdentity(BlockID.ICE); // exception
        addIdentity(BlockID.SNOW_BLOCK); // rule 3
        addIdentity(BlockID.CACTUS);
        addIdentity(BlockID.CLAY); // rule 3
        nonDataBlockBagItems.put(BlockID.REED, new BaseItem(ItemID.SUGAR_CANE_ITEM)); // rule 3
        addIdentity(BlockID.JUKEBOX); // rule 1
        addIdentity(BlockID.FENCE); // rule 1
        addIdentity(BlockID.PUMPKIN); // rule 1
        addIdentity(BlockID.NETHERRACK); // rule 1
        addIdentity(BlockID.SLOW_SAND); // rule 1
        addIdentity(BlockID.LIGHTSTONE); // rule 4
        // PORTAL
        addIdentity(BlockID.JACKOLANTERN); // rule 1
        nonDataBlockBagItems.put(BlockID.CAKE_BLOCK, new BaseItem(ItemID.CAKE_ITEM)); // rule 3
        nonDataBlockBagItems.put(BlockID.REDSTONE_REPEATER_OFF, new BaseItem(ItemID.REDSTONE_REPEATER)); // rule 3
        nonDataBlockBagItems.put(BlockID.REDSTONE_REPEATER_ON, new BaseItem(ItemID.REDSTONE_REPEATER)); // rule 3
        addIdentities(BlockID.STAINED_GLASS_PANE, 16); // ???
        addIdentity(BlockID.TRAP_DOOR); // rule 1
        nonDataBlockBagItems.put(BlockID.SILVERFISH_BLOCK, doNotDestroy); // exception
        addIdentity(BlockID.STONE_BRICK); // rule 1
        addIdentity(BlockID.BROWN_MUSHROOM_CAP);
        addIdentity(BlockID.RED_MUSHROOM_CAP);
        addIdentity(BlockID.IRON_BARS); // rule 1
        addIdentity(BlockID.GLASS_PANE); // rule 1
        addIdentity(BlockID.MELON_BLOCK); // rule 3
        nonDataBlockBagItems.put(BlockID.PUMPKIN_STEM, new BaseItem(ItemID.PUMPKIN_SEEDS)); // rule 3
        nonDataBlockBagItems.put(BlockID.MELON_STEM, new BaseItem(ItemID.MELON_SEEDS)); // rule 3
        nonDataBlockBagItems.put(BlockID.VINE, doNotDestroy); // exception
        addIdentity(BlockID.FENCE_GATE); // rule 1
        addIdentity(BlockID.BRICK_STAIRS); // rule 3
        addIdentity(BlockID.STONE_BRICK_STAIRS); // rule 3

        // 1.9 blocks
        nonDataBlockBagItems.put(BlockID.MYCELIUM, new BaseItem(BlockID.DIRT));
        addIdentity(BlockID.LILY_PAD);
        addIdentity(BlockID.NETHER_BRICK);
        addIdentity(BlockID.NETHER_BRICK_FENCE);
        addIdentity(BlockID.NETHER_BRICK_STAIRS);
        nonDataBlockBagItems.put(BlockID.NETHER_WART, new BaseItem(ItemID.NETHER_WART_SEED));
        addIdentity(BlockID.ENCHANTMENT_TABLE);
        nonDataBlockBagItems.put(BlockID.BREWING_STAND, new BaseItem(ItemID.BREWING_STAND));
        nonDataBlockBagItems.put(BlockID.CAULDRON, new BaseItem(ItemID.CAULDRON));
        nonDataBlockBagItems.put(BlockID.END_PORTAL, doNotDestroy);
        nonDataBlockBagItems.put(BlockID.END_PORTAL_FRAME, doNotDestroy);
        addIdentity(BlockID.END_STONE);

        addIdentity(BlockID.REDSTONE_LAMP_OFF);
        nonDataBlockBagItems.put(BlockID.REDSTONE_LAMP_ON, new BaseItem(BlockID.REDSTONE_LAMP_OFF));

        addIdentities(BlockID.DOUBLE_WOODEN_STEP, 7); // rule 3
        addIdentities(BlockID.WOODEN_STEP, 7); // rule 1
        nonDataBlockBagItems.put(BlockID.COCOA_PLANT, new BaseItem(ItemID.INK_SACK, (short) (15 - ClothColor.ID.BROWN))); // rule 3
        addIdentity(BlockID.SANDSTONE_STAIRS); // rule 1
        nonDataBlockBagItems.put(BlockID.EMERALD_ORE, new BaseItem(ItemID.EMERALD)); // rule 5
        addIdentity(BlockID.ENDER_CHEST); // rule 3
        addIdentity(BlockID.TRIPWIRE_HOOK); // rule 1
        nonDataBlockBagItems.put(BlockID.TRIPWIRE, new BaseItem(ItemID.STRING)); // rule 3
        addIdentity(BlockID.EMERALD_BLOCK); // rule 1
        addIdentity(BlockID.SPRUCE_WOOD_STAIRS); // rule 1
        addIdentity(BlockID.BIRCH_WOOD_STAIRS); // rule 1
        addIdentity(BlockID.JUNGLE_WOOD_STAIRS); // rule 1
        addIdentity(BlockID.COMMAND_BLOCK); // rule 1
        addIdentities(BlockID.COBBLESTONE_WALL, 1); // rule 4
        nonDataBlockBagItems.put(BlockID.FLOWER_POT, new BaseItemStack(ItemID.FLOWER_POT)); // rule 3
        nonDataBlockBagItems.put(BlockID.CARROTS, new BaseItemStack(ItemID.CARROT)); // rule 3
        nonDataBlockBagItems.put(BlockID.POTATOES, new BaseItemStack(ItemID.POTATO)); // rule 3
        addIdentity(BlockID.WOODEN_BUTTON); // rule 1
        nonDataBlockBagItems.put(BlockID.HEAD, doNotDestroy); // exception, can't handle TE data
        addIdentities(BlockID.ANVIL, 2); // rule 4
        addIdentity(BlockID.TRAPPED_CHEST); // rule 1
        addIdentity(BlockID.PRESSURE_PLATE_LIGHT); // rule 1
        addIdentity(BlockID.PRESSURE_PLATE_HEAVY); // rule 1
        nonDataBlockBagItems.put(BlockID.COMPARATOR_OFF, new BaseItemStack(ItemID.COMPARATOR)); // rule 3
        nonDataBlockBagItems.put(BlockID.COMPARATOR_ON, new BaseItemStack(ItemID.COMPARATOR)); // rule 3
        addIdentity(BlockID.DAYLIGHT_SENSOR); // rule 1
        addIdentity(BlockID.REDSTONE_BLOCK); // rule 1
        nonDataBlockBagItems.put(BlockID.QUARTZ_ORE, new BaseItemStack(ItemID.NETHER_QUARTZ)); // rule 3
        addIdentity(BlockID.HOPPER); // rule 1
        addIdentities(BlockID.QUARTZ_BLOCK, 1); // rule 4
        for (int i = 2; i <= 4; i++) {
            dataBlockBagItems.put(typeDataKey(BlockID.QUARTZ_BLOCK, i), new BaseItem(BlockID.QUARTZ_BLOCK, (short) 2)); // rule 4, quartz pillars
        }
        addIdentity(BlockID.QUARTZ_STAIRS); // rule 1
        addIdentity(BlockID.ACTIVATOR_RAIL); // rule 1
        addIdentity(BlockID.DROPPER); // rule 1

        addIdentities(BlockID.STAINED_CLAY, 16); // rule 1
        addIdentity(BlockID.HAY_BLOCK); // rule 1
        addIdentities(BlockID.CARPET, 16); // rule 1
        addIdentity(BlockID.HARDENED_CLAY); // rule 1
        addIdentity(BlockID.COAL_BLOCK); // rule 1

        addIdentities(BlockID.LOG2, 1);
        addIdentities(BlockID.LEAVES2, 1);
        addIdentity(BlockID.ACACIA_STAIRS);
        addIdentity(BlockID.DARK_OAK_STAIRS);
        addIdentity(BlockID.PACKED_ICE);
        addIdentities(BlockID.STAINED_GLASS_PANE, 16);
        addIdentities(BlockID.DOUBLE_PLANT, 6);
    }

    /**
     * Get the block or item that this block can be constructed from. If nothing is
     * dropped, a block with a BaseItemStack of type AIR and size 0 will be returned.
     * If the block should not be destroyed (i.e. bedrock), null will be returned.
     *
     * @param type
     * @param data
     * @return
     */
    public static BaseItem getBlockBagItem(int type, int data) {
        BaseItem dropped = nonDataBlockBagItems.get(type);
        if (dropped != null) return dropped;

        dropped = dataBlockBagItems.get(typeDataKey(type, data));

        if (dropped == null) {
            return new BaseItemStack(BlockID.AIR, 0);
        }

        if (dropped == doNotDestroy) {
            return null;
        }

        return dropped;
    }

    private static void addIdentity(int type) {
        nonDataBlockBagItems.put(type, new BaseItem(type));
    }

    private static void addIdentities(int type, int maxData) {
        for (int data = 0; data < maxData; ++data) {
            dataBlockBagItems.put(typeDataKey(type, data), new BaseItem(type, (short) data));
        }
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
        BaseItem dropped = nonDataBlockBagItems.get(id);
        if (dropped == null) {
            return BlockID.AIR;
        }
        return dropped.getType();
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

        case BlockID.MYCELIUM:
            return new BaseItemStack(BlockID.DIRT);

        case BlockID.LILY_PAD:
            return new BaseItemStack(BlockID.LILY_PAD);

        case BlockID.NETHER_WART:
            return new BaseItemStack(ItemID.NETHER_WART_SEED, random.nextInt(3) + 1);

        case BlockID.BREWING_STAND:
            return new BaseItemStack(ItemID.BREWING_STAND);

        case BlockID.CAULDRON:
            return new BaseItemStack(ItemID.CAULDRON);

        case BlockID.REDSTONE_LAMP_ON:
            return new BaseItemStack(BlockID.REDSTONE_LAMP_OFF);

        case BlockID.DOUBLE_WOODEN_STEP:
            return new BaseItemStack(BlockID.WOODEN_STEP, 2, data);

        case BlockID.COCOA_PLANT:
            return new BaseItemStack(ItemID.INK_SACK, (data >= 2 ? 3 : 1), (short) 3);

        case BlockID.EMERALD_ORE:
            return new BaseItemStack(ItemID.EMERALD);

        case BlockID.TRIPWIRE:
            return new BaseItemStack(ItemID.STRING);

        case BlockID.FLOWER_POT:
            return new BaseItemStack(ItemID.FLOWER_POT);

        case BlockID.CARROTS:
            return new BaseItemStack(ItemID.CARROT, random.nextInt(3) + 1);

        case BlockID.POTATOES:
            return new BaseItemStack(ItemID.POTATO, random.nextInt(3) + 1);

        case BlockID.COMPARATOR_OFF:
        case BlockID.COMPARATOR_ON:
            return new BaseItemStack(ItemID.COMPARATOR);

        case BlockID.QUARTZ_ORE:
            return new BaseItemStack(ItemID.NETHER_QUARTZ);

        case BlockID.QUARTZ_BLOCK:
            return new BaseItemStack(BlockID.QUARTZ_BLOCK, 1, (data >= 2 ? 2 : data));

        case BlockID.LOG:
            return new BaseItemStack(BlockID.LOG, 1, (short) (data & 0x3)); // strip orientation data

        case BlockID.HAY_BLOCK:
            return new BaseItemStack(BlockID.HAY_BLOCK); // strip orientation data

        case BlockID.OAK_WOOD_STAIRS:
        case BlockID.COBBLESTONE_STAIRS:
        case BlockID.BRICK_STAIRS:
        case BlockID.STONE_BRICK_STAIRS:
        case BlockID.NETHER_BRICK_STAIRS:
        case BlockID.SPRUCE_WOOD_STAIRS:
        case BlockID.BIRCH_WOOD_STAIRS:
        case BlockID.JUNGLE_WOOD_STAIRS:
        case BlockID.QUARTZ_STAIRS:
            return new BaseItemStack(id); // strip data from stairs

        case BlockID.BEDROCK:
        case BlockID.WATER:
        case BlockID.STATIONARY_WATER:
        case BlockID.LAVA:
        case BlockID.STATIONARY_LAVA:
        case BlockID.GLASS:
        case BlockID.STAINED_GLASS_PANE:
        case BlockID.PISTON_EXTENSION:
        case BlockID.BOOKCASE:
        case BlockID.FIRE:
        case BlockID.MOB_SPAWNER:
        case BlockID.SNOW:
        case BlockID.ICE:
        case BlockID.PORTAL:
        case BlockID.AIR:
        case BlockID.SILVERFISH_BLOCK:
        case BlockID.VINE:
        case BlockID.END_PORTAL:
        case BlockID.END_PORTAL_FRAME:
        case BlockID.HEAD:
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
        nonDataAttachments.put(BlockID.LONG_GRASS, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.DEAD_BUSH, PlayerDirection.DOWN);
        for (int offset = 0; offset < 16; offset += 8) {
            dataAttachments.put(typeDataKey(BlockID.PISTON_EXTENSION, offset + 0), PlayerDirection.UP);
            dataAttachments.put(typeDataKey(BlockID.PISTON_EXTENSION, offset + 1), PlayerDirection.DOWN);
            addCardinals(BlockID.PISTON_EXTENSION, offset + 2, offset + 5, offset + 3, offset + 4);
        }
        nonDataAttachments.put(BlockID.YELLOW_FLOWER, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.RED_FLOWER, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.BROWN_MUSHROOM, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.RED_MUSHROOM, PlayerDirection.DOWN);
        for (int blockId : new int[] { BlockID.TORCH, BlockID.REDSTONE_TORCH_ON, BlockID.REDSTONE_TORCH_OFF }) {
            dataAttachments.put(typeDataKey(blockId, 0), PlayerDirection.DOWN);
            dataAttachments.put(typeDataKey(blockId, 5), PlayerDirection.DOWN); // According to the minecraft wiki, this one is history. Keeping both, for now...
            addCardinals(blockId, 4, 1, 3, 2);
        }
        nonDataAttachments.put(BlockID.REDSTONE_WIRE, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.CROPS, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.SIGN_POST, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.WOODEN_DOOR, PlayerDirection.DOWN);
        addCardinals(BlockID.LADDER, 2, 5, 3, 4);
        addCardinals(BlockID.WALL_SIGN, 2, 5, 3, 4);
        for (int offset = 0; offset < 16; offset += 8) {
            addCardinals(BlockID.LEVER, offset + 4, offset + 1, offset + 3, offset + 2);
            dataAttachments.put(typeDataKey(BlockID.LEVER, offset + 5), PlayerDirection.DOWN);
            dataAttachments.put(typeDataKey(BlockID.LEVER, offset + 6), PlayerDirection.DOWN);
            dataAttachments.put(typeDataKey(BlockID.LEVER, offset + 7), PlayerDirection.UP);
            dataAttachments.put(typeDataKey(BlockID.LEVER, offset + 0), PlayerDirection.UP);
        }
        nonDataAttachments.put(BlockID.STONE_PRESSURE_PLATE, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.IRON_DOOR, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.WOODEN_PRESSURE_PLATE, PlayerDirection.DOWN);
        // redstone torches: see torches
        for (int offset = 0; offset < 16; offset += 8) {
            addCardinals(BlockID.STONE_BUTTON, offset + 4, offset + 1, offset + 3, offset + 2);
            addCardinals(BlockID.WOODEN_BUTTON, offset + 4, offset + 1, offset + 3, offset + 2);
        }
        nonDataAttachments.put(BlockID.CACTUS, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.REED, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.CAKE_BLOCK, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.REDSTONE_REPEATER_OFF, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.REDSTONE_REPEATER_ON, PlayerDirection.DOWN);
        for (int offset = 0; offset < 16; offset += 4) {
            addCardinals(BlockID.TRAP_DOOR, offset + 0, offset + 3, offset + 1, offset + 2);
        }
        nonDataAttachments.put(BlockID.PUMPKIN_STEM, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.MELON_STEM, PlayerDirection.DOWN);
        // vines are complicated, but I'll list the single-attachment variants anyway
        dataAttachments.put(typeDataKey(BlockID.VINE, 0), PlayerDirection.UP);
        addCardinals(BlockID.VINE, 1, 2, 4, 8);
        nonDataAttachments.put(BlockID.NETHER_WART, PlayerDirection.DOWN);
        for (int offset = 0; offset < 16; offset += 4) {
            addCardinals(BlockID.COCOA_PLANT, offset + 0, offset + 1, offset + 2, offset + 3);
        }
        for (int offset = 0; offset < 16; offset += 4) {
            addCardinals(BlockID.TRIPWIRE_HOOK, offset + 2, offset + 3, offset + 0, offset + 1);
        }
        nonDataAttachments.put(BlockID.TRIPWIRE, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.FLOWER_POT, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.CARROTS, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.POTATOES, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.ANVIL, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.PRESSURE_PLATE_LIGHT, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.PRESSURE_PLATE_HEAVY, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.COMPARATOR_OFF, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.COMPARATOR_ON, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.CARPET, PlayerDirection.DOWN);
        nonDataAttachments.put(BlockID.DOUBLE_PLANT, PlayerDirection.DOWN);

        // Rails are hardcoded to be attached to the block below them.
        // In addition to that, let's attach ascending rails to the block they're ascending towards.
        for (int offset = 0; offset < 16; offset += 8) {
            addCardinals(BlockID.POWERED_RAIL, offset + 3, offset + 4, offset + 2, offset + 5);
            addCardinals(BlockID.DETECTOR_RAIL, offset + 3, offset + 4, offset + 2, offset + 5);
            addCardinals(BlockID.MINECART_TRACKS, offset + 3, offset + 4, offset + 2, offset + 5);
            addCardinals(BlockID.ACTIVATOR_RAIL, offset + 3, offset + 4, offset + 2, offset + 5);
        }
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

        return dataAttachments.get(typeDataKey(type, data));
    }

    private static int typeDataKey(int type, int data) {
        return (type << 4) | (data & 0xf);
    }

    private static void addCardinals(int type, int west, int north, int east, int south) {
        dataAttachments.put(typeDataKey(type, west), PlayerDirection.WEST);
        dataAttachments.put(typeDataKey(type, north), PlayerDirection.NORTH);
        dataAttachments.put(typeDataKey(type, east), PlayerDirection.EAST);
        dataAttachments.put(typeDataKey(type, south), PlayerDirection.SOUTH);
    }
}
