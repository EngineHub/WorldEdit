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

package com.sk89q.worldedit.function.operation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.CombinedRegionFunction;
import com.sk89q.worldedit.function.RegionFunction;
import com.sk89q.worldedit.function.RegionMaskingFilter;
import com.sk89q.worldedit.function.biome.ExtentBiomeCopy;
import com.sk89q.worldedit.function.block.ExtentBlockCopy;
import com.sk89q.worldedit.function.entity.ExtentEntityCopy;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.Masks;
import com.sk89q.worldedit.function.visitor.EntityVisitor;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.Identity;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Makes a copy of a portion of one extent to another extent or another point.
 *
 * <p>This is a forward extent copy, meaning that it iterates over the blocks
 * in the source extent, and will copy as many blocks as there are in the
 * source. Therefore, interpolation will not occur to fill in the gaps.</p>
 */
public class ForwardExtentCopy implements Operation {

    private final Extent source;
    private final Extent destination;
    private final Region region;
    private final BlockVector3 from;
    private final BlockVector3 to;
    private int repetitions = 1;
    private Mask sourceMask = Masks.alwaysTrue();
    private boolean removingEntities;
    private boolean copyingEntities = true; // default to true for backwards compatibility, sort of
    private boolean copyingBiomes;
    private RegionFunction sourceFunction = null;
    private Transform transform = new Identity();
    private Transform currentTransform = null;

    private RegionVisitor lastVisitor;
    private RegionVisitor lastBiomeVisitor;
    private EntityVisitor lastEntityVisitor;

    private int affectedBlocks;
    private int affectedBiomeCols;
    private int affectedEntities;

    /**
     * Create a new copy using the region's lowest minimum point as the
     * "from" position.
     *
     * @param source the source extent
     * @param region the region to copy
     * @param destination the destination extent
     * @param to the destination position
     * @see #ForwardExtentCopy(Extent, Region, BlockVector3, Extent, BlockVector3) the main constructor
     */
    public ForwardExtentCopy(Extent source, Region region, Extent destination, BlockVector3 to) {
        this(source, region, region.getMinimumPoint(), destination, to);
    }

    /**
     * Create a new copy.
     *
     * @param source the source extent
     * @param region the region to copy
     * @param from the source position
     * @param destination the destination extent
     * @param to the destination position
     */
    public ForwardExtentCopy(Extent source, Region region, BlockVector3 from, Extent destination, BlockVector3 to) {
        checkNotNull(source);
        checkNotNull(region);
        checkNotNull(from);
        checkNotNull(destination);
        checkNotNull(to);
        this.source = source;
        this.destination = destination;
        this.region = region;
        this.from = from;
        this.to = to;
    }

    /**
     * Get the transformation that will occur on every point.
     *
     * <p>The transformation will stack with each repetition.</p>
     *
     * @return a transformation
     */
    public Transform getTransform() {
        return transform;
    }

    /**
     * Set the transformation that will occur on every point.
     *
     * @param transform a transformation
     * @see #getTransform()
     */
    public void setTransform(Transform transform) {
        checkNotNull(transform);
        this.transform = transform;
    }

    /**
     * Get the mask that gets applied to the source extent.
     *
     * <p>This mask can be used to filter what will be copied from the source.</p>
     *
     * @return a source mask
     */
    public Mask getSourceMask() {
        return sourceMask;
    }

    /**
     * Set a mask that gets applied to the source extent.
     *
     * @param sourceMask a source mask
     * @see #getSourceMask()
     */
    public void setSourceMask(Mask sourceMask) {
        checkNotNull(sourceMask);
        this.sourceMask = sourceMask;
    }

    /**
     * Get the function that gets applied to all source blocks <em>after</em>
     * the copy has been made.
     *
     * @return a source function, or null if none is to be applied
     */
    public RegionFunction getSourceFunction() {
        return sourceFunction;
    }

    /**
     * Set the function that gets applied to all source blocks <em>after</em>
     * the copy has been made.
     *
     * @param function a source function, or null if none is to be applied
     */
    public void setSourceFunction(RegionFunction function) {
        this.sourceFunction = function;
    }

    /**
     * Get the number of repetitions left.
     *
     * @return the number of repetitions
     */
    public int getRepetitions() {
        return repetitions;
    }

    /**
     * Set the number of repetitions left.
     *
     * @param repetitions the number of repetitions
     */
    public void setRepetitions(int repetitions) {
        checkArgument(repetitions >= 0, "number of repetitions must be non-negative");
        this.repetitions = repetitions;
    }

    /**
     * Return whether entities should be copied along with blocks.
     *
     * @return true if copying
     */
    public boolean isCopyingEntities() {
        return copyingEntities;
    }

    /**
     * Set whether entities should be copied along with blocks.
     *
     * @param copyingEntities true if copying
     */
    public void setCopyingEntities(boolean copyingEntities) {
        this.copyingEntities = copyingEntities;
    }

    /**
     * Return whether entities that are copied should be removed.
     *
     * @return true if removing
     */
    public boolean isRemovingEntities() {
        return removingEntities;
    }

    /**
     * Set whether entities that are copied should be removed.
     *
     * @param removingEntities true if removing
     */
    public void setRemovingEntities(boolean removingEntities) {
        this.removingEntities = removingEntities;
    }

    /**
     * Return whether biomes should be copied along with blocks.
     *
     * @return true if copying biomes
     */
    public boolean isCopyingBiomes() {
        return copyingBiomes;
    }

    /**
     * Set whether biomes should be copies along with blocks.
     *
     * @param copyingBiomes true if copying
     */
    public void setCopyingBiomes(boolean copyingBiomes) {
        this.copyingBiomes = copyingBiomes;
    }

    /**
     * Get the number of affected objects.
     *
     * @return the number of affected
     */
    public int getAffected() {
        return affectedBlocks + affectedBiomeCols + affectedEntities;
    }

    @Override
    public Operation resume(RunContext run) throws WorldEditException {
        if (lastVisitor != null) {
            affectedBlocks += lastVisitor.getAffected();
            lastVisitor = null;
        }
        if (lastBiomeVisitor != null) {
            affectedBiomeCols += lastBiomeVisitor.getAffected();
            lastBiomeVisitor = null;
        }
        if (lastEntityVisitor != null) {
            affectedEntities += lastEntityVisitor.getAffected();
            lastEntityVisitor = null;
        }

        if (repetitions > 0) {
            repetitions--;

            if (currentTransform == null) {
                currentTransform = transform;
            } else {
                currentTransform = currentTransform.combine(transform);
            }

            ExtentBlockCopy blockCopy = new ExtentBlockCopy(source, from, destination, to, currentTransform);
            RegionMaskingFilter filteredFunction = new RegionMaskingFilter(sourceMask,
                    sourceFunction == null ? blockCopy : new CombinedRegionFunction(blockCopy, sourceFunction));
            RegionVisitor blockVisitor = new RegionVisitor(region, filteredFunction);

            lastVisitor = blockVisitor;

            if (!copyingBiomes && !copyingEntities) {
                return new DelegateOperation(this, blockVisitor);
            }

            List<Operation> ops = Lists.newArrayList(blockVisitor);

            if (copyingBiomes) {
                ExtentBiomeCopy biomeCopy = new ExtentBiomeCopy(source, from,
                        destination, to, currentTransform);
                RegionFunction biomeFunction = sourceFunction == null ? biomeCopy
                        : new RegionMaskingFilter(sourceMask, biomeCopy);
                RegionVisitor biomeVisitor = new RegionVisitor(region, biomeFunction);
                ops.add(biomeVisitor);
                lastBiomeVisitor = biomeVisitor;
            }

            if (copyingEntities) {
                ExtentEntityCopy entityCopy = new ExtentEntityCopy(from.toVector3(), destination, to.toVector3(), currentTransform);
                entityCopy.setRemoving(removingEntities);
                List<? extends Entity> entities = Lists.newArrayList(source.getEntities(region));
                entities.removeIf(entity -> {
                    EntityProperties properties = entity.getFacet(EntityProperties.class);
                    return properties != null && !properties.isPasteable();
                });
                EntityVisitor entityVisitor = new EntityVisitor(entities.iterator(), entityCopy);
                ops.add(entityVisitor);
                lastEntityVisitor = entityVisitor;
            }

            return new DelegateOperation(this, new OperationQueue(ops));
        } else {
            return null;
        }
    }

    @Override
    public void cancel() {
    }

    @Override
    public Iterable<Component> getStatusMessages() {
        return ImmutableList.of(
            TranslatableComponent.of("worldedit.operation.affected.block",
                    TextComponent.of(affectedBlocks)).color(TextColor.LIGHT_PURPLE),
            TranslatableComponent.of("worldedit.operation.affected.biome",
                    TextComponent.of(affectedBiomeCols)).color(TextColor.LIGHT_PURPLE),
            TranslatableComponent.of("worldedit.operation.affected.entity",
                    TextComponent.of(affectedEntities)).color(TextColor.LIGHT_PURPLE)
        );
    }

}
