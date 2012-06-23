package com.sk89q.worldedit.shape;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.regions.Region;

public class ModelShape extends ArbitraryShape {
    private final Vector offset;
    private final Vector scale;
    private final double radius;
    private final Model model; 

    public ModelShape(Region extent, Model model, boolean preserveAspect) {
        super(extent);
        this.model = model;

        Vector modelMin = model.getMinimumPoint();
        Vector modelMax = model.getMaximumPoint();
        Vector modelSize = modelMax.subtract(modelMin);

        Vector regionMin = extent.getMinimumPoint();
        Vector regionMax = extent.getMaximumPoint();
        Vector regionSize = regionMax.subtract(regionMin);

        Vector anisotropicScale = modelSize.divide(regionSize);

        if (preserveAspect) {
            double maxScale = Math.max(anisotropicScale.getX(), Math.max(anisotropicScale.getY(), anisotropicScale.getZ()));

            Vector modelCenter = modelMin.add(modelMax).multiply(0.5);
            Vector regionCenter = regionMin.add(regionMax).multiply(0.5);

            scale = new Vector(maxScale, maxScale, maxScale);
            offset = modelCenter.divide(maxScale).subtract(regionCenter);

            /*
            (regionCenter+offset)*scale = modelCenter;
            regionCenter+offset = modelCenter/scale;
            offset = modelCenter/scale - regionCenter;
            */
        }
        else {
            scale = anisotropicScale;
            offset = modelMin.divide(scale).subtract(regionMin);

            /*
             I (regionMax+offset)*scale = modelMax;
            II (regionMin+offset)*scale = modelMin;

             I (regionMax-regionMin)*scale = modelMax-modelMin;
            II regionMin+offset = modelMin/scale;

             I scale = (modelMax-modelMin)/(regionMax-regionMin);
            II offset = modelMin/scale - regionMin;
            */
        }

        radius = scale.length()*0.5;
    }

    @Override
    protected BaseBlock getMaterial(int x, int y, int z, BaseBlock defaultMaterial) {
        Vector center = offset.add(x, y, z).multiply(scale);

        Iterable<Vertex> nearby = collectInRadius(center, radius);

        double value = 0;
        Vector normal = new Vector();
        double weightSum = 0;

        for (Vertex vertex : nearby) {
            Vector difference = vertex.getPosition().subtract(center);
            double distance = difference.length();
            double weight = wendland(distance, radius);

            value += difference.dot(vertex.getNormal()) * weight;
            normal = normal.add(vertex.getNormal().multiply(weight));
            weightSum += weight;
        }

        if (weightSum < 0.0000001) {
            final Vertex closestVertex = collectKNearest(center, 1).iterator().next();
            Vector difference = closestVertex.getPosition().subtract(center);
            normal = closestVertex.getNormal();
            value = difference.dot(normal);
        }
        else {
            value /= weightSum;
            normal = normal.normalize();
        }

        return value > 0 ? defaultMaterial : null;
    }

    private static double wendland(double distance, double radius) {
        distance /= radius;

        return Math.pow(1 - distance, 4) * (4 * distance + 1);
    }

    private Iterable<Vertex> collectInRadius(Vector center, double radius) {
        List<Vertex> ret = new ArrayList<Vertex>();

        for (Vertex vertex : model.getVertices()) {
            if (vertex.getPosition().distance(center) <= radius) {
                ret.add(vertex);
            }
        }

        return ret;
    }

    private Iterable<Vertex> collectKNearest(Vector center, int amount) {
        SortedMap<Double, Vertex> ret = new TreeMap<Double, Vertex>(); // a TreeMap will eliminate entries at equal distance. I don't care right now since i only want one entry anyway...

        for (Vertex vertex : model.getVertices()) {
            final double distance = vertex.getPosition().distance(center);

            ret.put(distance, vertex);
            if (ret.size() > amount) {
                ret.remove(ret.lastKey());
            }
        }

        return ret.values();
    }
}
