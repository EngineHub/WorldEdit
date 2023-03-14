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

package com.sk89q.worldedit.session;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class PlacementTypeTest {
    @Test
    void testPlacementPLAYER() throws Exception {
        final RegionSelector regionSelector = mock(RegionSelector.class, Answers.RETURNS_SMART_NULLS);
        final Player player = mock(Player.class, Answers.RETURNS_SMART_NULLS);

        final BlockVector3 playerPosition = BlockVector3.at(42, 1337, 23);
        final Location playerLocation = new Location(mock(World.class), playerPosition.toVector3());
        doReturn(playerLocation).when(player).getLocation();
        doCallRealMethod().when(player).getBlockLocation();

        assertEquals(playerPosition, PlacementType.PLAYER.getPlacementPosition(regionSelector, player));

        verifyNoInteractions(regionSelector);
        verify(player, times(1)).getLocation();
        verify(player, times(1)).getBlockLocation();
        verifyNoMoreInteractions(player);
    }

    @Test
    void testPlacementPOS1() throws Exception {
        final RegionSelector regionSelector = mock(RegionSelector.class, Answers.RETURNS_SMART_NULLS);
        final Actor actor = mock(Actor.class, Answers.RETURNS_SMART_NULLS);

        final BlockVector3 pos1 = BlockVector3.at(1337, 42, 23);
        doReturn(pos1).when(regionSelector).getPrimaryPosition();

        assertEquals(pos1, PlacementType.POS1.getPlacementPosition(regionSelector, actor));

        verify(regionSelector, times(1)).getPrimaryPosition();
        verifyNoMoreInteractions(regionSelector);
        verifyNoInteractions(actor);
    }
}
