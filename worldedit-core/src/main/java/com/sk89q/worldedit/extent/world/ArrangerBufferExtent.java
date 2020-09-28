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

package com.sk89q.worldedit.extent.world;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.arranger.Action;
import com.sk89q.worldedit.arranger.ApplySideEffectsAction;
import com.sk89q.worldedit.arranger.SetBlockAction;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.SideEffectSet;
import com.sk89q.worldedit.util.collection.BlockMap;
import com.sk89q.worldedit.util.collection.LowMemoryLists;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.block.BlockTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * An extent that buffers actions to be set by arrangers.
 */
public class ArrangerBufferExtent extends AbstractDelegateExtent {

    private final BlockMap<Integer> indexes = BlockMap.create();
    private final ArrayList<Action> actions = new ArrayList<>();
    private final ArrayList<Action> postEditActions = new ArrayList<>();
    private SideEffectSet sideEffectSet = SideEffectSet.defaults();
    private boolean postEditSimulation;

    /**
     * Create a new instance.
     *
     * @param world the world
     */
    public ArrangerBufferExtent(World world) {
        super(world);
    }

    /**
     * Builds the full action list, and clears the internal lists to free memory.
     *
     * @return the full action list
     */
    public List<Action> computeAllActions() {
        return LowMemoryLists.copyWithLowOverhead(actions, postEditActions);
    }

    public boolean isPostEditSimulationEnabled() {
        return postEditSimulation;
    }

    public void setPostEditSimulationEnabled(boolean enabled) {
        this.postEditSimulation = enabled;
    }

    public SideEffectSet getSideEffectSet() {
        return this.sideEffectSet;
    }

    public void setSideEffectSet(SideEffectSet sideEffectSet) {
        this.sideEffectSet = sideEffectSet;
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        return getFullBlock(position).toImmutableState();
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        Integer index = indexes.get(position);
        if (index == null) {
            return BlockTypes.AIR.getDefaultState().toBaseBlock();
        }
        return ((SetBlockAction) actions.get(index)).getBlock();
    }

    @Override
    public <B extends BlockStateHolder<B>> boolean setBlock(BlockVector3 location, B block) throws WorldEditException {
        actions.add(new SetBlockAction(
            location, block.toBaseBlock()
        ));
        indexes.put(location, actions.size() - 1);

        if (postEditSimulation) {
            postEditActions.add(new ApplySideEffectsAction(
                location, getBlock(location), sideEffectSet
            ));
        }

        return true;
    }

}
