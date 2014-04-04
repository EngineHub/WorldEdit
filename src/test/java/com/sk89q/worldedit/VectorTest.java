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

package com.sk89q.worldedit;

import org.junit.*;
import static org.junit.Assert.*;

public class VectorTest {
    @Test
    public void collinearityTest() {
        assertCollinear(0,0,0, 0,0,0);

        assertCollinear(0,0,0, 1,0,0);
        assertCollinear(0,0,0, 0,1,0);
        assertCollinear(0,0,0, 0,0,1);

        assertCollinear(1,0,0, 0,0,0);
        assertCollinear(0,1,0, 0,0,0);
        assertCollinear(0,0,1, 0,0,0);

        assertCollinear(1,0,0, 2,0,0);
        assertNotCollinear(1,0,0, 0,1,0);

        assertNotCollinear(2,2,2, 8,4,4);
        assertCollinear(8,2,2, 8,2,2);
        assertNotCollinear(4,2,4, 4,4,4);
        assertNotCollinear(1,1,2, 4,8,2);
        assertNotCollinear(4,1,8, 1,4,4);
        assertCollinear(2,4,2, 1,2,1);
        assertNotCollinear(2,2,4, 1,2,1);
        assertNotCollinear(4,4,1, 4,4,4);
        assertNotCollinear(4,1,4, 1,8,2);
        assertCollinear(8,8,4, 4,4,2);
        assertNotCollinear(2,1,8, 1,1,2);
        assertNotCollinear(8,1,2, 2,1,2);
        assertNotCollinear(4,4,8, 2,2,8);
        assertNotCollinear(8,4,8, 1,4,8);
        assertNotCollinear(2,2,2, 1,4,2);
        assertNotCollinear(1,1,2, 8,8,2);
        assertNotCollinear(4,4,8, 8,4,4);
        assertNotCollinear(1,8,2, 4,4,4);
        assertNotCollinear(8,4,2, 1,2,2);
        assertNotCollinear(1,8,2, 8,1,4);
        assertNotCollinear(4,8,1, 4,8,8);
        assertNotCollinear(8,1,8, 8,8,8);
        assertNotCollinear(8,4,1, 4,2,2);
        assertNotCollinear(4,8,1, 4,2,1);
        assertNotCollinear(8,8,1, 2,4,2);
        assertCollinear(8,1,4, 8,1,4);
        assertNotCollinear(4,1,1, 2,4,8);
        assertNotCollinear(4,2,8, 1,4,1);
        assertNotCollinear(1,8,2, 1,8,1);
        assertNotCollinear(1,1,2, 4,2,2);

        assertCollinear(0,0, 0,0);

        assertCollinear(0,0, 1,0);
        assertCollinear(0,0, 0,1);
        assertCollinear(0,0, 0,0);

        assertCollinear(1,0, 0,0);
        assertCollinear(0,1, 0,0);
        assertCollinear(0,0, 0,0);

        assertCollinear(1,0, 2,0);
        assertNotCollinear(1,0, 0,1);

        assertNotCollinear(2,2, 8,4);
        assertCollinear(8,2, 8,2);
        assertNotCollinear(4,2, 4,4);
        assertNotCollinear(1,1, 4,8);
        assertNotCollinear(4,1, 1,4);
        assertCollinear(2,4, 1,2);
        assertNotCollinear(2,2, 1,2);
        assertCollinear(4,4, 4,4);
        assertNotCollinear(4,1, 1,8);
        assertCollinear(8,8, 4,4);
        assertNotCollinear(2,1, 1,1);
        assertNotCollinear(8,1, 2,1);
        assertCollinear(4,4, 2,2);
        assertNotCollinear(8,4, 1,4);
        assertNotCollinear(2,2, 1,4);
        assertCollinear(1,1, 8,8);
        assertNotCollinear(4,4, 8,4);
        assertNotCollinear(1,8, 4,4);
        assertNotCollinear(8,4, 1,2);
        assertNotCollinear(1,8, 8,1);
        assertCollinear(4,8, 4,8);
        assertNotCollinear(8,1, 8,8);
        assertCollinear(8,4, 4,2);
        assertNotCollinear(4,8, 4,2);
        assertNotCollinear(8,8, 2,4);
        assertCollinear(8,1, 8,1);
        assertNotCollinear(4,1, 2,4);
        assertNotCollinear(4,2, 1,4);
        assertCollinear(1,8, 1,8);
        assertNotCollinear(1,1, 4,2);
    }

    private void assertCollinear(double ax, double ay, double az, double bx, double by, double bz) {
        final Vector a = new Vector(ax,ay,az);
        final Vector b = new Vector(bx,by,bz);
        assertTrue(a.isCollinearWith(b));
        assertTrue(b.isCollinearWith(a));
        assertTrue(a.multiply(-1.0).isCollinearWith(b));
        assertTrue(a.isCollinearWith(b.multiply(-1.0)));
    }
    private void assertNotCollinear(double ax, double ay, double az, double bx, double by, double bz) {
        final Vector a = new Vector(ax,ay,az);
        final Vector b = new Vector(bx,by,bz);
        assertFalse(a.isCollinearWith(b));
        assertFalse(b.isCollinearWith(a));
        assertFalse(a.multiply(-1.0).isCollinearWith(b));
        assertFalse(a.isCollinearWith(b.multiply(-1.0)));
    }

    private void assertCollinear(double ax, double az, double bx, double bz) {
        final Vector2D a = new Vector2D(ax,az);
        final Vector2D b = new Vector2D(bx,bz);
        assertTrue(a.isCollinearWith(b));
        assertTrue(b.isCollinearWith(a));
        assertTrue(a.multiply(-1.0).isCollinearWith(b));
        assertTrue(a.isCollinearWith(b.multiply(-1.0)));
    }
    private void assertNotCollinear(double ax, double az, double bx, double bz) {
        final Vector2D a = new Vector2D(ax,az);
        final Vector2D b = new Vector2D(bx,bz);
        assertFalse(a.isCollinearWith(b));
        assertFalse(b.isCollinearWith(a));
        assertFalse(a.multiply(-1.0).isCollinearWith(b));
        assertFalse(a.isCollinearWith(b.multiply(-1.0)));
    }
}
