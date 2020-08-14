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

package com.sk89q.worldedit.fabric;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.text.Text;

import java.util.UUID;
import javax.annotation.Nullable;

public class WorldEditFakePlayer extends ServerPlayerEntity {
    private static final GameProfile FAKE_WORLDEDIT_PROFILE = new GameProfile(UUID.nameUUIDFromBytes("worldedit".getBytes()), "[WorldEdit]");

    public WorldEditFakePlayer(ServerWorld world) {
        super(world.getServer(), world, FAKE_WORLDEDIT_PROFILE, new ServerPlayerInteractionManager(world));
    }

    @Override
    public void tick() {
    }

    @Override
    public void increaseStat(Stat<?> stat, int incrementer) {
    }

    @Override
    public void incrementStat(Stat<?> stat) {
    }

    @Override
    public void sendMessage(Text message, boolean actionBar) {
        super.sendMessage(message, actionBar);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }
}
