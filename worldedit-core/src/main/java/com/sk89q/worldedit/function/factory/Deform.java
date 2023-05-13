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

package com.sk89q.worldedit.function.factory;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.InputExtent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.transform.Identity;
import com.sk89q.worldedit.math.transform.SimpleTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.NullRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.Placement;
import com.sk89q.worldedit.session.PlacementType;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.util.GuavaUtil.firstNonNull;

public class Deform implements Contextual<Operation> {

    private Extent destination;
    private Region region;
    private final Expression expression;
    private Mode mode;
    Placement placement;
    private boolean useClipboard;

    public Deform(String expression) {
        this(new NullExtent(), new NullRegion(), expression);
    }

    public Deform(String expression, Mode mode) {
        this(new NullExtent(), new NullRegion(), expression, mode);
    }

    public Deform(Extent destination, Region region, String expression) {
        this(destination, region, expression, Mode.UNIT_CUBE);
    }

    public Deform(Extent destination, Region region, String expression, Mode mode) {
        checkNotNull(destination, "destination");
        checkNotNull(region, "region");
        checkNotNull(mode, "mode");
        checkNotNull(expression, "expression");
        this.expression = Expression.compile(expression, "x", "y", "z");
        this.expression.optimize();
        this.destination = destination;
        this.region = region;
        this.mode = mode;
    }

    public Deform(Placement placement, String expression, Mode mode) {
        checkNotNull(mode, "mode");
        checkNotNull(expression, "expression");

        this.placement = placement;
        this.expression = Expression.compile(expression, "x", "y", "z");
        this.expression.optimize();
        this.mode = mode;
    }


    public Extent getDestination() {
        return destination;
    }

    public void setDestination(Extent destination) {
        checkNotNull(destination, "destination");
        this.destination = destination;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        checkNotNull(region, "region");
        this.region = region;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        checkNotNull(mode, "mode");
        this.mode = mode;
    }

    @Deprecated
    public Vector3 getOffset() {
        if (this.placement.getPlacementType() != PlacementType.WORLD) {
            throw new IllegalStateException("Deform.getOffset is deprecated and only supported after using setOffset.");
        }
        return placement.getOffset().toVector3();
    }

    public void setOffset(Vector3 offset) {
        checkNotNull(offset, "offset");
        this.placement = new Placement(PlacementType.WORLD, offset.toBlockPoint());
    }

    @Override
    public String toString() {
        return "deformation of " + expression.getSource();
    }

    private Transform createTransform(Vector3 min, Vector3 max, Vector3 placement) {
        switch (mode) {
            case UNIT_CUBE:
                final Vector3 zero = max.add(min).multiply(0.5);
                Vector3 unit = max.subtract(zero);

                if (unit.getX() == 0) {
                    unit = unit.withX(1.0);
                }
                if (unit.getY() == 0) {
                    unit = unit.withY(1.0);
                }
                if (unit.getZ() == 0) {
                    unit = unit.withZ(1.0);
                }

                return new SimpleTransform(zero, unit);

            case RAW_COORD:
                return new Identity();

            case OFFSET:
            default:
                return new SimpleTransform(placement, Vector3.ONE);
        }
    }

    @Override
    public Operation createFromContext(final EditContext context) {
        return new DeformOperation(context);
    }

    private class DeformOperation implements Operation {
        private final EditContext context;

        private DeformOperation(EditContext context) {
            this.context = context;
        }

        @Override
        public Operation resume(RunContext run) throws WorldEditException {
            try {
                final Region region = firstNonNull(context.getRegion(), Deform.this.region);

                final Vector3 min = region.getMinimumPoint().toVector3();
                final Vector3 max = region.getMaximumPoint().toVector3();

                final LocalSession session = context.getSession();
                final EditSession editSession = (EditSession) context.getDestination();

                // TODO: deal with session == null
                final BlockVector3 placement = Deform.this.placement.getPlacementPosition(session.getRegionSelector(editSession.getWorld()), editSession.getActor());
                final Transform outputTransform = createTransform(min, max, placement.toVector3());

                final InputExtent inputExtent;
                final Transform inputTransform;
                if (Deform.this.useClipboard && session != null) {
                    final Clipboard clipboard = session.getClipboard().getClipboard();
                    inputExtent = clipboard;

                    final Vector3 clipboardMin = clipboard.getMinimumPoint().toVector3();
                    final Vector3 clipboardMax = clipboard.getMaximumPoint().toVector3();

                    inputTransform = createTransform(clipboardMin, clipboardMax, clipboardMin);
                } else {
                    inputExtent = editSession.getWorld();
                    inputTransform = outputTransform;
                }
                final int timeout = session == null ? WorldEdit.getInstance().getConfiguration().calculationTimeout : session.getTimeout();

                // TODO: Move deformation code
                editSession.deformRegion(region, outputTransform, Deform.this.expression, timeout, inputExtent, inputTransform);
                return null;
            } catch (ExpressionException e) {
                throw new RuntimeException("Failed to execute expression", e); // TODO: Better exception to throw here?
            }
        }

        @Override
        public void cancel() {
        }


        @Override
        public Iterable<Component> getStatusMessages() {
            return ImmutableList.of(TranslatableComponent.of("worldedit.operation.deform.expression",
                    TextComponent.of(Deform.this.expression.getSource()).color(TextColor.LIGHT_PURPLE)));
        }

    }

    public enum Mode {
        RAW_COORD,
        OFFSET,
        UNIT_CUBE
    }

}
