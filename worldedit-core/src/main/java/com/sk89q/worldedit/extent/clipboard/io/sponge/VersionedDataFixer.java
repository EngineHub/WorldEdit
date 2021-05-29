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

package com.sk89q.worldedit.extent.clipboard.io.sponge;


import com.sk89q.worldedit.world.DataFixer;

import javax.annotation.Nullable;

final class VersionedDataFixer {
    private final int dataVersion;
    @Nullable
    private final DataFixer fixer;

    VersionedDataFixer(int dataVersion, @Nullable DataFixer fixer) {
        this.dataVersion = dataVersion;
        this.fixer = fixer;
    }

    public boolean isActive() {
        return fixer != null;
    }

    public <T> T fixUp(DataFixer.FixType<T> type, T original) {
        if (!isActive()) {
            return original;
        }
        return fixer.fixUp(type, original, dataVersion);
    }
}
