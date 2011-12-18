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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Set;

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
    MYCELIUM(BlockID.MYCELIUM, "Mycelium", "fungus", "mycel"),
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

    // Items
    IRON_SHOVEL(ItemID.IRON_SHOVEL, "Iron shovel", "ironshovel"),
    IRON_PICK(ItemID.IRON_PICK, "Iron pick", "ironpick", "ironpickaxe"),
    IRON_AXE(ItemID.IRON_AXE, "Iron axe", "ironaxe"),
    FLINT_AND_TINDER(ItemID.FLINT_AND_TINDER, "Flint and tinder", "flintandtinder", "lighter", "flintandsteel", "flintsteel", "flintandiron", "flintnsteel", "flintniron", "flintntinder"),
    RED_APPLE(ItemID.RED_APPLE, "Red apple", "redapple", "apple"),
    BOW(ItemID.BOW, "Bow", "bow"),
    ARROW(ItemID.ARROW, "Arrow", "arrow"),
    COAL(ItemID.COAL, "Coal", "coal"),
    DIAMOND(ItemID.DIAMOND, "Diamond", "diamond"),
    IRON_BAR(ItemID.IRON_BAR, "Iron bar", "ironbar", "iron"),
    GOLD_BAR(ItemID.GOLD_BAR, "Gold bar", "goldbar", "gold"),
    IRON_SWORD(ItemID.IRON_SWORD, "Iron sword", "ironsword"),
    WOOD_SWORD(ItemID.WOOD_SWORD, "Wooden sword", "woodsword"),
    WOOD_SHOVEL(ItemID.WOOD_SHOVEL, "Wooden shovel", "woodshovel"),
    WOOD_PICKAXE(ItemID.WOOD_PICKAXE, "Wooden pickaxe", "woodpick", "woodpickaxe"),
    WOOD_AXE(ItemID.WOOD_AXE, "Wooden axe", "woodaxe"),
    STONE_SWORD(ItemID.STONE_SWORD, "Stone sword", "stonesword"),
    STONE_SHOVEL(ItemID.STONE_SHOVEL, "Stone shovel", "stoneshovel"),
    STONE_PICKAXE(ItemID.STONE_PICKAXE, "Stone pickaxe", "stonepick", "stonepickaxe"),
    STONE_AXE(ItemID.STONE_AXE, "Stone pickaxe", "stoneaxe"),
    DIAMOND_SWORD(ItemID.DIAMOND_SWORD, "Diamond sword", "diamondsword"),
    DIAMOND_SHOVEL(ItemID.DIAMOND_SHOVEL, "Diamond shovel", "diamondshovel"),
    DIAMOND_PICKAXE(ItemID.DIAMOND_PICKAXE, "Diamond pickaxe", "diamondpick", "diamondpickaxe"),
    DIAMOND_AXE(ItemID.DIAMOND_AXE, "Diamond axe", "diamondaxe"),
    STICK(ItemID.STICK, "Stick", "stick"),
    BOWL(ItemID.BOWL, "Bowl", "bowl"),
    MUSHROOM_SOUP(ItemID.MUSHROOM_SOUP, "Mushroom soup", "mushroomsoup", "soup", "brbsoup"),
    GOLD_SWORD(ItemID.GOLD_SWORD, "Golden sword", "goldsword"),
    GOLD_SHOVEL(ItemID.GOLD_SHOVEL, "Golden shovel", "goldshovel"),
    GOLD_PICKAXE(ItemID.GOLD_PICKAXE, "Golden pickaxe", "goldpick", "goldpickaxe"),
    GOLD_AXE(ItemID.GOLD_AXE, "Golden axe", "goldaxe"),
    STRING(ItemID.STRING, "String", "string"),
    FEATHER(ItemID.FEATHER, "Feather", "feather"),
    SULPHUR(ItemID.SULPHUR, "Sulphur", "sulphur", "sulfur", "gunpowder"),
    WOOD_HOE(ItemID.WOOD_HOE, "Wooden hoe", "woodhoe"),
    STONE_HOE(ItemID.STONE_HOE, "Stone hoe", "stonehoe"),
    IRON_HOE(ItemID.IRON_HOE, "Iron hoe", "ironhoe"),
    DIAMOND_HOE(ItemID.DIAMOND_HOE, "Diamond hoe", "diamondhoe"),
    GOLD_HOE(ItemID.GOLD_HOE, "Golden hoe", "goldhoe"),
    SEEDS(ItemID.SEEDS, "Seeds", "seeds", "seed"),
    WHEAT(ItemID.WHEAT, "Wheat", "wheat"),
    BREAD(ItemID.BREAD, "Bread", "bread"),
    LEATHER_HELMET(ItemID.LEATHER_HELMET, "Leather helmet", "leatherhelmet", "leatherhat"),
    LEATHER_CHEST(ItemID.LEATHER_CHEST, "Leather chestplate", "leatherchest", "leatherchestplate", "leathervest", "leatherbreastplate", "leatherplate", "leathercplate", "leatherbody"),
    LEATHER_PANTS(ItemID.LEATHER_PANTS, "Leather pants", "leatherpants", "leathergreaves", "leatherlegs", "leatherleggings", "leatherstockings", "leatherbreeches"),
    LEATHER_BOOTS(ItemID.LEATHER_BOOTS, "Leather boots", "leatherboots", "leathershoes", "leatherfoot", "leatherfeet"),
    CHAINMAIL_HELMET(ItemID.CHAINMAIL_HELMET, "Chainmail helmet", "chainmailhelmet", "chainmailhat"),
    CHAINMAIL_CHEST(ItemID.CHAINMAIL_CHEST, "Chainmail chestplate", "chainmailchest", "chainmailchestplate", "chainmailvest", "chainmailbreastplate", "chainmailplate", "chainmailcplate", "chainmailbody"),
    CHAINMAIL_PANTS(ItemID.CHAINMAIL_PANTS, "Chainmail pants", "chainmailpants", "chainmailgreaves", "chainmaillegs", "chainmailleggings", "chainmailstockings", "chainmailbreeches"),
    CHAINMAIL_BOOTS(ItemID.CHAINMAIL_BOOTS, "Chainmail boots", "chainmailboots", "chainmailshoes", "chainmailfoot", "chainmailfeet"),
    IRON_HELMET(ItemID.IRON_HELMET, "Iron helmet", "ironhelmet", "ironhat"),
    IRON_CHEST(ItemID.IRON_CHEST, "Iron chestplate", "ironchest", "ironchestplate", "ironvest", "ironbreastplate", "ironplate", "ironcplate", "ironbody"),
    IRON_PANTS(ItemID.IRON_PANTS, "Iron pants", "ironpants", "irongreaves", "ironlegs", "ironleggings", "ironstockings", "ironbreeches"),
    IRON_BOOTS(ItemID.IRON_BOOTS, "Iron boots", "ironboots", "ironshoes", "ironfoot", "ironfeet"),
    DIAMOND_HELMET(ItemID.DIAMOND_HELMET, "Diamond helmet", "diamondhelmet", "diamondhat"),
    DIAMOND_CHEST(ItemID.DIAMOND_CHEST, "Diamond chestplate", "diamondchest", "diamondchestplate", "diamondvest", "diamondbreastplate", "diamondplate", "diamondcplate", "diamondbody"),
    DIAMOND_PANTS(ItemID.DIAMOND_PANTS, "Diamond pants", "diamondpants", "diamondgreaves", "diamondlegs", "diamondleggings", "diamondstockings", "diamondbreeches"),
    DIAMOND_BOOTS(ItemID.DIAMOND_BOOTS, "Diamond boots", "diamondboots", "diamondshoes", "diamondfoot", "diamondfeet"),
    GOLD_HELMET(ItemID.GOLD_HELMET, "Gold helmet", "goldhelmet", "goldhat"),
    GOLD_CHEST(ItemID.GOLD_CHEST, "Gold chestplate", "goldchest", "goldchestplate", "goldvest", "goldbreastplate", "goldplate", "goldcplate", "goldbody"),
    GOLD_PANTS(ItemID.GOLD_PANTS, "Gold pants", "goldpants", "goldgreaves", "goldlegs", "goldleggings", "goldstockings", "goldbreeches"),
    GOLD_BOOTS(ItemID.GOLD_BOOTS, "Gold boots", "goldboots", "goldshoes", "goldfoot", "goldfeet"),
    FLINT(ItemID.FLINT, "Flint", "flint"),
    RAW_PORKCHOP(ItemID.RAW_PORKCHOP, "Raw porkchop", "rawpork", "rawporkchop", "rawbacon", "baconstrips", "rawmeat"),
    COOKED_PORKCHOP(ItemID.COOKED_PORKCHOP, "Cooked porkchop", "pork", "cookedpork", "cookedporkchop", "cookedbacon", "bacon", "meat"),
    PAINTING(ItemID.PAINTING, "Painting", "painting"),
    GOLD_APPLE(ItemID.GOLD_APPLE, "Golden apple", "goldapple", "goldenapple"),
    SIGN(ItemID.SIGN, "Wooden sign", "sign"),
    WOODEN_DOOR_ITEM(ItemID.WOODEN_DOOR_ITEM, "Wooden door", "wooddoor", "door"),
    BUCKET(ItemID.BUCKET, "Bucket", "bucket", "bukkit"),
    WATER_BUCKET(ItemID.WATER_BUCKET, "Water bucket", "waterbucket", "waterbukkit"),
    LAVA_BUCKET(ItemID.LAVA_BUCKET, "Lava bucket", "lavabucket", "lavabukkit"),
    MINECART(ItemID.MINECART, "Minecart", "minecart", "cart"),
    SADDLE(ItemID.SADDLE, "Saddle", "saddle"),
    IRON_DOOR_ITEM(ItemID.IRON_DOOR_ITEM, "Iron door", "irondoor"),
    REDSTONE_DUST(ItemID.REDSTONE_DUST, "Redstone dust", "redstonedust", "reddust", "redstone", "dust", "wire"),
    SNOWBALL(ItemID.SNOWBALL, "Snowball", "snowball"),
    WOOD_BOAT(ItemID.WOOD_BOAT, "Wooden boat", "woodboat", "woodenboat", "boat"),
    LEATHER(ItemID.LEATHER, "Leather", "leather", "cowhide"),
    MILK_BUCKET(ItemID.MILK_BUCKET, "Milk bucket", "milkbucket", "milk", "milkbukkit"),
    BRICK_BAR(ItemID.BRICK_BAR, "Brick", "brickbar"),
    CLAY_BALL(ItemID.CLAY_BALL, "Clay", "clay"),
    SUGAR_CANE_ITEM(ItemID.SUGAR_CANE_ITEM, "Sugar cane", "sugarcane", "reed", "reeds"),
    PAPER(ItemID.PAPER, "Paper", "paper"),
    BOOK(ItemID.BOOK, "Book", "book"),
    SLIME_BALL(ItemID.SLIME_BALL, "Slime ball", "slimeball", "slime"),
    STORAGE_MINECART(ItemID.STORAGE_MINECART, "Storage minecart", "storageminecart", "storagecart"),
    POWERED_MINECART(ItemID.POWERED_MINECART, "Powered minecart", "poweredminecart", "poweredcart"),
    EGG(ItemID.EGG, "Egg", "egg"),
    COMPASS(ItemID.COMPASS, "Compass", "compass"),
    FISHING_ROD(ItemID.FISHING_ROD, "Fishing rod", "fishingrod", "fishingpole"),
    WATCH(ItemID.WATCH, "Watch", "watch", "clock", "timer"),
    LIGHTSTONE_DUST(ItemID.LIGHTSTONE_DUST, "Glowstone dust", "lightstonedust", "glowstonedone", "brightstonedust", "brittlegolddust", "brimstonedust"),
    RAW_FISH(ItemID.RAW_FISH, "Raw fish", "rawfish", "fish"),
    COOKED_FISH(ItemID.COOKED_FISH, "Cooked fish", "cookedfish"),
    INK_SACK(ItemID.INK_SACK, "Ink sac", "inksac", "ink", "dye", "inksack"),
    BONE(ItemID.BONE, "Bone", "bone"),
    SUGAR(ItemID.SUGAR, "Sugar", "sugar"),
    CAKE_ITEM(ItemID.CAKE_ITEM, "Cake", "cake"),
    BED_ITEM(ItemID.BED_ITEM, "Bed", "bed"),
    REDSTONE_REPEATER(ItemID.REDSTONE_REPEATER, "Redstone repeater", "redstonerepeater", "diode", "delayer", "repeater"),
    COOKIE(ItemID.COOKIE, "Cookie", "cookie"),
    MAP(ItemID.MAP, "Map", "map"),
    SHEARS(ItemID.SHEARS, "Shears", "shears", "scissors"),
    MELON(ItemID.MELON, "Melon Slice", "melon", "melonslice"),
    PUMPKIN_SEEDS(ItemID.PUMPKIN_SEEDS, "Pumpkin seeds", "pumpkinseed", "pumpkinseeds"),
    MELON_SEEDS(ItemID.MELON_SEEDS, "Melon seeds", "melonseed", "melonseeds"),
    RAW_BEEF(ItemID.RAW_BEEF, "Raw beef", "rawbeef", "rawcow", "beef"),
    COOKED_BEEF(ItemID.COOKED_BEEF, "Steak", "steak", "cookedbeef", "cookedcow"),
    RAW_CHICKEN(ItemID.RAW_CHICKEN, "Raw chicken", "rawchicken"),
    COOKED_CHICKEN(ItemID.COOKED_CHICKEN, "Cooked chicken", "cookedchicken", "chicken", "grilledchicken"),
    ROTTEN_FLESH(ItemID.ROTTEN_FLESH, "Rotten flesh", "rottenflesh", "zombiemeat", "flesh"),
    ENDER_PEARL(ItemID.ENDER_PEARL, "Ender pearl", "pearl", "enderpearl"),
    BLAZE_ROD(ItemID.BLAZE_ROD, "Blaze rod", "blazerod"),
    GHAST_TEAR(ItemID.GHAST_TEAR, "Ghast tear", "ghasttear"),
    GOLD_NUGGET(ItemID.GOLD_NUGGET, "Gold nuggest", "goldnugget"),
    NETHER_WART_ITEM(ItemID.NETHER_WART_SEED, "Nether wart", "netherwart", "netherwartseed"),
    POTION(ItemID.POTION, "Potion", "potion"),
    GLASS_BOTTLE(ItemID.GLASS_BOTTLE, "Glass bottle", "glassbottle"),
    SPIDER_EYE(ItemID.SPIDER_EYE, "Spider eye", "spidereye"),
    FERMENTED_SPIDER_EYE(ItemID.FERMENTED_SPIDER_EYE, "Fermented spider eye", "fermentedspidereye", "fermentedeye"),
    BLAZE_POWDER(ItemID.BLAZE_POWDER, "Blaze powder", "blazepowder"),
    MAGMA_CREAM(ItemID.MAGMA_CREAM, "Magma cream", "magmacream"),
    BREWING_STAND_ITEM(ItemID.BREWING_STAND, "Brewing stand", "brewingstand"),
    CAULDRON_ITEM(ItemID.CAULDRON, "Cauldron", "cauldron"),
    EYE_OF_ENDER(ItemID.EYE_OF_ENDER, "Eye of Ender", "eyeofender", "endereye"),
    GLISTERING_MELON(ItemID.GLISTERING_MELON, "Glistering Melon", "glisteringmelon", "goldmelon"),
    DISC_13(ItemID.DISC_13, "Music Disc - 13", "disc_13"),
    DISC_CAT(ItemID.DISC_CAT, "Music Disc - Cat", "disc_cat"),
    DISC_BLOCKS(ItemID.DISC_BLOCKS, "Music Disc - blocks", "disc_blocks"),
    DISC_CHIRP(ItemID.DISC_CHIRP, "Music Disc - chirp", "disc_chirp"),
    DISC_FAR(ItemID.DISC_FAR, "Music Disc - far", "disc_far"),
    DISC_MALL(ItemID.DISC_MALL, "Music Disc - mall", "disc_mall"),
    DISC_MELLOHI(ItemID.DISC_MELLOHI, "Music Disc - mellohi", "disc_mellohi"),
    DISC_STAL(ItemID.DISC_STAL, "Music Disc - stal", "disc_stal"),
    DISC_STRAD(ItemID.DISC_STRAD, "Music Disc - strad", "disc_strad"),
    DISC_WARD(ItemID.DISC_WARD, "Music Disc - ward", "disc_ward"),
    DISC_11(ItemID.DISC_11, "Music Disc - 11", "disc_11"),

    // deprecated
    GOLD_RECORD(ItemID.GOLD_RECORD, "Gold Record", "goldrecord", "golddisc"),
    GREEN_RECORD(ItemID.GREEN_RECORD, "Green Record", "greenrecord", "greenddisc");

    /**
     * Stores a map of the IDs for fast access.
     */
    private static final Map<Integer, ItemType> ids = new HashMap<Integer, ItemType>();
    /**
     * Stores a map of the names for fast access.
     */
    private static final Map<String, ItemType> lookup = new LinkedHashMap<String, ItemType>();

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
        this.lookupKeys = new String[] { lookupKey };
    }

    /**
     * Construct the type.
     *
     * @param id
     * @param name
     */
    ItemType(int id, String name, String... lookupKeys) {
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

    private static final Set<Integer> shouldNotStack = new HashSet<Integer>();
    static {
        shouldNotStack.add(ItemID.IRON_SHOVEL);
        shouldNotStack.add(ItemID.IRON_PICK);
        shouldNotStack.add(ItemID.IRON_AXE);
        shouldNotStack.add(ItemID.FLINT_AND_TINDER);
        shouldNotStack.add(ItemID.BOW);
        shouldNotStack.add(ItemID.IRON_SWORD);
        shouldNotStack.add(ItemID.WOOD_SWORD);
        shouldNotStack.add(ItemID.WOOD_SHOVEL);
        shouldNotStack.add(ItemID.WOOD_PICKAXE);
        shouldNotStack.add(ItemID.WOOD_AXE);
        shouldNotStack.add(ItemID.STONE_SWORD);
        shouldNotStack.add(ItemID.STONE_SHOVEL);
        shouldNotStack.add(ItemID.STONE_PICKAXE);
        shouldNotStack.add(ItemID.STONE_AXE);
        shouldNotStack.add(ItemID.DIAMOND_SWORD);
        shouldNotStack.add(ItemID.DIAMOND_SHOVEL);
        shouldNotStack.add(ItemID.DIAMOND_PICKAXE);
        shouldNotStack.add(ItemID.DIAMOND_AXE);
        shouldNotStack.add(ItemID.BOWL);
        shouldNotStack.add(ItemID.GOLD_SWORD);
        shouldNotStack.add(ItemID.GOLD_SHOVEL);
        shouldNotStack.add(ItemID.GOLD_PICKAXE);
        shouldNotStack.add(ItemID.GOLD_AXE);
        shouldNotStack.add(ItemID.WOOD_HOE);
        shouldNotStack.add(ItemID.STONE_HOE);
        shouldNotStack.add(ItemID.IRON_HOE);
        shouldNotStack.add(ItemID.DIAMOND_HOE);
        shouldNotStack.add(ItemID.GOLD_HOE);
        shouldNotStack.add(ItemID.LEATHER_HELMET);
        shouldNotStack.add(ItemID.LEATHER_CHEST);
        shouldNotStack.add(ItemID.LEATHER_PANTS);
        shouldNotStack.add(ItemID.LEATHER_BOOTS);
        shouldNotStack.add(ItemID.CHAINMAIL_CHEST);
        shouldNotStack.add(ItemID.CHAINMAIL_HELMET);
        shouldNotStack.add(ItemID.CHAINMAIL_BOOTS);
        shouldNotStack.add(ItemID.CHAINMAIL_PANTS);
        shouldNotStack.add(ItemID.IRON_HELMET);
        shouldNotStack.add(ItemID.IRON_CHEST);
        shouldNotStack.add(ItemID.IRON_PANTS);
        shouldNotStack.add(ItemID.IRON_BOOTS);
        shouldNotStack.add(ItemID.DIAMOND_HELMET);
        shouldNotStack.add(ItemID.DIAMOND_PANTS);
        shouldNotStack.add(ItemID.DIAMOND_CHEST);
        shouldNotStack.add(ItemID.DIAMOND_BOOTS);
        shouldNotStack.add(ItemID.GOLD_HELMET);
        shouldNotStack.add(ItemID.GOLD_CHEST);
        shouldNotStack.add(ItemID.GOLD_PANTS);
        shouldNotStack.add(ItemID.GOLD_BOOTS);
        shouldNotStack.add(ItemID.SIGN);
        shouldNotStack.add(ItemID.WOODEN_DOOR_ITEM);
        shouldNotStack.add(ItemID.BUCKET);
        shouldNotStack.add(ItemID.WATER_BUCKET);
        shouldNotStack.add(ItemID.LAVA_BUCKET);
        shouldNotStack.add(ItemID.MINECART);
        shouldNotStack.add(ItemID.SADDLE);
        shouldNotStack.add(ItemID.IRON_DOOR_ITEM);
        shouldNotStack.add(ItemID.WOOD_BOAT);
        shouldNotStack.add(ItemID.MILK_BUCKET);
        shouldNotStack.add(ItemID.STORAGE_MINECART);
        shouldNotStack.add(ItemID.POWERED_MINECART);
        shouldNotStack.add(ItemID.WATCH);
        shouldNotStack.add(ItemID.CAKE_ITEM);
        shouldNotStack.add(ItemID.BED_ITEM);
        shouldNotStack.add(ItemID.MAP);
        shouldNotStack.add(ItemID.SHEARS);
        shouldNotStack.add(ItemID.DISC_13);
        shouldNotStack.add(ItemID.DISC_CAT);
        shouldNotStack.add(ItemID.DISC_BLOCKS);
        shouldNotStack.add(ItemID.DISC_CHIRP);
        shouldNotStack.add(ItemID.DISC_FAR);
        shouldNotStack.add(ItemID.DISC_MALL);
        shouldNotStack.add(ItemID.DISC_MELLOHI);
        shouldNotStack.add(ItemID.DISC_STAL);
        shouldNotStack.add(ItemID.DISC_STRAD);
        shouldNotStack.add(ItemID.DISC_WARD);
        shouldNotStack.add(ItemID.DISC_11);
    }

    /**
     * Returns true if an item should not be stacked.
     * 
     * @param id
     * @return
     */
    public static boolean shouldNotStack(int id) {
        return shouldNotStack.contains(id);
    }

    private static final Set<Integer> usesDamageValue = new HashSet<Integer>();
    static {
        usesDamageValue.add(BlockID.SAPLING);
        //usesDamageValue.add(BlockID.WATER);
        //usesDamageValue.add(BlockID.STATIONARY_WATER);
        //usesDamageValue.add(BlockID.LAVA);
        //usesDamageValue.add(BlockID.STATIONARY_LAVA);
        usesDamageValue.add(BlockID.LOG);
        usesDamageValue.add(BlockID.LEAVES);
        //usesDamageValue.add(BlockID.DISPENSER);
        //usesDamageValue.add(BlockID.BED);
        //usesDamageValue.add(BlockID.POWERED_RAIL);
        //usesDamageValue.add(BlockID.DETECTOR_RAIL);
        //usesDamageValue.add(BlockID.PISTON_STICKY_BASE);
        //usesDamageValue.add(BlockID.LONG_GRASS);
        //usesDamageValue.add(BlockID.PISTON_BASE);
        //usesDamageValue.add(BlockID.PISTON_EXTENSION);
        usesDamageValue.add(BlockID.CLOTH);
        usesDamageValue.add(BlockID.DOUBLE_STEP);
        usesDamageValue.add(BlockID.STEP);
        //usesDamageValue.add(BlockID.TORCH);
        //usesDamageValue.add(BlockID.FIRE);
        //usesDamageValue.add(BlockID.WOODEN_STAIRS);
        //usesDamageValue.add(BlockID.CHEST);
        //usesDamageValue.add(BlockID.REDSTONE_WIRE);
        //usesDamageValue.add(BlockID.CROPS);
        //usesDamageValue.add(BlockID.SOIL);
        //usesDamageValue.add(BlockID.FURNACE);
        //usesDamageValue.add(BlockID.BURNING_FURNACE);
        //usesDamageValue.add(BlockID.SIGN_POST);
        //usesDamageValue.add(BlockID.WOODEN_DOOR);
        //usesDamageValue.add(BlockID.LADDER);
        //usesDamageValue.add(BlockID.MINECART_TRACKS);
        //usesDamageValue.add(BlockID.COBBLESTONE_STAIRS);
        //usesDamageValue.add(BlockID.WALL_SIGN);
        //usesDamageValue.add(BlockID.LEVER);
        //usesDamageValue.add(BlockID.STONE_PRESSURE_PLATE);
        //usesDamageValue.add(BlockID.IRON_DOOR);
        //usesDamageValue.add(BlockID.WOODEN_PRESSURE_PLATE);
        //usesDamageValue.add(BlockID.REDSTONE_TORCH_OFF);
        //usesDamageValue.add(BlockID.REDSTONE_TORCH_ON);
        //usesDamageValue.add(BlockID.STONE_BUTTON);
        //usesDamageValue.add(BlockID.SNOW);
        //usesDamageValue.add(BlockID.CACTUS);
        //usesDamageValue.add(BlockID.REED);
        //usesDamageValue.add(BlockID.JUKEBOX);
        //usesDamageValue.add(BlockID.PUMPKIN);
        //usesDamageValue.add(BlockID.JACKOLANTERN);
        //usesDamageValue.add(BlockID.CAKE_BLOCK);
        //usesDamageValue.add(BlockID.REDSTONE_REPEATER_OFF);
        //usesDamageValue.add(BlockID.REDSTONE_REPEATER_ON);
        //usesDamageValue.add(BlockID.TRAP_DOOR);
        //usesDamageValue.add(BlockID.SILVERFISH_BLOCK);
        usesDamageValue.add(BlockID.STONE_BRICK);
        //usesDamageValue.add(BlockID.RED_MUSHROOM_CAP);
        //usesDamageValue.add(BlockID.BROWN_MUSHROOM_CAP);
        //usesDamageValue.add(BlockID.PUMPKIN_STEM);
        //usesDamageValue.add(BlockID.MELON_STEM);
        //usesDamageValue.add(BlockID.VINE);
        //usesDamageValue.add(BlockID.FENCE_GATE);
        //usesDamageValue.add(BlockID.BRICK_STAIRS);
        //usesDamageValue.add(BlockID.STONE_BRICK_STAIRS);
        //usesDamageValue.add(BlockID.NETHER_BRICK_STAIRS);
        //usesDamageValue.add(BlockID.NETHER_WART);
        //usesDamageValue.add(BlockID.ENCHANTMENT_TABLE);
        //usesDamageValue.add(BlockID.BREWING_STAND);
        //usesDamageValue.add(BlockID.CAULDRON);
        //usesDamageValue.add(BlockID.END_PORTAL_FRAME);
        usesDamageValue.add(ItemID.COAL);
        usesDamageValue.add(ItemID.INK_SACK);
        usesDamageValue.add(ItemID.POTION);
        usesDamageValue.add(ItemID.GLASS_BOTTLE);
    }

    /**
     * Returns true if an item uses its damage value for something
     * other than damage.
     * 
     * @param id
     * @return
     */
    public static boolean usesDamageValue(int id) {
        return usesDamageValue.contains(id);
    }
}
