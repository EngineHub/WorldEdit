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

package com.sk89q.worldedit.extension.factory.parser.pattern;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.NoMatchException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.buffer.ExtentBuffer;
import com.sk89q.worldedit.function.pattern.*;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.block.BlockState;
import org.enginehub.linbus.format.snbt.LinStringIO;
import org.enginehub.linbus.stream.exception.NbtParseException;
import org.enginehub.linbus.tree.LinCompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


public class PartiallyApplyingPatternParser extends InputParser<Pattern> {

    boolean compatibilityMode = false;

    public PartiallyApplyingPatternParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    protected PartiallyApplyingPatternParser(WorldEdit worldEdit, boolean compatibilityMode) {
        super(worldEdit);
        this.compatibilityMode = compatibilityMode;
    }

    @Override
    public Stream<String> getSuggestions(String input, ParserContext context) {
        if (input.isEmpty()) {
            return Stream.of("^");
        }
        if (!input.startsWith("^")) {
            return Stream.empty();
        }
        input = input.substring(1);

        if (input.isEmpty()) {
            //define properties, nbt or a type
            return Stream.concat(
                    Stream.of("^[", "^{", "^{,"),
                    worldEdit.getBlockFactory().getSuggestions(input, context)
                            .stream()
                            .map(s -> "^" + s)
            );
        }

        PartiallyApplyingComponents components = split(input);

        if (!components.nbt().isEmpty()) {
            if (!components.type().isEmpty() && !components.properties().isEmpty()) {
                //all of them are defined, we suggest like we would without ^
                return worldEdit.getBlockFactory().getSuggestions(input, context)
                        .stream()
                        .map(s -> "^" + s);
            }
            if (!components.type().isEmpty()) {
                //type and nbt. We currently don't support nbt hints, so nothing to suggest
                return Stream.empty();
            }
            if (!components.properties().isEmpty()) {
                //properties and nbt. We can't figure out possible nbt without type
                return Stream.empty();
            }
        }

        if (!components.properties().isEmpty()) {
            if (!components.type().isEmpty()) {
                //type and properties are defined, we suggest like we would without ^
                return worldEdit.getBlockFactory().getSuggestions(input, context)
                        .stream()
                        .map(s -> "^" + s);
            }
            return Stream.empty(); // without knowing a type, we can't really suggest states
        }
        //only type is defined, we suggest like we would without ^
        return worldEdit.getBlockFactory().getSuggestions(input, context)
                .stream()
                .map(s -> "^" + s);
    }

    private @NotNull PartiallyApplyingComponents split(String input) {
        String type;
        String properties = "";
        //default as delete NBT retains previous behaviour
        String nbt = compatibilityMode ? "{=}" : "";

        int startProperties = input.indexOf('[');
        int startNbt = input.indexOf('{');

        if (startProperties >= 0 && startNbt >= 0) {
            //properties and nbt and maybe type
            type = input.substring(0, startProperties);
            properties = input.substring(startProperties, startNbt);
            nbt = input.substring(startNbt);
        } else if (startProperties >= 0) {
            //properties and maybe type
            type = input.substring(0, startProperties);
            properties = input.substring(startProperties);
        } else if (startNbt >= 0) {
            //nbt and maybe type
            type = input.substring(0, startNbt);
            nbt = input.substring(startNbt);
        } else {
            type = input;
        }
        return new PartiallyApplyingComponents(type, properties, nbt);
    }

    private record PartiallyApplyingComponents(String type, String properties, String nbt) {
    }

    @Override
    public Pattern parseFromInput(String input, ParserContext context) throws InputParseException {
        if (!input.startsWith("^")) {
            return null;
        }
        Extent extent = context.requireExtent();
        input = input.substring(1);

        if (input.isEmpty()) {
            throw new NoMatchException(TranslatableComponent.of("worldedit.error.unknown-block", TextComponent.of(input)));
        }

        PartiallyApplyingComponents components = split(input);

        List<ExtendPatternFactory> extendPatternFactories = new ArrayList<>();

        if (!components.nbt().isEmpty()) {
            extendPatternFactories
                    .add(getNbtApplyingPatternFactory(input, components.nbt()));
        }
        if (!components.type().isEmpty()) {
            extendPatternFactories
                    .add(getTypeApplyingPatternFactory(context, components.type()));
        }
        if (!components.properties().isEmpty()) {
            extendPatternFactories
                    .add(getStateApplyingPatternFactory(components));
        }

        if (extendPatternFactories.size() > 1) {
            Extent buffer = new ExtentBuffer(extent);
            Pattern[] patterns = extendPatternFactories.stream()
                    .map(factory -> factory.forExtend(buffer))
                    .toArray(Pattern[]::new);
            return new ExtentBufferedCompositePattern(buffer, patterns);
        }

        return extendPatternFactories.getFirst().forExtend(extent);

    }

    private @NotNull ExtendPatternFactory getTypeApplyingPatternFactory(ParserContext context, String type) throws InputParseException {
        BlockState blockState = worldEdit.getBlockFactory()
                .parseFromInput(type, context).getBlockType().getDefaultState();
        return ext -> new TypeApplyingPattern(ext, blockState);
    }

    private static @NotNull ExtendPatternFactory getStateApplyingPatternFactory(PartiallyApplyingComponents components) throws InputParseException {
        String properties = components.properties();
        if (!properties.endsWith("]")) {
            throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.missing-rbracket"));
        }
        String propertiesWithoutBrackets = properties.substring(1, properties.length() - 1);
        final String[] states = propertiesWithoutBrackets.split(",");
        Map<String, String> statesToSet = new HashMap<>();
        for (String state : states) {
            if (state.isEmpty()) {
                throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.empty-state"));
            }
            String[] propVal = state.split("=", 2);
            if (propVal.length != 2) {
                throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.missing-equals-separator"));
            }
            final String prop = propVal[0];
            if (prop.isEmpty()) {
                throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.empty-property"));
            }
            final String value = propVal[1];
            if (value.isEmpty()) {
                throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.empty-value"));
            }
            if (statesToSet.put(prop, value) != null) {
                throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.duplicate-property", TextComponent.of(prop)));
            }
        }
        return ext -> new StateApplyingPattern(ext, statesToSet);
    }

    private static @NotNull ExtendPatternFactory getNbtApplyingPatternFactory(String input, String nbt) throws InputParseException {
        if (!nbt.endsWith("}")) {
            throw new InputParseException(TranslatableComponent.of("worldedit.error.parser.missing-rbrace"));
        }
        if (nbt.equals("{}")) {
            return (ext) -> new NBTApplyingPattern(ext, null);
        }
        boolean merge = true;
        if (nbt.startsWith("{=")) {
            merge = false;
            nbt = "{" + nbt.substring(2);
        }
        LinCompoundTag tag;
        try {
            if (nbt.equals("{}")) {
                tag = LinCompoundTag.builder().build();
            } else {
                tag = LinStringIO.readFromStringUsing(nbt, LinCompoundTag::readFrom);
            }
        } catch (NbtParseException e) {
            throw new NoMatchException(TranslatableComponent.of(
                    "worldedit.error.parser.invalid-nbt",
                    TextComponent.of("^" + input),
                    TextComponent.of(e.getMessage())
            ));
        }
        if (merge) {
            return (ext) -> new NBTMergingPattern(ext, tag.value());
        } else {
            return (ext) -> new NBTApplyingPattern(ext, tag);
        }
    }

    private interface ExtendPatternFactory {
        Pattern forExtend(Extent e);
    }

}
