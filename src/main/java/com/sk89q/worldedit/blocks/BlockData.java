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

import com.sk89q.worldedit.CuboidClipboard.FlipDirection;

/**
 * Block data related classes.
 *
 * @author sk89q
 */
public final class BlockData {

    private BlockData() {
    }

    /**
     * Rotate a block's data value 90 degrees (north->east->south->west->north);
     * 
     * @param type
     * @param data
     * @return
     */
    public static int rotate90(int type, int data) {
        switch (type) {
        case BlockID.TORCH:
        case BlockID.REDSTONE_TORCH_OFF:
        case BlockID.REDSTONE_TORCH_ON:
            switch (data) {
            case 1: return 3;
            case 2: return 4;
            case 3: return 2;
            case 4: return 1;
            }
            break;

        case BlockID.MINECART_TRACKS:
            switch (data) {
            case 6: return 7;
            case 7: return 8;
            case 8: return 9;
            case 9: return 6;
            }
            /* FALL-THROUGH */

        case BlockID.POWERED_RAIL:
        case BlockID.DETECTOR_RAIL:
        case BlockID.ACTIVATOR_RAIL:
            switch (data & 0x7) {
            case 0: return 1 | (data & ~0x7);
            case 1: return 0 | (data & ~0x7);
            case 2: return 5 | (data & ~0x7);
            case 3: return 4 | (data & ~0x7);
            case 4: return 2 | (data & ~0x7);
            case 5: return 3 | (data & ~0x7);
            }
            break;

        case BlockID.OAK_WOOD_STAIRS:
        case BlockID.COBBLESTONE_STAIRS:
        case BlockID.BRICK_STAIRS:
        case BlockID.STONE_BRICK_STAIRS:
        case BlockID.NETHER_BRICK_STAIRS:
        case BlockID.SANDSTONE_STAIRS:
        case BlockID.SPRUCE_WOOD_STAIRS:
        case BlockID.BIRCH_WOOD_STAIRS:
        case BlockID.JUNGLE_WOOD_STAIRS:
        case BlockID.QUARTZ_STAIRS:
        case BlockID.ACACIA_STAIRS:
        case BlockID.DARK_OAK_STAIRS:
            switch (data) {
            case 0: return 2;
            case 1: return 3;
            case 2: return 1;
            case 3: return 0;
            case 4: return 6;
            case 5: return 7;
            case 6: return 5;
            case 7: return 4;
            }
            break;

        case BlockID.LEVER:
        case BlockID.STONE_BUTTON:
        case BlockID.WOODEN_BUTTON:
            int thrown = data & 0x8;
            int withoutThrown = data & ~0x8;
            switch (withoutThrown) {
            case 1: return 3 | thrown;
            case 2: return 4 | thrown;
            case 3: return 2 | thrown;
            case 4: return 1 | thrown;
            case 5: return 6 | thrown;
            case 6: return 5 | thrown;
            case 7: return 0 | thrown;
            case 0: return 7 | thrown;
            }
            break;

        case BlockID.WOODEN_DOOR:
        case BlockID.IRON_DOOR:
            if ((data & 0x8) != 0) {
                // door top halves contain no orientation information
                break;
            }

            /* FALL-THROUGH */

        case BlockID.COCOA_PLANT:
        case BlockID.TRIPWIRE_HOOK:
            int extra = data & ~0x3;
            int withoutFlags = data & 0x3;
            switch (withoutFlags) {
            case 0: return 1 | extra;
            case 1: return 2 | extra;
            case 2: return 3 | extra;
            case 3: return 0 | extra;
            }
            break;

        case BlockID.SIGN_POST:
            return (data + 4) % 16;

        case BlockID.LADDER:
        case BlockID.WALL_SIGN:
        case BlockID.CHEST:
        case BlockID.FURNACE:
        case BlockID.BURNING_FURNACE:
        case BlockID.ENDER_CHEST:
        case BlockID.TRAPPED_CHEST:
        case BlockID.HOPPER:
            switch (data) {
            case 2: return 5;
            case 3: return 4;
            case 4: return 2;
            case 5: return 3;
            }
            break;

        case BlockID.DISPENSER:
        case BlockID.DROPPER:
            int dispPower = data & 0x8;
            switch (data & ~0x8) {
            case 2: return 5 | dispPower;
            case 3: return 4 | dispPower;
            case 4: return 2 | dispPower;
            case 5: return 3 | dispPower;
            }
            break;

        case BlockID.PUMPKIN:
        case BlockID.JACKOLANTERN:
            switch (data) {
            case 0: return 1;
            case 1: return 2;
            case 2: return 3;
            case 3: return 0;
            }
            break;

        case BlockID.HAY_BLOCK:
        case BlockID.LOG:
        case BlockID.LOG2:
            if (data >= 4 && data <= 11) data ^= 0xc;
            break;

        case BlockID.COMPARATOR_OFF:
        case BlockID.COMPARATOR_ON:
        case BlockID.REDSTONE_REPEATER_OFF:
        case BlockID.REDSTONE_REPEATER_ON:
            int dir = data & 0x03;
            int delay = data - dir;
            switch (dir) {
            case 0: return 1 | delay;
            case 1: return 2 | delay;
            case 2: return 3 | delay;
            case 3: return 0 | delay;
            }
            break;

        case BlockID.TRAP_DOOR:
            int withoutOrientation = data & ~0x3;
            int orientation = data & 0x3;
            switch (orientation) {
            case 0: return 3 | withoutOrientation;
            case 1: return 2 | withoutOrientation;
            case 2: return 0 | withoutOrientation;
            case 3: return 1 | withoutOrientation;
            }
            break;

        case BlockID.PISTON_BASE:
        case BlockID.PISTON_STICKY_BASE:
        case BlockID.PISTON_EXTENSION:
            final int rest = data & ~0x7;
            switch (data & 0x7) {
            case 2: return 5 | rest;
            case 3: return 4 | rest;
            case 4: return 2 | rest;
            case 5: return 3 | rest;
            }
            break;

        case BlockID.BROWN_MUSHROOM_CAP:
        case BlockID.RED_MUSHROOM_CAP:
            if (data >= 10) return data;
            return (data * 3) % 10;

        case BlockID.VINE:
            return ((data << 1) | (data >> 3)) & 0xf;

        case BlockID.FENCE_GATE:
            return ((data + 1) & 0x3) | (data & ~0x3);

        case BlockID.ANVIL:
            return data ^ 0x1;

        case BlockID.BED:
            return data & ~0x3 | (data + 1) & 0x3;

        case BlockID.HEAD:
            switch (data) {
                case 2: return 5;
                case 3: return 4;
                case 4: return 2;
                case 5: return 3;
            }
        }

        return data;
    }

    /**
     * Rotate a block's data value -90 degrees (north<-east<-south<-west<-north);
     * 
     * @param type
     * @param data
     * @return
     */
    public static int rotate90Reverse(int type, int data) {
        // case ([0-9]+): return ([0-9]+) -> case \2: return \1

        switch (type) {
        case BlockID.TORCH:
        case BlockID.REDSTONE_TORCH_OFF:
        case BlockID.REDSTONE_TORCH_ON:
            switch (data) {
            case 3: return 1;
            case 4: return 2;
            case 2: return 3;
            case 1: return 4;
            }
            break;

        case BlockID.MINECART_TRACKS:
            switch (data) {
            case 7: return 6;
            case 8: return 7;
            case 9: return 8;
            case 6: return 9;
            }
            /* FALL-THROUGH */

        case BlockID.POWERED_RAIL:
        case BlockID.DETECTOR_RAIL:
        case BlockID.ACTIVATOR_RAIL:
            int power = data & ~0x7;
            switch (data & 0x7) {
            case 1: return 0 | power;
            case 0: return 1 | power;
            case 5: return 2 | power;
            case 4: return 3 | power;
            case 2: return 4 | power;
            case 3: return 5 | power;
            }
            break;

        case BlockID.OAK_WOOD_STAIRS:
        case BlockID.COBBLESTONE_STAIRS:
        case BlockID.BRICK_STAIRS:
        case BlockID.STONE_BRICK_STAIRS:
        case BlockID.NETHER_BRICK_STAIRS:
        case BlockID.SANDSTONE_STAIRS:
        case BlockID.SPRUCE_WOOD_STAIRS:
        case BlockID.BIRCH_WOOD_STAIRS:
        case BlockID.JUNGLE_WOOD_STAIRS:
        case BlockID.QUARTZ_STAIRS:
        case BlockID.ACACIA_STAIRS:
        case BlockID.DARK_OAK_STAIRS:
            switch (data) {
            case 2: return 0;
            case 3: return 1;
            case 1: return 2;
            case 0: return 3;
            case 6: return 4;
            case 7: return 5;
            case 5: return 6;
            case 4: return 7;
            }
            break;

        case BlockID.LEVER:
        case BlockID.STONE_BUTTON:
        case BlockID.WOODEN_BUTTON:
            int thrown = data & 0x8;
            int withoutThrown = data & ~0x8;
            switch (withoutThrown) {
            case 3: return 1 | thrown;
            case 4: return 2 | thrown;
            case 2: return 3 | thrown;
            case 1: return 4 | thrown;
            case 6: return 5 | thrown;
            case 5: return 6 | thrown;
            case 0: return 7 | thrown;
            case 7: return 0 | thrown;
            }
            break;

        case BlockID.WOODEN_DOOR:
        case BlockID.IRON_DOOR:
            if ((data & 0x8) != 0) {
                // door top halves contain no orientation information
                break;
            }

            /* FALL-THROUGH */

        case BlockID.COCOA_PLANT:
        case BlockID.TRIPWIRE_HOOK:
            int extra = data & ~0x3;
            int withoutFlags = data & 0x3;
            switch (withoutFlags) {
            case 1: return 0 | extra;
            case 2: return 1 | extra;
            case 3: return 2 | extra;
            case 0: return 3 | extra;
            }
            break;

        case BlockID.SIGN_POST:
            return (data + 12) % 16;

        case BlockID.LADDER:
        case BlockID.WALL_SIGN:
        case BlockID.CHEST:
        case BlockID.FURNACE:
        case BlockID.BURNING_FURNACE:
        case BlockID.ENDER_CHEST:
        case BlockID.TRAPPED_CHEST:
        case BlockID.HOPPER:
            switch (data) {
            case 5: return 2;
            case 4: return 3;
            case 2: return 4;
            case 3: return 5;
            }
            break;

        case BlockID.DISPENSER:
        case BlockID.DROPPER:
            int dispPower = data & 0x8;
            switch (data & ~0x8) {
            case 5: return 2 | dispPower;
            case 4: return 3 | dispPower;
            case 2: return 4 | dispPower;
            case 3: return 5 | dispPower;
            }
            break;
        case BlockID.PUMPKIN:
        case BlockID.JACKOLANTERN:
            switch (data) {
            case 1: return 0;
            case 2: return 1;
            case 3: return 2;
            case 0: return 3;
            }
            break;

        case BlockID.HAY_BLOCK:
        case BlockID.LOG:
        case BlockID.LOG2:
            if (data >= 4 && data <= 11) data ^= 0xc;
            break;

        case BlockID.COMPARATOR_OFF:
        case BlockID.COMPARATOR_ON:
        case BlockID.REDSTONE_REPEATER_OFF:
        case BlockID.REDSTONE_REPEATER_ON:
            int dir = data & 0x03;
            int delay = data - dir;
            switch (dir) {
            case 1: return 0 | delay;
            case 2: return 1 | delay;
            case 3: return 2 | delay;
            case 0: return 3 | delay;
            }
            break;

        case BlockID.TRAP_DOOR:
            int withoutOrientation = data & ~0x3;
            int orientation = data & 0x3;
            switch (orientation) {
            case 3: return 0 | withoutOrientation;
            case 2: return 1 | withoutOrientation;
            case 0: return 2 | withoutOrientation;
            case 1: return 3 | withoutOrientation;
            }

        case BlockID.PISTON_BASE:
        case BlockID.PISTON_STICKY_BASE:
        case BlockID.PISTON_EXTENSION:
            final int rest = data & ~0x7;
            switch (data & 0x7) {
            case 5: return 2 | rest;
            case 4: return 3 | rest;
            case 2: return 4 | rest;
            case 3: return 5 | rest;
            }
            break;

        case BlockID.BROWN_MUSHROOM_CAP:
        case BlockID.RED_MUSHROOM_CAP:
            if (data >= 10) return data;
            return (data * 7) % 10;

        case BlockID.VINE:
            return ((data >> 1) | (data << 3)) & 0xf;

        case BlockID.FENCE_GATE:
            return ((data + 3) & 0x3) | (data & ~0x3);

        case BlockID.ANVIL:
            return data ^ 0x1;

        case BlockID.BED:
            return data & ~0x3 | (data - 1) & 0x3;

        case BlockID.HEAD:
            switch (data) {
                case 2: return 4;
                case 3: return 5;
                case 4: return 3;
                case 5: return 2;
            }
        }

        return data;
    }

    /**
     * Flip a block's data value.
     * 
     * @param type
     * @param data
     * @return
     */
    public static int flip(int type, int data) {
        return rotate90(type, rotate90(type, data));
    }

    /**
     * Flip a block's data value.
     * 
     * @param type
     * @param data
     * @param direction
     * @return
     */
    public static int flip(int type, int data, FlipDirection direction) {
        int flipX = 0;
        int flipY = 0;
        int flipZ = 0;

        switch (direction) {
        case NORTH_SOUTH:
            flipZ = 1;
            break;

        case WEST_EAST:
            flipX = 1;
            break;

        case UP_DOWN:
            flipY = 1;
            break;
        }

        switch (type) {
        case BlockID.TORCH:
        case BlockID.REDSTONE_TORCH_OFF:
        case BlockID.REDSTONE_TORCH_ON:
            if (data < 1 || data > 4) break;
            /* FALL-THROUGH */

        case BlockID.LEVER:
        case BlockID.STONE_BUTTON:
        case BlockID.WOODEN_BUTTON:
            switch (data & ~0x8) {
            case 1: return data + flipX;
            case 2: return data - flipX;
            case 3: return data + flipZ;
            case 4: return data - flipZ;
            case 5:
            case 7:
                return data ^ flipY << 1;
            case 6:
            case 0:
                return data ^ flipY * 6;
            }
            break;

        case BlockID.MINECART_TRACKS:
            switch (data) {
            case 6: return data + flipX + flipZ * 3;
            case 7: return data - flipX + flipZ;
            case 8: return data + flipX - flipZ;
            case 9: return data - flipX - flipZ * 3;
            }
            /* FALL-THROUGH */

        case BlockID.POWERED_RAIL:
        case BlockID.DETECTOR_RAIL:
        case BlockID.ACTIVATOR_RAIL:
            switch (data & 0x7) {
            case 0:
            case 1:
                return data;
            case 2:
            case 3:
                return data ^ flipX;
            case 4:
            case 5:
                return data ^ flipZ;
            }
            break;

        case BlockID.STEP:
        case BlockID.WOODEN_STEP:
            return data ^ (flipY << 3);

        case BlockID.OAK_WOOD_STAIRS:
        case BlockID.COBBLESTONE_STAIRS:
        case BlockID.BRICK_STAIRS:
        case BlockID.STONE_BRICK_STAIRS:
        case BlockID.NETHER_BRICK_STAIRS:
        case BlockID.SANDSTONE_STAIRS:
        case BlockID.SPRUCE_WOOD_STAIRS:
        case BlockID.BIRCH_WOOD_STAIRS:
        case BlockID.JUNGLE_WOOD_STAIRS:
        case BlockID.QUARTZ_STAIRS:
        case BlockID.ACACIA_STAIRS:
        case BlockID.DARK_OAK_STAIRS:
            data ^= flipY << 2;
            switch (data) {
            case 0:
            case 1:
            case 4:
            case 5:
                return data ^ flipX;
            case 2:
            case 3:
            case 6:
            case 7:
                return data ^ flipZ;
            }
            break;

        case BlockID.WOODEN_DOOR:
        case BlockID.IRON_DOOR:
            if ((data & 0x8) != 0) {
                // door top halves contain no orientation information
                break;
            }

            switch (data & 0x3) {
            case 0: return data + flipX + flipZ * 3;
            case 1: return data - flipX + flipZ;
            case 2: return data + flipX - flipZ;
            case 3: return data - flipX - flipZ * 3;
            }
            break;

        case BlockID.SIGN_POST:
            switch (direction) {
            case NORTH_SOUTH:
                return (16 - data) & 0xf;
            case WEST_EAST:
                return (8 - data) & 0xf;
            default:
            }
            break;

        case BlockID.LADDER:
        case BlockID.WALL_SIGN:
        case BlockID.CHEST:
        case BlockID.FURNACE:
        case BlockID.BURNING_FURNACE:
        case BlockID.ENDER_CHEST:
        case BlockID.TRAPPED_CHEST:
        case BlockID.HOPPER:
            switch (data) {
            case 2:
            case 3:
                return data ^ flipZ;
            case 4:
            case 5:
                return data ^ flipX;
            }
            break;

        case BlockID.DROPPER:
        case BlockID.DISPENSER:
            int dispPower = data & 0x8;
            switch (data & ~0x8) {
            case 2:
            case 3:
                return (data ^ flipZ) | dispPower;
            case 4:
            case 5:
                return (data ^ flipX) | dispPower;
            case 0:
            case 1:
                return (data ^ flipY) | dispPower;
            }
            break;

        case BlockID.PUMPKIN:
        case BlockID.JACKOLANTERN:
            if (data > 3) break;
            /* FALL-THROUGH */

        case BlockID.REDSTONE_REPEATER_OFF:
        case BlockID.REDSTONE_REPEATER_ON:
        case BlockID.COMPARATOR_OFF:
        case BlockID.COMPARATOR_ON:
        case BlockID.COCOA_PLANT:
        case BlockID.TRIPWIRE_HOOK:
            switch (data & 0x3) {
            case 0:
            case 2:
                return data ^ (flipZ << 1);
            case 1:
            case 3:
                return data ^ (flipX << 1);
            }
            break;

        case BlockID.TRAP_DOOR:
            switch (data & 0x3) {
            case 0:
            case 1:
                return data ^ flipZ;
            case 2:
            case 3:
                return data ^ flipX;
            }
            break;

        case BlockID.PISTON_BASE:
        case BlockID.PISTON_STICKY_BASE:
        case BlockID.PISTON_EXTENSION:
            switch (data & ~0x8) {
            case 0:
            case 1:
                return data ^ flipY;
            case 2:
            case 3:
                return data ^ flipZ;
            case 4:
            case 5:
                return data ^ flipX;
            }
            break;

        case BlockID.RED_MUSHROOM_CAP:
        case BlockID.BROWN_MUSHROOM_CAP:
            switch (data) {
            case 1:
            case 4:
            case 7:
                data += flipX * 2;
                break;
            case 3:
            case 6:
            case 9:
                data -= flipX * 2;
                break;
            }
            switch (data) {
            case 1:
            case 2:
            case 3:
                return data + flipZ * 6;
            case 7:
            case 8:
            case 9:
                return data - flipZ * 6;
            }
            break;

        case BlockID.VINE: 
            final int bit1, bit2;
            switch (direction) {
            case NORTH_SOUTH:
                bit1 = 0x2;
                bit2 = 0x8;
                break;
            case WEST_EAST:
                bit1 = 0x1;
                bit2 = 0x4;
                break;
            default:
                return data;
            }
            int newData = data & ~(bit1 | bit2);
            if ((data & bit1) != 0) newData |= bit2;
            if ((data & bit2) != 0) newData |= bit1;
            return newData;

        case BlockID.FENCE_GATE:
            switch (data & 0x3) {
            case 0:
            case 2:
                return data ^ flipZ << 1;
            case 1:
            case 3:
                return data ^ flipX << 1;
            }
            break;

        case BlockID.BED:
            switch (data & 0x3) {
            case 0:
            case 2:
                return data ^ flipZ << 1;
            case 1:
            case 3:
                return data ^ flipX << 1;
            }
            break;

        case BlockID.HEAD:
            switch (data) {
                case 2:
                case 3:
                    return data ^ flipZ;
                case 4:
                case 5:
                    return data ^ flipX;
            }
            break;
        }

        return data;
    }

    /**
     * Cycle a block's data value. This usually goes through some rotational pattern
     * depending on the block. If it returns -1, it means the id and data specified
     * do not have anything to cycle to.
     *
     * @param type block id to be cycled
     * @param data block data value that it starts at
     * @param increment whether to go forward (1) or backward (-1) in the cycle
     * @return the new data value for the block
     */
    public static int cycle(int type, int data, int increment) {
        if (increment != -1 && increment != 1) {
            throw new IllegalArgumentException("Increment must be 1 or -1.");
        }

        int store;
        switch (type) {

        // special case here, going to use "forward" for type and "backward" for orientation
        case BlockID.LOG:
        case BlockID.LOG2:
            if (increment == -1) {
                store = data & 0x3; // copy bottom (type) bits
                return mod((data & ~0x3) + 4, 16) | store; // switch orientation with top bits and reapply bottom bits;
            } else {
                store = data & ~0x3; // copy top (orientation) bits
                return mod((data & 0x3) + 1, 4) | store;  // switch type with bottom bits and reapply top bits
            }

        // <del>same here</del> - screw you unit tests
        /*case BlockID.QUARTZ_BLOCK:
            if (increment == -1 && data > 2) {
                switch (data) {
                case 2: return 3;
                case 3: return 4;
                case 4: return 2;
                }
            } else if (increment == 1) {
                switch (data) {
                case 0:
                    return 1;
                case 1:
                    return 2;
                case 2:
                case 3:
                case 4:
                    return 0;
                }
            } else {
                return -1;
            }*/

        case BlockID.LONG_GRASS:
        case BlockID.SANDSTONE:
        case BlockID.DIRT:
            if (data > 2) return -1;
            return mod((data + increment), 3);

        case BlockID.TORCH:
        case BlockID.REDSTONE_TORCH_ON:
        case BlockID.REDSTONE_TORCH_OFF:
            if (data < 1 || data > 4) return -1;
            return mod((data - 1 + increment), 4) + 1;

        case BlockID.OAK_WOOD_STAIRS:
        case BlockID.COBBLESTONE_STAIRS:
        case BlockID.BRICK_STAIRS:
        case BlockID.STONE_BRICK_STAIRS:
        case BlockID.NETHER_BRICK_STAIRS:
        case BlockID.SANDSTONE_STAIRS:
        case BlockID.SPRUCE_WOOD_STAIRS:
        case BlockID.BIRCH_WOOD_STAIRS:
        case BlockID.JUNGLE_WOOD_STAIRS:
        case BlockID.QUARTZ_STAIRS:
        case BlockID.ACACIA_STAIRS:
        case BlockID.DARK_OAK_STAIRS:
            if (data > 7) return -1;
            return mod((data + increment), 8);

        case BlockID.STONE_BRICK:
        case BlockID.QUARTZ_BLOCK:
        case BlockID.PUMPKIN:
        case BlockID.JACKOLANTERN:
        case BlockID.NETHER_WART:
        case BlockID.CAULDRON:
        case BlockID.WOODEN_STEP:
        case BlockID.DOUBLE_WOODEN_STEP:
        case BlockID.HAY_BLOCK:
            if (data > 3) return -1;
            return mod((data + increment), 4);

        case BlockID.STEP:
        case BlockID.DOUBLE_STEP:
        case BlockID.CAKE_BLOCK:
        case BlockID.PISTON_BASE:
        case BlockID.PISTON_STICKY_BASE:
        case BlockID.SILVERFISH_BLOCK:
            if (data > 5) return -1;
            return mod((data + increment), 6);

        case BlockID.DOUBLE_PLANT:
            store = data & 0x8; // top half flag
            data &= ~0x8;
            if (data > 5) return -1;
            return mod((data + increment), 6) | store;

        case BlockID.CROPS:
        case BlockID.PUMPKIN_STEM:
        case BlockID.MELON_STEM:
            if (data > 6) return -1;
            return mod((data + increment), 7);

        case BlockID.SOIL:
        case BlockID.RED_FLOWER:
            if (data > 8) return -1;
            return mod((data + increment), 9);

        case BlockID.RED_MUSHROOM_CAP:
        case BlockID.BROWN_MUSHROOM_CAP:
            if (data > 10) return -1;
            return mod((data + increment), 11);

        case BlockID.CACTUS:
        case BlockID.REED:
        case BlockID.SIGN_POST:
        case BlockID.VINE:
        case BlockID.SNOW:
        case BlockID.COCOA_PLANT:
            if (data > 15) return -1;
            return mod((data + increment), 16);

        case BlockID.FURNACE:
        case BlockID.BURNING_FURNACE:
        case BlockID.WALL_SIGN:
        case BlockID.LADDER:
        case BlockID.CHEST:
        case BlockID.ENDER_CHEST:
        case BlockID.TRAPPED_CHEST:
        case BlockID.HOPPER:
            if (data < 2 || data > 5) return -1;
            return mod((data - 2 + increment), 4) + 2;

        case BlockID.DISPENSER:
        case BlockID.DROPPER:
            store = data & 0x8;
            data &= ~0x8;
            if (data > 5) return -1;
            return mod((data + increment), 6) | store;

        case BlockID.REDSTONE_REPEATER_OFF:
        case BlockID.REDSTONE_REPEATER_ON:
        case BlockID.COMPARATOR_OFF:
        case BlockID.COMPARATOR_ON:
        case BlockID.TRAP_DOOR:
        case BlockID.FENCE_GATE:
        case BlockID.LEAVES:
        case BlockID.LEAVES2:
            if (data > 7) return -1;
            store = data & ~0x3;
            return mod(((data & 0x3) + increment), 4) | store;

        case BlockID.MINECART_TRACKS:
            if (data < 6 || data > 9) return -1;
            return mod((data - 6 + increment), 4) + 6;

        case BlockID.SAPLING:
            if ((data & 0x3) == 3 || data > 15) return -1;
            store = data & ~0x3;
            return mod(((data & 0x3) + increment), 3) | store;

        case BlockID.FLOWER_POT:
            if (data > 13) return -1;
            return mod((data + increment), 14);

        case BlockID.CLOTH:
        case BlockID.STAINED_CLAY:
        case BlockID.CARPET:
        case BlockID.STAINED_GLASS:
        case BlockID.STAINED_GLASS_PANE:
            if (increment == 1) {
                data = nextClothColor(data);
            } else if (increment == -1) {
                data = prevClothColor(data);
            }
            return data;

        default:
            return -1;
        }
    }

    /**
     * Better modulo, not just remainder.
     */
    private static int mod(int x, int y) {
        int res = x % y;
        return res < 0 ? res + y : res;
    }

    /**
     * Returns the data value for the next color of cloth in the rainbow. This
     * should not be used if you want to just increment the data value.
     * @param data
     * @return
     */
    public static int nextClothColor(int data) {
        switch (data) {
            case ClothColor.ID.WHITE: return ClothColor.ID.LIGHT_GRAY;
            case ClothColor.ID.LIGHT_GRAY: return ClothColor.ID.GRAY;
            case ClothColor.ID.GRAY: return ClothColor.ID.BLACK;
            case ClothColor.ID.BLACK: return ClothColor.ID.BROWN;
            case ClothColor.ID.BROWN: return ClothColor.ID.RED;
            case ClothColor.ID.RED: return ClothColor.ID.ORANGE;
            case ClothColor.ID.ORANGE: return ClothColor.ID.YELLOW;
            case ClothColor.ID.YELLOW: return ClothColor.ID.LIGHT_GREEN;
            case ClothColor.ID.LIGHT_GREEN: return ClothColor.ID.DARK_GREEN;
            case ClothColor.ID.DARK_GREEN: return ClothColor.ID.CYAN;
            case ClothColor.ID.CYAN: return ClothColor.ID.LIGHT_BLUE;
            case ClothColor.ID.LIGHT_BLUE: return ClothColor.ID.BLUE;
            case ClothColor.ID.BLUE: return ClothColor.ID.PURPLE;
            case ClothColor.ID.PURPLE: return ClothColor.ID.MAGENTA;
            case ClothColor.ID.MAGENTA: return ClothColor.ID.PINK;
            case ClothColor.ID.PINK: return ClothColor.ID.WHITE;
        }

        return ClothColor.ID.WHITE;
    }

    /**
     * Returns the data value for the previous ext color of cloth in the rainbow.
     * This should not be used if you want to just increment the data value.
     * @param data
     * @return
     */
    public static int prevClothColor(int data) {
        switch (data) {
            case ClothColor.ID.LIGHT_GRAY: return ClothColor.ID.WHITE;
            case ClothColor.ID.GRAY: return ClothColor.ID.LIGHT_GRAY;
            case ClothColor.ID.BLACK: return ClothColor.ID.GRAY;
            case ClothColor.ID.BROWN: return ClothColor.ID.BLACK;
            case ClothColor.ID.RED: return ClothColor.ID.BROWN;
            case ClothColor.ID.ORANGE: return ClothColor.ID.RED;
            case ClothColor.ID.YELLOW: return ClothColor.ID.ORANGE;
            case ClothColor.ID.LIGHT_GREEN: return ClothColor.ID.YELLOW;
            case ClothColor.ID.DARK_GREEN: return ClothColor.ID.LIGHT_GREEN;
            case ClothColor.ID.CYAN: return ClothColor.ID.DARK_GREEN;
            case ClothColor.ID.LIGHT_BLUE: return ClothColor.ID.CYAN;
            case ClothColor.ID.BLUE: return ClothColor.ID.LIGHT_BLUE;
            case ClothColor.ID.PURPLE: return ClothColor.ID.BLUE;
            case ClothColor.ID.MAGENTA: return ClothColor.ID.PURPLE;
            case ClothColor.ID.PINK: return ClothColor.ID.MAGENTA;
            case ClothColor.ID.WHITE: return ClothColor.ID.PINK;
        }

        return ClothColor.ID.WHITE;
    }
}
