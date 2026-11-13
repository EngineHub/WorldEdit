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

package com.sk89q.worldedit.util;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TargetBlockTest {

    private static final double EYE_HEIGHT = 1.65;

    private World world;

    @BeforeEach
    void setUp() {
        world = mock(World.class);
        when(world.getMinY()).thenReturn(-64);
        when(world.getMaxY()).thenReturn(319);
    }

    private TargetBlock targetFrom(Vector3 feet, float yaw, float pitch, int maxDistance) {
        Player player = mock(Player.class);
        when(player.getWorld()).thenReturn(world);
        when(player.getLocation()).thenReturn(new Location(world, feet, yaw, pitch));
        return new TargetBlock(player, maxDistance, 0.2);
    }

    private static BlockVector3 block(Location location) {
        return location.toVector().toBlockPoint();
    }

    @ParameterizedTest
    @CsvSource({
        // yaw, axis of the wall, wall coordinate, coordinate of the block in front of it
        "0,    z, 5,  4",
        "90,   x, -5, -4",
        "180,  z, -5, -4",
        "270,  x, 5,  4",
    })
    void cardinalYawsHitTheWallInFront(float yaw, char axis, int wall, int previousCoordinate) {
        Mask stop = axis == 'x'
            ? pos -> wall > 0 ? pos.x() >= wall : pos.x() <= wall
            : pos -> wall > 0 ? pos.z() >= wall : pos.z() <= wall;
        TargetBlock target = targetFrom(Vector3.at(0.5, 64, 0.5), yaw, 0, 20);
        target.setStopMask(stop);

        Location hit = target.getTargetBlock();
        assertNotNull(hit);
        int eyeY = (int) Math.floor(64 + EYE_HEIGHT);
        BlockVector3 expectedHit = axis == 'x' ? BlockVector3.at(wall, eyeY, 0) : BlockVector3.at(0, eyeY, wall);
        BlockVector3 expectedPrevious = axis == 'x'
            ? BlockVector3.at(previousCoordinate, eyeY, 0) : BlockVector3.at(0, eyeY, previousCoordinate);
        assertEquals(expectedHit, block(hit));
        assertEquals(expectedPrevious, block(target.getPreviousBlock()));
    }

    @Test
    void lookingStraightDownHitsTheFloor() {
        TargetBlock target = targetFrom(Vector3.at(0.5, 64, 0.5), 0, 90, 20);
        target.setStopMask(pos -> pos.y() <= 60);

        assertEquals(BlockVector3.at(0, 60, 0), block(target.getTargetBlock()));
        assertEquals(BlockVector3.at(0, 61, 0), block(target.getPreviousBlock()));
    }

    @Test
    void lookingStraightUpHitsTheCeiling() {
        TargetBlock target = targetFrom(Vector3.at(0.5, -20, 0.5), 123, -90, 20);
        target.setStopMask(pos -> pos.y() >= -10);

        assertEquals(BlockVector3.at(0, -10, 0), block(target.getTargetBlock()));
        assertEquals(BlockVector3.at(0, -11, 0), block(target.getPreviousBlock()));
    }

    @Test
    void negativeCoordinatesRoundTowardsNegativeInfinity() {
        TargetBlock target = targetFrom(Vector3.at(-0.5, 64, -0.5), 180, 0, 20);
        target.setStopMask(pos -> pos.z() <= -4);

        assertEquals(BlockVector3.at(-1, 65, -4), block(target.getTargetBlock()));
    }

    @Test
    void maxDistanceLimitsTheTrace() {
        TargetBlock tooShort = targetFrom(Vector3.at(0.5, 64, 0.5), 0, 0, 10);
        tooShort.setStopMask(pos -> pos.z() >= 11);
        assertNull(tooShort.getTargetBlock());
        assertNull(tooShort.getCurrentBlock());
        assertNull(tooShort.getNextBlock());

        TargetBlock longEnough = targetFrom(Vector3.at(0.5, 64, 0.5), 0, 0, 10);
        longEnough.setStopMask(pos -> pos.z() >= 8);
        assertEquals(8, block(longEnough.getTargetBlock()).z());
    }

    @Test
    void faceDirectionPointsFromPreviousToHitBlock() {
        TargetBlock target = targetFrom(Vector3.at(0.5, 64, 0.5), 90, 0, 20);
        target.setStopMask(pos -> pos.x() <= -3);

        Location face = target.getTargetBlockFace();
        assertNotNull(face);
        Vector3 direction = face.getDirection();
        assertEquals(-1, direction.x(), 1e-6);
        assertEquals(0, direction.y(), 1e-6);
        assertEquals(0, direction.z(), 1e-6);
    }

    @Test
    void faceIsNullWhenNothingIsHit() {
        TargetBlock target = targetFrom(Vector3.at(0.5, 64, 0.5), 0, 0, 5);
        target.setStopMask(_ -> false);
        assertNull(target.getTargetBlockFace());
    }

    @Test
    void anyTargetBlockGivesUpOutsideTheWorldHeight() {
        TargetBlock up = targetFrom(Vector3.at(0.5, 300, 0.5), 0, -90, 300);
        up.setStopMask(_ -> false);
        assertNull(up.getAnyTargetBlock());

        TargetBlock down = targetFrom(Vector3.at(0.5, -60, 0.5), 0, 90, 300);
        down.setStopMask(_ -> false);
        assertNull(down.getAnyTargetBlock());
    }

    @Test
    void anyTargetBlockStopsAtTheStopMask() {
        TargetBlock target = targetFrom(Vector3.at(0.5, 64, 0.5), 0, 0, 50);
        target.setStopMask(pos -> pos.z() >= 6);
        assertEquals(6, block(target.getAnyTargetBlock()).z());
    }

    @Test
    void solidMaskIsIndependentOfTheStopMask() {
        TargetBlock target = targetFrom(Vector3.at(0.5, 64, 0.5), 0, 0, 50);
        target.setStopMask(pos -> pos.z() >= 3);
        target.setSolidMask(pos -> pos.z() >= 9);
        assertEquals(9, block(target.getSolidTargetBlock()).z());
    }

    @ParameterizedTest
    @CsvSource({"30, 20", "135, -40", "250, 75", "315, 10", "0, 45"})
    void steppedBlocksAreContiguousAndNeverRepeat(float yaw, float pitch) {
        TargetBlock target = targetFrom(Vector3.at(0.5, 64, 0.5), yaw, pitch, 15);
        List<BlockVector3> visited = new ArrayList<>();
        visited.add(block(target.getPreviousBlock()));
        Location next;
        while ((next = target.getNextBlock()) != null) {
            visited.add(block(next));
        }

        assertTrue(visited.size() > 10);
        for (int i = 1; i < visited.size(); i++) {
            BlockVector3 step = visited.get(i).subtract(visited.get(i - 1));
            assertNotEquals(BlockVector3.ZERO, step, "block repeated at step " + i);
            assertTrue(Math.abs(step.x()) <= 1 && Math.abs(step.y()) <= 1 && Math.abs(step.z()) <= 1,
                "skipped a block at step " + i + ": " + step);
        }
    }
}
