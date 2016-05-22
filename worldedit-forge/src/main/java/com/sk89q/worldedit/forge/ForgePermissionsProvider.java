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

package com.sk89q.worldedit.forge;

import net.minecraft.command.ICommand;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.api.entity.living.player.Player;

public interface ForgePermissionsProvider {

    public boolean hasPermission(EntityPlayerMP player, String permission);

    public void registerPermission(ICommand command, String permission);

    public static class VanillaPermissionsProvider implements ForgePermissionsProvider {

        private ForgePlatform platform;

        public VanillaPermissionsProvider(ForgePlatform platform) {
            this.platform = platform;
        }

        @Override
        public boolean hasPermission(EntityPlayerMP player, String permission) {
            ForgeConfiguration configuration = platform.getConfiguration();
            return configuration.cheatMode ||
                    FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().canSendCommands(player.getGameProfile()) ||
                    (configuration.creativeEnable && player.interactionManager.getGameType() == GameType.CREATIVE);
        }

        @Override
        public void registerPermission(ICommand command, String permission) {}
    }

    public static class SpongePermissionsProvider implements ForgePermissionsProvider {

        @Override
        public boolean hasPermission(EntityPlayerMP player, String permission) {
            return ((Player) player).hasPermission(permission);
        }

        @Override
        public void registerPermission(ICommand command, String permission) {

        }
    }
}
