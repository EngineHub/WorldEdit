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

package com.sk89q.worldedit.internal;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.TreeGenerator.TreeType;
import com.sk89q.worldedit.world.World;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Wraps {@link World}s into {@link LocalWorld}.
 */
public class LocalWorldAdapter extends LocalWorld {

    private final World world;

    private LocalWorldAdapter(World world) {
        checkNotNull(world);
        this.world = world;
    }

    @Override
    public String getName() {
        return world.getName();
    }

    @Override
    public int getMaxY() {
        return world.getMaxY();
    }

    @Override
    public boolean isValidBlockType(int id) {
        return world.isValidBlockType(id);
    }

    @Override
    public boolean usesBlockData(int id) {
        return world.usesBlockData(id);
    }

    @Override
    public Mask createLiquidMask() {
        return world.createLiquidMask();
    }

    @Override
    @Deprecated
    public int getBlockType(Vector pt) {
        return world.getBlockType(pt);
    }

    @Override
    @Deprecated
    public int getBlockData(Vector pt) {
        return world.getBlockData(pt);
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block, boolean notifyAndLight) throws WorldEditException {
        return world.setBlock(position, block, notifyAndLight);
    }

    @Override
    @Deprecated
    public boolean setBlockType(Vector position, int type) {
        return world.setBlockType(position, type);
    }

    @Override
    @Deprecated
    public boolean setBlockTypeFast(Vector position, int type) {
        return world.setBlockTypeFast(position, type);
    }

    @Override
    @Deprecated
    public void setBlockData(Vector position, int data) {
        world.setBlockData(position, data);
    }

    @Override
    @Deprecated
    public void setBlockDataFast(Vector position, int data) {
        world.setBlockDataFast(position, data);
    }

    @Override
    @Deprecated
    public boolean setTypeIdAndData(Vector position, int type, int data) {
        return world.setTypeIdAndData(position, type, data);
    }

    @Override
    @Deprecated
    public boolean setTypeIdAndDataFast(Vector position, int type, int data) {
        return world.setTypeIdAndDataFast(position, type, data);
    }

    @Override
    public int getBlockLightLevel(Vector position) {
        return world.getBlockLightLevel(position);
    }

    @Override
    public boolean clearContainerBlockContents(Vector position) {
        return world.clearContainerBlockContents(position);
    }

    @Override
    public BiomeType getBiome(Vector2D position) {
        return world.getBiome(position);
    }

    @Override
    public void setBiome(Vector2D position, BiomeType biome) {
        world.setBiome(position, biome);
    }

    @Override
    public void dropItem(Vector position, BaseItemStack item, int count) {
        world.dropItem(position, item, count);
    }

    @Override
    public void dropItem(Vector position, BaseItemStack item) {
        world.dropItem(position, item);
    }

    @Override
    public void simulateBlockMine(Vector position) {
        world.simulateBlockMine(position);
    }

    @Override
    public LocalEntity[] getEntities(Region region) {
        return world.getEntities(region);
    }

    @Override
    public int killEntities(LocalEntity... entity) {
        return world.killEntities(entity);
    }

    @Override
    @Deprecated
    public int killMobs(Vector origin, int radius) {
        return world.killMobs(origin, radius);
    }

    @Override
    @Deprecated
    public int killMobs(Vector origin, int radius, boolean killPets) {
        return world.killMobs(origin, radius, killPets);
    }

    @Override
    public int killMobs(Vector origin, double radius, int flags) {
        return world.killMobs(origin, radius, flags);
    }

    @Override
    public int removeEntities(EntityType type, Vector origin, int radius) {
        return world.removeEntities(type, origin, radius);
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        return world.regenerate(region, editSession);
    }

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, Vector position) throws MaxChangedBlocksException {
        return world.generateTree(type, editSession, position);
    }

    @Override
    @Deprecated
    public boolean generateTree(EditSession editSession, Vector position) throws MaxChangedBlocksException {
        return world.generateTree(editSession, position);
    }

    @Override
    @Deprecated
    public boolean generateBigTree(EditSession editSession, Vector position) throws MaxChangedBlocksException {
        return world.generateBigTree(editSession, position);
    }

    @Override
    @Deprecated
    public boolean generateBirchTree(EditSession editSession, Vector position) throws MaxChangedBlocksException {
        return world.generateBirchTree(editSession, position);
    }

    @Override
    @Deprecated
    public boolean generateRedwoodTree(EditSession editSession, Vector position) throws MaxChangedBlocksException {
        return world.generateRedwoodTree(editSession, position);
    }

    @Override
    @Deprecated
    public boolean generateTallRedwoodTree(EditSession editSession, Vector position) throws MaxChangedBlocksException {
        return world.generateTallRedwoodTree(editSession, position);
    }

    @Override
    public void checkLoadedChunk(Vector position) {
        world.checkLoadedChunk(position);
    }

    @Override
    public void fixAfterFastMode(Iterable<BlockVector2D> chunks) {
        world.fixAfterFastMode(chunks);
    }

    @Override
    public void fixLighting(Iterable<BlockVector2D> chunks) {
        world.fixLighting(chunks);
    }

    @Override
    public boolean playEffect(Vector position, int type, int data) {
        return world.playEffect(position, type, data);
    }

    @Override
    public boolean queueBlockBreakEffect(ServerInterface server, Vector position, int blockId, double priority) {
        return world.queueBlockBreakEffect(server, position, blockId, priority);
    }

    @Override
    public boolean equals(Object other) {
        return world.equals(other);
    }

    @Override
    public int hashCode() {
        return world.hashCode();
    }

    @Override
    public Vector getMinimumPoint() {
        return world.getMinimumPoint();
    }

    @Override
    public Vector getMaximumPoint() {
        return world.getMaximumPoint();
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        return world.getBlock(position);
    }

    @Override
    public boolean copyFromWorld(Vector position, BaseBlock block) {
        return false;
    }

    @Override
    public boolean copyToWorld(Vector position, BaseBlock block) {
        return false;
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        return world.getLazyBlock(position);
    }

    @Override
    public boolean setBlock(Vector position, BaseBlock block) {
        try {
            return world.setBlock(position, block);
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Nullable
    public Operation commit() {
        return world.commit();
    }

    public static LocalWorldAdapter wrap(World world) {
        return new LocalWorldAdapter(world);
    }

}
