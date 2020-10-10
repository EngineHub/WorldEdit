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

package com.sk89q.worldedit.command.argument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Locatable;
import com.sk89q.worldedit.internal.annotation.Offset;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.MathUtils;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import org.enginehub.piston.CommandManager;
import org.enginehub.piston.converter.ArgumentConverter;
import org.enginehub.piston.converter.ConversionResult;
import org.enginehub.piston.converter.FailedConversion;
import org.enginehub.piston.inject.InjectedValueAccess;
import org.enginehub.piston.inject.Key;

import java.util.List;
import java.util.stream.Collectors;

public class OffsetConverter implements ArgumentConverter<BlockVector3> {

    public static void register(WorldEdit worldEdit, CommandManager commandManager) {
        commandManager.registerConverter(
            Key.of(BlockVector3.class, Offset.class),
            new OffsetConverter(worldEdit)
        );
    }

    private final DirectionVectorConverter directionVectorConverter;
    private final VectorConverter<Integer, BlockVector3> vectorConverter =
        VectorConverter.BLOCK_VECTOR_3_CONVERTER;

    private OffsetConverter(WorldEdit worldEdit) {
        directionVectorConverter = new DirectionVectorConverter(worldEdit, true);
    }

    @Override
    public Component describeAcceptableArguments() {
        return TextComponent.builder()
            .append(directionVectorConverter.describeAcceptableArguments())
            .append(", or ")
            .append(vectorConverter.describeAcceptableArguments())
            .build();
    }

    @Override
    public List<String> getSuggestions(String input, InjectedValueAccess context) {
        if (input.startsWith("^") && context.injectedValue(Key.of(Actor.class))
            .filter(actor -> actor instanceof Locatable).isPresent()) {
            return vectorConverter.getSuggestions(input.substring(1), context).stream()
                .map(s -> "^" + s)
                .collect(Collectors.toList());
        }
        return ImmutableList.copyOf(Iterables.concat(
            directionVectorConverter.getSuggestions(input, context),
            vectorConverter.getSuggestions(input, context)
        ));
    }

    private BlockVector3 rotateToRelative(Location location, BlockVector3 relativeOffset) {
        float pitch = location.getPitch();
        float yaw = location.getYaw();

        // This math was borrowed from the MC codebase, with some changes made for accuracy
        double f = MathUtils.dCos(yaw + 90.0);
        double g = MathUtils.dSin(yaw + 90.0);
        double j = MathUtils.dCos(-pitch + 90.0);
        double k = MathUtils.dSin(-pitch + 90.0);

        Vector3 m1 = location.getDirection();
        Vector3 m2 = Vector3.at(f * j, k, g * j);
        Vector3 m3 = m1.cross(m2).multiply(-1.0D);

        // Create an affine transform of the columns (col4 is empty due to no translation)
        AffineTransform transform = new AffineTransform(
            m1.getX(), m2.getX(), m3.getX(), 0,
            m1.getY(), m2.getY(), m3.getY(), 0,
            m1.getZ(), m2.getZ(), m3.getZ(), 0
        );

        return transform
            .apply(relativeOffset.toVector3())
            // This *MUST* be rounded before converting to block points
            .round().toBlockPoint();
    }

    @Override
    public ConversionResult<BlockVector3> convert(String input, InjectedValueAccess context) {
        if (input.startsWith("^")) {
            try {
                // Looking at a relative vector.
                Actor actor = context.injectedValue(Key.of(Actor.class))
                    .orElseThrow(() -> new IllegalStateException("An actor is required to use relative offsets"));

                if (!(actor instanceof Locatable)) {
                    throw new IllegalStateException("Only a locatable actor may use relative offsets");
                }

                Location location = ((Locatable) actor).getLocation();

                return vectorConverter.convert(input.substring(1), context).map(blockVector3s ->
                    blockVector3s.stream()
                        .map(vector -> rotateToRelative(location, vector))
                        .collect(Collectors.toList())
                );
            } catch (IllegalStateException e) {
                return FailedConversion.from(e);
            }
        } else {
            return directionVectorConverter.convert(input, context)
                .orElse(vectorConverter.convert(input, context));
        }
    }
}
