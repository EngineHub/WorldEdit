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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;
import java.util.Map.Entry;
import com.sk89q.util.StringUtil;

/**
 * ItemType types.
 *
 * @author sk89q
 */
public enum ItemType {
    // Blocks
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

    // Items
    IRON_SHOVEL(256, "Iron shovel", "ironshovel"),
    IRON_PICK(257, "Iron pick", "ironpick", "ironpickaxe"),
    IRON_AXE(258, "Iron axe", "ironaxe"),
    FLINT_AND_TINDER(259, "Flint and tinder", "flintandtinder", "lighter", "flintandsteel", "flintsteel", "flintandiron", "flintnsteel", "flintniron", "flintntinder"),
    RED_APPLE(260, "Red apple", "redapple", "apple"),
    BOW(261, "Bow", "bow"),
    ARROW(262, "Arrow", "arrow"),
    COAL(263, "Coal", "coal"),
    DIAMOND(264, "Diamond", "diamond"),
    IRON_BAR(265, "Iron bar", "ironbar", "iron"),
    GOLD_BAR(266, "Gold bar", "goldbar", "gold"),
    IRON_SWORD(267, "Iron sword", "ironsword"),
    WOOD_SWORD(268, "Wooden sword", "woodsword"),
    WOOD_SHOVEL(269, "Wooden shovel", "woodshovel"),
    WOOD_PICKAXE(270, "Wooden pickaxe", "woodpick", "woodpickaxe"),
    WOOD_AXE(271, "Wooden axe", "woodaxe"),
    STONE_SWORD(272, "Stone sword", "stonesword"),
    STONE_SHOVEL(273, "Stone shovel", "stoneshovel"),
    STONE_PICKAXE(274, "Stone pickaxe", "stonepick", "stonepickaxe"),
    STONE_AXE(275, "Stone pickaxe", "stoneaxe"),
    DIAMOND_SWORD(276, "Diamond sword", "diamondsword"),
    DIAMOND_SHOVEL(277, "Diamond shovel", "diamondshovel"),
    DIAMOND_PICKAXE(278, "Diamond pickaxe", "diamondpick", "diamondpickaxe"),
    DIAMOND_AXE(279, "Diamond axe", "diamondaxe"),
    STICK(280, "Stick", "stick"),
    BOWL(281, "Bowl", "bowl"),
    MUSHROOM_SOUP(282, "Mushroom soup", "mushroomsoup", "soup", "brbsoup"),
    GOLD_SWORD(283, "Golden sword", "goldsword"),
    GOLD_SHOVEL(284, "Golden shovel", "goldshovel"),
    GOLD_PICKAXE(285, "Golden pickaxe", "goldpick", "goldpickaxe"),
    GOLD_AXE(286, "Golden axe", "goldaxe"),
    STRING(287, "String", "string"),
    FEATHER(288, "Feather", "feather"),
    SULPHUR(289, "Sulphur", "sulphur", "sulfur", "gunpowder"),
    WOOD_HOE(290, "Wooden hoe", "woodhoe"),
    STONE_HOE(291, "Stone hoe", "stonehoe"),
    IRON_HOE(292, "Iron hoe", "ironhoe"),
    DIAMOND_HOE(293, "Diamond hoe", "diamondhoe"),
    GOLD_HOE(294, "Golden hoe", "goldhoe"),
    SEEDS(295, "Seeds", "seeds", "seed"),
    WHEAT(296, "Wheat", "wheat"),
    BREAD(297, "Bread", "bread"),
    LEATHER_HELMET(298, "Leather helmet", "leatherhelmet", "leatherhat"),
    LEATHER_CHEST(299, "Leather chestplate", "leatherchest", "leatherchestplate", "leathervest", "leatherbreastplate", "leatherplate", "leathercplate", "leatherbody"),
    LEATHER_PANTS(300, "Leather pants", "leatherpants", "leathergreaves", "leatherlegs", "leatherleggings", "leatherstockings", "leatherbreeches"),
    LEATHER_BOOTS(301, "Leather boots", "leatherboots", "leathershoes", "leatherfoot", "leatherfeet"),
    CHAINMAIL_HELMET(302, "Chainmail helmet", "chainmailhelmet", "chainmailhat"),
    CHAINMAIL_CHEST(303, "Chainmail chestplate", "chainmailchest", "chainmailchestplate", "chainmailvest", "chainmailbreastplate", "chainmailplate", "chainmailcplate", "chainmailbody"),
    CHAINMAIL_PANTS(304, "Chainmail pants", "chainmailpants", "chainmailgreaves", "chainmaillegs", "chainmailleggings", "chainmailstockings", "chainmailbreeches"),
    CHAINMAIL_BOOTS(305, "Chainmail boots", "chainmailboots", "chainmailshoes", "chainmailfoot", "chainmailfeet"),
    IRON_HELMET(306, "Iron helmet", "ironhelmet", "ironhat"),
    IRON_CHEST(307, "Iron chestplate", "ironchest", "ironchestplate", "ironvest", "ironbreastplate", "ironplate", "ironcplate", "ironbody"),
    IRON_PANTS(308, "Iron pants", "ironpants", "irongreaves", "ironlegs", "ironleggings", "ironstockings", "ironbreeches"),
    IRON_BOOTS(309, "Iron boots", "ironboots", "ironshoes", "ironfoot", "ironfeet"),
    DIAMOND_HELMET(310, "Diamond helmet", "diamondhelmet", "diamondhat"),
    DIAMOND_CHEST(311, "Diamond chestplate", "diamondchest", "diamondchestplate", "diamondvest", "diamondbreastplate", "diamondplate", "diamondcplate", "diamondbody"),
    DIAMOND_PANTS(312, "Diamond pants", "diamondpants", "diamondgreaves", "diamondlegs", "diamondleggings", "diamondstockings", "diamondbreeches"),
    DIAMOND_BOOTS(313, "Diamond boots", "diamondboots", "diamondshoes", "diamondfoot", "diamondfeet"),
    GOLD_HELMET(314, "Gold helmet", "goldhelmet", "goldhat"),
    GOLD_CHEST(315, "Gold chestplate", "goldchest", "goldchestplate", "goldvest", "goldbreastplate", "goldplate", "goldcplate", "goldbody"),
    GOLD_PANTS(316, "Gold pants", "goldpants", "goldgreaves", "goldlegs", "goldleggings", "goldstockings", "goldbreeches"),
    GOLD_BOOTS(317, "Gold boots", "goldboots", "goldshoes", "goldfoot", "goldfeet"),
    FLINT(318, "Flint", "flint"),
    RAW_PORKCHOP(319, "Raw porkchop", "rawpork", "rawporkchop", "rawbacon", "baconstrips", "rawmeat"),
    COOKED_PORKCHOP(320, "Cooked porkchop", "pork", "cookedpork", "cookedporkchop", "cookedbacon", "bacon", "meat"),
    PAINTING(321, "Painting", "painting"),
    GOLD_APPLE(322, "Golden apple", "goldapple", "goldenapple"),
    SIGN(323, "Wooden sign", "sign"),
    WOODEN_DOOR_ITEM(324, "Wooden door", "wooddoor", "door"),
    BUCKET(325, "Bucket", "bucket", "bukkit"),
    WATER_BUCKET(326, "Water bucket", "waterbucket", "waterbukkit"),
    LAVA_BUCKET(327, "Lava bucket", "lavabucket", "lavabukkit"),
    MINECART(328, "Minecart", "minecart", "cart"),
    SADDLE(329, "Saddle", "saddle"),
    IRON_DOOR_ITEM(330, "Iron door", "irondoor"),
    REDSTONE_DUST(331, "Redstone dust", "redstonedust", "reddust", "redstone", "dust", "wire"),
    SNOWBALL(332, "Snowball", "snowball"),
    WOOD_BOAT(333, "Wooden boat", "woodboat", "woodenboat", "boat"),
    LEATHER(334, "Leather", "leather", "cowhide"),
    MILK_BUCKET(335, "Milk bucket", "milkbucket", "milk", "milkbukkit"),
    BRICK_BAR(336, "Brick", "brickbar"),
    CLAY_BALL(337, "Clay", "clay"),
    SUGAR_CANE_ITEM(338, "Sugar cane", "sugarcane", "reed", "reeds"),
    PAPER(339, "Paper", "paper"),
    BOOK(340, "Book", "book"),
    SLIME_BALL(341, "Slime ball", "slimeball", "slime"),
    STORAGE_MINECART(342, "Storage minecart", "storageminecart", "storagecart"),
    POWERED_MINECART(343, "Powered minecart", "poweredminecart", "poweredcart"),
    EGG(344, "Egg", "egg"),
    COMPASS(345, "Compass", "compass"),
    FISHING_ROD(346, "Fishing rod", "fishingrod", "fishingpole"),
    WATCH(347, "Watch", "watch", "clock", "timer"),
    LIGHTSTONE_DUST(348, "Glowstone dust", "lightstonedust", "glowstonedone", "brightstonedust", "brittlegolddust", "brimstonedust"),
    RAW_FISH(349, "Raw fish", "rawfish", "fish"),
    COOKED_FISH(350, "Cooked fish", "cookedfish"),
    INK_SACK(351, "Ink sac", "inksac", "ink", "dye", "inksack"),
    BONE(352, "Bone", "bone"),
    SUGAR(353, "Sugar", "sugar"),
    CAKE_ITEM(354, "Cake", "cake"),
    BED_ITEM(355, "Bed", "bed"),
    REDSTONE_REPEATER(356, "Redstone repeater", "redstonerepeater", "diode", "delayer", "repeater"),
    COOKIE(357, "Cookie", "cookie"),
    MAP(358, "Map", "map"),
    SHEARS(359, "Shears", "shears", "scissors"),
    GOLD_RECORD(2256, "Gold Record", "goldrecord", "golddisc"),
    GREEN_RECORD(2257, "Green Record", "greenrecord", "greenddisc");

    /**
     * Stores a map of the IDs for fast access.
     */
    private static final Map<Integer,ItemType> ids = new HashMap<Integer,ItemType>();
    /**
     * Stores a map of the names for fast access.
     */
    private static final Map<String,ItemType> lookup = new LinkedHashMap<String,ItemType>();

    private final int id;
    private final String name;
    private final String[] lookupKeys;

    static {
        for (ItemType type : EnumSet.allOf(ItemType.class)) {
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
    ItemType(int id, String name, String lookupKey) {
        this.id = id;
        this.name = name;
        this.lookupKeys = new String[] {lookupKey};
    }

    /**
     * Construct the type.
     *
     * @param id
     * @param name
     */
    ItemType(int id, String name, String ... lookupKeys) {
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
    public static ItemType fromID(int id) {
        return ids.get(id);
    }

    /**
     * Get a name for the item.
     *
     * @param id
     * @return
     */
    public static String toName(int id) {
        ItemType type = ids.get(id);
        if (type != null) {
            return type.getName();
        } else {
            return "#" + id;
        }
    }

    /**
     * Get a name for a held item.
     *
     * @param id
     * @return
     */
    public static String toHeldName(int id) {
        if (id == 0) {
            return "Hand";
        }
        ItemType type = ids.get(id);
        if (type != null) {
            return type.getName();
        } else {
            return "#" + id;
        }
    }

    /**
     * Return type from name. May return null.
     *
     * @param name
     * @return
     */
    public static ItemType lookup(String name) {
        return lookup(name, true);
    }

    /**
     * Return type from name. May return null.
     *
     * @param name
     * @param fuzzy
     * @return
     */
    public static ItemType lookup(String name, boolean fuzzy) {
        String testName = name.replace(" ", "").toLowerCase();
        
        ItemType type = lookup.get(testName);
        
        if (type != null) {
            return type;
        }
        
        if (!fuzzy) {
            return null;
        }
        
        int minDist = -1;
        
        for (Entry<String, ItemType> entry : lookup.entrySet()) {
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
     * Get item numeric ID.
     *
     * @return
     */
    public int getID() {
        return id;
    }

    /**
     * Get user-friendly item name.
     *
     * @return
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get a list of aliases.
     * 
     * @return
     */
    public String[] getAliases() {
        return lookupKeys;
    }
    
    /**
     * Returns true if an item should not be stacked.
     * 
     * @param id
     * @return
     */
    public static boolean shouldNotStack(int id) {
        ItemType t = ItemType.fromID(id);
        if (t == null) return false;
        return t == ItemType.IRON_SHOVEL
            || t == ItemType.IRON_PICK
            || t == ItemType.IRON_AXE
            || t == ItemType.FLINT_AND_TINDER
            || t == ItemType.BOW
            || t == ItemType.IRON_SWORD
            || t == ItemType.WOOD_SWORD
            || t == ItemType.WOOD_SHOVEL
            || t == ItemType.WOOD_PICKAXE
            || t == ItemType.WOOD_AXE
            || t == ItemType.STONE_SWORD
            || t == ItemType.STONE_SHOVEL
            || t == ItemType.STONE_PICKAXE
            || t == ItemType.STONE_AXE
            || t == ItemType.DIAMOND_SWORD
            || t == ItemType.DIAMOND_SHOVEL
            || t == ItemType.DIAMOND_PICKAXE
            || t == ItemType.DIAMOND_AXE
            || t == ItemType.BOWL
            || t == ItemType.GOLD_SWORD
            || t == ItemType.GOLD_SHOVEL
            || t == ItemType.GOLD_PICKAXE
            || t == ItemType.GOLD_AXE
            || t == ItemType.WOOD_HOE
            || t == ItemType.STONE_HOE
            || t == ItemType.IRON_HOE
            || t == ItemType.DIAMOND_HOE
            || t == ItemType.GOLD_HOE
            || t == ItemType.BREAD
            || t == ItemType.LEATHER_HELMET
            || t == ItemType.LEATHER_CHEST
            || t == ItemType.LEATHER_PANTS
            || t == ItemType.LEATHER_BOOTS
            || t == ItemType.CHAINMAIL_CHEST
            || t == ItemType.CHAINMAIL_HELMET
            || t == ItemType.CHAINMAIL_BOOTS
            || t == ItemType.CHAINMAIL_PANTS
            || t == ItemType.IRON_HELMET
            || t == ItemType.IRON_CHEST
            || t == ItemType.IRON_PANTS
            || t == ItemType.IRON_BOOTS
            || t == ItemType.DIAMOND_HELMET
            || t == ItemType.DIAMOND_PANTS
            || t == ItemType.DIAMOND_CHEST
            || t == ItemType.DIAMOND_BOOTS
            || t == ItemType.GOLD_HELMET
            || t == ItemType.GOLD_CHEST
            || t == ItemType.GOLD_PANTS
            || t == ItemType.GOLD_BOOTS
            || t == ItemType.RAW_PORKCHOP
            || t == ItemType.COOKED_PORKCHOP
            || t == ItemType.SIGN
            || t == ItemType.WOODEN_DOOR_ITEM
            || t == ItemType.BUCKET
            || t == ItemType.WATER_BUCKET
            || t == ItemType.LAVA_BUCKET
            || t == ItemType.MINECART
            || t == ItemType.SADDLE
            || t == ItemType.IRON_DOOR_ITEM
            || t == ItemType.WOOD_BOAT
            || t == ItemType.MILK_BUCKET
            || t == ItemType.STORAGE_MINECART
            || t == ItemType.POWERED_MINECART
            || t == ItemType.WATCH
            || t == ItemType.RAW_FISH
            || t == ItemType.COOKED_FISH
            || t == ItemType.CAKE_ITEM
            || t == ItemType.BED_ITEM
            || t == ItemType.MAP
            || t == ItemType.GOLD_RECORD
            || t == ItemType.GREEN_RECORD;
    }
    
    /**
     * Returns true if an item uses its damage value for something
     * other than damage.
     * 
     * @param id
     * @return
     */
    public static boolean usesDamageValue(int id) {
        return id == ItemType.CLOTH.getID()
            || id == ItemType.INK_SACK.getID();
    }
}
