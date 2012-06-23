package com.sk89q.worldedit.shape.kdtree;

import com.sk89q.worldedit.Vector;

class SubTree implements Node {
    private final Vector axis;
    private final double value;
    private final Node left;
    private final Node right;

    public SubTree(Vector axis, double value, Node left, Node right) {
        this.axis = axis;
        this.value = value;
        this.left = left;
        this.right = right;
    }
}
