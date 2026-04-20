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

package com.sk89q.worldedit.coremc.mixin;

import com.google.errorprone.annotations.Keep;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.coremc.internal.CoreMcPlatform;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.util.lifecycle.ConstantLifecycled;
import com.sk89q.worldedit.util.lifecycle.Lifecycled;
import com.sk89q.worldedit.util.lifecycle.SimpleLifecycled;
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

    @Unique
    private static final Lifecycled<CoreMcPlatform> WORLD_EDITING_PLATFORM = WorldEdit.getInstance().getPlatformManager()
        .getPreferred(Capability.WORLD_EDITING)
        .flatMap(platform -> {
            if (platform instanceof CoreMcPlatform coreMcPlatform) {
                return new ConstantLifecycled<>(coreMcPlatform);
            } else {
                return SimpleLifecycled.invalid();
            }
        });

    @Shadow
    public ServerPlayer player;

    @Unique
    private int ignoreSwingPackets;

    @Keep
    @Inject(
        method = "handleAnimate",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;swing(Lnet/minecraft/world/InteractionHand;)V")
    )
    private void onAnimate(ServerboundSwingPacket packet, @SuppressWarnings("UnusedVariable") CallbackInfo ci) {
        if (!((AccessorServerPlayerGameMode) this.player.gameMode).isDestroyingBlock()) {
            if (this.ignoreSwingPackets > 0) {
                this.ignoreSwingPackets--;
            } else {
                WORLD_EDITING_PLATFORM.value().ifPresent(platform ->
                    platform.getMod().onLeftClickAir(this.player, packet.getHand())
                );
            }
        }
    }

    @Keep
    @Inject(
        method = "handlePlayerAction",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;resetLastActionTime()V")
    )
    private void onAction(ServerboundPlayerActionPacket packet, @SuppressWarnings("UnusedVariable") CallbackInfo ci) {
        switch (packet.getAction()) {
            case DROP_ITEM, DROP_ALL_ITEMS, START_DESTROY_BLOCK -> this.ignoreSwingPackets++;
            default -> {
            }
        }
    }
}
