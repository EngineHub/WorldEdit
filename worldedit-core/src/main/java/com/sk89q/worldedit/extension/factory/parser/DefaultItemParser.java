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

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseItem;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.command.util.SuggestionHelper;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.concurrency.LazyReference;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.item.ItemTypes;
import com.sk89q.worldedit.world.registry.LegacyMapper;
import org.enginehub.linbus.format.snbt.LinStringIO;
import org.enginehub.linbus.stream.exception.NbtParseException;
import org.enginehub.linbus.tree.LinCompoundTag;

import java.util.Locale;
import java.util.stream.Stream;

public class DefaultItemParser extends InputParser<BaseItem> {

    public DefaultItemParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public Stream<String> getSuggestions(String input, ParserContext context) {
        return SuggestionHelper.getNamespacedRegistrySuggestions(ItemType.REGISTRY, input);
    }

    @Override
    public BaseItem parseFromInput(String input, ParserContext context) throws InputParseException {
        ItemType itemType;
        LinCompoundTag itemNbtData = null;

        BaseItem item = null;

        // Legacy matcher
        if (context.isTryingLegacy()) {
            try {
                String[] split = input.split(":");
                if (split.length == 0) {
                    throw new InputParseException(Component.translatable("worldedit.error.parser.invalid-colon"));
                } else if (split.length == 1) {
                    itemType = LegacyMapper.getInstance().getItemFromLegacy(Integer.parseInt(split[0]));
                } else {
                    itemType = LegacyMapper.getInstance().getItemFromLegacy(Integer.parseInt(split[0]), Integer.parseInt(split[1]));
                }
                if (itemType != null) {
                    item = new BaseItem(itemType);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        if (item == null) {
            String typeString;
            String nbtString = null;
            int nbtStart = input.indexOf('{');

            if (nbtStart == -1) {
                typeString = input;
            } else {
                typeString = input.substring(0, nbtStart);
                if (nbtStart + 1 >= input.length()) {
                    throw new InputParseException(Component.translatable("worldedit.error.parser.hanging-lbrace", Component.text(nbtStart)));
                }
                int stateEnd = input.lastIndexOf('}');
                if (stateEnd < 0) {
                    throw new InputParseException(Component.translatable("worldedit.error.parser.missing-rbrace"));
                }
                nbtString = input.substring(nbtStart);
            }

            if ("hand".equalsIgnoreCase(typeString)) {
                BaseItemStack heldItem = getItemInHand(context.requireActor(), HandSide.MAIN_HAND);
                itemType = heldItem.getType();
                itemNbtData = heldItem.getNbt();
            } else if ("offhand".equalsIgnoreCase(typeString)) {
                BaseItemStack heldItem = getItemInHand(context.requireActor(), HandSide.OFF_HAND);
                itemType = heldItem.getType();
                itemNbtData = heldItem.getNbt();
            } else {
                itemType = ItemTypes.get(typeString.toLowerCase(Locale.ROOT));
            }

            if (itemType == null) {
                throw new NoMatchException(Component.translatable("worldedit.error.unknown-item", Component.text(input)));
            }

            if (nbtString != null) {
                LinCompoundTag otherTag;
                try {
                    otherTag = LinStringIO.readFromStringUsing(nbtString, LinCompoundTag::readFrom);
                } catch (NbtParseException e) {
                    throw new NoMatchException(Component.translatable(
                        "worldedit.error.invalid-nbt",
                        Component.text(input),
                        Component.text(e.getMessage())
                    ));
                }
                if (itemNbtData == null) {
                    itemNbtData = otherTag;
                } else {
                    itemNbtData = itemNbtData.toBuilder().putAll(otherTag.value()).build();
                }
            }

            item = new BaseItem(itemType, itemNbtData == null ? null : LazyReference.computed(itemNbtData));
        }

        return item;
    }

    private BaseItemStack getItemInHand(Actor actor, HandSide handSide) throws InputParseException {
        if (actor instanceof Player) {
            return ((Player) actor).getItemInHand(handSide);
        } else {
            throw new InputParseException(Component.translatable(
                    "worldedit.error.parser.player-only",
                    Component.text(handSide == HandSide.MAIN_HAND ? "hand" : "offhand")
            ));
        }
    }

}
