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
import com.sk89q.jnbt.JSON2NBT;
import com.sk89q.jnbt.Tag;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.NotABlockException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.blocks.ClothColor;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.NoteBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.blocks.SkullBlock;
import com.sk89q.worldedit.blocks.metadata.MobType;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.DisallowedUsageException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.math.MathUtils;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.registry.BundledBlockData;
import com.sk89q.worldedit.world.registry.SimpleState;
import com.sk89q.worldedit.world.registry.SimpleStateValue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses block input strings.
 */
public class DefaultBlockParser extends InputParser<BaseBlock> {

    public  DefaultBlockParser(WorldEdit worldEdit) {
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
//        input = input.replace("_", " ");
//        input = input.replace(";", "|");
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
            if (MathUtils.isInteger(testId)) {
                blockId = Integer.parseInt(testId);
                blockType = BlockType.fromID(blockId);
            } else {
                BundledBlockData.BlockEntry block = BundledBlockData.getInstance().findById(testId);
                if (block == null) {
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
                } else {
                    blockId = block.legacyId;
                    blockType = BlockType.fromID(blockId);
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
                    if (MathUtils.isInteger(typeAndData[1])) {
                        data = Integer.parseInt(typeAndData[1]);
                    } else {
                        data = Integer.MAX_VALUE; // Some invalid value
                        BundledBlockData.BlockEntry block = BundledBlockData.getInstance().findById(blockId);
                        if (block != null && block.states != null) {
                            loop:
                            for (Map.Entry<String, SimpleState> stateEntry : block.states.entrySet()) {
                                for (Map.Entry<String, SimpleStateValue> valueEntry : stateEntry.getValue().valueMap().entrySet()) {
                                    String key = valueEntry.getKey();
                                    if (key.equalsIgnoreCase(typeAndData[1])) {
                                        data = valueEntry.getValue().data;
                                        break loop;
                                    }
                                }

                            }
                        }
                    }
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

        if (blockAndExtraData.length > 1 && blockAndExtraData[1].startsWith("{")) {
            String joined = StringUtil.joinString(Arrays.copyOfRange(blockAndExtraData, 1, blockAndExtraData.length), "|");
            try {
                CompoundTag nbt = JSON2NBT.getTagFromJson(joined);
                if (nbt != null) {
                    if (context.isRestricted() && actor != null && !actor.hasPermission("worldedit.anyblock")) {
                        throw new DisallowedUsageException("You are not allowed to nbt'");
                    }
                    return new BaseBlock(blockId, data, nbt);
                }
            } catch (InputParseException e) {
                throw new NoMatchException(e.getMessage());
            }
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
            case CHEST:
            case END_GATEWAY:
            case END_PORTAL:
                return new BaseBlock(blockId, data, new CompoundTag(new HashMap<String, Tag>()));
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