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

package com.sk89q.worldedit.command.tool;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalConfiguration;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.command.tool.brush.Brush;
import com.sk89q.worldedit.command.tool.brush.SphereBrush;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.function.mask.MaskIntersection;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Builds a shape at the place being looked at.
 */
public class BrushTool implements TraceTool {

    protected static int MAX_RANGE = 500;
    protected int range = -1;
    private Mask mask = null;
    private Mask traceMask = null;
    private Brush brush = new SphereBrush();
    @Nullable
    private Pattern material;
    private double size = 1;
    private String permission;

    /**
     * Construct the tool.
     *
     * @param permission the permission to check before use is allowed
     */
    public BrushTool(String permission) {
        checkNotNull(permission);
        this.permission = permission;
    }

    @Override
    public boolean canUse(Actor player) {
        return player.hasPermission(permission);
    }

    /**
     * Get the filter.
     *
     * @return the filter
     */
    public Mask getMask() {
        return mask;
    }

    /**
     * Set the block filter used for identifying blocks to replace.
     *
     * @param filter the filter to set
     */
    public void setMask(Mask filter) {
        this.mask = filter;
    }

    /**
     * Get the mask used for identifying where to stop traces.
     *
     * @return the mask used to stop block traces
     */
    public @Nullable Mask getTraceMask() {
        return this.traceMask;
    }

    /**
     * Set the block mask used for identifying where to stop traces.
     *
     * @param traceMask the mask used to stop block traces
     */
    public void setTraceMask(@Nullable Mask traceMask) {
        this.traceMask = traceMask;
    }

    /**
     * Set the brush.
     *
     * @param brush tbe brush
     * @param permission the permission
     */
    public void setBrush(Brush brush, String permission) {
        this.brush = brush;
        this.permission = permission;
    }

    /**
     * Get the current brush.
     *
     * @return the current brush
     */
    public Brush getBrush() {
        return brush;
    }

    /**
     * Set the material.
     *
     * @param material the material
     */
    public void setFill(@Nullable Pattern material) {
        this.material = material;
    }

    /**
     * Get the material.
     *
     * @return the material
     */
    @Nullable public Pattern getMaterial() {
        return material;
    }

    /**
     * Get the set brush size.
     *
     * @return a radius
     */
    public double getSize() {
        return size;
    }

    /**
     * Set the set brush size.
     *
     * @param radius a radius
     */
    public void setSize(double radius) {
        this.size = radius;
    }

    /**
     * Get the set brush range.
     *
     * @return the range of the brush in blocks
     */
    public int getRange() {
        return (range < 0) ? MAX_RANGE : Math.min(range, MAX_RANGE);
    }

    /**
     * Set the set brush range.
     *
     * @param range the range of the brush in blocks
     */
    public void setRange(int range) {
        this.range = range;
    }

    @Override
    public boolean actPrimary(Platform server, LocalConfiguration config, Player player, LocalSession session) {
        Location target = player.getBlockTrace(getRange(), true, traceMask);

        if (target == null) {
            player.printError(TranslatableComponent.of("worldedit.tool.no-block"));
            return true;
        }

        BlockBag bag = session.getBlockBag(player);

        try (EditSession editSession = session.createEditSession(player)) {
            if (mask != null) {
                Mask existingMask = editSession.getMask();

                if (existingMask == null) {
                    editSession.setMask(mask);
                } else if (existingMask instanceof MaskIntersection) {
                    ((MaskIntersection) existingMask).add(mask);
                } else {
                    MaskIntersection newMask = new MaskIntersection(existingMask);
                    newMask.add(mask);
                    editSession.setMask(newMask);
                }
            }

            try {
                brush.build(editSession, target.toVector().toBlockPoint(), material, size);
            } catch (MaxChangedBlocksException e) {
                player.printError(TranslatableComponent.of("worldedit.tool.max-block-changes"));
            } finally {
                session.remember(editSession);
            }
        } finally {
            if (bag != null) {
                bag.flushChanges();
            }
        }

        return true;
    }

}
