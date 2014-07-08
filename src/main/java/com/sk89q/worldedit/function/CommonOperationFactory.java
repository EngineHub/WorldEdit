/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.function;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.extent.buffer.ForgetfulExtentBuffer;
import com.sk89q.worldedit.function.block.BlockCounter;
import com.sk89q.worldedit.function.block.BlockReplace;
import com.sk89q.worldedit.function.block.Naturalizer;
import com.sk89q.worldedit.function.mask.*;
import com.sk89q.worldedit.function.operation.*;
import com.sk89q.worldedit.function.pattern.BlockPattern;
import com.sk89q.worldedit.function.pattern.Pattern;
import com.sk89q.worldedit.function.pattern.Patterns;
import com.sk89q.worldedit.function.util.RegionOffset;
import com.sk89q.worldedit.function.visitor.DownwardVisitor;
import com.sk89q.worldedit.function.visitor.LayerVisitor;
import com.sk89q.worldedit.function.visitor.RecursiveVisitor;
import com.sk89q.worldedit.function.visitor.RegionVisitor;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.regions.EllipsoidRegion;
import com.sk89q.worldedit.regions.FlatRegion;
import com.sk89q.worldedit.regions.Region;

import javax.annotation.Nullable;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.sk89q.worldedit.regions.Regions.maximumBlockY;
import static com.sk89q.worldedit.regions.Regions.minimumBlockY;

public final class CommonOperationFactory {
    private CommonOperationFactory() {
    }

    public static CountingOperation countBlocks(EditSession session, Region region, Set<BaseBlock> searchBlocks) {
        FuzzyBlockMask mask = new FuzzyBlockMask(session, searchBlocks);
        BlockCounter count = new BlockCounter();
        RegionMaskingFilter filter = new RegionMaskingFilter(mask, count);
        RegionVisitor visitor = new RegionVisitor(region, filter);
        return new CountDelegatedOperation(visitor, count);
    }

    public static CountingOperation fillXZ(EditSession session, Vector origin, Pattern pattern, double radius, int depth, boolean recursive) {
        checkArgument(radius >= 0, "radius >= 0");
        checkArgument(depth >= 1, "depth >= 1");

        MaskIntersection mask = new MaskIntersection(
                new RegionMask(new EllipsoidRegion(null, origin, new Vector(radius, radius, radius))),
                new BoundedHeightMask(
                        Math.max(origin.getBlockY() - depth + 1, 0),
                        Math.min(session.getWorld().getMaxY(), origin.getBlockY())),
                Masks.negate(new ExistingBlockMask(session)));

        // Want to replace blocks
        BlockReplace replace = new BlockReplace(session, pattern);

        // Pick how we're going to visit blocks
        RecursiveVisitor visitor;
        if (recursive) {
            visitor = new RecursiveVisitor(mask, replace);
        } else {
            visitor = new DownwardVisitor(mask, replace, origin.getBlockY());
        }
        // Start at the origin
        visitor.visit(origin);

        return visitor;
    }

    public static CountingOperation setBlocks(EditSession session, Region region, Pattern pattern) {
        BlockReplace replace = new BlockReplace(session, pattern);
        return new RegionVisitor(region, replace);
    }

    public static CountingOperation replaceBlocks(EditSession session, Region region, Mask mask, Pattern pattern) {
        BlockReplace replace = new BlockReplace(session, pattern);
        RegionMaskingFilter filter = new RegionMaskingFilter(mask, replace);
        return new RegionVisitor(region, filter);
    }

    public static CountingOperation groundOverlay(EditSession session, FlatRegion region, Pattern pattern) {
        BlockReplace replace = new BlockReplace(session, pattern);
        RegionOffset offset = new RegionOffset(new Vector(0, 1, 0), replace);
        GroundFunction ground = new GroundFunction(new ExistingBlockMask(session), offset);
        LayerVisitor visitor = new LayerVisitor(region, minimumBlockY(region), maximumBlockY(region), ground);

        return new CountDelegatedOperation(visitor, ground);
    }

    public static CountingOperation naturalize(EditSession session, FlatRegion region) {
        Naturalizer naturalizer = new Naturalizer(session);
        LayerVisitor visitor = new LayerVisitor(region, minimumBlockY(region), maximumBlockY(region), naturalizer);

        return new CountDelegatedOperation(visitor, naturalizer);
    }

    public static CountingOperation stackCubiodRegion(EditSession session, Region region, Vector dir, int count, boolean copyAir) {
        checkArgument(count >= 1, "count >= 1 required");

        Vector size = region.getMaximumPoint().subtract(region.getMinimumPoint()).add(1, 1, 1);
        Vector to = region.getMinimumPoint();
        ForwardExtentCopy copy = new ForwardExtentCopy(session, region, session, to);
        copy.setRepetitions(count);
        copy.setTransform(new AffineTransform().translate(dir.multiply(size)));
        if (!copyAir) {
            copy.setSourceMask(new ExistingBlockMask(session));
        }
        return copy;
    }

    public static CountingOperation moveRegion(EditSession session, Region region, Vector dir, int distance, boolean copyAir, @Nullable BaseBlock replacement) {
        checkArgument(distance >= 1, "distance >= 1 required");
        Vector to = region.getMinimumPoint();

        if (replacement == null) {
            replacement = new BaseBlock(BlockID.AIR);
        }
        // Remove original blocks
        BlockReplace remove = new BlockReplace(session, new BlockPattern(replacement));

        // Copy to a buffer so we don't destroy our original before we can copy all the blocks from it
        ForgetfulExtentBuffer buffer = new ForgetfulExtentBuffer(session, new RegionMask(region));
        ForwardExtentCopy copy = new ForwardExtentCopy(session, region, buffer, to);
        copy.setTransform(new AffineTransform().translate(dir.multiply(distance)));
        copy.setSourceFunction(remove); // Remove
        if (!copyAir) {
            copy.setSourceMask(new ExistingBlockMask(session));
        }

        // Then we need to copy the buffer to the world
        BlockReplace replace = new BlockReplace(session, buffer);
        RegionVisitor placer = new RegionVisitor(buffer.asRegion(), replace);

        return new CountDelegatedOperation(new OperationQueue(copy, placer), copy);
    }
}
