package com.sk89q.worldedit.extent.clipboard;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.biome.BiomeTypes;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class MultiClipboard implements Clipboard {

    private static final Clipboard EMPTY_CLIPBOARD = new EmptyClipboard();

    private String current;
    private Map<String, Clipboard> clipboards = Maps.newHashMap();

    public MultiClipboard() {
    }

    public boolean removeClipboard(String name) {
        return clipboards.remove(name) != null;
    }

    public boolean addClipboard(String name, Clipboard clipboard) {
        checkNotNull(name);
        checkNotNull(clipboard);
        boolean first = clipboards.isEmpty();
        boolean added = clipboards.putIfAbsent(name, clipboard) == null;
        if (added && first) {
            setCurrent(name);
        }
        return added;
    }

    public List<String> getNames() {
        return Lists.newArrayList(clipboards.keySet());
    }

    public String getCurrentName() {
        return current;
    }

    @Nullable
    public Clipboard getCurrentClipboard() {
        if (current == null) return EMPTY_CLIPBOARD;
        return clipboards.get(current);
    }

    @Nullable
    public Clipboard getClipboard(String name) {
        return clipboards.getOrDefault(name, null);
    }

    public void setCurrent(String current) {
        checkArgument(clipboards.containsKey(current), "Name doesn't exist in current clipboards.");
        this.current = current;
    }

    @Override
    public Region getRegion() {
        return clipboards.getOrDefault(current, EMPTY_CLIPBOARD).getRegion();
    }

    @Override
    public BlockVector3 getDimensions() {
        return clipboards.getOrDefault(current, EMPTY_CLIPBOARD).getDimensions();
    }

    @Override
    public BlockVector3 getOrigin() {
        return clipboards.getOrDefault(current, EMPTY_CLIPBOARD).getOrigin();
    }

    @Override
    public void setOrigin(BlockVector3 origin) {
        clipboards.getOrDefault(current, EMPTY_CLIPBOARD).setOrigin(origin);
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        return clipboards.getOrDefault(current, EMPTY_CLIPBOARD).getMinimumPoint();
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        return clipboards.getOrDefault(current, EMPTY_CLIPBOARD).getMaximumPoint();
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return clipboards.getOrDefault(current, EMPTY_CLIPBOARD).getEntities(region);
    }

    @Override
    public List<? extends Entity> getEntities() {
        return clipboards.getOrDefault(current, EMPTY_CLIPBOARD).getEntities();
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        return clipboards.getOrDefault(current, EMPTY_CLIPBOARD).createEntity(location, entity);
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        return clipboards.getOrDefault(current, EMPTY_CLIPBOARD).getBlock(position);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        return clipboards.getOrDefault(current, EMPTY_CLIPBOARD).getFullBlock(position);
    }

    @Override
    public BiomeType getBiome(BlockVector2 position) {
        return clipboards.getOrDefault(current, EMPTY_CLIPBOARD).getBiome(position);
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
        return clipboards.getOrDefault(current, EMPTY_CLIPBOARD).setBlock(position, block);
    }

    @Override
    public boolean setBiome(BlockVector2 position, BiomeType biome) {
        return clipboards.getOrDefault(current, EMPTY_CLIPBOARD).setBiome(position, biome);
    }

    @Nullable
    @Override
    public Operation commit() {
        return clipboards.getOrDefault(current, EMPTY_CLIPBOARD).commit();
    }

    private static class EmptyClipboard implements Clipboard {
        private static final CuboidRegion EMPTY_REGION = new CuboidRegion(BlockVector3.ZERO, BlockVector3.ZERO);

        @Override
        public Region getRegion() {
            return EMPTY_REGION;
        }

        @Override
        public BlockVector3 getDimensions() {
            return BlockVector3.ZERO;
        }

        @Override
        public BlockVector3 getOrigin() {
            return BlockVector3.ZERO;
        }

        @Override
        public void setOrigin(BlockVector3 origin) {
        }

        @Override
        public BlockVector3 getMinimumPoint() {
            return BlockVector3.ZERO;
        }

        @Override
        public BlockVector3 getMaximumPoint() {
            return BlockVector3.ZERO;
        }

        @Override
        public List<? extends Entity> getEntities(Region region) {
            return Collections.emptyList();
        }

        @Override
        public List<? extends Entity> getEntities() {
            return Collections.emptyList();
        }

        @Nullable
        @Override
        public Entity createEntity(Location location, BaseEntity entity) {
            return null;
        }

        @Override
        public BlockState getBlock(BlockVector3 position) {
            return BlockTypes.AIR.getDefaultState();
        }

        @Override
        public BaseBlock getFullBlock(BlockVector3 position) {
            return BlockTypes.AIR.getDefaultState().toBaseBlock();
        }

        @Override
        public BiomeType getBiome(BlockVector2 position) {
            return BiomeTypes.THE_VOID;
        }

        @Override
        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 position, T block) throws WorldEditException {
            return false;
        }

        @Override
        public boolean setBiome(BlockVector2 position, BiomeType biome) {
            return false;
        }

        @Nullable
        @Override
        public Operation commit() {
            return null;
        }
    }
}
