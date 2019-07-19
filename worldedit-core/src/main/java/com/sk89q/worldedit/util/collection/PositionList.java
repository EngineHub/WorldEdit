package com.sk89q.worldedit.util.collection;

import com.sk89q.worldedit.math.BlockVector3;

import java.util.Iterator;

interface PositionList {

    static PositionList create(boolean extendedYLimit) {
        if (extendedYLimit) {
            return new VectorPositionList();
        }
        return new LongPositionList();
    }

    BlockVector3 get(int index);

    void add(BlockVector3 vector);

    int size();

    void clear();

    Iterator<BlockVector3> iterator();

    Iterator<BlockVector3> reverseIterator();

}
