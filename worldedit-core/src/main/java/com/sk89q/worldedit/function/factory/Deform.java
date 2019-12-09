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

package com.sk89q.worldedit.function.factory;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.util.GuavaUtil.firstNonNull;

import com.google.common.collect.ImmutableList;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.NullExtent;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.EditContext;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.RunContext;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.NullRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.TranslatableComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;

public class Deform implements Contextual<Operation> {

    private Extent destination;
    private Region region;
    private String expression;
    private Mode mode = Mode.UNIT_CUBE;
    private Vector3 offset = Vector3.ZERO;

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
        checkNotNull(expression, "expression");
        checkNotNull(mode, "mode");
        this.destination = destination;
        this.region = region;
        this.expression = expression;
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

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        checkNotNull(expression, "expression");
        this.expression = expression;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        checkNotNull(mode, "mode");
        this.mode = mode;
    }

    public Vector3 getOffset() {
        return offset;
    }

    public void setOffset(Vector3 offset) {
        checkNotNull(offset, "offset");
        this.offset = offset;
    }

    @Override
    public String toString() {
        return "deformation of " + expression;
    }

    @Override
    public Operation createFromContext(final EditContext context) {
        final Vector3 zero;
        Vector3 unit;

        Region region = firstNonNull(context.getRegion(), this.region);

        switch (mode) {
            case UNIT_CUBE:
                final Vector3 min = region.getMinimumPoint().toVector3();
                final Vector3 max = region.getMaximumPoint().toVector3();

                zero = max.add(min).multiply(0.5);
                unit = max.subtract(zero);

                if (unit.getX() == 0) unit = unit.withX(1.0);
                if (unit.getY() == 0) unit = unit.withY(1.0);
                if (unit.getZ() == 0) unit = unit.withZ(1.0);
                break;
            case RAW_COORD:
                zero = Vector3.ZERO;
                unit = Vector3.ONE;
                break;
            case OFFSET:
            default:
                zero = offset;
                unit = Vector3.ONE;
        }

        LocalSession session = context.getSession();
        return new DeformOperation(context.getDestination(), region, zero, unit, expression,
                session == null ? WorldEdit.getInstance().getConfiguration().calculationTimeout : session.getTimeout());
    }

    private static final class DeformOperation implements Operation {
        private final Extent destination;
        private final Region region;
        private final Vector3 zero;
        private final Vector3 unit;
        private final String expression;
        private final int timeout;

        private DeformOperation(Extent destination, Region region, Vector3 zero, Vector3 unit, String expression, int timeout) {
            this.destination = destination;
            this.region = region;
            this.zero = zero;
            this.unit = unit;
            this.expression = expression;
            this.timeout = timeout;
        }

        @Override
        public Operation resume(RunContext run) throws WorldEditException {
            try {
                // TODO: Move deformation code
                ((EditSession) destination).deformRegion(region, zero, unit, expression, timeout);
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
                    TextComponent.of(expression).color(TextColor.LIGHT_PURPLE)));
        }

    }

    public enum Mode {
        RAW_COORD,
        OFFSET,
        UNIT_CUBE
    }

}
