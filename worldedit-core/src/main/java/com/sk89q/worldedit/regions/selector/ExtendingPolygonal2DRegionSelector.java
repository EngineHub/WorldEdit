/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.regions.selector;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector2;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;

import java.util.List;
import javax.annotation.Nullable;

/**
 * Creates a {@code Polygonal2DRegion} from a user's selections by expanding
 * the region on every right click.
 */
public class ExtendingPolygonal2DRegionSelector extends Polygonal2DRegionSelector {

    /**
     * Create a new selector with a {@code null} world.
     */
    public ExtendingPolygonal2DRegionSelector() {
        super((World) null);
    }

    /**
     * Create a new selector.
     *
     * @param world the world, which may be {@code null}
     */
    public ExtendingPolygonal2DRegionSelector(@Nullable World world) {
        super(world);
    }

    /**
     * Create a new selector from another one.
     *
     * @param oldSelector the other selector
     */
    public ExtendingPolygonal2DRegionSelector(RegionSelector oldSelector) {
        super(oldSelector);
    }

    /**
     * Create a new selector.
     *
     * @param world the world
     * @param points a list of points
     * @param minY the minimum Y
     * @param maxY the maximum Y
     */
    public ExtendingPolygonal2DRegionSelector(@Nullable World world, List<BlockVector2> points, int minY, int maxY) {
        super(world, points, minY, maxY);
    }

    private static double distanceFromLine(Vector2 position, Vector2 start, Vector2 end) {
        // Vector from start to end
        final Vector2 difference = end.subtract(start);

        // Distance between start and end
        final double length = difference.length();

        // Normalized direction vector pointing from start towards end
        final Vector2 normalizedDifference = difference.divide(length);

        // Distance from start to position, projected onto the line
        final double determinant = position.subtract(start).dot(normalizedDifference);

        if (determinant <= 0) {
            // We're before the line start
            return position.distance(start);
        } else if (determinant >= length) {
            // We're beyond the line end
            return position.distance(end);
        } else {
            // We're next to the line

            // Calculate normalized normal by rotating the normalized difference 90°
            final Vector2 normalizedNormal = new Vector2(-normalizedDifference.z(), normalizedDifference.x());

            // Calculate and return the absolute distance along that normal
            return Math.abs(position.subtract(start).dot(normalizedNormal));
        }
    }

    @Override
    public boolean selectSecondary(BlockVector3 position, SelectorLimits limits) {
        // Use the regular algo until we have at least 3 points
        if (region.size() < 3) {
            return super.selectSecondary(position, limits);
        }

        final BlockVector2 blockPosition2D = position.toBlockVector2();
        final Vector2 position2D = blockPosition2D.toVector2();

        // Find the closest edge
        final List<BlockVector2> points = region.getPoints();
        double minDistance = Integer.MAX_VALUE;
        int insertBeforeIndex = -1;

        // Start with the last vertex to close the loop
        Vector2 previousPoint = points.getLast().toVector2();

        for (int i = 0; i < points.size(); i++) {
            final BlockVector2 blockPoint = points.get(i);

            // Don't duplicate points
            if (blockPoint.equals(blockPosition2D)) {
                return false;
            }

            final Vector2 point = blockPoint.toVector2();

            // Calculate distance from edge
            final double currentDistance = distanceFromLine(position2D, previousPoint, point);
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                insertBeforeIndex = i;
            }

            // Keep previousPoint for the next iteration
            previousPoint = point;
        }

        assert insertBeforeIndex >= 0;

        // Insert new point on the determined edge
        region.addPoint(insertBeforeIndex, blockPosition2D);
        region.expandY(position.y());

        return true;
    }

    @Override
    public void explainSecondarySelection(Actor player, LocalSession session, BlockVector3 pos) {
        player.printInfo(TranslatableComponent.of(
                "worldedit.selection.polygon2d.explain.secondary",
                TextComponent.of(region.size()),
                TextComponent.of(pos.toString())
        ));

        explainRegionAdjust(player, session);
    }
}
