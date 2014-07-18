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

package com.sk89q.worldedit.util;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.world.World;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Tests {@link Location}.
 */
public class LocationTest {

    private static final int TEST_VALUE = 10;
    private static final double EPSILON = 0.0001;

    @Test
    public void testGetWorld() throws Exception {
        World world = mock(World.class);
        Location location = new Location(world);
        assertEquals(world, location.getExtent());
    }

    @Test
    public void testSetWorld() throws Exception {
        World world1 = mock(World.class);
        World world2 = mock(World.class);
        Location location1 = new Location(world1);
        Location location2 = location1.setExtent(world2);
        assertEquals(world1, location1.getExtent());
        assertEquals(world2, location2.getExtent());
    }

    @Test
    public void testToVector() throws Exception {
        World world = mock(World.class);
        Vector position = new Vector(1, 1, 1);
        Location location = new Location(world, position);
        assertEquals(position, location.toVector());
    }

    @Test
    public void testGetX() throws Exception {
        World world = mock(World.class);
        Location location = new Location(world, new Vector(TEST_VALUE, 0, 0));
        assertEquals(TEST_VALUE, location.getX(), EPSILON);
    }

    @Test
    public void testGetBlockX() throws Exception {
        World world = mock(World.class);
        Location location = new Location(world, new Vector(TEST_VALUE, 0, 0));
        assertEquals(TEST_VALUE, location.getBlockX());
    }

    @Test
    public void testSetX() throws Exception {
        World world = mock(World.class);
        Location location1 = new Location(world, new Vector());
        Location location2 = location1.setX(TEST_VALUE);
        assertEquals(0, location1.getX(), EPSILON);
        assertEquals(TEST_VALUE, location2.getX(), EPSILON);
        assertEquals(0, location2.getY(), EPSILON);
        assertEquals(0, location2.getZ(), EPSILON);
    }

    @Test
    public void testGetY() throws Exception {
        World world = mock(World.class);
        Location location = new Location(world, new Vector(0, TEST_VALUE, 0));
        assertEquals(TEST_VALUE, location.getY(), EPSILON);
    }

    @Test
    public void testGetBlockY() throws Exception {
        World world = mock(World.class);
        Location location = new Location(world, new Vector(0, TEST_VALUE, 0));
        assertEquals(TEST_VALUE, location.getBlockY());
    }

    @Test
    public void testSetY() throws Exception {
        World world = mock(World.class);
        Location location1 = new Location(world, new Vector());
        Location location2 = location1.setY(TEST_VALUE);
        assertEquals(0, location1.getY(), EPSILON);
        assertEquals(0, location2.getX(), EPSILON);
        assertEquals(TEST_VALUE, location2.getY(), EPSILON);
        assertEquals(0, location2.getZ(), EPSILON);
    }

    @Test
    public void testGetZ() throws Exception {
        World world = mock(World.class);
        Location location = new Location(world, new Vector(0, 0, TEST_VALUE));
        assertEquals(TEST_VALUE, location.getZ(), EPSILON);
    }

    @Test
    public void testGetBlockZ() throws Exception {
        World world = mock(World.class);
        Location location = new Location(world, new Vector(0, 0, TEST_VALUE));
        assertEquals(TEST_VALUE, location.getBlockZ());
    }

    @Test
    public void testSetZ() throws Exception {
        World world = mock(World.class);
        Location location1 = new Location(world, new Vector());
        Location location2 = location1.setZ(TEST_VALUE);
        assertEquals(0, location1.getZ(), EPSILON);
        assertEquals(0, location2.getX(), EPSILON);
        assertEquals(0, location2.getY(), EPSILON);
        assertEquals(TEST_VALUE, location2.getZ(), EPSILON);
    }

}
