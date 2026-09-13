/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.extension.factory.parser;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMap;
import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.NotABlockException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.MobSpawnerBlock;
import com.sk89q.worldedit.blocks.SignBlock;
import com.sk89q.worldedit.blocks.SkullBlock;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.DisallowedUsageException;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.internal.util.DeprecationUtil;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.block.FuzzyBlockState;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.entity.EntityTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.enginehub.linbus.format.snbt.LinStringIO;
import org.enginehub.linbus.stream.exception.NbtParseException;
import org.enginehub.linbus.tree.LinCompoundTag;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

import static org.enginehub.piston.converter.SuggestionHelper.byPrefix;

/**
 * Parses block input strings.
 */
public class DefaultBlockParser extends InputParser<BaseBlock> {

    public DefaultBlockParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    private static BaseBlock getBlockInHand(Actor actor, HandSide handSide) throws InputParseException {
        if (actor instanceof Player player) {
            try {
                return player.getBlockInHand(handSide);
            } catch (NotABlockException e) {
                throw new InputParseException(e.getRichMessage());
            } catch (WorldEditException e) {
                throw new InputParseException(TranslatableComponent.of("worldedit.error.unknown", e.getRichMessage()), e);
            }
        } else {
            throw new InputParseException(TranslatableComponent.of(
                    "worldedit.error.parser.player-only",
                    TextComponent.of(handSide == HandSide.MAIN_HAND ? "hand" : "offhand")
            ));
        }
    }

    @Override
    public BaseBlock parseFromInput(String input, ParserContext context)
            throws InputParseException {
        return parseLogic(input, context);
    }

    private static final String[] EMPTY_STRING_ARRAY = {};
    private static final CharMatcher EXTRA_DATA_DELIMITERS = CharMatcher.anyOf("|;");

    private static String[] splitExtraData(String input) {
        List<String> parts = StringUtil.splitOutsideBrackets(input, EXTRA_DATA_DELIMITERS);
        int end = parts.size();
        while (end > 1 && parts.get(end - 1).isEmpty()) {
            end--;
        }
        return parts.subList(0, end).toArray(EMPTY_STRING_ARRAY);
    }

    /**
     * Backwards compatibility for wool colours in block syntax.
     *
     * @param string Input string
     * @return Mapped string
     */
    @SuppressWarnings("ConstantConditions")
    private String woolMapper(String string) {
        return switch (string.toLowerCase(Locale.ROOT)) {
            case "white" -> BlockTypes.WHITE_WOOL.id();
            case "black" -> BlockTypes.BLACK_WOOL.id();
            case "blue" -> BlockTypes.BLUE_WOOL.id();
            case "brown" -> BlockTypes.BROWN_WOOL.id();
            case "cyan" -> BlockTypes.CYAN_WOOL.id();
            case "gray", "grey" -> BlockTypes.GRAY_WOOL.id();
            case "green" -> BlockTypes.GREEN_WOOL.id();
            case "light_blue", "lightblue" -> BlockTypes.LIGHT_BLUE_WOOL.id();
            case "light_gray", "light_grey", "lightgray", "lightgrey" -> BlockTypes.LIGHT_GRAY_WOOL.id();
            case "lime" -> BlockTypes.LIME_WOOL.id();
            case "magenta" -> BlockTypes.MAGENTA_WOOL.id();
            case "orange" -> BlockTypes.ORANGE_WOOL.id();
            case "pink" -> BlockTypes.PINK_WOOL.id();
            case "purple" -> BlockTypes.PURPLE_WOOL.id();
            case "yellow" -> BlockTypes.YELLOW_WOOL.id();
            case "red" -> BlockTypes.RED_WOOL.id();
            default -> string;
        };
    }

    private static Map<Property<?>, Object> parseProperties(BlockType type, String[] stateProperties, ParserContext context) throws InputParseException {
        Map<Property<?>, Object> blockStates = new HashMap<>();

        // Parse the block data (optional)
        for (String parseableData : stateProperties) {
            try {
                String[] parts = parseableData.split("=", 0);
                if (parts.length != 2) {
                    throw new InputParseException(
                            TranslatableComponent.of("worldedit.error.parser.bad-state-format",
                            TextComponent.of(parseableData))
                    );
                }

                @SuppressWarnings("unchecked")
                Property<Object> propertyKey = (Property<Object>) type.getPropertyMap().get(parts[0]);
                if (propertyKey == null) {
                    if (context.getActor() != null) {
                        throw new NoMatchException(TranslatableComponent.of(
                                "worldedit.error.parser.unknown-property",
                                TextComponent.of(parts[0]),
                                TextComponent.of(type.id())
                        ));
                    } else {
                        WorldEdit.logger.debug("Unknown property " + parts[0] + " for block " + type.id());
                    }
                    return Map.of();
                }
                if (blockStates.containsKey(propertyKey)) {
                    throw new InputParseException(TranslatableComponent.of(
                            "worldedit.error.parser.duplicate-property",
                            TextComponent.of(parts[0])
                    ));
                }
                Object value;
                try {
                    value = propertyKey.getValueFor(parts[1]);
                } catch (IllegalArgumentException e) {
                    throw new NoMatchException(TranslatableComponent.of(
                            "worldedit.error.parser.unknown-value",
                            TextComponent.of(parts[1]),
                            TextComponent.of(propertyKey.name())
                    ));
                }

                blockStates.put(propertyKey, value);
            } catch (InputParseException e) {
                throw e; // Pass-through
            } catch (Exception e) {
                throw new InputParseException(TranslatableComponent.of(
                        "worldedit.error.parser.bad-state-format",
                        TextComponent.of(parseableData)
                ));
            }
        }

        return ImmutableMap.copyOf(blockStates);
    }

    @Override
    public Stream<String> getSuggestions(String input, ParserContext context) {
        final int idx = input.lastIndexOf('[');
        if (idx < 0) {
            Stream<String> additionalSuggestions = context.getActor() != null && context.getActor().isPlayer()
                    ? Stream.of("pos1", "hand", "offhand")
                    : Stream.of("pos1");

            return Stream.concat(
                    additionalSuggestions.filter(byPrefix(input)),
                    SuggestionHelper.getNamespacedRegistrySuggestions(BlockType.REGISTRY, input)
            );
        }
        String blockType = input.substring(0, idx);
        BlockType type = BlockTypes.get(blockType.toLowerCase(Locale.ROOT));
        if (type == null) {
            var lowerBlockType = blockType.toLowerCase(Locale.ROOT);
            switch (lowerBlockType) {
                case "hand", "offhand" -> {
                    var actor = context.getActor();
                    if (actor instanceof Player player) {
                        var itemInHand = player.getItemInHand(lowerBlockType.equals("hand") ? HandSide.MAIN_HAND : HandSide.OFF_HAND);
                        if (itemInHand.getType().hasBlockType()) {
                            type = itemInHand.getType().getBlockType();
                        }
                    }
                }
                case "pos1" -> {
                    // Get the block type from the "primary position"
                    World world = context.getWorld();
                    LocalSession session = context.getSession();
                    if (world != null && session != null) {
                        try {
                            BlockVector3 primaryPosition = session.getRegionSelector(world).getPrimaryPosition();
                            type = world.getBlock(primaryPosition).getBlockType();
                        } catch (IncompleteRegionException ignored) {
                            // We don't care if the region is incomplete here, we'll just not provide suggestions
                        }
                    }
                }
                default -> {
                }
            }

            if (type == null) {
                return Stream.empty();
            }
        }

        String props = input.substring(idx + 1);
        if (props.isEmpty()) {
            return type.getProperties().stream().map(p -> input + p.name() + "=");
        }

        return SuggestionHelper.getBlockPropertySuggestions(blockType, type, props);
    }

    private BaseBlock parseLogic(String input, ParserContext context) throws InputParseException {
        BlockType blockType = null;
        Map<Property<?>, Object> blockStates = new HashMap<>();
        String[] blockAndExtraData = splitExtraData(input.trim());
        if (blockAndExtraData.length == 0) {
            throw new NoMatchException(TranslatableComponent.of("worldedit.error.unknown-block", TextComponent.of(input)));
        }
        if (context.isTryingLegacy()) {
            // Perform a legacy wool colour mapping
            blockAndExtraData[0] = woolMapper(blockAndExtraData[0]);
        }

        BlockState state = null;
        LinCompoundTag blockNbtData = null;

        // Legacy matcher
        if (context.isTryingLegacy()) {
            try {
                String[] split = blockAndExtraData[0].split(":", 2);
                if (split.length == 0) {
                    throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.invalid-colon"));
                } else if (split.length == 1) {
                    var legacyTypeId = Integer.parseInt(split[0]);
                    state = LegacyMapper.getInstance().getBlockFromLegacy(legacyTypeId);
                } else {
                    var legacyTypeId = Integer.parseInt(split[0]);
                    var legacyDataValue = Integer.parseInt(split[1]);
                    state = LegacyMapper.getInstance().getBlockFromLegacy(legacyTypeId, legacyDataValue);
                }
                if (state != null) {
                    blockType = state.getBlockType();
                }
            } catch (NumberFormatException ignored) {
                // If it doesn't match legacy, just parse it normally
            }
        }

        if (state == null) {
            String blockString = blockAndExtraData[0];
            int cursor = 0;
            // Advance cursor until we hit a state or NBT data, or the end of the string
            while (cursor < blockString.length() && blockString.charAt(cursor) != '[' && blockString.charAt(cursor) != '{') {
                cursor++;
            }
            String typeString = blockString.substring(0, cursor);

            // Parse state string first
            String stateString = null;
            if (cursor < blockString.length() && blockString.charAt(cursor) == '[') {
                if (cursor + 1 >= blockString.length()) {
                    throw new InputParseException(TranslatableComponent.of(
                        "worldedit.error.parser.hanging-lbracket", TextComponent.of(cursor)
                    ));
                }
                int stateEnd = StringUtil.findMatchingCloseBracket(blockString, cursor);
                if (stateEnd < 0) {
                    throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.missing-rbracket"));
                }
                stateString = blockString.substring(cursor + 1, stateEnd);
                cursor = stateEnd + 1;
            }

            // Parse NBT data next
            String nbtString = null;
            if (cursor < blockString.length() && blockString.charAt(cursor) == '{') {
                if (cursor + 1 >= blockString.length()) {
                    throw new InputParseException(TranslatableComponent.of(
                        "worldedit.error.parser.hanging-lbrace", TextComponent.of(cursor)
                    ));
                }
                int nbtEnd = StringUtil.findMatchingCloseBracket(blockString, cursor);
                if (nbtEnd < 0) {
                    throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.missing-rbrace"));
                }
                nbtString = blockString.substring(cursor, nbtEnd + 1);
                cursor = nbtEnd + 1;
            }

            // Validate that there is no more input after sections we found
            if (cursor < blockString.length()) {
                char next = blockString.charAt(cursor);
                String key;
                if (next == '[') {
                    key = stateString == null
                        ? "worldedit.error.parser.state-after-nbt"
                        : "worldedit.error.parser.duplicate-state";
                } else if (next == '{') {
                    key = "worldedit.error.parser.duplicate-nbt";
                } else {
                    key = "worldedit.error.parser.trailing-input";
                }
                throw new NoMatchException(TranslatableComponent.of(
                    key, TextComponent.of(blockString.substring(cursor))
                ));
            }

            if (typeString.isEmpty()) {
                throw new InputParseException(TranslatableComponent.of(
                    "worldedit.error.parser.missing-type",
                    TextComponent.of(blockString)
                ));
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
                blockStates.putAll(blockInHand.getStates());
                blockNbtData = blockInHand.getNbt();
            } else if ("offhand".equalsIgnoreCase(typeString)) {
                // Get the block type from the item in the user's off hand.
                final BaseBlock blockInHand = getBlockInHand(context.requireActor(), HandSide.OFF_HAND);
                if (blockInHand.getClass() != BaseBlock.class) {
                    return blockInHand;
                }

                blockType = blockInHand.getBlockType();
                blockStates.putAll(blockInHand.getStates());
                blockNbtData = blockInHand.getNbt();
            } else if ("pos1".equalsIgnoreCase(typeString)) {
                // Get the block type from the "primary position"
                final World world = context.requireWorld();
                final BlockVector3 primaryPosition;
                try {
                    primaryPosition = context.requireSession().getRegionSelector(world).getPrimaryPosition();
                } catch (IncompleteRegionException e) {
                    throw new InputParseException(TranslatableComponent.of("worldedit.error.incomplete-region"));
                }
                final BaseBlock blockInHand = world.getFullBlock(primaryPosition);

                blockType = blockInHand.getBlockType();
                blockStates.putAll(blockInHand.getStates());
                blockNbtData = blockInHand.getNbt();
            } else {
                // Attempt to lookup a block from ID or name.
                blockType = BlockTypes.get(typeString.toLowerCase(Locale.ROOT));
            }

            if (blockType == null) {
                throw new NoMatchException(TranslatableComponent.of("worldedit.error.unknown-block", TextComponent.of(input)));
            }

            blockStates.putAll(parseProperties(blockType, stateProperties, context));

            if (context.isPreferringWildcard()) {
                FuzzyBlockState.Builder fuzzyBuilder = FuzzyBlockState.builder();
                fuzzyBuilder.type(blockType);
                for (Map.Entry<Property<?>, Object> blockState : blockStates.entrySet()) {
                    @SuppressWarnings("unchecked")
                    Property<Object> objProp = (Property<Object>) blockState.getKey();
                    fuzzyBuilder.withProperty(objProp, blockState.getValue());
                }
                state = fuzzyBuilder.build();
            } else {
                // No wildcards allowed => eliminate them. (Start with default state)
                state = blockType.getDefaultState();
                for (Map.Entry<Property<?>, Object> blockState : blockStates.entrySet()) {
                    @SuppressWarnings("unchecked")
                    Property<Object> objProp = (Property<Object>) blockState.getKey();
                    state = state.with(objProp, blockState.getValue());
                }
            }

            if (nbtString != null) {
                LinCompoundTag otherTag;
                try {
                    otherTag = LinStringIO.readFromStringUsing(nbtString, LinCompoundTag::readFrom);
                } catch (NbtParseException e) {
                    throw new NoMatchException(TranslatableComponent.of(
                        "worldedit.error.parser.invalid-nbt",
                        TextComponent.of(input),
                        TextComponent.of(e.getMessage())
                    ));
                }
                if (blockNbtData == null) {
                    blockNbtData = otherTag;
                } else {
                    blockNbtData = blockNbtData.toBuilder().putAll(otherTag.value()).build();
                }
            }
        }
        // this should be impossible but IntelliJ isn't that smart
        if (blockType == null) {
            throw new NoMatchException(TranslatableComponent.of("worldedit.error.unknown-block", TextComponent.of(input)));
        }

        // Check if the item is allowed
        if (context.isRestricted()) {
            Actor actor = context.requireActor();
            if (actor != null && !actor.hasPermission("worldedit.anyblock")
                    && worldEdit.getConfiguration().disallowedBlocks.contains(blockType.id())) {
                throw new DisallowedUsageException(TranslatableComponent.of("worldedit.error.disallowed-block", TextComponent.of(input)));
            }
        }

        BaseBlock baseBlock = state.toBaseBlock(blockNbtData == null ? null : LazyReference.computed(blockNbtData));

        if (blockNbtData != null && blockAndExtraData.length > 1) {
            throw new InputParseException(TranslatableComponent.of(
                "worldedit.error.parser.nbt-with-extra-data",
                TextComponent.of(blockAndExtraData[1])
            ));
        }

        if (!context.isTryingLegacy() || blockNbtData != null) {
            return baseBlock;
        }

        if (DeprecationUtil.isSign(blockType) && blockAndExtraData.length > 1) {
            // Allow special sign text syntax
            String[] text = new String[4];
            text[0] = blockAndExtraData[1];
            text[1] = blockAndExtraData.length > 2 ? blockAndExtraData[2] : "";
            text[2] = blockAndExtraData.length > 3 ? blockAndExtraData[3] : "";
            text[3] = blockAndExtraData.length > 4 ? blockAndExtraData[4] : "";
            @SuppressWarnings("deprecation")
            SignBlock signBlock = new SignBlock(state, text);
            return signBlock;
        } else if (blockType == BlockTypes.SPAWNER) {
            // Allow setting mob spawn type
            String mobName;
            if (blockAndExtraData.length > 1) {
                mobName = blockAndExtraData[1];
                EntityType ent = EntityTypes.get(mobName.toLowerCase(Locale.ROOT));
                if (ent == null) {
                    throw new NoMatchException(TranslatableComponent.of("worldedit.error.unknown-entity", TextComponent.of(mobName)));
                }
                mobName = ent.id();
                if (!worldEdit.getPlatformManager().queryCapability(Capability.USER_COMMANDS).isValidMobType(mobName)) {
                    throw new NoMatchException(TranslatableComponent.of("worldedit.error.unknown-mob", TextComponent.of(mobName)));
                }
            } else {
                mobName = EntityTypes.PIG.id();
            }
            @SuppressWarnings("deprecation")
            MobSpawnerBlock mobSpawnerBlock = new MobSpawnerBlock(state, mobName);
            return mobSpawnerBlock;
        } else if (blockType == BlockTypes.PLAYER_HEAD || blockType == BlockTypes.PLAYER_WALL_HEAD) {
            // allow setting type/player/rotation
            if (blockAndExtraData.length == 1) {
                @SuppressWarnings("deprecation")
                SkullBlock skullBlock = new SkullBlock(state);
                return skullBlock;
            }

            String type = blockAndExtraData[1];

            @SuppressWarnings("deprecation")
            SkullBlock skullBlock = new SkullBlock(state, type.replace(" ", "_")); // valid MC usernames
            return skullBlock;
        } else {
            return baseBlock;
        }
    }

}
