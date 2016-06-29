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

package com.sk89q.worldedit.extension.factory;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ClothColor;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.factory.delegate.block.MobSpawnerParser;
import com.sk89q.worldedit.extension.factory.delegate.block.NoteBlockParser;
import com.sk89q.worldedit.extension.factory.delegate.block.SkullParser;
import com.sk89q.worldedit.extension.factory.delegate.block.WallSignParser;
import com.sk89q.worldedit.extension.input.DisallowedUsageException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.world.World;

import java.util.HashMap;

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
    public BaseBlock parseFromInput(String input, ParserContext context)
            throws InputParseException {
        // TODO: Rewrite this entire method to use BaseBlocks and ignore
        // BlockType, as well as to properly handle mod:name IDs

        String originalInput = input;
        input = input.replace("_", " ");
        input = input.replace(";", "|");
        Exception suppressed = null;
        try {
            BaseBlock modified = parseLogic(input, context);
            if (modified != null) {
                return modified;
            }
        } catch (Exception e) {
            suppressed = e;
        }
        try {
            return parseLogic(originalInput, context);
        } catch (Exception e) {
            if (suppressed != null) {
                e.addSuppressed(suppressed);
            }
            throw e;
        }
    }

    private BaseBlock parseLogic(String input, ParserContext context)
            throws InputParseException, NoMatchException,
            DisallowedUsageException {
        BlockType blockType;
        String[] blockAndExtraData = input.split("\\|");
        String[] blockLocator = blockAndExtraData[0].split(":", 3);
        String[] typeAndData;
        switch (blockLocator.length) {
            case 3:
                typeAndData = new String[] {
                        blockLocator[0] + ":" + blockLocator[1],
                        blockLocator[2] };
                break;
            default:
                typeAndData = blockLocator;
        }
        String testId = typeAndData[0];

        int blockId = -1;

        int data = -1;

        boolean parseDataValue = true;

        if ("hand".equalsIgnoreCase(testId)) {
            // Get the block type from the item in the user's hand.
            final BaseBlock blockInHand = getBlockInHand(context.requireActor());
            if (blockInHand.getClass() != BaseBlock.class) {
                return blockInHand;
            }

            blockId = blockInHand.getId();
            blockType = BlockType.fromID(blockId);
            data = blockInHand.getData();
        } else if ("pos1".equalsIgnoreCase(testId)) {
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
                blockId = Integer.parseInt(testId);
                blockType = BlockType.fromID(blockId);
            } catch (NumberFormatException e) {
                blockType = BlockType.lookup(testId);
                if (blockType == null) {
                    int t = worldEdit.getServer().resolveItem(testId);
                    if (t >= 0) {
                        blockType = BlockType.fromID(t); // Could be null
                        blockId = t;
                    } else if (blockLocator.length == 2) { // Block IDs in MC 1.7 and above use mod:name
                        t = worldEdit.getServer().resolveItem(blockAndExtraData[0]);
                        if (t >= 0) {
                            blockType = BlockType.fromID(t); // Could be null
                            blockId = t;
                            typeAndData = new String[] { blockAndExtraData[0] };
                            testId = blockAndExtraData[0];
                        }
                    }
                }
            }

            if (blockId == -1 && blockType == null) {
                // Maybe it's a cloth
                ClothColor col = ClothColor.lookup(testId);
                if (col == null) {
                    throw new NoMatchException("Can't figure out what block '" + input + "' refers to");
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
                if (typeAndData.length > 1 && !typeAndData[1].isEmpty()) {
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
            return WorldEdit.getInstance().getBaseBlockFactory().getBaseBlock(blockId, data);
        }

        HashMap<Integer, DelegateParser<CompoundTag>> delegateParsers = new HashMap<Integer, DelegateParser<CompoundTag>>();
        WallSignParser wallSignParser = new WallSignParser();
        delegateParsers.put(BlockID.SIGN_POST, wallSignParser);
        delegateParsers.put(BlockID.WALL_SIGN, wallSignParser);
        delegateParsers.put(BlockID.MOB_SPAWNER, new MobSpawnerParser());
        delegateParsers.put(BlockID.NOTE_BLOCK, new NoteBlockParser());
        delegateParsers.put(BlockID.HEAD, new SkullParser());

        CompoundTag nbtData = null;

        DelegateParser<CompoundTag> tagParser = delegateParsers.get(blockId);
        if (tagParser != null) {
            nbtData = tagParser.createFromArguments(blockAndExtraData);
        }

        return WorldEdit.getInstance().getBaseBlockFactory().getBaseBlock(blockId, data, nbtData);
    }

}
