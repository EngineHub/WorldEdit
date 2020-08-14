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

package com.sk89q.worldedit.forge.net.packet;

import com.sk89q.worldedit.forge.ForgeWorldEdit;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty;
import net.minecraftforge.fml.network.NetworkEvent.Context;

import java.util.Objects;
import java.util.function.Supplier;

public class LeftClickAirEventMessage {

    public static final LeftClickAirEventMessage INSTANCE = new LeftClickAirEventMessage();

    public static final class Handler {
        public static void handle(final LeftClickAirEventMessage message, Supplier<Context> ctx) {
            Context context = ctx.get();
            context.enqueueWork(() -> ForgeWorldEdit.inst.onPlayerInteract(new LeftClickEmpty(Objects.requireNonNull(context.getSender()))));
        }
    }

    public static LeftClickAirEventMessage decode(ByteBuf buf) {
        return INSTANCE;
    }

    public static void encode(LeftClickAirEventMessage msg, PacketBuffer buf) {
    }

    private LeftClickAirEventMessage() {
    }

}
