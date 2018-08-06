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

package com.sk89q.worldedit.forge.net;

import com.sk89q.worldedit.forge.ForgeWorldEdit;
import io.netty.buffer.ByteBuf;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class LeftClickAirEventMessage implements IMessage {

    public static final class Handler implements IMessageHandler<LeftClickAirEventMessage, IMessage> {

        @Override
        public IMessage onMessage(LeftClickAirEventMessage message, final MessageContext ctx) {
            ctx.getServerHandler().player.mcServer.addScheduledTask(
                    () -> ForgeWorldEdit.inst.onPlayerInteract(new PlayerInteractEvent.LeftClickEmpty(ctx.getServerHandler().player)));
            return null;
        }

    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

}
