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

package com.sk89q.worldedit.command.composition;

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandLocals;
import com.sk89q.minecraft.util.commands.CommandPermissionsException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.MaxBrushRadiusException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.argument.NumberParser;
import com.sk89q.worldedit.command.argument.RegionFactoryParser;
import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.command.tool.brush.OperationFactoryBrush;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.function.Contextual;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.regions.factory.RegionFactory;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.command.argument.CommandArgs;
import com.sk89q.worldedit.util.command.composition.CommandExecutor;
import com.sk89q.worldedit.util.command.composition.SimpleCommand;

public class ShapedBrushCommand extends SimpleCommand<Object> {

    private final CommandExecutor<? extends Contextual<? extends Operation>> delegate;
    private final String permission;

    private final RegionFactoryParser regionFactoryParser = addParameter(new RegionFactoryParser());
    private final NumberParser radiusCommand = addParameter(new NumberParser("size", "The size of the brush", "5"));

    public ShapedBrushCommand(CommandExecutor<? extends Contextual<? extends Operation>> delegate, String permission) {
        checkNotNull(delegate, "delegate");
        this.permission = permission;
        this.delegate = delegate;
        addParameter(delegate);
    }

    @Override
    public Object call(CommandArgs args, CommandLocals locals) throws CommandException {
        if (!testPermission(locals)) {
            throw new CommandPermissionsException();
        }

        RegionFactory regionFactory = regionFactoryParser.call(args, locals);
        int radius = radiusCommand.call(args, locals).intValue();
        Contextual<? extends Operation> factory = delegate.call(args, locals);

        Player player = (Player) locals.get(Actor.class);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(player);

        try {
            WorldEdit.getInstance().checkMaxBrushRadius(radius);
            BrushTool tool = session.getBrushTool(player.getItemInHand(HandSide.MAIN_HAND).getType());
            tool.setSize(radius);
            tool.setBrush(new OperationFactoryBrush(factory, regionFactory), permission);
        } catch (MaxBrushRadiusException | InvalidToolBindException e) {
            WorldEdit.getInstance().getPlatformManager().getCommandManager().getExceptionConverter().convert(e);
        }

        player.print("Set brush to " + factory);

        return true;
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public boolean testPermission0(CommandLocals locals) {
        Actor sender = locals.get(Actor.class);
        if (sender == null) {
            throw new RuntimeException("Uh oh! No 'Actor' specified so that we can check permissions");
        } else {
            return sender.hasPermission(permission);
        }
    }

}
