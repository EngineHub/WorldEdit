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

package com.sk89q.worldedit.bukkit.adapter.impl.v1_21_4;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;

import java.util.OptionalInt;
import java.util.UUID;

class PaperweightFakePlayer extends ServerPlayer {
    private static final GameProfile FAKE_WORLDEDIT_PROFILE = new GameProfile(UUID.nameUUIDFromBytes("worldedit".getBytes()), "[WorldEdit]");
    private static final Vec3 ORIGIN = new Vec3(0.0D, 0.0D, 0.0D);
    private static final ClientInformation FAKE_CLIENT_INFO = new ClientInformation(
        "en_US", 16, ChatVisiblity.FULL, true, 0, HumanoidArm.LEFT, false, false, ParticleStatus.MINIMAL
    );

    PaperweightFakePlayer(ServerLevel world) {
        super(world.getServer(), world, FAKE_WORLDEDIT_PROFILE, FAKE_CLIENT_INFO);
    }

    @Override
    public Vec3 position() {
        return ORIGIN;
    }

    @Override
    public void tick() {
    }

    @Override
    public void die(DamageSource damagesource) {
    }

    @Override
    public OptionalInt openMenu(MenuProvider factory) {
        return OptionalInt.empty();
    }

    @Override
    public void updateOptions(ClientInformation clientOptions) {
    }

    @Override
    public void displayClientMessage(Component message, boolean actionBar) {
    }

    @Override
    public void awardStat(Stat<?> stat, int amount) {
    }

    @Override
    public void awardStat(Stat<?> stat) {
    }

    @Override
    public void openTextEdit(SignBlockEntity sign, boolean front) {
    }
}
