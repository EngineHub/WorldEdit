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

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.NotABlockException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.blocks.SkullBlock;
import com.sk89q.worldedit.blocks.metadata.MobType;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.DisallowedUsageException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses block input strings.
 */
class DefaultBlockParser extends InputParser<BlockStateHolder> {

    protected DefaultBlockParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    private static BaseBlock getBlockInHand(Actor actor, HandSide handSide) throws InputParseException {
        if (actor instanceof Player) {
            try {
                return ((Player) actor).getBlockInHand(handSide);
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
    public BlockStateHolder parseFromInput(String input, ParserContext context)
            throws InputParseException {
        String originalInput = input;
        input = input.replace(";", "|");
        Exception suppressed = null;
        try {
            BlockStateHolder modified = parseLogic(input, context);
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

    private static String[] EMPTY_STRING_ARRAY = new String[]{};

    /**
     * Backwards compatibility for wool colours in block syntax.
     *
     * @param string Input string
     * @return Mapped string
     */
    private String woolMapper(String string) {
        switch (string.toLowerCase()) {
            case "white":
                return BlockTypes.WHITE_WOOL.getId();
            case "black":
                return BlockTypes.BLACK_WOOL.getId();
            case "blue":
                return BlockTypes.BLUE_WOOL.getId();
            case "brown":
                return BlockTypes.BROWN_WOOL.getId();
            case "cyan":
                return BlockTypes.CYAN_WOOL.getId();
            case "gray":
            case "grey":
                return BlockTypes.GRAY_WOOL.getId();
            case "green":
                return BlockTypes.GREEN_WOOL.getId();
            case "light_blue":
            case "lightblue":
                return BlockTypes.LIGHT_BLUE_WOOL.getId();
            case "light_gray":
            case "light_grey":
            case "lightgray":
            case "lightgrey":
                return BlockTypes.LIGHT_GRAY_WOOL.getId();
            case "lime":
                return BlockTypes.LIME_WOOL.getId();
            case "magenta":
                return BlockTypes.MAGENTA_WOOL.getId();
            case "orange":
                return BlockTypes.ORANGE_WOOL.getId();
            case "pink":
                return BlockTypes.PINK_WOOL.getId();
            case "purple":
                return BlockTypes.PURPLE_WOOL.getId();
            case "yellow":
                return BlockTypes.YELLOW_WOOL.getId();
            case "red":
                return BlockTypes.RED_WOOL.getId();
            default:
                return string;
        }
    }

    private static BlockState applyProperties(BlockState state, String[] stateProperties) throws NoMatchException {
        if (stateProperties.length > 0) { // Block data not yet detected
            // Parse the block data (optional)
            for (String parseableData : stateProperties) {
                try {
                    String[] parts = parseableData.split("=");
                    if (parts.length != 2) {
                        throw new NoMatchException("Bad state format in " + parseableData);
                    }

                    Property propertyKey = state.getBlockType().getPropertyMap().get(parts[0]);
                    if (propertyKey == null) {
                        throw new NoMatchException("Unknown state " + parts[0] + " for block " + state.getBlockType().getName());
                    }
                    Object value;
                    try {
                        value = propertyKey.getValueFor(parts[1]);
                    } catch (IllegalArgumentException e) {
                        throw new NoMatchException("Unknown value " + parts[1] + " for state " + parts[0]);
                    }

                    state = state.with(propertyKey, value);
                } catch (NoMatchException e) {
                    throw e; // Pass-through
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new NoMatchException("Unknown state '" + parseableData + "'");
                }
            }
        }

        return state;
    }

    private BlockStateHolder parseLogic(String input, ParserContext context) throws InputParseException {
        BlockType blockType = null;
        Map<Property<?>, Object> blockStates = new HashMap<>();
        String[] blockAndExtraData = input.trim().split("\\|");
        blockAndExtraData[0] = woolMapper(blockAndExtraData[0]);

        BlockState state = null;

        // Legacy matcher
        if (context.isTryingLegacy()) {
            try {
                String[] split = blockAndExtraData[0].split(":");
                if (split.length == 1) {
                    state = LegacyMapper.getInstance().getBlockFromLegacy(Integer.parseInt(split[0]));
                } else {
                    state = LegacyMapper.getInstance().getBlockFromLegacy(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                }
                if (state != null) {
                    blockType = state.getBlockType();
                }
            } catch (NumberFormatException e) {
            }
        }

        if (state == null) {
            String typeString;
            String stateString = null;
            int stateStart = blockAndExtraData[0].indexOf('[');
            if (stateStart == -1) {
                typeString = blockAndExtraData[0];
            } else {
                typeString = blockAndExtraData[0].substring(0, stateStart);
                stateString = blockAndExtraData[0].substring(stateStart + 1, blockAndExtraData[0].length() - 1);
            }
            if (typeString == null || typeString.isEmpty()) {
                throw new InputParseException("Invalid format");
            }
            String[] stateProperties = EMPTY_STRING_ARRAY;
            if (stateString != null) {
                stateProperties = stateString.split(",");
            }

            if ("hand".equalsIgnoreCase(typeString)) {
                // Get the block type from the item in the user's hand.
                final BaseBlock blockInHand = getBlockInHand(context.requireActor(), HandSide.MAIN_HAND);
                if (blockInHand.getClass() != BaseBlock.class) {
                    return blockInHand;
                }

                blockType = blockInHand.getBlockType();
                blockStates = blockInHand.getStates();
            } else if ("offhand".equalsIgnoreCase(typeString)) {
                // Get the block type from the item in the user's off hand.
                final BaseBlock blockInHand = getBlockInHand(context.requireActor(), HandSide.OFF_HAND);
                if (blockInHand.getClass() != BaseBlock.class) {
                    return blockInHand;
                }

                blockType = blockInHand.getBlockType();
                blockStates = blockInHand.getStates();
            } else if ("pos1".equalsIgnoreCase(typeString)) {
                // Get the block type from the "primary position"
                final World world = context.requireWorld();
                final BlockVector primaryPosition;
                try {
                    primaryPosition = context.requireSession().getRegionSelector(world).getPrimaryPosition();
                } catch (IncompleteRegionException e) {
                    throw new InputParseException("Your selection is not complete.");
                }
                final BlockState blockInHand = world.getBlock(primaryPosition);

                blockType = blockInHand.getBlockType();
                blockStates = blockInHand.getStates();
            } else {
                // Attempt to lookup a block from ID or name.
                blockType = BlockTypes.get(typeString.toLowerCase());

                if (blockType == null) {
                    throw new NoMatchException("Does not match a valid block type: '" + input + "'");
                }
            }

            if (!context.isPreferringWildcard()) {
                // No wildcards allowed => eliminate them. (Start with default state)
                state = blockType.getDefaultState();
            } else {
                state = blockType.getDefaultState().toFuzzy();
                for (Map.Entry<Property<?>, Object> blockState : blockStates.entrySet()) {
                    state = state.with((Property) blockState.getKey(), blockState.getValue());
                }
            }

            state = applyProperties(state, stateProperties);
        }

        // Check if the item is allowed
        if (context.isRestricted()) {
            Actor actor = context.requireActor();
            if (actor != null && !actor.hasPermission("worldedit.anyblock")
                    && worldEdit.getConfiguration().disallowedBlocks.contains(blockType.getId())) {
                throw new DisallowedUsageException("You are not allowed to use '" + input + "'");
            }
        }

        if (blockType == BlockTypes.SIGN || blockType == BlockTypes.WALL_SIGN) {
            // Allow special sign text syntax
            String[] text = new String[4];
            text[0] = blockAndExtraData.length > 1 ? blockAndExtraData[1] : "";
            text[1] = blockAndExtraData.length > 2 ? blockAndExtraData[2] : "";
            text[2] = blockAndExtraData.length > 3 ? blockAndExtraData[3] : "";
            text[3] = blockAndExtraData.length > 4 ? blockAndExtraData[4] : "";
            return new SignBlock(state, text);
        } else if (blockType == BlockTypes.SPAWNER) {
            // Allow setting mob spawn type
            if (blockAndExtraData.length > 1) {
                String mobName = blockAndExtraData[1];
                for (MobType mobType : MobType.values()) {
                    if (mobType.getName().toLowerCase().equals(mobName.toLowerCase())) {
                        mobName = mobType.getName();
                        break;
                    }
                }
                if (!worldEdit.getPlatformManager().queryCapability(Capability.USER_COMMANDS).isValidMobType(mobName)) {
                    throw new NoMatchException("Unknown mob type '" + mobName + "'");
                }
                return new MobSpawnerBlock(state, mobName);
            } else {
                return new MobSpawnerBlock(state, MobType.PIG.getName());
            }
        } else if (blockType == BlockTypes.PLAYER_HEAD || blockType == BlockTypes.PLAYER_WALL_HEAD) {
            // allow setting type/player/rotation
            if (blockAndExtraData.length <= 1) {
                return new SkullBlock(state);
            }

            String type = blockAndExtraData[1];

            return new SkullBlock(state, type.replace(" ", "_")); // valid MC usernames
        } else {
            return state;
        }
    }

}
