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

package com.sk89q.worldedit.cli.schematic;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.cli.CLIWorld;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import javax.annotation.Nullable;

public class ClipboardWorld extends AbstractWorld implements Clipboard, CLIWorld {

    private final File file;
    private final Clipboard clipboard;
    private final String name;

    private boolean dirty = false;

    public ClipboardWorld(File file, Clipboard clipboard, String name) {
        this.file = file;
        this.clipboard = clipboard;
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getId() {
        return getName().replace(" ", "_").toLowerCase(Locale.ROOT);
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 position, B block, boolean notifyAndLight)
            throws WorldEditException {
        dirty = true;
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
        dirty = true;
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
        dirty = true;
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
        dirty = true;
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

    @Override
    public void save(boolean force) {
        if (dirty || force) {
            try (ClipboardWriter writer = ClipboardFormats.findByFile(file).getWriter(new FileOutputStream(file))) {
                writer.write(this);
                dirty = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
