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

package com.sk89q.worldedit.fabric.mixin;

import com.google.errorprone.annotations.Keep;
import com.sk89q.worldedit.fabric.FabricWorldEdit;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class MixinServerGamePacketListenerImpl {
    @Shadow
    public ServerPlayer player;

    @Unique
    private int ignoreSwingPackets;

    @Keep
    @SuppressWarnings("UnusedVariable")
    @Inject(method = "handleAnimate", at = @At("HEAD"))
    private void onAnimate(ServerboundSwingPacket packet, CallbackInfo ci) {
        if (!this.player.gameMode.isDestroyingBlock) {
            if (this.ignoreSwingPackets > 0) {
                this.ignoreSwingPackets--;
            } else if (FabricWorldEdit.inst != null) {
                FabricWorldEdit.inst.onLeftClickAir(this.player, packet.getHand());
            }
        }
    }

    @Keep
    @SuppressWarnings("UnusedVariable")
    @Inject(method = "handlePlayerAction", at = @At("HEAD"))
    private void onAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        switch (packet.getAction()) {
            case DROP_ITEM, DROP_ALL_ITEMS, START_DESTROY_BLOCK -> this.ignoreSwingPackets++;
            default -> {
            }
        }
    }
}
