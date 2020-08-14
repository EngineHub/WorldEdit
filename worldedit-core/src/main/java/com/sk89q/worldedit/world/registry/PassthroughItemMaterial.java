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

package com.sk89q.worldedit.world.registry;

import javax.annotation.Nullable;

import static com.sk89q.worldedit.util.GuavaUtil.firstNonNull;

public class PassthroughItemMaterial implements ItemMaterial {

    private static final ItemMaterial DEFAULT_MATERIAL = new SimpleItemMaterial(0, 0);

    private final ItemMaterial itemMaterial;

    public PassthroughItemMaterial(@Nullable ItemMaterial material) {
        this.itemMaterial = firstNonNull(material, DEFAULT_MATERIAL);
    }

    @Override
    public int getMaxStackSize() {
        return itemMaterial.getMaxStackSize();
    }

    @Override
    public int getMaxDamage() {
        return itemMaterial.getMaxDamage();
    }
}
