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
import com.sk89q.worldedit.internal.cui.SelectionShapeEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.limit.SelectorLimits;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.event.ClickEvent;
import com.sk89q.worldedit.util.formatting.text.event.HoverEvent;
import com.sk89q.worldedit.world.World;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Creates a {@code CuboidRegion} from a user's selections.
 */
public class CuboidRegionSelector implements RegionSelector, CUIRegion {

    protected transient BlockVector3 position1;
    protected transient BlockVector3 position2;
    protected transient CuboidRegion region;

    /**
     * Create a new region selector with a {@code null} world.
     */
    public CuboidRegionSelector() {
        this((World) null);
    }

    /**
     * Create a new region selector.
     *
     * @param world the world, which may be {@code null}
     */
    public CuboidRegionSelector(@Nullable World world) {
        region = new CuboidRegion(world, BlockVector3.ZERO, BlockVector3.ZERO);
    }

    /**
     * Create a copy of another selector.
     *
     * @param oldSelector another selector
     */
    public CuboidRegionSelector(RegionSelector oldSelector) {
        this(checkNotNull(oldSelector).getIncompleteRegion().getWorld());

        if (oldSelector instanceof CuboidRegionSelector) {
            final CuboidRegionSelector cuboidRegionSelector = (CuboidRegionSelector) oldSelector;

            position1 = cuboidRegionSelector.position1;
            position2 = cuboidRegionSelector.position2;
        } else {
            final Region oldRegion;
            try {
                oldRegion = oldSelector.getRegion();
            } catch (IncompleteRegionException e) {
                return;
            }

            position1 = oldRegion.getMinimumPoint();
            position2 = oldRegion.getMaximumPoint();
        }

        region.setPos1(position1);
        region.setPos2(position2);
    }

    /**
     * Create a new region selector with the given two positions.
     *
     * @param world the world
     * @param position1 position 1
     * @param position2 position 2
     */
    public CuboidRegionSelector(@Nullable World world, BlockVector3 position1, BlockVector3 position2) {
        this(world);
        checkNotNull(position1);
        checkNotNull(position2);
        this.position1 = position1;
        this.position2 = position2;
        region.setPos1(position1);
        region.setPos2(position2);
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

        if (position.equals(position1)) {
            return false;
        }

        position1 = position;
        region.setPos1(position1);
        return true;
    }

    @Override
    public boolean selectSecondary(BlockVector3 position, SelectorLimits limits) {
        checkNotNull(position);

        if (position.equals(position2)) {
            return false;
        }

        position2 = position;
        region.setPos2(position2);
        return true;
    }

    @Override
    public void explainPrimarySelection(Actor player, LocalSession session, BlockVector3 pos) {
        checkNotNull(player);
        checkNotNull(session);
        checkNotNull(pos);

        if (position1 != null && position2 != null) {
            player.printInfo(TranslatableComponent.of(
                    "worldedit.selection.cuboid.explain.primary-area",
                    TextComponent.of(position1.toString()),
                    TextComponent.of(region.getVolume())
            ));
        } else if (position1 != null) {
            player.printInfo(TranslatableComponent.of("worldedit.selection.cuboid.explain.primary", TextComponent.of(position1.toString())));
        }

        session.dispatchCUIEvent(player, new SelectionPointEvent(0, pos, getVolume()));
    }

    @Override
    public void explainSecondarySelection(Actor player, LocalSession session, BlockVector3 pos) {
        checkNotNull(player);
        checkNotNull(session);
        checkNotNull(pos);

        if (position1 != null && position2 != null) {
            player.printInfo(TranslatableComponent.of(
                    "worldedit.selection.cuboid.explain.secondary-area",
                    TextComponent.of(position2.toString()),
                    TextComponent.of(region.getVolume())
            ));
        } else if (position2 != null) {
            player.printInfo(TranslatableComponent.of("worldedit.selection.cuboid.explain.secondary", TextComponent.of(position2.toString())));
        }

        session.dispatchCUIEvent(player, new SelectionPointEvent(1, pos, getVolume()));
    }

    @Override
    public void explainRegionAdjust(Actor player, LocalSession session) {
        checkNotNull(player);
        checkNotNull(session);

        session.dispatchCUIEvent(player, new SelectionShapeEvent(getTypeID()));

        if (position1 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(0, position1, getVolume()));
        }

        if (position2 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(1, position2, getVolume()));
        }
    }

    @Override
    public BlockVector3 getPrimaryPosition() throws IncompleteRegionException {
        if (position1 == null) {
            throw new IncompleteRegionException();
        }

        return position1;
    }

    @Override
    public boolean isDefined() {
        return position1 != null && position2 != null;
    }

    @Override
    public CuboidRegion getRegion() throws IncompleteRegionException {
        if (position1 == null || position2 == null) {
            throw new IncompleteRegionException();
        }

        return region;
    }

    @Override
    public CuboidRegion getIncompleteRegion() {
        return region;
    }

    @Override
    public void learnChanges() {
        position1 = region.getPos1();
        position2 = region.getPos2();
    }

    @Override
    public void clear() {
        position1 = null;
        position2 = null;
        region.setPos1(BlockVector3.ZERO);
        region.setPos2(BlockVector3.ZERO);
    }

    @Override
    public String getTypeName() {
        return "cuboid";
    }

    @Override
    public List<Component> getSelectionInfoLines() {
        final List<Component> lines = new ArrayList<>();

        if (position1 != null) {
            lines.add(TranslatableComponent.of("worldedit.selection.cuboid.info.pos1", TextComponent.of(position1.toString())
                    .clickEvent(ClickEvent.of(ClickEvent.Action.COPY_TO_CLIPBOARD, position1.toParserString()))
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to copy")))));
        }

        if (position2 != null) {
            lines.add(TranslatableComponent.of("worldedit.selection.cuboid.info.pos2", TextComponent.of(position2.toString())
                    .clickEvent(ClickEvent.of(ClickEvent.Action.COPY_TO_CLIPBOARD, position2.toParserString()))
                    .hoverEvent(HoverEvent.of(HoverEvent.Action.SHOW_TEXT, TextComponent.of("Click to copy")))));
        }

        return lines;
    }

    @Override
    public long getVolume() {
        if (position1 == null) {
            return -1;
        }

        if (position2 == null) {
            return -1;
        }

        return region.getVolume();
    }

    @Override
    public void describeCUI(LocalSession session, Actor player) {
        if (position1 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(0, position1, getVolume()));
        }

        if (position2 != null) {
            session.dispatchCUIEvent(player, new SelectionPointEvent(1, position2, getVolume()));
        }
    }

    @Override
    public void describeLegacyCUI(LocalSession session, Actor player) {
        describeCUI(session, player);
    }

    @Override
    public int getProtocolVersion() {
        return 0;
    }

    @Override
    public String getTypeID() {
        return "cuboid";
    }

    @Override
    public String getLegacyTypeID() {
        return "cuboid";
    }

}
