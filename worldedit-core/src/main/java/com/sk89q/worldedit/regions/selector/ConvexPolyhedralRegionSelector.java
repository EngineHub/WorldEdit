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

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.internal.cui.CUIRegion;
import com.sk89q.worldedit.internal.cui.SelectionPointEvent;
import com.sk89q.worldedit.internal.cui.SelectionPolygonEvent;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.ConvexPolyhedralRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.polyhedron.Triangle;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates a {@code ConvexPolyhedralRegion} from a user's selections.
 */
public class ConvexPolyhedralRegionSelector implements RegionSelector, CUIRegion {

    private final transient ConvexPolyhedralRegion region;
    private transient BlockVector3 pos1;

    /**
     * Create a new selector with a {@code null} world.
     */
    public ConvexPolyhedralRegionSelector() {
        this((World) null);
    }

    /**
     * Create a new selector.
     *
     * @param world the world, which may be {@code null}
     */
    public ConvexPolyhedralRegionSelector(@Nullable World world) {
        region = new ConvexPolyhedralRegion(world);
    }

    /**
     * Create a new selector.
     *
     * @param oldSelector the old selector
     */
    public ConvexPolyhedralRegionSelector(RegionSelector oldSelector) {
        checkNotNull(oldSelector);

        if (oldSelector instanceof ConvexPolyhedralRegionSelector) {
            final ConvexPolyhedralRegionSelector convexPolyhedralRegionSelector = (ConvexPolyhedralRegionSelector) oldSelector;

            pos1 = convexPolyhedralRegionSelector.pos1;
            region = new ConvexPolyhedralRegion(convexPolyhedralRegionSelector.region);
        } else {
            final Region oldRegion;
            try {
                oldRegion = oldSelector.getRegion();
            } catch (IncompleteRegionException e) {
                region = new ConvexPolyhedralRegion(oldSelector.getIncompleteRegion().getWorld());
                return;
            }

            final int minY = oldRegion.getMinimumPoint().getBlockY();
            final int maxY = oldRegion.getMaximumPoint().getBlockY();

            region = new ConvexPolyhedralRegion(oldRegion.getWorld());

            for (final BlockVector2 pt : new ArrayList<>(oldRegion.polygonize(Integer.MAX_VALUE))) {
                region.addVertex(pt.toBlockVector3(minY));
                region.addVertex(pt.toBlockVector3(maxY));
            }

            learnChanges();
        }
    }

    @Nullable
    @Override
    public World getWorld() {
        return region.getWorld();
    }

    @Override
    public void setWorld(@Nullable World world) {
        region.setWorld(world);
    }

    @Override
    public boolean selectPrimary(BlockVector3 position, SelectorLimits limits) {
        checkNotNull(position);
        clear();
        pos1 = position;
        return region.addVertex(position);
    }

    @Override
    public boolean selectSecondary(BlockVector3 position, SelectorLimits limits) {
        checkNotNull(position);

        Optional<Integer> vertexLimit = limits.getPolyhedronVertexLimit();

        if (vertexLimit.isPresent() && region.getVertices().size() > vertexLimit.get()) {
            return false;
        }

        return region.addVertex(position);
    }

    @Override
    public BlockVector3 getPrimaryPosition() throws IncompleteRegionException {
        return pos1;
    }

    @Override
    public Region getRegion() throws IncompleteRegionException {
        if (!region.isDefined()) {
            throw new IncompleteRegionException();
        }

        return region;
    }

    @Override
    public Region getIncompleteRegion() {
        return region;
    }

    @Override
    public boolean isDefined() {
        return region.isDefined();
    }

    @Override
    public long getVolume() {
        return region.getVolume();
    }

    @Override
    public void learnChanges() {
        pos1 = region.getVertices().iterator().next();
    }

    @Override
    public void clear() {
        region.clear();
    }

    @Override
    public String getTypeName() {
        return "Convex Polyhedron";
    }

    @Override
    public List<Component> getSelectionInfoLines() {
        List<Component> ret = new ArrayList<>();

        ret.add(TranslatableComponent.of("worldedit.selection.convex.info.vertices", TextComponent.of(region.getVertices().size())));
        ret.add(TranslatableComponent.of("worldedit.selection.convex.info.triangles", TextComponent.of(region.getTriangles().size())));

        return ret;
    }

    @Override
    public void explainPrimarySelection(Actor player, LocalSession session, BlockVector3 pos) {
        checkNotNull(player);
        checkNotNull(session);
        checkNotNull(pos);

        session.describeCUI(player);

        player.printInfo(TranslatableComponent.of("worldedit.selection.convex.explain.primary", TextComponent.of(pos.toString())));
    }

    @Override
    public void explainSecondarySelection(Actor player, LocalSession session, BlockVector3 pos) {
        checkNotNull(player);
        checkNotNull(session);
        checkNotNull(pos);

        session.describeCUI(player);

        player.printInfo(TranslatableComponent.of("worldedit.selection.convex.explain.secondary", TextComponent.of(pos.toString())));
    }

    @Override
    public void explainRegionAdjust(Actor player, LocalSession session) {
        checkNotNull(player);
        checkNotNull(session);
        session.describeCUI(player);
    }

    @Override
    public int getProtocolVersion() {
        return 3;
    }

    @Override
    public String getTypeID() {
        return "polyhedron";
    }

    @Override
    public void describeCUI(LocalSession session, Actor player) {
        checkNotNull(player);
        checkNotNull(session);

        Collection<BlockVector3> vertices = region.getVertices();
        Collection<Triangle> triangles = region.getTriangles();

        Map<BlockVector3, Integer> vertexIds = new HashMap<>(vertices.size());
        int lastVertexId = -1;
        for (BlockVector3 vertex : vertices) {
            vertexIds.put(vertex, ++lastVertexId);
            session.dispatchCUIEvent(player, new SelectionPointEvent(lastVertexId, vertex, getVolume()));
        }

        for (Triangle triangle : triangles) {
            final int[] v = new int[3];
            for (int i = 0; i < 3; ++i) {
                v[i] = vertexIds.get(triangle.getVertex(i).toBlockPoint());
            }
            session.dispatchCUIEvent(player, new SelectionPolygonEvent(v));
        }
    }

    @Override
    public String getLegacyTypeID() {
        return "cuboid";
    }

    @Override
    public void describeLegacyCUI(LocalSession session, Actor player) {
        checkNotNull(player);
        checkNotNull(session);

        if (isDefined()) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(0, region.getMinimumPoint(), getVolume()));
            session.dispatchCUIEvent(player, new SelectionPointEvent(1, region.getMaximumPoint(), getVolume()));
        }
    }

}
