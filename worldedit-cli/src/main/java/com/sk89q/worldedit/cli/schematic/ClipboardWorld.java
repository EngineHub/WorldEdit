package com.sk89q.worldedit.cli.schematic;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TreeGenerator;
import com.sk89q.worldedit.world.AbstractWorld;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;

import java.util.List;

import javax.annotation.Nullable;

public class ClipboardWorld extends AbstractWorld implements Clipboard {

    private final Clipboard clipboard;
    private final String name;

    public ClipboardWorld(Clipboard clipboard, String name) {
        this.clipboard = clipboard;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, boolean notifyAndLight)
            throws WorldEditException {
        return clipboard.setBlock(position, block);
    }

    @Override
    public boolean notifyAndLightBlock(BlockVector3 position, BlockState previousType) throws WorldEditException {
        return false;
    }

    @Override
    public int getBlockLightLevel(BlockVector3 position) {
        return 0;
    }

    @Override
    public boolean clearContainerBlockContents(BlockVector3 position) {
        return false;
    }

    @Override
    public void dropItem(Vector3 position, BaseItemStack item) {
    }

    @Override
    public void simulateBlockMine(BlockVector3 position) {
    }

    @Override
    public boolean regenerate(Region region, EditSession editSession) {
        return false;
    }

    @Override
    public boolean generateTree(TreeGenerator.TreeType type, EditSession editSession, BlockVector3 position)
            throws MaxChangedBlocksException {
        return false;
    }

    @Override
    public BlockVector3 getSpawnPosition() {
        return clipboard.getOrigin();
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return clipboard.getEntities(region);
    }

    @Override
    public List<? extends Entity> getEntities() {
        return clipboard.getEntities();
    }

    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        return clipboard.createEntity(location, entity);
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        return clipboard.getBlock(position);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        return clipboard.getFullBlock(position);
    }

    @Override
    public BiomeType getBiome(BlockVector2 position) {
        return clipboard.getBiome(position);
    }

    @Override
    public boolean setBiome(BlockVector2 position, BiomeType biome) {
        return clipboard.setBiome(position, biome);
    }

    @Override
    public Region getRegion() {
        return clipboard.getRegion();
    }

    @Override
    public BlockVector3 getDimensions() {
        return clipboard.getDimensions();
    }

    @Override
    public BlockVector3 getOrigin() {
        return clipboard.getOrigin();
    }

    @Override
    public void setOrigin(BlockVector3 origin) {
        clipboard.setOrigin(origin);
    }

    @Override
    public boolean hasBiomes() {
        return clipboard.hasBiomes();
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        return clipboard.getMaximumPoint();
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        return clipboard.getMinimumPoint();
    }
}
