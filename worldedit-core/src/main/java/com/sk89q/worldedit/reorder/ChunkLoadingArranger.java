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

import com.sk89q.worldedit.action.ChunkLoad;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.reorder.arrange.Arranger;
import com.sk89q.worldedit.reorder.arrange.ArrangerContext;

public class ChunkLoadingArranger implements Arranger {

    @Override
    public void rearrange(ArrangerContext context) {
        BlockVector2 chunkPos = null;
        int start = 0;
        for (int i = 0; i < context.getActionCount(); i++) {
            BlockVector3 blockPos = WorldActionUtil.worldActionAsBlockVector3(context.getAction(i));
            if (blockPos == null) {
                continue;
            }
            BlockVector2 thisCp = blockPos.toBlockVector2().shr(4);
            if (chunkPos == null) {
                chunkPos = thisCp;
                continue;
            }
            if (!chunkPos.equals(thisCp)) {
                context.getActionWriteList().add(start, ChunkLoad.create(chunkPos));
                chunkPos = thisCp;
                i++; // account for add
                start = i;
            }
        }
        if (chunkPos != null) {
            context.getActionWriteList().add(start, ChunkLoad.create(chunkPos));
        }
        context.markGroup(0, context.getActionCount());
    }
}
