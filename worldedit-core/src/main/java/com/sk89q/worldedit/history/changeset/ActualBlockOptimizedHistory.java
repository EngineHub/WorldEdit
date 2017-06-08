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

package com.sk89q.worldedit.history.changeset;

import com.google.common.collect.Iterators;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.util.collection.TupleArrayList;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;


import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An extension of {@link BlockOptimizedHistory} that stores {@link BlockChange}s
 * separately in a compressed byte array.
 *
 * Further optimizations that could be done
 *  - Iterate over the raw uncompressed data instead of constructing the tuple list
 *  - Use a mutable change object instead of constructing new objects each time
 *  - Use a realtime compression algorithm (e.g. LZ4/LZO/ZSTD)
 */
public class ActualBlockOptimizedHistory extends BlockOptimizedHistory {
    /**
    Format
    x (int)
    y (byte)
    z (int)
    combined id/data from (short)
    combined id/data to (short)

    The x,y,z are stored as relative coordinates to the previous block
    The first position is stored as a relative coordinate to the origin
    */
    private static int ENTRY_SIZE = 13;

    /**
     * Origin position
     */
    private int originX,originY,originZ;
    private int lastX,lastY,lastZ;
    private byte[] compressedBlocks;
    private ByteArrayOutputStream outByteArray;
    private DeflaterOutputStream outCompress;
    private DataOutputStream out;
    private HashMap<BlockVector, CompoundTag> previousNBT;
    private HashMap<BlockVector, CompoundTag> currentNBT;
    private int size;
    private BaseBlock air = new BaseBlock(BlockID.AIR);

    public ActualBlockOptimizedHistory() {
        originX = originY = originZ = Integer.MAX_VALUE; // Magic value, but it's well outside the world
        previousNBT = new HashMap<BlockVector, CompoundTag>();
        currentNBT = new HashMap<BlockVector, CompoundTag>();
        init();
    }

    private void init() {
        if (out == null) {
            this.outByteArray = new ByteArrayOutputStream();
            Deflater deflater = new Deflater(4, true);
            deflater.setStrategy(Deflater.FILTERED);
            this.outCompress = new DeflaterOutputStream(outByteArray, deflater, 8192);
            this.out = new DataOutputStream(new BufferedOutputStream(outCompress));
            if (compressedBlocks != null) {
                try {
                    this.outByteArray.write(compressedBlocks);
                } catch (IOException neverHappens) {
                    neverHappens.printStackTrace();
                }
                compressedBlocks = null;
            }
        }
    }

    public void close() {
        if (this.out != null) {
            try {
                this.out.close();
            } catch (IOException neverHappens) {
                neverHappens.printStackTrace();
            }
            this.compressedBlocks = outByteArray.toByteArray();
            this.outByteArray = null;
            this.outCompress = null;
            this.out = null;
        }
    }

    private byte[] getUncompressed() {
        close();
        try {
            Inflater inflate = new Inflater(true);
            inflate.setInput(compressedBlocks);
            byte[] buffer = new byte[size * ENTRY_SIZE];
            inflate.inflate(buffer);
            return buffer;
        } catch (DataFormatException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private TupleArrayList<BlockVector, BaseBlock> getBlockChanges(boolean forward) {
        byte[] raw = getUncompressed();
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(raw));
        TupleArrayList<BlockVector, BaseBlock> changes = new TupleArrayList<BlockVector, BaseBlock>();
        int lx,ly,lz ;
        lx = ly = lz = 0;
        try {
            for (int i = 0; i < raw.length / ENTRY_SIZE; i++) {
                int x = in.readInt() + originX + lx;
                int y = (in.read() + ly + originY) & 0xFF;
                int z = in.readInt() + originZ + lz;
                lx = x;
                ly = y;
                lz = z;
                BlockVector position = new BlockVector(x, y, z);
                BaseBlock block;
                if (forward) {
                    in.readChar();
                    int combinedTo = in.readChar();
                    CompoundTag nbt = currentNBT.isEmpty() ? null : currentNBT.get(position);
                    block = new BaseBlock(combinedTo >> 4, combinedTo & 0xF, nbt);
                } else {
                    int combinedFrom = in.readChar();
                    CompoundTag nbt = previousNBT.isEmpty() ? null : previousNBT.get(position);
                    in.readChar();
                    block = new BaseBlock(combinedFrom >> 4, combinedFrom & 0xF, nbt);
                }
                changes.put(position, block);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return changes;
    }

    @Override
    public void add(Change change) {
        checkNotNull(change);

        if (change instanceof BlockChange) {
            if (out == null) {
                init();
            }
            BlockChange blockChange = (BlockChange) change;
            BlockVector pos = blockChange.getPosition();
            if (originX == Integer.MAX_VALUE) {
                originX = pos.getBlockX();
                originY = pos.getBlockY();
                originZ = pos.getBlockZ();
            }
            int x = -originX - lastX + (lastX = pos.getBlockX());
            byte y = (byte) (-originY - lastY + (lastY = pos.getBlockY()));
            int z = -originZ - lastZ + (lastZ = pos.getBlockZ());
            BaseBlock from = blockChange.getPrevious();
            BaseBlock to = blockChange.getCurrent();
            if (from.hasNbtData()) {
                previousNBT.put(pos, from.getNbtData());
            }
            if (to.hasNbtData()) {
                currentNBT.put(pos, to.getNbtData());
            }
            char combinedFrom = (char) ((from.getId() << 4) + from.getData());
            char combinedTo = (char) ((to.getId() << 4) + to.getData());
            try {
                out.writeInt(x);
                out.writeByte(y);
                out.writeInt(z);
                out.writeChar(combinedFrom);
                out.writeChar(combinedTo);
            } catch (IOException neverHappens) {
                neverHappens.printStackTrace();
            }
            size++;
        } else {
            super.add(change);
        }
    }

    @Override
    public Iterator<Change> forwardIterator() {
        return Iterators.concat(
                super.forwardIterator(),
                Iterators.transform(getBlockChanges(true).iterator(), createTransform()));
    }

    @Override
    public Iterator<Change> backwardIterator() {
        return Iterators.concat(
                super.backwardIterator(),
                Iterators.transform(getBlockChanges(false).iterator(true), createTransform()));
    }

    @Override
    public int size() {
        return super.size() + size;
    }
}
