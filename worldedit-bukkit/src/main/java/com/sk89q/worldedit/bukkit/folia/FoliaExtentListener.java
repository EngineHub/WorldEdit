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

package com.sk89q.worldedit.bukkit.folia;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Bukkit;

public class FoliaExtentListener {

    @Subscribe
    public void onEditSessionCreation(EditSessionEvent event) {
        // This injects an extent that only allows modifying blocks & biomes that are within the current region.
        // This prevents WorldEdit from inadvertently causing chunk loads or other thread-shenanigans when running on Folia.
        event.setExtent(new AbstractDelegateExtent(event.getExtent()) {

            private boolean isWithinRegion(BlockVector3 location) {
                return Bukkit.isOwnedByCurrentRegion(BukkitAdapter.adapt(BukkitAdapter.adapt(event.getWorld()), location));
            }

            @Override
            public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
                if (isWithinRegion(location)) {
                    return super.setBlock(location, block);
                }

                return false;
            }

            @Override
            public boolean setBiome(BlockVector3 position, BiomeType biome) {
                if (isWithinRegion(position)) {
                    return super.setBiome(position, biome);
                }

                return false;
            }
        });
    }
}
