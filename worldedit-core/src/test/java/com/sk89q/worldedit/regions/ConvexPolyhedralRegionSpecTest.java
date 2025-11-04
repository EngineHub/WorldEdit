/*
 * Tests for ConvexPolyhedralRegion and ConvexPolyhedralRegionSelector edge cases per specification.
 */
package com.sk89q.worldedit.regions;

import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.selector.ConvexPolyhedralRegionSelector;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ConvexPolyhedralRegionSpecTest {

    @Test
    void allCollinearPoints_areUndefined() {
        ConvexPolyhedralRegion region = new ConvexPolyhedralRegion((com.sk89q.worldedit.world.World) null);
        assertTrue(region.addVertex(BlockVector3.at(0, 0, 0)));
        assertTrue(region.addVertex(BlockVector3.at(10, 0, 0)));
        // Third collinear point should be backlogged; no triangles should exist
        assertTrue(region.addVertex(BlockVector3.at(20, 0, 0)));
        assertFalse(region.isDefined());
        assertEquals(0, region.getTriangles().size());
    }

    @Test
    void coplanarPoints_form2DDefinedRegion() {
        ConvexPolyhedralRegion region = new ConvexPolyhedralRegion((com.sk89q.worldedit.world.World) null);
        // Three non-collinear, coplanar points define a 2D region (two triangles)
        assertTrue(region.addVertex(BlockVector3.at(0, 0, 0)));
        assertTrue(region.addVertex(BlockVector3.at(10, 0, 0)));
        assertTrue(region.addVertex(BlockVector3.at(0, 0, 10)));
        assertTrue(region.isDefined());
        assertEquals(2, region.getTriangles().size());

        // Add another coplanar point; should remain planar (still two triangles)
        assertTrue(region.addVertex(BlockVector3.at(10, 0, 10)));
        assertEquals(2, region.getTriangles().size());
    }

    @Test
    void vertexLimitEnforced_includesBacklog() {
        ConvexPolyhedralRegionSelector selector = new ConvexPolyhedralRegionSelector((com.sk89q.worldedit.world.World) null);
        SelectorLimits limits = new SelectorLimits() {
            @Override
            public Optional<Integer> getPolygonVertexLimit() {
                return Optional.empty();
            }

            @Override
            public Optional<Integer> getPolyhedronVertexLimit() {
                // Limit total (vertices + backlog) to 3
                return Optional.of(3);
            }
        };

        // First two vertices
        assertTrue(selector.selectPrimary(BlockVector3.at(0, 0, 0), limits));
        assertTrue(selector.selectSecondary(BlockVector3.at(10, 0, 0), limits));
        // Third collinear point goes to backlog but counts towards limit
        assertTrue(selector.selectSecondary(BlockVector3.at(20, 0, 0), limits));
        // Fourth should be rejected due to limit (>= check)
        assertFalse(selector.selectSecondary(BlockVector3.at(0, 1, 0), limits));
    }

    @Test
    void duplicateVertex_isRejected() {
        ConvexPolyhedralRegion region = new ConvexPolyhedralRegion((com.sk89q.worldedit.world.World) null);
        BlockVector3 a = BlockVector3.at(1, 2, 3);
        assertTrue(region.addVertex(a));
        assertFalse(region.addVertex(a));
    }

    @Test
    void hullBecomes3D_withFourNonCoplanarPoints() {
        ConvexPolyhedralRegion region = new ConvexPolyhedralRegion((com.sk89q.worldedit.world.World) null);
        assertTrue(region.addVertex(BlockVector3.at(0, 0, 0)));
        assertTrue(region.addVertex(BlockVector3.at(10, 0, 0)));
        assertTrue(region.addVertex(BlockVector3.at(0, 0, 10)));
        // Non-coplanar fourth point
        assertTrue(region.addVertex(BlockVector3.at(0, 10, 0)));
        assertTrue(region.isDefined());
        assertTrue(region.getTriangles().size() > 2, "3D hull should have more than 2 triangles");
    }

    @Test
    void backlogProcessingOrder_isMaintained() {
        ConvexPolyhedralRegion region = new ConvexPolyhedralRegion((com.sk89q.worldedit.world.World) null);
        BlockVector3 a = BlockVector3.at(0, 0, 0);
        BlockVector3 b = BlockVector3.at(10, 0, 0);
        BlockVector3 c = BlockVector3.at(5, 0, 0); // collinear -> backlog
        BlockVector3 d = BlockVector3.at(0, 0, 10); // non-collinear

        assertTrue(region.addVertex(a));
        assertTrue(region.addVertex(b));
        assertTrue(region.addVertex(c));
        assertTrue(region.getBacklogVertices().contains(c));

        // Adding d will trigger backlog processing; c should remain backlogged until non-coplanar point added later
        assertTrue(region.addVertex(d));
        assertTrue(region.getBacklogVertices().contains(c));
    }
}
