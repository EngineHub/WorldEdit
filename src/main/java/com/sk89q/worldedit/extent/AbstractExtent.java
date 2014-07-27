package com.sk89q.worldedit.extent;

import com.sk89q.worldedit.function.operation.Operation;

import javax.annotation.Nullable;

/**
 * An abstract implementation of an {@code Extent}.
 */
public abstract class AbstractExtent implements Extent {

    @Nullable
    @Override
    public Operation getInterleaveOperation() {
        return null;
    }

    @Nullable
    @Override
    public Operation getFinalizeOperation() {
        return null;
    }

}
