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

package com.sk89q.worldedit.extent.transform;

import com.google.common.collect.Maps;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockData;
import com.sk89q.worldedit.blocks.BlockType;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.registry.BlockRegistry;
import com.sk89q.worldedit.world.registry.State;
import com.sk89q.worldedit.world.registry.StateValue;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Transforms blocks themselves (but not their position) according to a
 * given transform.
 */
public class BlockTransformExtent extends AbstractDelegateExtent {

    private static final double RIGHT_ANGLE = Math.toRadians(90);

    private final Transform transform;
    private final BlockRegistry blockRegistry;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     * @param blockRegistry the block registry used for block direction data
     */
    public BlockTransformExtent(Extent extent, Transform transform, BlockRegistry blockRegistry) {
        super(extent);
        checkNotNull(transform);
        checkNotNull(blockRegistry);
        this.transform = transform;
        this.blockRegistry = blockRegistry;
    }

    /**
     * Get the transform.
     *
     * @return the transform
     */
    public Transform getTransform() {
        return transform;
    }

    /**
     * Transform a block without making a copy.
     *
     * @param block the block
     * @param reverse true to transform in the opposite direction
     * @return the same block
     */
    private BaseBlock transformBlock(BaseBlock block, boolean reverse) {
        transform(block, reverse ? transform.inverse() : transform, blockRegistry);
        return block;
    }

    @Override
    public BaseBlock getBlock(Vector position) {
        return transformBlock(super.getBlock(position), false);
    }

    @Override
    public BaseBlock getLazyBlock(Vector position) {
        return transformBlock(super.getLazyBlock(position), false);
    }

    @Override
    public boolean setBlock(Vector location, BaseBlock block) throws WorldEditException {
        return super.setBlock(location, transformBlock(new BaseBlock(block), true));
    }


    /**
     * Transform the given block using the given transform.
     *
     * <p>The provided block is modified.</p>
     *
     * @param block the block
     * @param transform the transform
     * @param registry the registry
     * @return the same block
     */
    public static BaseBlock transform(BaseBlock block, Transform transform, BlockRegistry registry) {
        return transform(block, transform, registry, block);
    }

    private static Map<BlockType,BlockRotator> LEGACY_ROTATION = Maps.newHashMap();

    static {
        List<BlockRotator> rotators = new ArrayList<BlockRotator>();
        rotators.add(new LegacyBlockRotator(BlockType.WOODEN_DOOR));
        rotators.add(new LegacyBlockRotator(BlockType.IRON_DOOR));

        rotators.add(new LegacyBlockRotator(BlockType.ACACIA_DOOR));
        rotators.add(new LegacyBlockRotator(BlockType.BIRCH_DOOR));
        rotators.add(new LegacyBlockRotator(BlockType.DARK_OAK_DOOR));
        rotators.add(new LegacyBlockRotator(BlockType.JUNGLE_DOOR));
        rotators.add(new LegacyBlockRotator(BlockType.SPRUCE_DOOR));

        for(BlockRotator r : rotators){
            LEGACY_ROTATION.put(r.getBlockID(),r);
        }
    }

    /**
     * Transform the given block using the given transform.
     *
     * @param block the block
     * @param transform the transform
     * @param registry the registry
     * @param changedBlock the block to change
     * @return the changed block
     */
    private static BaseBlock transform(BaseBlock block, Transform transform, BlockRegistry registry, BaseBlock changedBlock) {
        checkNotNull(block);
        checkNotNull(transform);
        checkNotNull(registry);

        Map<String, ? extends State> states = registry.getStates(block);

        // --- dirty hack, special case iron and wooden doors (the newer wood doors dont seem to work :/ )
        BlockRotator rotator = LEGACY_ROTATION.get(BlockType.fromID(changedBlock.getId()));
        if(rotator != null){
            Vector nx = transform.apply(Vector.UNIT_X).subtract(transform.apply(Vector.ZERO)).normalize();
            WorldEdit.logger.info("Dot:  " + (nx.dot(Vector.UNIT_X)));
            WorldEdit.logger.info("Arccos:  " + Math.acos(nx.dot(Vector.UNIT_X)));
            double angle = Math.toDegrees(Math.acos(nx.dot(Vector.UNIT_X)));
            WorldEdit.logger.info("Angle:  " + angle);
            int n = (int) Math.round(angle/90);
            WorldEdit.logger.info("Rotating 90x " + n);
            for(int i=0;i<n;i++){
                changedBlock = rotator.rotate90CW(changedBlock);
            }
            return changedBlock;
        }

        if (states == null) {
            return changedBlock;
        }

        for (State state : states.values()) {
            if (state.hasDirection()) {
                StateValue value = state.getValue(block);
                if (value != null && value.getDirection() != null) {
                    StateValue newValue = getNewStateValue(state, transform, value.getDirection());
                    if (newValue != null) {
                        newValue.set(changedBlock);
                    }
                }
            }
        }

        return changedBlock;
    }

    /**
     * Get the new value with the transformed direction.
     *
     * @param state the state
     * @param transform the transform
     * @param oldDirection the old direction to transform
     * @return a new state or null if none could be found
     */
    @Nullable
    private static StateValue getNewStateValue(State state, Transform transform, Vector oldDirection) {
        Vector newDirection = transform.apply(oldDirection).subtract(transform.apply(Vector.ZERO)).normalize();
        StateValue newValue = null;
        double closest = -2;
        boolean found = false;

        for (StateValue v : state.valueMap().values()) {
            if (v.getDirection() != null) {
                double dot = v.getDirection().normalize().dot(newDirection);
                if (dot >= closest) {
                    closest = dot;
                    newValue = v;
                    found = true;
                }
            }
        }

        if (found) {
            return newValue;
        } else {
            return null;
        }
    }

    private static final class LegacyBlockRotator implements BlockRotator {
        private BlockType type;

        public LegacyBlockRotator(BlockType type) {
            this.type = type;
        }

        @Override
        public BaseBlock rotate90CW(BaseBlock block) {
            int newData = BlockData.rotate90(block.getType(),block.getData());
            WorldEdit.logger.info(String.format("[Legacy Rotation] Type: %d Data: %d -> %d",block.getType(),block.getData(),newData));
            block.setData(newData);
            return block;
        }

        @Override
        public BlockType getBlockID() {
            return type;
        }
    }

    private interface BlockRotator {
        BaseBlock rotate90CW(BaseBlock block);
        BlockType getBlockID();
    }
}
