package com.sk89q.worldedit.shape;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.expression.runtime.ExpressionEnvironment;

public class WorldEditExpressionEnvironment implements ExpressionEnvironment {
    private final Vector unit;
    private final Vector zero2;
    private Vector current = new Vector();
    private EditSession editSession;

    public WorldEditExpressionEnvironment(EditSession editSession, Vector unit, Vector zero) {
        this.editSession = editSession;
        this.unit = unit;
        this.zero2 = zero.add(0.5, 0.5, 0.5);
    }

    public BlockVector toWorld(double x, double y, double z) {
        // unscale, unoffset, round-nearest
        return new Vector(x, y, z).multiply(unit).add(zero2).toBlockPoint();
    }

    public Vector toWorldRel(double x, double y, double z) {
        return current.add(x, y, z);
    }

    @Override
    public int getBlockType(double x, double y, double z) {
        return editSession.getBlockType(toWorld(x, y, z));
    }

    @Override
    public int getBlockData(double x, double y, double z) {
        return editSession.getBlockData(toWorld(x, y, z));
    }

    @Override
    public int getBlockTypeAbs(double x, double y, double z) {
        return editSession.getBlockType(new Vector(x, y, z));
    }

    @Override
    public int getBlockDataAbs(double x, double y, double z) {
        return editSession.getBlockData(new Vector(x, y, z));
    }

    @Override
    public int getBlockTypeRel(double x, double y, double z) {
        return editSession.getBlockType(toWorldRel(x, y, z));
    }

    @Override
    public int getBlockDataRel(double x, double y, double z) {
        return editSession.getBlockData(toWorldRel(x, y, z));
    }

    public void setCurrentBlock(Vector current) {
        this.current = current;
    }
}
