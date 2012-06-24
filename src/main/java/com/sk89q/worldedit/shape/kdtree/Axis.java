package com.sk89q.worldedit.shape.kdtree;

import com.sk89q.worldedit.Vector;

public enum Axis {
    X {
        @Override
        public double dot(Vector other) {
            return other.getX();
        }

        @Override
        public Vector multiply(double n) {
            return new Vector(n, 0.0, 0.0);
        }
    },

    Y {
        @Override
        public double dot(Vector other) {
            return other.getY();
        }

        @Override
        public Vector multiply(double n) {
            return new Vector(0.0, n, 0.0);
        }
    },

    Z {
        @Override
        public double dot(Vector other) {
            return other.getZ();
        }

        @Override
        public Vector multiply(double n) {
            return new Vector(0.0, 0.0, n);
        }
    };

    public abstract double dot(Vector other);
    public abstract Vector multiply(double n);
}
