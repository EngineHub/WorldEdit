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

package com.sk89q.worldedit.extension.registry;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.*;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.DisallowedUsageException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.world.World;

/**
 * Parses block input strings.
 */
class DefaultBlockParser extends InputParser<BaseBlock> {

    protected DefaultBlockParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    private static BaseBlock getBlockInHand(Actor actor) throws InputParseException {
        if (actor instanceof Player) {
            try {
                return ((Player) actor).getBlockInHand();
            } catch (NotABlockException e) {
                throw new InputParseException("You're not holding a block!");
            } catch (WorldEditException e) {
                throw new InputParseException("Unknown error occurred: " + e.getMessage(), e);
            }
        } else {
            throw new InputParseException("The user is not a player!");
        }
    }

    @Override
    public BaseBlock parseFromInput(String input, ParserContext context) throws InputParseException {
        BlockType blockType;
        input = input.replace("_", " ");
        input = input.replace(";", "|");
        String[] blockAndExtraData = input.split("\\|");
        String[] typeAndData = blockAndExtraData[0].split(":", 2);
        String testID = typeAndData[0];

        int blockId = -1;

        int data = -1;

        boolean parseDataValue = true;

        if ("hand".equalsIgnoreCase(testID)) {
            // Get the block type from the item in the user's hand.
            final BaseBlock blockInHand = getBlockInHand(context.requireActor());
            if (blockInHand.getClass() != BaseBlock.class) {
                return blockInHand;
            }

            blockId = blockInHand.getId();
            blockType = BlockType.fromID(blockId);
            data = blockInHand.getData();
        } else if ("pos1".equalsIgnoreCase(testID)) {
            // Get the block type from the "primary position"
            final World world = context.requireWorld();
            final BlockVector primaryPosition;
            try {
                primaryPosition = context.requireSession().getRegionSelector(world).getPrimaryPosition();
            } catch (IncompleteRegionException e) {
                throw new InputParseException("Your selection is not complete.");
            }
            final BaseBlock blockInHand = world.getBlock(primaryPosition);
            if (blockInHand.getClass() != BaseBlock.class) {
                return blockInHand;
            }

            blockId = blockInHand.getId();
            blockType = BlockType.fromID(blockId);
            data = blockInHand.getData();
        } else {
            // Attempt to parse the item ID or otherwise resolve an item/block
            // name to its numeric ID
            try {
                blockId = Integer.parseInt(testID);
                blockType = BlockType.fromID(blockId);
            } catch (NumberFormatException e) {
                blockType = BlockType.lookup(testID);
                if (blockType == null) {
                    int t = worldEdit.getServer().resolveItem(testID);
                    if (t > 0) {
                        blockType = BlockType.fromID(t); // Could be null
                        blockId = t;
                    }
                }
            }

            if (blockId == -1 && blockType == null) {
                // Maybe it's a cloth
                ClothColor col = ClothColor.lookup(testID);
                if (col == null) {
                    throw new NoMatchException("Unknown wool color '" + input + "'");
                }

                blockType = BlockType.CLOTH;
                data = col.getID();

                // Prevent overriding the data value
                parseDataValue = false;
            }

            // Read block ID
            if (blockId == -1) {
                blockId = blockType.getID();
            }

            if (!context.requireWorld().isValidBlockType(blockId)) {
                throw new NoMatchException("Does not match a valid block type: '" + input + "'");
            }
        }

        if (!context.isPreferringWildcard() && data == -1) {
            // No wildcards allowed => eliminate them.
            data = 0;
        }

        if (parseDataValue) { // Block data not yet detected
            // Parse the block data (optional)
            try {
                if (typeAndData.length > 1 && typeAndData[1].length() > 0) {
                    data = Integer.parseInt(typeAndData[1]);
                }

                if (data > 15) {
                    throw new NoMatchException("Invalid data value '" + typeAndData[1] + "'");
                }

                if (data < 0 && (context.isRestricted() || data != -1)) {
                    data = 0;
                }
            } catch (NumberFormatException e) {
                if (blockType == null) {
                    throw new NoMatchException("Unknown data value '" + typeAndData[1] + "'");
                }

                switch (blockType) {
                    case CLOTH:
                    case STAINED_CLAY:
                    case CARPET:
                        ClothColor col = ClothColor.lookup(typeAndData[1]);
                        if (col == null) {
                            throw new NoMatchException("Unknown wool color '" + typeAndData[1] + "'");
                        }

                        data = col.getID();
                        break;

                    case STEP:
                    case DOUBLE_STEP:
                        BlockType dataType = BlockType.lookup(typeAndData[1]);

                        if (dataType == null) {
                            throw new NoMatchException("Unknown step type '" + typeAndData[1] + "'");
                        }

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
                                throw new NoMatchException("Invalid step type '" + typeAndData[1] + "'");
                        }
                        break;

                    default:
                        throw new NoMatchException("Unknown data value '" + typeAndData[1] + "'");
                }
            }
        }

        // Check if the item is allowed
        Actor actor = context.requireActor();
        if (context.isRestricted() && actor != null && !actor.hasPermission("worldedit.anyblock")
                && worldEdit.getConfiguration().disallowedBlocks.contains(blockId)) {
            throw new DisallowedUsageException("You are not allowed to use '" + input + "'");
        }

        if (blockType == null) {
            return new BaseBlock(blockId, data);
        }

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
                    if (!worldEdit.getServer().isValidMobType(mobName)) {
                        throw new NoMatchException("Unknown mob type '" + mobName + "'");
                    }
                    return new MobSpawnerBlock(data, mobName);
                } else {
                    return new MobSpawnerBlock(data, MobType.PIG.getName());
                }

            case NOTE_BLOCK:
                // Allow setting note
                if (blockAndExtraData.length <= 1) {
                    return new NoteBlock(data, (byte) 0);
                }

                byte note = Byte.parseByte(blockAndExtraData[1]);
                if (note < 0 || note > 24) {
                    throw new InputParseException("Out of range note value: '" + blockAndExtraData[1] + "'");
                }

                return new NoteBlock(data, note);

            case HEAD:
                // allow setting type/player/rotation
                if (blockAndExtraData.length <= 1) {
                    return new SkullBlock(data);
                }

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
                            throw new InputParseException("Second part of skull metadata should be a number.");
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

            default:
                return new BaseBlock(blockId, data);
        }
    }

}
