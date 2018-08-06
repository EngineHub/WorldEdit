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

import com.sk89q.worldedit.world.block.BlockTypes;

/**
 * List of block IDs.
 *
 * {@deprecated Please use {@link BlockTypes }}
 */
@Deprecated
public final class BlockID {
    static final int SAPLING = 6;
    static final int POWERED_RAIL = 27; // GOLDEN_RAIL
    static final int DETECTOR_RAIL = 28;
    static final int LONG_GRASS = 31; // TALLGRASS
    static final int DEAD_BUSH = 32; // DEADBUSH
    static final int PISTON_EXTENSION = 34; // PISTON_HEAD
    static final int YELLOW_FLOWER = 37;
    static final int RED_FLOWER = 38;
    static final int BROWN_MUSHROOM = 39;
    static final int RED_MUSHROOM = 40;
    static final int TORCH = 50;
    static final int REDSTONE_WIRE = 55;
    static final int CROPS = 59; // WHEAT
    static final int SIGN_POST = 63; // STANDING_SIGN
    static final int WOODEN_DOOR = 64; // WOODEN_DOOR
    static final int LADDER = 65;
    static final int MINECART_TRACKS = 66; // RAIL
    static final int WALL_SIGN = 68;
    static final int LEVER = 69;
    static final int STONE_PRESSURE_PLATE = 70;
    static final int IRON_DOOR = 71;
    static final int WOODEN_PRESSURE_PLATE = 72;
    static final int REDSTONE_TORCH_OFF = 75; // UNLIT_REDSTONE_TORCH
    static final int REDSTONE_TORCH_ON = 76; // LIT_REDSTONE_TORCH
    static final int STONE_BUTTON = 77;
    static final int CACTUS = 81;
    static final int REED = 83; // REEDS
    static final int CAKE_BLOCK = 92; // CAKE
    static final int REDSTONE_REPEATER_OFF = 93; // UNPOWERED_REPEATER
    static final int REDSTONE_REPEATER_ON = 94; // POWERED_REPEATER
    static final int TRAP_DOOR = 96; // TRAPDOOR
    static final int PUMPKIN_STEM = 104;
    static final int MELON_STEM = 105;
    static final int VINE = 106;
    static final int NETHER_WART = 115;
    static final int COCOA_PLANT = 127; // COCOA
    static final int TRIPWIRE_HOOK = 131;
    static final int TRIPWIRE = 132;
    static final int FLOWER_POT = 140;
    static final int CARROTS = 141;
    static final int POTATOES = 142;
    static final int WOODEN_BUTTON = 143;
    static final int ANVIL = 145;
    static final int PRESSURE_PLATE_LIGHT = 147; // LIGHT_WEIGHTED_PRESSURE_PLATE
    static final int PRESSURE_PLATE_HEAVY = 148; // HEAVY_WEIGHTED_PRESSURE_PLATE
    static final int COMPARATOR_OFF = 149; // UNPOWERED_COMPARATOR
    static final int COMPARATOR_ON = 150; // COMPARATOR
    static final int ACTIVATOR_RAIL = 157;
    static final int IRON_TRAP_DOOR = 167;
    static final int CARPET = 171;
    static final int DOUBLE_PLANT = 175;
    static final int STANDING_BANNER = 176;
    static final int WALL_BANNER = 177;
    static final int SPRUCE_DOOR = 193;
    static final int BIRCH_DOOR = 194;
    static final int JUNGLE_DOOR = 195;
    static final int ACACIA_DOOR = 196;
    static final int DARK_OAK_DOOR = 197;

    private BlockID() {
    }
}
