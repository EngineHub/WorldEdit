// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
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

package com.sk89q.worldedit.data;

import com.sk89q.worldedit.blocks.BlockID;

/**
 * Block data related classes.
 *
 * @author sk89q
 */
public final class BlockData {
    /**
     * Rotate a block's data value 90 degrees (north->east->south->west->north);
     * 
     * @param type
     * @param data
     * @return
     */
    public static int rotate90(int type, int data) {
        if (type == BlockID.TORCH
                || type == BlockID.REDSTONE_TORCH_OFF
                || type == BlockID.REDSTONE_TORCH_ON) {
            switch (data) {
                case 1: return 3;
                case 2: return 4;
                case 3: return 2;
                case 4: return 1;
            }
        } else if (type == BlockID.MINECART_TRACKS) {
            switch (data) {
                case 0: return 1;
                case 1: return 0;
                case 2: return 5;
                case 3: return 4;
                case 4: return 2;
                case 5: return 3;
                case 6: return 7;
                case 7: return 8;
                case 8: return 9;
                case 9: return 6;
            }
        } else if (type == BlockID.WOODEN_STAIRS
                || type == BlockID.COBBLESTONE_STAIRS) {
            switch (data) {
                case 0: return 2;
                case 1: return 3;
                case 2: return 1;
                case 3: return 0;
            }
        } else if (type == BlockID.LEVER) {
            int thrown = data & 0x8;
            int withoutThrown = data ^ 0x8;
            switch (withoutThrown) {
                case 1: return 3 | thrown;
                case 2: return 4 | thrown;
                case 3: return 2 | thrown;
                case 4: return 1 | thrown;
            }
        } else if (type == BlockID.WOODEN_DOOR
                || type == BlockID.IRON_DOOR) {
            int topHalf = data & 0x8;
            int swung = data & 0x4;
            int withoutFlags = data ^ (0x8 | 0x4);
            switch (withoutFlags) {
                case 0: return 1 | topHalf | swung;
                case 1: return 2 | topHalf | swung;
                case 2: return 3 | topHalf | swung;
                case 3: return 0 | topHalf | swung;
            }
        } else if (type == BlockID.STONE_BUTTON) {
            int thrown = data & 0x8;
            int withoutThrown = data ^ 0x8;
            switch (withoutThrown) {
                case 1: return 3 | thrown;
                case 2: return 4 | thrown;
                case 3: return 2 | thrown;
                case 4: return 1 | thrown;
            }
        } else if (type == BlockID.SIGN_POST) {
            return (data + 4) % 16;
        } else if (type == BlockID.LADDER
                || type == BlockID.WALL_SIGN
                || type == BlockID.FURNACE
                || type == BlockID.BURNING_FURNACE
                || type == BlockID.DISPENSER) {
            switch (data) {
                case 2: return 5;
                case 3: return 4;
                case 4: return 2;
                case 5: return 3;
            }
        } else if (type == BlockID.PUMPKIN
                || type == BlockID.JACKOLANTERN) {
            switch (data) {
                case 0: return 1;
                case 1: return 2;
                case 2: return 3;
                case 3: return 0;
            }
        } else if (type == BlockID.TRAP_DOOR) {
            int open = data & 0x4;
            int withoutOpen = data ^ 0x4;
            switch (withoutOpen) {
                case 0: return 3 | open;
                case 1: return 2 | open;
                case 2: return 0 | open;
                case 3: return 1 | open;
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
        
        if (type == BlockID.TORCH
                || type == BlockID.REDSTONE_TORCH_OFF
                || type == BlockID.REDSTONE_TORCH_ON) {
            switch (data) {
                case 3: return 1;
                case 4: return 2;
                case 2: return 3;
                case 1: return 4;
            }
        } else if (type == BlockID.MINECART_TRACKS) {
            switch (data) {
                case 1: return 0;
                case 0: return 1;
                case 5: return 2;
                case 4: return 3;
                case 2: return 4;
                case 3: return 5;
                case 7: return 6;
                case 8: return 7;
                case 9: return 8;
                case 6: return 9;
            }
        } else if (type == BlockID.WOODEN_STAIRS
                || type == BlockID.COBBLESTONE_STAIRS) {
            switch (data) {
                case 2: return 0;
                case 3: return 1;
                case 1: return 2;
                case 0: return 3;
            }
        } else if (type == BlockID.LEVER) {
            int thrown = data & 0x8;
            int withoutThrown = data ^ 0x8;
            switch (withoutThrown) {
                case 3: return 1 | thrown;
                case 4: return 2 | thrown;
                case 2: return 3 | thrown;
                case 1: return 4 | thrown;
            }
        } else if (type == BlockID.WOODEN_DOOR
                || type == BlockID.IRON_DOOR) {
            int topHalf = data & 0x8;
            int swung = data & 0x4;
            int withoutFlags = data ^ (0x8 | 0x4);
            switch (withoutFlags) {
                case 1: return 0 | topHalf | swung;
                case 2: return 1 | topHalf | swung;
                case 3: return 2 | topHalf | swung;
                case 0: return 3 | topHalf | swung;
            }
        } else if (type == BlockID.STONE_BUTTON) {
            int thrown = data & 0x8;
            int withoutThrown = data ^ 0x8;
            switch (withoutThrown) {
                case 3: return 1 | thrown;
                case 4: return 2 | thrown;
                case 2: return 3 | thrown;
                case 1: return 4 | thrown;
            }
        } else if (type == BlockID.SIGN_POST) {
            int newData = (data - 4);
            if (newData < 0) {
                newData += 16;
            }
            return newData;
        } else if (type == BlockID.LADDER
                || type == BlockID.WALL_SIGN
                || type == BlockID.FURNACE
                || type == BlockID.BURNING_FURNACE
                || type == BlockID.DISPENSER) {
            switch (data) {
                case 5: return 2;
                case 4: return 3;
                case 2: return 4;
                case 3: return 5;
            }
        } else if (type == BlockID.PUMPKIN
                || type == BlockID.JACKOLANTERN) {
            switch (data) {
                case 1: return 0;
                case 2: return 1;
                case 3: return 2;
                case 0: return 3;
            }
        } else if (type == BlockID.TRAP_DOOR) {
            int open = data & 0x4;
            int withoutOpen = data ^ 0x4;
            switch (withoutOpen) {
                case 3: return 9 | open;
                case 2: return 1 | open;
                case 0: return 2 | open;
                case 1: return 3 | open;
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
}
