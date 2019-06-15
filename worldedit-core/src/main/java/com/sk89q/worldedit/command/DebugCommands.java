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

package com.sk89q.worldedit.command;

import com.google.common.collect.ImmutableSet;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.transform.BlockTransformExtent;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import org.enginehub.piston.annotation.Command;
import org.enginehub.piston.annotation.CommandContainer;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@CommandContainer
public class DebugCommands {

    private static final Transform ROTATE_90 = new AffineTransform().rotateY(-90);
    private static final Transform ROTATE_180 = new AffineTransform().rotateY(180);
    private Set<BlockType> ignoreTransform = ImmutableSet.of();

    @Command(
            name = "testtransforms",
            desc = "Test BlockTransformExtent"
    )
    public void testTransforms(Actor actor) {
        AtomicBoolean hadError = new AtomicBoolean(false);
        for (BlockType type : BlockType.REGISTRY.values()) {
            if (ignoreTransform.contains(type)) {
                continue;
            }

            type.getAllStates().forEach(base -> {
                BlockState rotated = base;

                // test full rotation
                for (int i = 0; i < 4; i++) {
                    rotated = BlockTransformExtent.transform(rotated, ROTATE_90);
                }
                assertEquals(base, rotated, "Rotate90x4", hadError);
                // test half rotation
                rotated = BlockTransformExtent.transform(BlockTransformExtent.transform(base, ROTATE_180), ROTATE_180);
                assertEquals(base, rotated, "Rotate180x2", hadError);
            });
        }
        if (hadError.get()) {
            actor.printError("One or more errors found. See log for details.");
        } else {
            actor.print("All 90 degree rotation tests passed.");
        }
    }

    private static void assertEquals(BlockState base, BlockState rotated, String testName, AtomicBoolean hadError) {
        if (base != rotated) {
            hadError.set(true);
            WorldEdit.logger.warn(String.format("%s: expected %s, but got %s", testName, base, rotated));
        }
    }
}
