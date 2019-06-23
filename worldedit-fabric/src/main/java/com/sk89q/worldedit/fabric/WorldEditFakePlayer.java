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

package com.sk89q.worldedit.fabric;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.stat.Stat;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import java.util.UUID;

import javax.annotation.Nullable;

public class WorldEditFakePlayer extends PlayerEntity {
    private static final GameProfile FAKE_WORLDEDIT_PROFILE = new GameProfile(UUID.nameUUIDFromBytes("worldedit".getBytes()), "[WorldEdit]");

    public WorldEditFakePlayer(World world) {
        super(world, FAKE_WORLDEDIT_PROFILE);
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public void increaseStat(Stat<?> stat, int incrementer) {
    }

    @Override
    public void incrementStat(Stat<?> stat) {
    }

    @Override
    public void sendMessage(Component component) {
    }

    @Override
    public void addChatMessage(Component component, boolean opt) {
    }

    @Nullable
    @Override
    public Entity changeDimension(DimensionType dimensionType) {
        return this;
    }

    @Override
    public boolean isInvulnerableTo(DamageSource damageSource) {
        return true;
    }
}
