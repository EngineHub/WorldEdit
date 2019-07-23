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

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.action.BlockWorldAction;
import com.sk89q.worldedit.action.WorldAction;
import com.sk89q.worldedit.reorder.arrange.Arranger;
import com.sk89q.worldedit.reorder.arrange.ArrangerContext;
import com.sk89q.worldedit.reorder.arrange.WorldActionOutputStream;
import com.sk89q.worldedit.reorder.arrange.SimpleAttributeKey;
import com.sk89q.worldedit.reorder.buffer.MutableArrayWorldActionBuffer;
import com.sk89q.worldedit.reorder.buffer.MutableWorldActionBuffer;
import com.sk89q.worldedit.reorder.buffer.WorldActionBuffer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ChunkBatchingArranger implements Arranger {

    private static final SimpleAttributeKey<Map<BlockVector3, List<BlockWorldAction>>>
        CHUNK_STORAGE = SimpleAttributeKey.create("chunkStorage", Object2ObjectOpenHashMap::new);
    private static final SimpleAttributeKey<Boolean> IMMEDIATE_PENDING
        = SimpleAttributeKey.create("immediatePending", () -> false);

    @Override
    public void onWrite(ArrangerContext stream, WorldActionBuffer buffer) {
        // for actions we don't batch:
        List<WorldAction> immediateTransfer = new ArrayList<>();
        Map<BlockVector3, List<BlockWorldAction>> chunkStorage = CHUNK_STORAGE.get(stream);
        while (buffer.hasRemaining()) {
            WorldAction next = buffer.get();
            if (!(next instanceof BlockWorldAction)) {
                immediateTransfer.add(next);
                continue;
            }
            BlockWorldAction bwa = (BlockWorldAction) next;
            BlockVector3 chunkKey = bwa.getPosition().shr(4);
            List<BlockWorldAction> chunkMap = chunkStorage.computeIfAbsent(chunkKey,
                k -> new ObjectArrayList<>());
            chunkMap.add(bwa);
        }
        if (immediateTransfer.size() > 0) {
            stream.write(MutableArrayWorldActionBuffer.wrap(
                immediateTransfer.toArray(new WorldAction[0])
            ));
            IMMEDIATE_PENDING.set(stream, true);
        }
    }

    private void write(WorldActionOutputStream stream, List<BlockWorldAction> chunkMap) {
        MutableWorldActionBuffer data = MutableArrayWorldActionBuffer.allocate(chunkMap.size());
        chunkMap.toArray(data.array());
        stream.write(data);
        // flush after each chunk, to mark it as a group
        stream.flush();
    }

    @Override
    public void onFlush(ArrangerContext stream) {
        if (IMMEDIATE_PENDING.get(stream)) {
            // flush immediate first
            stream.flush();
            IMMEDIATE_PENDING.set(stream, false);
        }
        // write & flush chunks
        CHUNK_STORAGE.get(stream).forEach((chunkKey, chunkMap) -> write(stream, chunkMap));
    }
}
