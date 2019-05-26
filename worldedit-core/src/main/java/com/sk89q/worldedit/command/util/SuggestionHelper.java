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

package com.sk89q.worldedit.command.util;

import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SuggestionHelper {
    private SuggestionHelper() {
    }

    public static Stream<String> getBlockCategorySuggestions(String tag, boolean allowRandom) {
        final Stream<String> allTags = BlockCategory.REGISTRY.keySet().stream().map(str -> "##" + str);
        if (tag.isEmpty()) {
            return allTags;
        }
        if (tag.startsWith("#")) {
            String key;
            if (tag.startsWith("##")) {
                key = tag.substring(2);
                if (key.isEmpty()) {
                    return allTags;
                }
                boolean anyState = false;
                if (allowRandom && key.charAt(0) == '*') {
                    key = key.substring(1);
                    anyState = true;
                }
                if (key.indexOf(':') < 0) {
                    key = "minecraft:" + key;
                }
                String finalTag = key.toLowerCase(Locale.ROOT);
                final Stream<String> stream = BlockCategory.REGISTRY.keySet().stream().filter(s ->
                        s.startsWith(finalTag));
                return anyState ? stream.map(s -> "##*" + s) : stream.map(s -> "##" + s);
            } else if (tag.length() == 1) {
                return allTags;
            }
        }
        return Stream.empty();
    }

    public static Stream<String> getBlockPropertySuggestions(String blockType, String props) {
        BlockType type = BlockTypes.get(blockType.toLowerCase(Locale.ROOT));
        if (type == null) {
            return Stream.empty();
        }
        final Map<String, ? extends Property<?>> propertyMap = type.getPropertyMap();
        Set<String> matchedProperties = new HashSet<>();
        String[] propParts = props.split(",", -1);
        for (int i = 0; i < propParts.length; i++) {
            String[] propVal = propParts[i].split("=");
            final String matchProp = propVal[0].toLowerCase(Locale.ROOT);
            if (i == propParts.length - 1) {
                // suggest for next property
                String previous = Arrays.stream(propParts, 0, propParts.length - 1).collect(Collectors.joining(","))
                        + (propParts.length == 1 ? "" : ",");
                String lastValidInput = (blockType + "[" + previous).toLowerCase(Locale.ROOT);
                if (propVal.length == 1) {
                    // only property, no value yet
                    final List<? extends Property<?>> matchingProps = propertyMap.entrySet().stream()
                            .filter(p -> !matchedProperties.contains(p.getKey()) && p.getKey().startsWith(matchProp))
                            .map(Map.Entry::getValue).collect(Collectors.toList());
                    switch (matchingProps.size()) {
                        case 0:
                            return propertyMap.keySet().stream().filter(p -> !matchedProperties.contains(p)).map(prop ->
                                    lastValidInput + prop + "=");
                        case 1:
                            return matchingProps.get(0).getValues().stream().map(val ->
                                    lastValidInput +  matchingProps.get(0).getName() + "="
                                            + val.toString().toLowerCase(Locale.ROOT));
                        default:
                            return matchingProps.stream().map(p -> lastValidInput + p.getName() + "=");
                    }
                } else {
                    Property<?> prop = propertyMap.get(matchProp);
                    if (prop == null) {
                        return Stream.empty();
                    }
                    final List<String> values = prop.getValues().stream().map(v -> v.toString().toLowerCase(Locale.ROOT)).collect(Collectors.toList());
                    String matchVal = propVal[1].toLowerCase(Locale.ROOT);
                    List<String> matchingVals = values.stream().filter(val -> val.startsWith(matchVal)).collect(Collectors.toList());
                    if (matchingVals.isEmpty()) {
                        return values.stream().map(val -> lastValidInput + prop.getName() + "=" + val);
                    } else {
                        if (matchingVals.size() == 1 && matchingVals.get(0).equals(matchVal)) {
                            String currProp = lastValidInput + prop.getName() + "=" + matchVal;
                            if (matchingVals.size() < values.size()) {
                                return Stream.of(currProp + "] ", currProp + ",");
                            }
                            return Stream.of(currProp + "] ");
                        }
                        return matchingVals.stream().map(val -> lastValidInput + prop.getName() + "=" + val);
                    }
                }
            } else {
                // validate previous properties
                if (propVal.length != 2) {
                    return Stream.empty();
                }
                Property<?> prop = propertyMap.get(matchProp);
                if (prop == null) {
                    return Stream.empty();
                }
                try {
                    prop.getValueFor(propVal[1]);
                    matchedProperties.add(prop.getName());
                } catch (IllegalArgumentException ignored) {
                    return Stream.empty();
                }
            }
        }
        return Stream.empty();
    }
}
