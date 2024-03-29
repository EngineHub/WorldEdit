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

package com.sk89q.worldedit;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.permission.ActorSelectorLimits;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.session.Placement;
import com.sk89q.worldedit.session.PlacementType;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Answers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class LocalSessionTest extends BaseWorldEditTest {
    private LocalSession session;
    private World world;
    private Player player;

    @BeforeEach
    void setUp() {
        session = new LocalSession();

        world = mock(World.class, Answers.RETURNS_SMART_NULLS);
        player = mock(Player.class, Answers.RETURNS_SMART_NULLS);
        doReturn(world).when(player).getWorld();
    }

    @SuppressWarnings("deprecation")
    @Test
    void testPlacementSet() {
        session.setPlaceAtPos1(true);
        assertTrue(session.isPlaceAtPos1());

        session.setPlaceAtPos1(false);
        assertFalse(session.isPlaceAtPos1());

        session.setPlaceAtPos1(true);
        assertTrue(session.isPlaceAtPos1());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testPlacementToggle() {
        // Start with pos1 and verify that toggling back and forth works
        session.setPlaceAtPos1(true);
        assertTrue(session.isPlaceAtPos1());

        session.togglePlacementPosition();
        assertFalse(session.isPlaceAtPos1());

        session.togglePlacementPosition();
        assertTrue(session.isPlaceAtPos1());

        // Same sequence, but this time start with player placement
        session.setPlaceAtPos1(false);
        assertFalse(session.isPlaceAtPos1());

        session.togglePlacementPosition();
        assertTrue(session.isPlaceAtPos1());

        session.togglePlacementPosition();
        assertFalse(session.isPlaceAtPos1());
    }

    @SuppressWarnings("deprecation")
    @Test
    void testPlacementPos1() throws Exception {
        final ActorSelectorLimits limits = ActorSelectorLimits.forActor(player);
        final RegionSelector regionSelector = session.getRegionSelector(world);

        final BlockVector3 pos1 = BlockVector3.at(1337, 42, 23);
        final BlockVector3 pos2 = BlockVector3.at(123, 456, 789);

        session.setPlaceAtPos1(true);

        // No selection
        assertThrows(IncompleteRegionException.class, () -> session.getPlacementPosition(player));

        // pos1 set
        regionSelector.selectPrimary(pos1, limits);
        assertEquals(pos1, session.getPlacementPosition(player));

        // pos1+2 set
        regionSelector.selectSecondary(pos2, limits);
        assertEquals(pos1, session.getPlacementPosition(player));
    }

    @SuppressWarnings("deprecation")
    @Test
    void testPlacementPlayer() throws Exception {
        final BlockVector3 playerPosition = BlockVector3.at(42, 1337, 23);
        final Location playerLocation = new Location(world, playerPosition.toVector3());

        doReturn(playerLocation).when(player).getLocation();
        doCallRealMethod().when(player).getBlockLocation();

        session.setPlaceAtPos1(false);
        assertEquals(playerPosition, session.getPlacementPosition(player));
    }

    @ParameterizedTest
    @EnumSource(PlacementType.class)
    void testPlacementGeneric(PlacementType placementType) throws Exception {
        if (placementType == PlacementType.HERE) {
            // HERE is a special case that's handled in command code and doesn't have an actual implementation
            return;
        }

        final ActorSelectorLimits limits = ActorSelectorLimits.forActor(player);
        final RegionSelector regionSelector = session.getRegionSelector(world);

        // Make sure player-based PlacementTypes work
        final BlockVector3 playerPosition = BlockVector3.at(42, 1337, 23);
        final Location playerLocation = new Location(world, playerPosition.toVector3());

        doReturn(playerLocation).when(player).getLocation();
        doCallRealMethod().when(player).getBlockLocation();

        // Make sure selection-based PlacementTypes work
        final BlockVector3 pos1 = BlockVector3.at(1337, 42, 23);
        final BlockVector3 pos2 = BlockVector3.at(123, 456, 789);
        regionSelector.selectPrimary(pos1, limits);
        regionSelector.selectSecondary(pos2, limits);

        final BlockVector3 offset = BlockVector3.at(987, 654, 321);

        final Placement placement = new Placement(placementType, offset);
        session.setPlacement(placement);
        final BlockVector3 expected = placement.getPlacementPosition(regionSelector, player);
        assertEquals(expected, session.getPlacementPosition(player));
    }

}
