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

package com.sk89q.worldedit.internal.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DeprecationUtilTest {

    public interface ModifiedApi {
        @Deprecated
        default boolean oldApi() {
            return newApi();
        }

        @NonAbstractForCompatibility(
            delegateName = "oldApi",
            delegateParams = {}
        )
        default boolean newApi() {
            DeprecationUtil.checkDelegatingOverride(getClass());
            return oldApi();
        }
    }

    public static class OldImpl implements ModifiedApi {
        @SuppressWarnings("deprecation")
        @Override
        public boolean oldApi() {
            return false;
        }
    }

    public static class NewImpl implements ModifiedApi {
        @Override
        public boolean newApi() {
            return true;
        }
    }

    public static class NewBadImpl implements ModifiedApi {
    }

    @Test
    void oldImpl() {
        assertFalse(new OldImpl().oldApi());
        assertFalse(new OldImpl().newApi());
    }

    @Test
    void newImpl() {
        assertTrue(new NewImpl().oldApi());
        assertTrue(new NewImpl().newApi());
    }

    @Test
    void newBadImpl() {
        // regardless of which method is called, the message is the same
        Exception ex = assertThrows(IllegalStateException.class, new NewBadImpl()::oldApi);
        assertTrue(ex.getMessage().contains("must override"));
        assertTrue(ex.getMessage().contains("ModifiedApi.newApi()"));

        ex = assertThrows(IllegalStateException.class, new NewBadImpl()::newApi);
        assertTrue(ex.getMessage().contains("must override"));
        assertTrue(ex.getMessage().contains("ModifiedApi.newApi()"));
    }

}
