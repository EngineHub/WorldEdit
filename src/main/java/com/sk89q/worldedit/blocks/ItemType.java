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
    PISTON_EXTENSION(34, "Piston extension", "pistonhead"),
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
    TRAP_DOOR(96, "Trap door", "trapdoor", "hatch", "floordoor"),

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
        return (id >= 256 && id <= 259)
            || id == 261
            || (id >= 267 && id <= 279)
            || (id >= 281 && id <= 286)
            || (id >= 290 && id <= 294)
            || (id >= 298 && id <= 317)
            || (id >= 325 && id <= 327)
            || id == 335
            || id == 354
            || id == 355
            || id >= 2256;
    }
    
    /**
     * Returns true if an item uses its damage value for something
     * other than damage.
     * 
     * @param id
     * @return
     */
    public static boolean usesDamageValue(int id) {
        return id == 35
            || id == 351;
    }
}
