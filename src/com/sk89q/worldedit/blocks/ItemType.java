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
 * ItemType types.
 *
 * @author sk89q
 */
public enum ItemType {
    AIR(0, "Air", "air"),
    STONE(1, "Stone", new String[]{"stone", "rock"}),
    GRASS(2, "Grass", "grass"),
    DIRT(3, "Dirt", "dirt"),
    COBBLESTONE(4, "Cobblestone", new String[]{"cobblestone", "cobble"}),
    WOOD(5, "Wood", "wood"),
    SAPLING(6, "Sapling", "sapling"),
    BEDROCK(7, "Bedrock", new String[]{"adminium", "bedrock"}),
    WATER(8, "Water", new String[]{"watermoving", "movingwater"}),
    STATIONARY_WATER(9, "Water (stationary)", new String[]{"water", "waterstationary", "stationarywater"}),
    LAVA(10, "Lava", new String[]{"lavamoving", "movinglava"}),
    STATIONARY_LAVA(11, "Lava (stationary)", new String[]{"lava", "lavastationary", "stationarylava"}),
    SAND(12, "Sand", "sand"),
    GRAVEL(13, "Gravel", "gravel"),
    GOLD_ORE(14, "Gold ore", "goldore"),
    IRON_ORE(15, "Iron ore", "ironore"),
    COAL_ORE(16, "Coal ore", "coalore"),
    LOG(17, "Log", new String[]{"log", "tree", "pine", "oak", "birch"}),
    LEAVES(18, "Leaves", new String[]{"leaves", "leaf"}),
    SPONGE(19, "Sponge", "sponge"),
    GLASS(20, "Glass", "glass"),
    LAPIS_LAZULI_ORE(21, "Lapis lazuli ore", new String[]{"lapislazuliore", "blueore"}),
    LAPIS_LAZULI(22, "Lapis lazuli", new String[]{"lapislazuli", "lapislazuliblock", "blue", "bluerock"}),
    DISPENSER(23, "Dispenser", "dispenser"),
    SANDSTONE(24, "Sandstone", "sandstone"),
    NOTE_BLOCK(25, "Note block", new String[]{"musicblock", "noteblock", "note", "music", "instrument"}),
    CLOTH(35, "Cloth", "cloth"),
    YELLOW_FLOWER(37, "Yellow flower", new String[]{"yellowflower", "flower"}),
    RED_FLOWER(38, "Red rose", new String[]{"redflower", "redrose", "rose"}),
    BROWN_MUSHROOM(39, "Brown mushroom", new String[]{"brownmushroom", "mushroom"}),
    RED_MUSHROOM(40, "Red mushroom", "redmushroom"),
    GOLD_BLOCK(41, "Gold block", new String[]{"gold", "goldblock"}),
    IRON_BLOCK(42, "Iron block", new String[]{"iron", "ironblock"}),
    DOUBLE_STEP(43, "Double step", new String[]{"doubleslab", "doublestoneslab", "doublestep"}),
    STEP(44, "Step", new String[]{"slab", "stoneslab", "step"}),
    BRICK(45, "Brick", new String[]{"brick", "brickblock"}),
    TNT(46, "TNT", "tnt"),
    BOOKCASE(47, "Bookcase", new String[]{"bookshelf", "bookshelves"}),
    MOSSY_COBBLESTONE(48, "Cobblestone (mossy)", new String[]{"mossycobblestone", "mossstone", "mossystone", "mosscobble", "mossycobble"}),
    OBSIDIAN(49, "Obsidian", "obsidian"),
    TORCH(50, "Torch", "torch"),
    FIRE(51, "Fire", "fire"),
    MOB_SPAWNER(52, "Mob spawner", "mobspawner"),
    WOODEN_STAIRS(53, "Wooden stairs", new String[]{"woodstair", "woodstairs", "woodenstair", "woodenstairs"}),
    CHEST(54, "Chest", "chest"),
    REDSTONE_WIRE(55, "Redstone wire", "redstone"),
    DIAMOND_ORE(56, "Diamond ore", "diamondore"),
    DIAMOND_BLOCK(57, "Diamond block", new String[]{"diamond", "diamondblock"}),
    WORKBENCH(58, "Workbench", new String[]{"workbench", "table", "craftingtable"}),
    CROPS(59, "Crops", new String[]{"crops", "crop", "plant", "plants"}),
    SOIL(60, "Soil", "soil"),
    FURNACE(61, "Furnace", "furnace"),
    BURNING_FURNACE(62, "Furnace (burning)", "burningfurnace"),
    SIGN_POST(63, "Sign post", new String[]{"sign", "signpost"}),
    WOODEN_DOOR(64, "Wooden door", new String[]{"wooddoor", "woodendoor"}),
    LADDER(65, "Ladder", "ladder"),
    MINECART_TRACKS(66, "Minecart tracks", new String[]{"track", "tracks", "minecrattrack", "minecarttracks"}),
    COBBLESTONE_STAIRS(67, "Cobblestone stairs", new String[]{"cobblestonestair", "cobblestonestairs", "cobblestair", "cobblestairs"}),
    WALL_SIGN(68, "Wall sign", "wallsign"),
    LEVER(69, "Lever", "lever"),
    STONE_PRESSURE_PLATE(70, "Stone pressure plate", new String[]{"stonepressureplate", "stoneplate"}),
    IRON_DOOR(71, "Iron Door", "irondoor"),
    WOODEN_PRESSURE_PLATE(72, "Wooden pressure plate", new String[]{"woodpressureplate", "woodplate", "woodenpressureplate", "woodenplate"}),
    REDSTONE_ORE(73, "Redstone ore", "redstoneore"),
    GLOWING_REDSTONE_ORE(74, "Glowing redstone ore", "glowingredstoneore"),
    REDSTONE_TORCH_OFF(75, "Redstone torch (off)",
            new String[]{"redstonetorch"," redstonetorchoff", "rstorch"}),
    REDSTONE_TORCH_ON(76, "Redstone torch (on)", "redstonetorchon"),
    STONE_BUTTON(77, "Stone Button", "stonebutton"),
    SNOW(78, "Snow", "snow"),
    ICE(79, "Ice", "ice"),
    SNOW_BLOCK(80, "Snow block", "snowblock"),
    CACTUS(81, "Cactus", "cactus"),
    CLAY(82, "Clay", "clay"),
    SUGAR_CANE(83, "Reed", new String[]{"reed", "cane", "sugarcane", "sugarcanes"}),
    JUKEBOX(84, "Jukebox", "jukebox"),
    FENCE(85, "Fence", "fence"),
    PUMPKIN(86, "Pumpkin", "pumpkin"),
    NETHERRACK(87, "Netherrack", new String[]{"redmossycobblestone", "redcobblestone", "redmosstone", "redcobble", "netherstone", "netherrack", "nether"}),
    SOUL_SAND(88, "Soul sand", new String[]{"slowmud", "mud", "soulsand"}),
    GLOWSTONE(89, "Glowstone", new String[]{"brittlegold", "glowstone", "lightstone"}),
    PORTAL(90, "Portal", "portal"),
    JACK_O_LANTERN(91, "Pumpkin (on)", new String[]{"pumpkinlighted", "pumpkinon", "litpumpkin"}),
    CAKE(92, "Cake", "cake"),
    IRON_SHOVEL(256, "Iron shovel", "ironshovel"),
    IRON_PICK(257, "Iron pick", "ironpick"),
    IRON_AXE(258, "Iron axe", "ironaxe"),
    FLINT_AND_TINDER(259, "Flint and tinder", "flintandtinder"),
    RED_APPLE(260, "Red apple", "redapple"),
    BOW(261, "Bow", "bow"),
    ARROW(262, "Arrow", "arrow"),
    COAL(263, "Coal", "coal"),
    DIAMOND(264, "Diamond", "diamond"),
    IRON_BAR(265, "Iron bar", "ironbar"),
    GOLD_BAR(266, "Gold bar", "goldbar"),
    IRON_SWORD(267, "Iron sword", "ironsword"),
    WOOD_SWORD(268, "Wooden sword", "woodsword"),
    WOOD_SHOVEL(269, "Wooden shovel", "woodshovel"),
    WOOD_PICKAXE(270, "Wooden pickaxe", "woodpick"),
    WOOD_AXE(271, "Wooden axe", "woodaxe"),
    STONE_SWORD(272, "Stone sword", "stonesword"),
    STONE_SHOVEL(273, "Stone shovel", "stoneshovel"),
    STONE_PICKAXE(274, "Stone pickaxe", "stonepick"),
    STONE_AXE(275, "Stone pickaxe", "stoneaxe"),
    DIAMOND_SWORD(276, "Diamond sword", "diamondsword"),
    DIAMOND_SHOVEL(277, "Diamond shovel", "diamondshovel"),
    DIAMOND_PICKAXE(278, "Diamond pickaxe", "diamondpick"),
    DIAMOND_AXE(279, "Diamond axe", "diamondaxe"),
    STICK(280, "Stick", "stick"),
    BOWL(281, "Bowl", "bowl"),
    MUSHROOM_SOUP(282, "Mushroom soup", "mushroomsoup"),
    GOLD_SWORD(283, "Golden sword", "goldsword"),
    GOLD_SHOVEL(284, "Golden shovel", "goldshovel"),
    GOLD_PICKAXE(285, "Golden pickaxe", "goldpick"),
    GOLD_AXE(286, "Golden axe", "goldaxe"),
    STRING(287, "String", "string"),
    FEATHER(288, "Feather", "feather"),
    SULPHUR(289, "Sulphur", "sulphur"),
    WOOD_HOE(290, "Wooden hoe", "woodhoe"),
    STONE_HOE(291, "Stone hoe", "stonehoe"),
    IRON_HOE(292, "Iron hoe", "ironhoe"),
    DIAMOND_HOE(293, "Diamond hoe", "diamondhoe"),
    GOLD_HOE(294, "Golden hoe", "goldhoe"),
    SEEDS(295, "Seeds", "seeds"),
    WHEAT(296, "Wheat", "wheat"),
    BREAD(297, "Bread", "bread"),
    LEATHER_HELMET(298, "Leather helmet", "leatherhelmet"),
    LEATHER_CHEST(299, "Leather chestplate", "leatherchest"),
    LEATHER_PANTS(300, "Leather pants", "leatherpants"),
    LEATHER_BOOTS(301, "Leather boots", "leatherboots"),
    CHAINMAIL_HELMET(302, "Chainmail helmet", "chainmailhelmet"),
    CHAINMAIL_CHEST(303, "Chainmail chestplate", "chainmailchest"),
    CHAINMAIL_PANTS(304, "Chainmail pants", "chainmailpants"),
    CHAINMAIL_BOOTS(305, "Chainmail boots", "chainmailboots"),
    IRON_HELMET(306, "Iron helmet", "ironhelmet"),
    IRON_CHEST(307, "Iron chestplate", "ironchest"),
    IRON_PANTS(308, "Iron pants", "ironpants"),
    IRON_BOOTS(309, "Iron boots", "ironboots"),
    DIAMOND_HELMET(310, "Diamond helmet", "diamondhelmet"),
    DIAMOND_CHEST(311, "Diamond chestplate", "diamondchest"),
    DIAMOND_PANTS(312, "Diamond pants", "diamondpants"),
    DIAMOND_BOOTS(313, "Diamond boots", "diamondboots"),
    GOLD_HELMET(314, "Gold helmet", "goldhelmet"),
    GOLD_CHEST(315, "Gold chestplate", "goldchest"),
    GOLD_PANTS(316, "Gold pants", "goldpants"),
    GOLD_BOOTS(317, "Gold boots", "goldboots"),
    FLINT(318, "Flint", "flint"),
    RAW_PORKCHOP(319, "Raw porkchop", "rawporkchop"),
    COOKED_PORKCHOP(320, "Cooked porkchop", "cookedporkchop"),
    PAINTING(321, "Painting", "painting"),
    GOLD_APPLE(322, "Golden apple", "goldapple"),
    SIGN(323, "Wooden sign", "sign"),
    WOODEN_DOOR_ITEM(324, "Wooden door", "wooddoor"),
    BUCKET(325, "Bucket", "bucket"),
    WATER_BUCKET(326, "Water bucket", "waterbucket"),
    LAVA_BUCKET(327, "Lava bucket", "lavabucket"),
    MINECART(328, "Minecart", "minecart"),
    SADDLE(329, "Saddle", "saddle"),
    IRON_DOOR_ITEM(330, "Iron door", "irondoor"),
    REDSTONE_DUST(331, "Redstone dust", "redstonedust"),
    SNOWBALL(332, "Snowball", "snowball"),
    WOOD_BOAT(333, "Wooden boat", "woodboat"),
    LEATHER(334, "Leather", "leather"),
    MILK_BUCKET(335, "Milk bucket", "milkbucket"),
    BRICK_BAR(336, "Brick", "brick"),
    CLAY_BALL(337, "Clay", "clay"),
    SUGAR_CANE_ITEM(338, "Sugar cane", new String[]{"sugarcane", "reed", "reeds"}),
    PAPER(339, "Paper", "paper"),
    BOOK(340, "Book", "book"),
    SLIME_BALL(341, "Slime ball", "slimeball"),
    STORAGE_MINECART(342, "Storage minecart", "storageminecart"),
    POWERED_MINECART(343, "Powered minecart", "poweredminecart"),
    EGG(344, "Egg", "egg"),
    COMPASS(345, "Compass", "compass"),
    FISHING_ROD(346, "Fishing rod", "fishingrod"),
    WATCH(347, "Watch", "watch"),
    LIGHTSTONE_DUST(348, "Lightstone dust", "lightstonedust"),
    RAW_FISH(349, "Raw fish", "rawfish"),
    COOKED_FISH(350, "Cooked fish", "cookedfish"),
    INK_SACK(351, "Ink sack", "inksack"),
    BONE(352, "Bone", "bone"),
    SUGAR(353, "Sugar", "sugar"),
    CAKE_ITEM(354, "Cake", "cake"),
    GOLD_RECORD(2256, "Gold Record", "goldrecord"),
    GREEN_RECORD(2257, "Green Record", "greenrecord");

    /**
     * Stores a map of the IDs for fast access.
     */
    private static final Map<Integer,ItemType> ids = new HashMap<Integer,ItemType>();
    /**
     * Stores a map of the names for fast access.
     */
    private static final Map<String,ItemType> lookup = new HashMap<String,ItemType>();

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
        this.lookupKeys = new String[]{lookupKey};
    }

    /**
     * Construct the type.
     *
     * @param id
     * @param name
     */
    ItemType(int id, String name, String[] lookupKeys) {
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
     * Return type from name. May return null.
     *
     * @param name
     * @return
     */
    public static ItemType lookup(String name) {
        return lookup.get(name.toLowerCase());
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
            || id == 346;
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