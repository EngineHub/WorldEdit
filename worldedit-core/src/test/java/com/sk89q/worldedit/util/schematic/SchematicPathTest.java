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

package com.sk89q.worldedit.util.schematic;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests {@link SchematicPath}.
 *
 * @see <a href="https://github.com/EngineHub/WorldEdit/pull/2212#discussion_r1016580671">
 *     Trusting me4502 is good, controlling is better! :)
 * </a>
 */
public class SchematicPathTest {

    @Test
    public void testHashAndEquality() {
        Path p0 = Path.of("/tmp/testpath0");
        Path p1 = Path.of("/tmp/testpath1");
        Path p0equiv = Path.of("/tmp/testpath0");

        SchematicPath s0 = new SchematicPath(p0);
        SchematicPath s1 = new SchematicPath(p1);
        SchematicPath s0equiv = new SchematicPath(p0equiv);

        assertEquals(s0.hashCode(), s0equiv.hashCode());
        assertNotEquals(s0.hashCode(), s1.hashCode());

        assertTrue(s0.equals(s0equiv));
        assertFalse(s0.equals(s1));
    }

}
