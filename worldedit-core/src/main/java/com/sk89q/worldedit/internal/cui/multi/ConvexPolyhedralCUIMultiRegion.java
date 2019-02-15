package com.sk89q.worldedit.internal.cui.multi;

import com.sk89q.worldedit.BlockVector2D;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.SelectionMinMaxEvent;
import com.sk89q.worldedit.internal.cui.SelectionPoint2DEvent;
import com.sk89q.worldedit.internal.cui.SelectionPointEvent;
import com.sk89q.worldedit.internal.cui.SelectionPolygonEvent;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.polyhedron.Triangle;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class ConvexPolyhedralCUIMultiRegion extends AbstractCUIMultiRegion {

    private final ConvexPolyhedralRegion region;

    public ConvexPolyhedralCUIMultiRegion(ConvexPolyhedralRegion region, MultiRegionStyle style) {
        super(style);
        checkNotNull(region);
        this.region = region;
    }

    public ConvexPolyhedralCUIMultiRegion(ConvexPolyhedralRegion region, MultiRegionStyle style, double gridSpacing, boolean gridCull) {
        super(style, gridSpacing, gridCull);
        checkNotNull(region);
        this.region = region;
    }

    @Override
    public void describeCUI(LocalSession session, Actor player) {
        super.describeCUI(session, player);
        Collection<Vector> vertices = region.getVertices();
        Collection<Triangle> triangles = region.getTriangles();

        Map<Vector, Integer> vertexIds = new HashMap<Vector, Integer>(vertices.size());
        int lastVertexId = -1;
        for (Vector vertex : vertices) {
            vertexIds.put(vertex, ++lastVertexId);
            session.dispatchClientCUIEvent(player,
                    new WrappedMultiCUIEvent(new SelectionPointEvent(lastVertexId, vertex, region.getArea())),
                    getProtocolVersion());
        }

        for (Triangle triangle : triangles) {
            final int[] v = new int[3];
            for (int i = 0; i < 3; ++i) {
                v[i] = vertexIds.get(triangle.getVertex(i));
            }
            session.dispatchClientCUIEvent(player,
                    new WrappedMultiCUIEvent(new SelectionPolygonEvent(v)),
                    getProtocolVersion());
        }
    }

    @Override
    public String getTypeID() {
        return "polyhedron";
    }
}
