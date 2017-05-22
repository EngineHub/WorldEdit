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

/**
 * List of block IDs.
 */
public final class BlockID {
    public static final int AIR = 0;
    public static final int STONE = 1;
    public static final int GRASS = 2;
    public static final int DIRT = 3;
    public static final int COBBLESTONE = 4;
    public static final int WOOD = 5; // PLANKS
    public static final int SAPLING = 6;
    public static final int BEDROCK = 7;
    public static final int WATER = 8; // FLOWING_WATER
    public static final int STATIONARY_WATER = 9; // WATER
    public static final int LAVA = 10; // FLOWING_LAVA
    public static final int STATIONARY_LAVA = 11; // LAVA
    public static final int SAND = 12;
    public static final int GRAVEL = 13;
    public static final int GOLD_ORE = 14;
    public static final int IRON_ORE = 15;
    public static final int COAL_ORE = 16;
    public static final int LOG = 17;
    public static final int LEAVES = 18;
    public static final int SPONGE = 19;
    public static final int GLASS = 20;
    public static final int LAPIS_LAZULI_ORE = 21; // LAPIS_ORE
    public static final int LAPIS_LAZULI_BLOCK = 22; // LAPIS_BLOCK
    public static final int DISPENSER = 23;
    public static final int SANDSTONE = 24;
    public static final int NOTE_BLOCK = 25; // NOTEBLOCK
    public static final int BED = 26;
    public static final int POWERED_RAIL = 27; // GOLDEN_RAIL
    public static final int DETECTOR_RAIL = 28;
    public static final int PISTON_STICKY_BASE = 29; // STICKY_PISTON
    public static final int WEB = 30;
    public static final int LONG_GRASS = 31; // TALLGRASS
    public static final int DEAD_BUSH = 32; // DEADBUSH
    public static final int PISTON_BASE = 33; // PISTON
    public static final int PISTON_EXTENSION = 34; // PISTON_HEAD
    public static final int CLOTH = 35; // WOOL
    public static final int PISTON_MOVING_PIECE = 36; // PISTON_EXTENSION
    public static final int YELLOW_FLOWER = 37;
    public static final int RED_FLOWER = 38;
    public static final int BROWN_MUSHROOM = 39;
    public static final int RED_MUSHROOM = 40;
    public static final int GOLD_BLOCK = 41;
    public static final int IRON_BLOCK = 42;
    public static final int DOUBLE_STEP = 43; // DOUBLE_STONE_SLAB
    public static final int STEP = 44; // STONE_SLAB
    public static final int BRICK = 45; // BRICK_BLOCK
    public static final int TNT = 46;
    public static final int BOOKCASE = 47; // BOOKSHELF
    public static final int MOSSY_COBBLESTONE = 48;
    public static final int OBSIDIAN = 49;
    public static final int TORCH = 50;
    public static final int FIRE = 51;
    public static final int MOB_SPAWNER = 52;
    @Deprecated
    public static final int WOODEN_STAIRS = 53;
    public static final int OAK_WOOD_STAIRS = 53; // OAK_STAIRS
    public static final int CHEST = 54;
    public static final int REDSTONE_WIRE = 55;
    public static final int DIAMOND_ORE = 56;
    public static final int DIAMOND_BLOCK = 57;
    public static final int WORKBENCH = 58; // CRAFTING_TABLE
    public static final int CROPS = 59; // WHEAT
    public static final int SOIL = 60;  // FARMLAND
    public static final int FURNACE = 61;
    public static final int BURNING_FURNACE = 62; // LIT_FURNACE
    public static final int SIGN_POST = 63; // STANDING_SIGN
    public static final int WOODEN_DOOR = 64; // WOODEN_DOOR
    public static final int LADDER = 65;
    public static final int MINECART_TRACKS = 66; // RAIL
    public static final int COBBLESTONE_STAIRS = 67; // STONE_STAIRS
    public static final int WALL_SIGN = 68;
    public static final int LEVER = 69;
    public static final int STONE_PRESSURE_PLATE = 70;
    public static final int IRON_DOOR = 71;
    public static final int WOODEN_PRESSURE_PLATE = 72;
    public static final int REDSTONE_ORE = 73; // LIT_REDSTONE_ORE
    public static final int GLOWING_REDSTONE_ORE = 74; // UNLIT_REDSTONE_ORE
    public static final int REDSTONE_TORCH_OFF = 75; // UNLIT_REDSTONE_TORCH
    public static final int REDSTONE_TORCH_ON = 76; // LIT_REDSTONE_TORCH
    public static final int STONE_BUTTON = 77;
    public static final int SNOW = 78; // SNOW_LAYER
    public static final int ICE = 79;
    public static final int SNOW_BLOCK = 80; // SNOW
    public static final int CACTUS = 81;
    public static final int CLAY = 82;
    public static final int REED = 83; // REEDS
    public static final int JUKEBOX = 84;
    public static final int FENCE = 85;
    public static final int PUMPKIN = 86;
    @Deprecated
    public static final int NETHERSTONE = 87;
    public static final int NETHERRACK = 87;
    public static final int SLOW_SAND = 88; // SOUL_SAND
    public static final int LIGHTSTONE = 89; // GLOWSTONE
    public static final int PORTAL = 90;
    public static final int JACKOLANTERN = 91; // LIT_PUMPKIN
    public static final int CAKE_BLOCK = 92; // CAKE
    public static final int REDSTONE_REPEATER_OFF = 93; // UNPOWERED_REPEATER
    public static final int REDSTONE_REPEATER_ON = 94; // POWERED_REPEATER
    @Deprecated
    public static final int LOCKED_CHEST = 95;
    public static final int STAINED_GLASS = 95;
    public static final int TRAP_DOOR = 96; // TRAPDOOR
    public static final int SILVERFISH_BLOCK = 97; // MONSTER_EGG
    public static final int STONE_BRICK = 98; // STONEBRICK
    public static final int BROWN_MUSHROOM_CAP = 99; // BROWN_MUSHROOM_BLOCK
    public static final int RED_MUSHROOM_CAP = 100; // RED_MUSHROOM_BLOCK
    public static final int IRON_BARS = 101;
    public static final int GLASS_PANE = 102;
    public static final int MELON_BLOCK = 103;
    public static final int PUMPKIN_STEM = 104;
    public static final int MELON_STEM = 105;
    public static final int VINE = 106;
    public static final int FENCE_GATE = 107;
    public static final int BRICK_STAIRS = 108;
    public static final int STONE_BRICK_STAIRS = 109;
    public static final int MYCELIUM = 110;
    public static final int LILY_PAD = 111; // WATERLILY
    public static final int NETHER_BRICK = 112;
    public static final int NETHER_BRICK_FENCE = 113;
    public static final int NETHER_BRICK_STAIRS = 114;
    public static final int NETHER_WART = 115;
    public static final int ENCHANTMENT_TABLE = 116; // ENCHANTING_TABLE
    public static final int BREWING_STAND = 117;
    public static final int CAULDRON = 118;
    public static final int END_PORTAL = 119;
    public static final int END_PORTAL_FRAME = 120;
    public static final int END_STONE = 121;
    public static final int DRAGON_EGG = 122;
    public static final int REDSTONE_LAMP_OFF = 123; // REDSTONE_LAMP
    public static final int REDSTONE_LAMP_ON = 124; // LIT_REDSTONE_LAMP
    public static final int DOUBLE_WOODEN_STEP = 125; // DOUBLE_WOODEN_SLAB
    public static final int WOODEN_STEP = 126; // WOODEN_SLAB
    public static final int COCOA_PLANT = 127; // COCOA
    public static final int SANDSTONE_STAIRS = 128;
    public static final int EMERALD_ORE = 129;
    public static final int ENDER_CHEST = 130;
    public static final int TRIPWIRE_HOOK = 131;
    public static final int TRIPWIRE = 132;
    public static final int EMERALD_BLOCK = 133;
    public static final int SPRUCE_WOOD_STAIRS = 134; // SPRUCE_STAIRS
    public static final int BIRCH_WOOD_STAIRS = 135; // BRUCE_STAIRS
    public static final int JUNGLE_WOOD_STAIRS = 136; // JUNGLE_STAIRS
    public static final int COMMAND_BLOCK = 137;
    public static final int BEACON = 138;
    public static final int COBBLESTONE_WALL = 139;
    public static final int FLOWER_POT = 140;
    public static final int CARROTS = 141;
    public static final int POTATOES = 142;
    public static final int WOODEN_BUTTON = 143;
    public static final int HEAD = 144; // SKULL
    public static final int ANVIL = 145;
    public static final int TRAPPED_CHEST = 146;
    public static final int PRESSURE_PLATE_LIGHT = 147; // LIGHT_WEIGHTED_PRESSURE_PLATE
    public static final int PRESSURE_PLATE_HEAVY = 148; // HEAVY_WEIGHTED_PRESSURE_PLATE
    public static final int COMPARATOR_OFF = 149; // UNPOWERED_COMPARATOR
    public static final int COMPARATOR_ON = 150; // COMPARATOR
    public static final int DAYLIGHT_SENSOR = 151; // DAYLIGHT_DETECTOR
    public static final int REDSTONE_BLOCK = 152;
    public static final int QUARTZ_ORE = 153;
    public static final int HOPPER = 154;
    public static final int QUARTZ_BLOCK = 155;
    public static final int QUARTZ_STAIRS = 156;
    public static final int ACTIVATOR_RAIL = 157;
    public static final int DROPPER = 158;
    public static final int STAINED_CLAY = 159; // STAINED_HARDENED_CLAY
    public static final int STAINED_GLASS_PANE = 160;
    public static final int LEAVES2 = 161;
    public static final int LOG2 = 162;
    public static final int ACACIA_STAIRS = 163;
    public static final int DARK_OAK_STAIRS = 164;
    public static final int SLIME = 165;
    public static final int BARRIER = 166;
    public static final int IRON_TRAP_DOOR = 167;
    public static final int PRISMARINE = 168;
    public static final int SEA_LANTERN = 169;
    public static final int HAY_BLOCK = 170;
    public static final int CARPET = 171;
    public static final int HARDENED_CLAY = 172;
    public static final int COAL_BLOCK = 173;
    public static final int PACKED_ICE = 174;
    public static final int DOUBLE_PLANT = 175;
    public static final int STANDING_BANNER = 176;
    public static final int WALL_BANNER = 177;
    public static final int DAYLIGHT_SENSOR_INVERTED = 178;
    public static final int RED_SANDSTONE = 179;
    public static final int RED_SANDSTONE_STAIRS = 180;
    public static final int DOUBLE_STEP2 = 181;
    public static final int STEP2 = 182;
    public static final int SPRUCE_FENCE_GATE = 183;
    public static final int BIRCH_FENCE_GATE = 184;
    public static final int JUNGLE_FENCE_GATE = 185;
    public static final int DARK_OAK_FENCE_GATE = 186;
    public static final int ACACIA_FENCE_GATE = 187;
    public static final int SPRUCE_FENCE = 188;
    public static final int BIRCH_FENCE = 189;
    public static final int JUNGLE_FENCE = 190;
    public static final int DARK_OAK_FENCE = 191;
    public static final int ACACIA_FENCE = 192;
    public static final int SPRUCE_DOOR = 193;
    public static final int BIRCH_DOOR = 194;
    public static final int JUNGLE_DOOR = 195;
    public static final int ACACIA_DOOR = 196;
    public static final int DARK_OAK_DOOR = 197;
    public static final int END_ROD = 198;
    public static final int CHORUS_PLANT = 199;
    public static final int CHORUS_FLOWER = 200;
    public static final int PURPUR_BLOCK = 201;
    public static final int PURPUR_PILLAR = 202;
    public static final int PURPUR_STAIRS = 203;
    public static final int PURPUR_DOUBLE_SLAB = 204;
    public static final int PURPUR_SLAB = 205;
    public static final int END_BRICKS = 206;
    public static final int BEETROOTS = 207;
    public static final int GRASS_PATH = 208;
    public static final int END_GATEWAY = 209;
    public static final int REPEATING_COMMAND_BLOCK = 210;
    public static final int CHAIN_COMMAND_BLOCK = 211;
    public static final int FROSTED_ICE = 212;
    public static final int MAGMA_BLOCK = 213;
    public static final int NETHER_WART_BLOCK = 214;
    public static final int RED_NETHER_BRICK = 215;
    public static final int BONE_BLOCK = 216;
    public static final int STRUCTURE_VOID = 217;
    public static final int OBSERVER = 218;
    public static final int SHULKER_BOX_WHITE = 219;
    public static final int SHULKER_BOX_ORANGE = 220;
    public static final int SHULKER_BOX_MAGENTA = 221;
    public static final int SHULKER_BOX_LIGHT_BLUE = 222;
    public static final int SHULKER_BOX_YELLOW = 223;
    public static final int SHULKER_BOX_LIME = 224;
    public static final int SHULKER_BOX_PINK = 225;
    public static final int SHULKER_BOX_GRAY = 226;
    public static final int SHULKER_BOX_LIGHT_GRAY = 227;
    public static final int SHULKER_BOX_CYAN = 228;
    public static final int SHULKER_BOX_PURPLE = 229;
    public static final int SHULKER_BOX_BLUE = 230;
    public static final int SHULKER_BOX_BROWN = 231;
    public static final int SHULKER_BOX_GREEN = 232;
    public static final int SHULKER_BOX_RED = 233;
    public static final int SHULKER_BOX_BLACK = 234;
    public static final int TERRACOTTA_WHITE = 235;
    public static final int TERRACOTTA_ORANGE = 236;
    public static final int TERRACOTTA_MAGENTA = 237;
    public static final int TERRACOTTA_LIGHT_BLUE = 238;
    public static final int TERRACOTTA_YELLOW = 239;
    public static final int TERRACOTTA_LIME = 240;
    public static final int TERRACOTTA_PINK = 241;
    public static final int TERRACOTTA_GRAY = 242;
    public static final int TERRACOTTA_LIGHT_GRAY = 243;
    public static final int TERRACOTTA_CYAN = 244;
    public static final int TERRACOTTA_PURPLE = 245;
    public static final int TERRACOTTA_BLUE = 246;
    public static final int TERRACOTTA_BROWN = 247;
    public static final int TERRACOTTA_GREEN = 248;
    public static final int TERRACOTTA_RED = 249;
    public static final int TERRACOTTA_BLACK = 250;
    public static final int CONCRETE = 251;
    public static final int CONCRETE_POWDER = 252;
    public static final int STRUCTURE_BLOCK = 255;

    private BlockID() {
    }
}
