package com.sk89q.worldedit.internal.expression.runtime;

/**
 * Represents a way to access blocks in a world. Has to accept non-rounded coordinates.
 */
public interface ExpressionEnvironment {
    int getBlockType(double x, double y, double z);
    int getBlockData(double x, double y, double z);
    int getBlockTypeAbs(double x, double y, double z);
    int getBlockDataAbs(double x, double y, double z);
    int getBlockTypeRel(double x, double y, double z);
    int getBlockDataRel(double x, double y, double z);
}
