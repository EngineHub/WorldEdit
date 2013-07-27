// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.factory;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Fetches materials, blocks, and items from user input.
 */
public class MaterialFactory extends AbstractFactory {

    /**
     * Create a new instance.
     *
     * @param worldEdit a WorldEdit instance
     */
    public MaterialFactory(WorldEdit worldEdit) {
        super(worldEdit);
    }

    /**
     * Match a block from user input.
     *
     * @param player the player
     * @param input the string describing a block
     * @param ignoreBlacklist true to ignore blacklists
     * @return a block
     * @throws com.sk89q.worldedit.UnknownItemException if an item cannot be matched
     * @throws com.sk89q.worldedit.DisallowedItemException if a disallowed item has been matched
     */
    public BaseBlock matchBlock(LocalPlayer player, String input, boolean ignoreBlacklist)
            throws UnknownItemException, DisallowedItemException {
        return matchBlock(player, input, ignoreBlacklist, false);
    }

    /**
     * Match a block from user input.
     *
     * @param player the player
     * @param input the string describing a block
     * @param ignoreBlacklist true to ignore blacklists
     * @param allowDataWildcard true to allow wildcard data match
     * @return a block
     * @throws UnknownItemException if an item cannot be matched
     * @throws DisallowedItemException if a disallowed item has been matched
     */
    public BaseBlock matchBlock(LocalPlayer player, String input,
                                boolean ignoreBlacklist, boolean allowDataWildcard)
            throws UnknownItemException, DisallowedItemException {
        BlockType blockType;
        input = input.replace("_", " ");
        input = input.replace(";", "|");
        String[] blockAndExtraData = input.split("\\|");
        String[] typeAndData = blockAndExtraData[0].split(":", 2);
        String testID = typeAndData[0];
        int blockId = -1;

        int data = -1;

        // Attempt to parse the item ID or otherwise resolve an item/block
        // name to its numeric ID
        try {
            blockId = Integer.parseInt(testID);
            blockType = BlockType.fromID(blockId);
        } catch (NumberFormatException e) {
            blockType = BlockType.lookup(testID);
            if (blockType == null) {
                int t = getServer().resolveItem(testID);
                if (t > 0) {
                    blockType = BlockType.fromID(t); // Could be null
                    blockId = t;
                }
            }
        }

        if (blockId == -1 && blockType == null) {
            // Maybe it's a cloth
            ClothColor col = ClothColor.lookup(testID);

            if (col != null) {
                blockType = BlockType.CLOTH;
                data = col.getID();
            } else {
                throw new UnknownItemException(input);
            }
        }

        // Read block ID
        if (blockId == -1) {
            blockId = blockType.getID();
        }

        if (!player.getWorld().isValidBlockType(blockId)) {
            throw new UnknownItemException(input);
        }

        if (data == -1) { // Block data not yet detected
            // Parse the block data (optional)
            try {
                data = (typeAndData.length > 1 && typeAndData[1].length() > 0) ?
                        Integer.parseInt(typeAndData[1]) : (allowDataWildcard ? -1 : 0);
                if ((data > 15 && !getConfiguration().allowExtraDataValues) ||
                        (data < 0 && !(ignoreBlacklist && data == -1))) {
                    data = 0;
                }
            } catch (NumberFormatException e) {
                if (blockType != null) {
                    switch (blockType) {
                        case CLOTH:
                            ClothColor col = ClothColor.lookup(typeAndData[1]);

                            if (col != null) {
                                data = col.getID();
                            } else {
                                throw new InvalidItemException(
                                        input, "Unknown cloth color '" + typeAndData[1] + "'");
                            }
                            break;

                        case STEP:
                        case DOUBLE_STEP:
                            BlockType dataType = BlockType.lookup(typeAndData[1]);

                            if (dataType != null) {
                                switch (dataType) {
                                    case STONE:
                                        data = 0;
                                        break;
                                    case SANDSTONE:
                                        data = 1;
                                        break;
                                    case WOOD:
                                        data = 2;
                                        break;
                                    case COBBLESTONE:
                                        data = 3;
                                        break;
                                    case BRICK:
                                        data = 4;
                                        break;
                                    case STONE_BRICK:
                                        data = 5;
                                        break;
                                    case NETHER_BRICK:
                                        data = 6;
                                        break;
                                    case QUARTZ_BLOCK:
                                        data = 7;
                                        break;

                                    default:
                                        throw new InvalidItemException(input, "Invalid step type '" + typeAndData[1] + "'");
                                }
                            } else {
                                throw new InvalidItemException(input, "Unknown step type '" + typeAndData[1] + "'");
                            }
                            break;

                        default:
                            throw new InvalidItemException(input, "Unknown data value '" + typeAndData[1] + "'");
                    }
                } else {
                    throw new InvalidItemException(input, "Unknown data value '" + typeAndData[1] + "'");
                }
            }
        }

        // Check if the item is allowed
        if (ignoreBlacklist || player.hasPermission("worldedit.anyblock") || !getConfiguration().disallowedBlocks.contains(blockId)) {
            if (blockType != null) {
                switch (blockType) {
                    case SIGN_POST:
                    case WALL_SIGN:
                        // Allow special sign text syntax
                        String[] text = new String[4];
                        text[0] = blockAndExtraData.length > 1 ? blockAndExtraData[1] : "";
                        text[1] = blockAndExtraData.length > 2 ? blockAndExtraData[2] : "";
                        text[2] = blockAndExtraData.length > 3 ? blockAndExtraData[3] : "";
                        text[3] = blockAndExtraData.length > 4 ? blockAndExtraData[4] : "";
                        return new SignBlock(blockType.getID(), data, text);

                    case MOB_SPAWNER:
                        // Allow setting mob spawn type
                        if (blockAndExtraData.length > 1) {
                            String mobName = blockAndExtraData[1];
                            for (MobType mobType : MobType.values()) {
                                if (mobType.getName().toLowerCase().equals(mobName.toLowerCase())) {
                                    mobName = mobType.getName();
                                    break;
                                }
                            }
                            if (!getServer().isValidMobType(mobName)) {
                                throw new InvalidItemException(input, "Unknown mob type '" + mobName + "'");
                            }
                            return new MobSpawnerBlock(data, mobName);
                        } else {
                            return new MobSpawnerBlock(data, MobType.PIG.getName());
                        }

                    case NOTE_BLOCK:
                        // Allow setting note
                        if (blockAndExtraData.length > 1) {
                            byte note = Byte.parseByte(blockAndExtraData[1]);
                            if (note < 0 || note > 24) {
                                throw new InvalidItemException(input, "Out of range note value: '" + blockAndExtraData[1] + "'");
                            } else {
                                return new NoteBlock(data, note);
                            }
                        } else {
                            return new NoteBlock(data, (byte) 0);
                        }

                    case HEAD:
                        // allow setting type/player/rotation
                        if (blockAndExtraData.length > 1) {
                            // and thus, the format shall be "|type|rotation" or "|type" or "|rotation"
                            byte rot = 0;
                            String type = "";
                            try {
                                rot = Byte.parseByte(blockAndExtraData[1]);
                            } catch (NumberFormatException e) {
                                type = blockAndExtraData[1];
                                if (blockAndExtraData.length > 2) {
                                    try {
                                        rot = Byte.parseByte(blockAndExtraData[2]);
                                    } catch (NumberFormatException e2) {
                                        throw new InvalidItemException(input, "Second part of skull metadata should be a number.");
                                    }
                                }
                            }
                            byte skullType = 0;
                            // type is either the mob type or the player name
                            // sorry for the four minecraft accounts named "skeleton", "wither", "zombie", or "creeper"
                            if (!type.isEmpty()) {
                                if (type.equalsIgnoreCase("skeleton")) skullType = 0;
                                else if (type.equalsIgnoreCase("wither")) skullType = 1;
                                else if (type.equalsIgnoreCase("zombie")) skullType = 2;
                                else if (type.equalsIgnoreCase("creeper")) skullType = 4;
                                else skullType = 3;
                            }
                            if (skullType == 3) {
                                return new SkullBlock(data, rot, type.replace(" ", "_")); // valid MC usernames
                            } else {
                                return new SkullBlock(data, skullType, rot);
                            }
                        } else {
                            return new SkullBlock(data);
                        }

                    default:
                        return new BaseBlock(blockId, data);
                }
            } else {
                return new BaseBlock(blockId, data);
            }
        }

        throw new DisallowedItemException(input);
    }


    /**
     * Match a block from user input.
     *
     * @param player the player
     * @param input the string describing a block
     * @return a block
     * @throws UnknownItemException if an item cannot be matched
     * @throws DisallowedItemException if a disallowed item has been matched
     */
    public BaseBlock matchBlock(LocalPlayer player, String input)
            throws UnknownItemException, DisallowedItemException {
        return matchBlock(player, input, false);
    }

    /**
     * Match a list of blocks from user input.
     *
     * @param player the player
     * @param input the string describing a block
     * @param ignoreBlacklist true to ignore blacklists
     * @param allowDataWildcard true to allow wildcard data match
     * @return a list of blocks
     * @throws com.sk89q.worldedit.UnknownItemException if an item cannot be matched
     * @throws com.sk89q.worldedit.DisallowedItemException if a disallowed item has been matched
     */
    public Set<BaseBlock> matchBlocks(LocalPlayer player, String input,
                                      boolean ignoreBlacklist, boolean allowDataWildcard)
            throws DisallowedItemException, UnknownItemException {
        String[] items = input.split(",");
        Set<BaseBlock> blocks = new HashSet<BaseBlock>();
        for (String id : items) {
            blocks.add(matchBlock(player, id, ignoreBlacklist, allowDataWildcard));
        }
        return blocks;
    }

    /**
     * Match a list of blocks from user input.
     *
     * @param player the player
     * @param input the string describing a block
     * @param ignoreBlacklist true to ignore blacklists
     * @return a list of blocks
     * @throws com.sk89q.worldedit.UnknownItemException if an item cannot be matched
     * @throws com.sk89q.worldedit.DisallowedItemException if a disallowed item has been matched
     */
    public Set<BaseBlock> matchBlocks(
            LocalPlayer player, String input, boolean ignoreBlacklist)
            throws DisallowedItemException, UnknownItemException {
        return matchBlocks(player, input, ignoreBlacklist, false);
    }

    /**
     * Match a list of blocks from user input.
     *
     * @param player the player
     * @param input the string describing a block
     * @return a list of blocks
     * @throws com.sk89q.worldedit.UnknownItemException if an item cannot be matched
     * @throws com.sk89q.worldedit.DisallowedItemException if a disallowed item has been matched
     */
    public Set<BaseBlock> matchBlocks(LocalPlayer player, String input)
            throws DisallowedItemException, UnknownItemException {
        return matchBlocks(player, input, false);
    }

    /**
     * Get a list of blocks as a set.
     *
     * @param player the player
     * @param input the user input
     * @param ignoreBlacklist true to ignore blacklists
     * @return the list of block IDs
     * @throws UnknownItemException
     * @throws DisallowedItemException
     */
    public Set<Integer> matchBlockIds(LocalPlayer player,
                                    String input, boolean ignoreBlacklist)
            throws UnknownItemException, DisallowedItemException {

        String[] items = input.split(",");
        Set<Integer> blocks = new HashSet<Integer>();
        for (String s : items) {
            blocks.add(matchBlock(player, s, ignoreBlacklist).getType());
        }
        return blocks;
    }

}
