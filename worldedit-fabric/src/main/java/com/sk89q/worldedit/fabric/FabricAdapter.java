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

package com.sk89q.worldedit.fabric;

import com.sk89q.worldedit.coremc.CoreMcAdapter;
import com.sk89q.worldedit.coremc.internal.CoreMcPlatform;
import com.sk89q.worldedit.fabric.internal.FabricWorldEdit;

/**
 * Public API to adapt between WorldEdit and Fabric.
 */
public final class FabricAdapter extends CoreMcAdapter {

    private static final FabricAdapter INSTANCE = new FabricAdapter();

    /**
     * {@return the Fabric adapter}
     */
    public static FabricAdapter get() {
        return INSTANCE;
    }

    private FabricAdapter() {
    }

    @Override
    protected CoreMcPlatform getPlatform() {
        return FabricWorldEdit.getPlatform();
    }
}
