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

package com.sk89q.worldedit.extent.transform;

import com.sk89q.worldedit.BaseWorldEditTest;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.test.TestBlockRegistry;
import com.sk89q.worldedit.util.test.TestRegistries;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Checks that transforming blocks is reversible for every state of every block in the test data.
 */
class BlockTransformExtentTest extends BaseWorldEditTest {

    private static final Transform ROTATE_90 = new AffineTransform().rotateY(-90);
    private static final Transform ROTATE_NEG_90 = new AffineTransform().rotateY(90);
    private static final Transform FLIP_X = new AffineTransform().scale(-1, 1, 1);
    private static final Transform VERTICAL_FLIP = new AffineTransform().scale(1, -1, 1);
    private static final Transform FLIP_Z = new AffineTransform().scale(1, 1, -1);
    private static final List<Transform> FLIPS = List.of(FLIP_X, VERTICAL_FLIP, FLIP_Z);

    @BeforeAll
    static void registerBlocks() {
        TestRegistries.registerBlocks();
    }

    @AfterAll
    static void unregisterBlocks() {
        TestRegistries.unregisterBlocks();
    }

    static Stream<String> blockIds() {
        return TestBlockRegistry.blockIds().stream();
    }

    private static BlockType type(String id) {
        return Objects.requireNonNull(BlockType.REGISTRY.get(id), id);
    }

    private static BlockState transformTimes(BlockState state, Transform transform, int times) {
        BlockState result = state;
        for (int i = 0; i < times; i++) {
            result = BlockTransformExtent.transform(result, transform);
        }
        return result;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blockIds")
    void fourQuarterTurnsAreIdentity(String id) {
        for (BlockState base : type(id).getAllStates()) {
            assertEquals(base, transformTimes(base, ROTATE_90, 4), () -> "4x +90 degrees: " + base);
            assertEquals(base, transformTimes(base, ROTATE_NEG_90, 4), () -> "4x -90 degrees: " + base);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blockIds")
    void rotationThenInverseRotationIsIdentity(String id) {
        for (BlockState base : type(id).getAllStates()) {
            BlockState rotated = BlockTransformExtent.transform(base, ROTATE_90);
            assertEquals(base, BlockTransformExtent.transform(rotated, ROTATE_90.inverse()),
                () -> "+90 then inverse: " + base);
            assertEquals(base, BlockTransformExtent.transform(rotated, ROTATE_NEG_90),
                () -> "+90 then -90: " + base);

            BlockState rotatedBack = BlockTransformExtent.transform(base, ROTATE_NEG_90);
            assertEquals(base, BlockTransformExtent.transform(rotatedBack, ROTATE_90),
                () -> "-90 then +90: " + base);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blockIds")
    void flippingTwiceIsIdentity(String id) {
        for (BlockState base : type(id).getAllStates()) {
            for (Transform flip : FLIPS) {
                if (id.equals("minecraft:hopper") && flip == VERTICAL_FLIP) {
                    // See downFacingHopperSurvivesVerticalFlip
                    continue;
                }
                assertEquals(base, transformTimes(base, flip, 2), () -> "flip twice: " + base + " with " + flip);
            }
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("blockIds")
    void transformKeepsBlockTypeAndIsValidState(String id) {
        BlockType type = type(id);
        for (BlockState base : type.getAllStates()) {
            for (Transform transform : List.of(ROTATE_90, ROTATE_NEG_90, FLIP_X, VERTICAL_FLIP, FLIP_Z)) {
                BlockState result = BlockTransformExtent.transform(base, transform);
                assertNotNull(result);
                assertEquals(type, result.getBlockType());
                assertEquals(type.getPropertyMap().size(), result.getStates().size());
            }
        }
    }

    /**
     * Builds a state from a block ID and {@code name=value} pairs, leaving other properties at their defaults.
     */
    @SuppressWarnings("unchecked")
    private static BlockState state(String id, String... properties) {
        BlockType type = type(id);
        BlockState state = type.getDefaultState();
        for (String property : properties) {
            String[] parts = property.split("=", 2);
            Property<Object> prop = Objects.requireNonNull(type.getProperty(parts[0]), property);
            Object value = prop.values().stream()
                .filter(v -> v.toString().equalsIgnoreCase(parts[1]))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No value " + parts[1] + " for " + parts[0]));
            state = state.with(prop, value);
        }
        return state;
    }

    private static Arguments transforms(Transform transform, BlockState before, BlockState after) {
        return Arguments.of(Named.of(before + " -> " + after, transform), before, after);
    }

    static Stream<Arguments> knownTransforms() {
        return Stream.of(
            // stairs
            transforms(FLIP_X, state("minecraft:oak_stairs", "facing=east", "shape=inner_left"),
                state("minecraft:oak_stairs", "facing=west", "shape=inner_right")),
            transforms(FLIP_Z, state("minecraft:oak_stairs", "facing=north", "shape=outer_right"),
                state("minecraft:oak_stairs", "facing=south", "shape=outer_left")),
            transforms(VERTICAL_FLIP, state("minecraft:oak_stairs", "facing=north", "half=bottom", "shape=inner_left"),
                state("minecraft:oak_stairs", "facing=north", "half=top", "shape=inner_left")),
            // slabs and chests share the "type" property but flip along different axes
            transforms(VERTICAL_FLIP, state("minecraft:oak_slab", "type=bottom"), state("minecraft:oak_slab", "type=top")),
            transforms(VERTICAL_FLIP, state("minecraft:oak_slab", "type=double"), state("minecraft:oak_slab", "type=double")),
            transforms(FLIP_X, state("minecraft:oak_slab", "type=bottom"), state("minecraft:oak_slab", "type=bottom")),
            transforms(FLIP_X, state("minecraft:chest", "facing=east", "type=left"),
                state("minecraft:chest", "facing=west", "type=right")),
            transforms(FLIP_Z, state("minecraft:chest", "facing=north", "type=right"),
                state("minecraft:chest", "facing=south", "type=left")),
            transforms(VERTICAL_FLIP, state("minecraft:chest", "facing=north", "type=left"),
                state("minecraft:chest", "facing=north", "type=left")),
            // doors, trapdoors, torches and other single-facing blocks
            transforms(VERTICAL_FLIP, state("minecraft:oak_trapdoor", "half=bottom"), state("minecraft:oak_trapdoor", "half=top")),
            transforms(FLIP_X, state("minecraft:wall_torch", "facing=east"), state("minecraft:wall_torch", "facing=west")),
            transforms(ROTATE_90, state("minecraft:wall_torch", "facing=north"), state("minecraft:wall_torch", "facing=east")),
            transforms(ROTATE_NEG_90, state("minecraft:wall_torch", "facing=north"), state("minecraft:wall_torch", "facing=west")),
            transforms(ROTATE_90, state("minecraft:piston", "facing=up"), state("minecraft:piston", "facing=up")),
            transforms(VERTICAL_FLIP, state("minecraft:piston", "facing=up"), state("minecraft:piston", "facing=down")),
            // axis
            transforms(ROTATE_90, state("minecraft:oak_log", "axis=x"), state("minecraft:oak_log", "axis=z")),
            transforms(ROTATE_90, state("minecraft:oak_log", "axis=z"), state("minecraft:oak_log", "axis=x")),
            transforms(ROTATE_90, state("minecraft:oak_log", "axis=y"), state("minecraft:oak_log", "axis=y")),
            transforms(FLIP_X, state("minecraft:oak_log", "axis=x"), state("minecraft:oak_log", "axis=x")),
            // rails
            transforms(ROTATE_90, state("minecraft:rail", "shape=north_south"), state("minecraft:rail", "shape=east_west")),
            transforms(VERTICAL_FLIP, state("minecraft:rail", "shape=ascending_east"),
                state("minecraft:rail", "shape=ascending_west")),
            transforms(ROTATE_90, state("minecraft:powered_rail", "shape=ascending_north"),
                state("minecraft:powered_rail", "shape=ascending_east")),
            // connections move with the block
            transforms(FLIP_Z, state("minecraft:oak_fence", "north=true"),
                state("minecraft:oak_fence", "south=true")),
            transforms(ROTATE_90, state("minecraft:oak_fence", "north=true", "east=true"),
                state("minecraft:oak_fence", "east=true", "south=true")),
            // standing signs use a 16-step rotation
            transforms(ROTATE_90, state("minecraft:oak_sign", "rotation=0"), state("minecraft:oak_sign", "rotation=4")),
            transforms(ROTATE_NEG_90, state("minecraft:oak_sign", "rotation=0"), state("minecraft:oak_sign", "rotation=12")),
            transforms(FLIP_X, state("minecraft:oak_sign", "rotation=4"), state("minecraft:oak_sign", "rotation=12")),
            // crafters
            transforms(VERTICAL_FLIP, state("minecraft:crafter", "orientation=up_north"),
                state("minecraft:crafter", "orientation=down_north")),
            transforms(ROTATE_90, state("minecraft:crafter", "orientation=north_up"),
                state("minecraft:crafter", "orientation=east_up")),
            // blocks without directional properties are untouched
            transforms(ROTATE_90, state("minecraft:stone"), state("minecraft:stone")),
            transforms(FLIP_X, state("minecraft:redstone_torch", "lit=true"), state("minecraft:redstone_torch", "lit=true"))
        );
    }

    @ParameterizedTest
    @MethodSource("knownTransforms")
    void transformsKnownStates(Transform transform, BlockState before, BlockState after) {
        assertEquals(after, BlockTransformExtent.transform(before, transform));
    }

    @Test
    void identityTransformReturnsTheSameBlock() {
        BlockState state = state("minecraft:oak_stairs", "facing=east", "half=top");
        assertSame(state, BlockTransformExtent.transform(state, new AffineTransform()));
    }

    @Test
    @Disabled("Bug: BlockTransformExtent.transform throws a NullPointerException for rails when the transform is a "
        + "rotation about the X or Z axis. Input: minecraft:rail[shape=north_south] with rotateX(90): "
        + "Direction.findClosest(.., CARDINAL) returns null for the vertical result and newStart.toString() NPEs. "
        + "Expected: no exception (the shape is left alone or mapped to a valid ascending shape).")
    void railRotatedAboutHorizontalAxisDoesNotThrow() {
        BlockState rail = state("minecraft:rail", "shape=north_south");
        assertDoesNotThrow(() -> BlockTransformExtent.transform(rail, new AffineTransform().rotateX(90)));
    }

    @Test
    @Disabled("Bug: BlockTransformExtent.transform loses the 'top' half of a crafter's orientation when it points "
        + "down. Input: minecraft:crafter[orientation=down_east] with rotateZ(-90) gives west_up, and undoing it "
        + "with rotateZ(90) gives down_west instead of down_east, because newTop DOWN is replaced by UP "
        + "(BlockTransformExtent, 'orientation' branch).")
    void crafterRotationAboutZIsReversible() {
        BlockState crafter = state("minecraft:crafter", "orientation=down_east");
        Transform rotation = new AffineTransform().rotateZ(-90);
        BlockState rotated = BlockTransformExtent.transform(crafter, rotation);
        assertEquals(crafter, BlockTransformExtent.transform(rotated, rotation.inverse()));
    }

    @Test
    @Disabled("Bug (or at least a surprising result): a vertical flip turns a down-facing hopper sideways. "
        + "Input: minecraft:hopper[facing=down] with scale(1, -1, 1) gives facing=east, because UP is not an allowed "
        + "value and getNewStateValue picks the last allowed direction when every candidate ties on the dot product. "
        + "Expected: the hopper keeps facing=down (flipping twice must be the identity).")
    void downFacingHopperSurvivesVerticalFlip() {
        BlockState hopper = state("minecraft:hopper", "facing=down");
        assertEquals(hopper, transformTimes(hopper, VERTICAL_FLIP, 2));
    }

    @Test
    @Disabled("Bug: BlockTransformExtent.transform does not swap a door's hinge when mirroring, but vanilla's "
        + "Mirror does (DoorBlock.mirror cycles HINGE). Input: minecraft:oak_door[facing=east,hinge=left] with "
        + "scale(-1, 1, 1) gives facing=west,hinge=left; expected facing=west,hinge=right. Mirrored doors "
        + "therefore open the wrong way.")
    void horizontalFlipSwapsDoorHinge() {
        BlockState door = state("minecraft:oak_door", "facing=east", "hinge=left");
        assertEquals(state("minecraft:oak_door", "facing=west", "hinge=right"),
            BlockTransformExtent.transform(door, FLIP_X));
    }
}
