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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public class LeftClickAirEventMessage {

    public static final LeftClickAirEventMessage INSTANCE = new LeftClickAirEventMessage();

    public static final class Handler {
        public static void handle(final LeftClickAirEventMessage message, Supplier<NetworkEvent.Context> ctx) {
            NetworkEvent.Context context = ctx.get();
            context.enqueueWork(() -> ForgeWorldEdit.inst.onPlayerInteract(new LeftClickEmpty(Objects.requireNonNull(context.getSender()))));
        }
    }

    public static LeftClickAirEventMessage decode(FriendlyByteBuf buf) {
        return INSTANCE;
    }

    public static void encode(LeftClickAirEventMessage msg, FriendlyByteBuf buf) {
    }

    private LeftClickAirEventMessage() {
    }

}
