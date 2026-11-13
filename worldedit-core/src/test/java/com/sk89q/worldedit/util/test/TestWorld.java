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

package com.sk89q.worldedit.util.test;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.SideEffect;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.RegenOptions;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import com.sk89q.worldedit.world.generation.TreeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * A small in-memory world for tests: unbounded in X and Z, with every unset position being air.
 *
 * <p>Blocks are looked up through {@link BlockTypes}, so tests using this must have registered
 * {@code minecraft:air}, for example through {@link TestRegistries#registerBlocks()}.</p>
 *
 * <p>Only blocks and the entity list are functional. Side effects, biomes, tree and entity creation,
 * regeneration and height limits are not simulated.</p>
 */
public class TestWorld extends AbstractWorld {

    private final String name;
    private final Map<BlockVector3, BaseBlock> blocks = new HashMap<>();
    private final List<Entity> entities = new ArrayList<>();

    public TestWorld() {
        this("test");
    }

    public TestWorld(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String id() {
        return "worldedit:" + name;
    }

    /**
     * Gets the positions that have been explicitly set to a non-air block.
     */
    public Map<BlockVector3, BaseBlock> getBlocks() {
        return blocks;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public int getMaxY() {
        return 319;
    }

    @Override
    public Mask createLiquidMask() {
        return _ -> false;
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        return getFullBlock(position).toImmutableState();
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        BaseBlock block = blocks.get(position);
        return block != null ? block : air().toBaseBlock();
    }

    private static BlockState air() {
        BlockType air = BlockTypes.AIR;
        if (air == null) {
            throw new IllegalStateException("minecraft:air is not registered");
        }
        return air.getDefaultState();
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, SideEffectSet sideEffects) {
        BaseBlock previous = getFullBlock(position);
        BaseBlock next = block.toBaseBlock();
        if (next.getBlockType().equals(BlockTypes.AIR)) {
            blocks.remove(position);
        } else {
            blocks.put(position, next);
        }
        return !next.equals(previous);
    }

    @Override
    public Operation commit() {
        return null;
    }

    @Override
    public Set<SideEffect> applySideEffects(BlockVector3 position, BlockState previousType, SideEffectSet sideEffectSet) {
        return Set.of();
    }

    @Override
    public boolean clearContainerBlockContents(BlockVector3 position) {
        return false;
    }

    @Override
    public void simulateBlockMine(BlockVector3 position) {
    }

    @Override
    public boolean regenerate(Region region, Extent extent, RegenOptions options) {
        return false;
    }

    @Override
    public boolean generateTree(TreeType type, EditSession editSession, BlockVector3 position) throws MaxChangedBlocksException {
        return false;
    }

    @Override
    public void dropItem(Vector3 position, BaseItemStack item) {
    }

    @Override
    public int getBlockLightLevel(BlockVector3 position) {
        return 0;
    }

    @Override
    public BlockVector3 getSpawnPosition() {
        return BlockVector3.at(0, 64, 0);
    }

    @Nullable
    @Override
    public BiomeType getBiome(BlockVector3 position) {
        return null;
    }

    @Override
    public boolean setBiome(BlockVector3 position, BiomeType biome) {
        return false;
    }

    /**
     * Gets the live list of entities, so tests can add to it directly.
     */
    public List<Entity> getEntityList() {
        return entities;
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        List<Entity> result = new ArrayList<>();
        for (Entity entity : entities) {
            if (region.contains(entity.getLocation().toVector().toBlockPoint())) {
                result.add(entity);
            }
        }
        return result;
    }

    @Override
    public List<? extends Entity> getEntities() {
        return entities;
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        return null;
    }
}
