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

package com.sk89q.worldedit.extension.factory.parser.mask;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.function.mask.ExpressionMask;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.expression.Expression;
import com.sk89q.worldedit.internal.expression.ExpressionException;
import com.sk89q.worldedit.internal.registry.InputParser;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.regions.shape.WorldEditExpressionEnvironment;
import com.sk89q.worldedit.session.SessionOwner;
import com.sk89q.worldedit.session.request.Request;
import com.sk89q.worldedit.session.request.RequestExtent;

import java.util.function.IntSupplier;

public class ExpressionMaskParser extends InputParser<Mask> {

    public ExpressionMaskParser(WorldEdit worldEdit) {
        super(worldEdit);
    }

    @Override
    public Mask parseFromInput(String input, ParserContext context) throws InputParseException {
        if (!input.startsWith("=")) {
            return null;
        }

        try {
            Expression exp = Expression.compile(input.substring(1), "x", "y", "z");
            WorldEditExpressionEnvironment env = new WorldEditExpressionEnvironment(
                    new RequestExtent(), Vector3.ONE, Vector3.ZERO);
            exp.setEnvironment(env);
            if (context.getActor() instanceof SessionOwner) {
                SessionOwner owner = (SessionOwner) context.getActor();
                IntSupplier timeout = () -> WorldEdit.getInstance().getSessionManager().get(owner).getTimeout();
                return new ExpressionMask(exp, timeout);
            }
            return new ExpressionMask(exp);
        } catch (ExpressionException e) {
            throw new InputParseException("Invalid expression: " + e.getMessage());
        }
    }
}
