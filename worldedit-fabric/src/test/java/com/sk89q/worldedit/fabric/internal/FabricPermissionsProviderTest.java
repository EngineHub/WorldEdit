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

package com.sk89q.worldedit.fabric.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FabricPermissionsProviderTest {

    @Test
    void mapsStandardPermission() {
        assertEquals(
            "worldedit:region.set",
            FabricPermissionsProvider.identifierFor("worldedit.region.set").toString()
        );
    }

    @Test
    void normalizesCase() {
        assertEquals(
            "worldedit:scripting.execute.build.js",
            FabricPermissionsProvider.identifierFor("WorldEdit.scripting.execute.Build.js").toString()
        );
    }

    @Test
    void escapesInvalidCharactersAndUnderscores() {
        assertEquals(
            "worldedit:scripting.execute.my_20script_5fv1_21",
            FabricPermissionsProvider.identifierFor("worldedit.scripting.execute.my script_v1!").toString()
        );
    }

    @Test
    void escapesUtf8Bytes() {
        assertEquals(
            "worldedit:scripting.execute.caf_c3_a9",
            FabricPermissionsProvider.identifierFor("worldedit.scripting.execute.café").toString()
        );
    }

    @Test
    void preservesPathSeparators() {
        assertEquals(
            "worldedit:scripting.execute/tools/build.js",
            FabricPermissionsProvider.identifierFor("worldedit.scripting.execute/tools/build.js").toString()
        );
    }

    @Test
    void rejectsInvalidPermissionNodes() {
        assertNull(FabricPermissionsProvider.identifierFor("worldedit"));
        assertNull(FabricPermissionsProvider.identifierFor("worldedit."));
    }
}
