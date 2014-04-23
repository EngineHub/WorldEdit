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

package com.sk89q.worldedit.world;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;

/**
 * A null implementation of {@link World} that drops all changes and
 * returns dummy data.
 */
public class NullWorld extends AbstractWorld {

    @Override
    public String getName() {
        return "null";
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block, boolean notifyAndLight) throws WorldEditException {
        return false;
    }

    @Override
    public int getBlockLightLevel(Vector position) {
        return 0;
    }

    @Override
    public boolean clearContainerBlockContents(Vector position) {
        return false;
    }

    @Override
    public BiomeType getBiome(Vector2D position) {
        return null;
    }

    @Override
    public void setBiome(Vector2D position, BiomeType biome) {
    }

    @Override
    public void dropItem(Vector position, BaseItemStack item) {
    }

    @Override
    public int killMobs(Vector origin, double radius, int flags) {
        return 0;
    }

    @Override
    public int removeEntities(EntityType type, Vector origin, int radius) {
        return 0;
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        return false;
    }

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, Vector position) throws MaxChangedBlocksException {
        return false;
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        return new BaseBlock(BlockID.AIR);
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        return new BaseBlock(BlockID.AIR);
    }

}
