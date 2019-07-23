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

package com.sk89q.worldedit.reorder;

import com.sk89q.worldedit.action.WorldAction;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.RegionOptimizedComparator;
import com.sk89q.worldedit.reorder.arrange.Arranger;
import com.sk89q.worldedit.reorder.arrange.ArrangerContext;

import java.util.Comparator;
import java.util.List;

public final class ChunkBatchingArranger implements Arranger {

    private static final Comparator<WorldAction> WORLD_ACTION_COMPARATOR =
        Comparator.comparing(WorldActionUtil::worldActionAsBlockVector3,
            RegionOptimizedComparator.INSTANCE);

    @Override
    public void rearrange(ArrangerContext context) {
        if (context.getActionCount() == 0) {
            context.markGroup(0, 0);
            return;
        }
        List<WorldAction> actions = context.getActionWriteList();
        actions.sort(WORLD_ACTION_COMPARATOR);
        BlockVector2 chunkPos = null;
        int groupStart = 0;
        for (int i = 0; i < actions.size(); i++) {
            BlockVector3 blockPos = WorldActionUtil.worldActionAsBlockVector3(actions.get(i));
            if (blockPos == null) {
                blockPos = WorldActionUtil.MIN_VECTOR;
            }
            BlockVector2 thisCp = blockPos.toBlockVector2().shr(4);
            if (chunkPos == null) {
                chunkPos = thisCp;
                continue;
            }
            if (!chunkPos.equals(thisCp)) {
                chunkPos = thisCp;
                context.markGroup(groupStart, i);
                groupStart = i;
            }
        }
        context.markGroup(groupStart, actions.size());
    }

}
