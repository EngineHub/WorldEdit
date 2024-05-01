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

package com.sk89q.worldedit.neoforge;

import com.sk89q.worldedit.entity.metadata.EntityProperties;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.trading.Merchant;

import static com.google.common.base.Preconditions.checkNotNull;

public class NeoForgeEntityProperties implements EntityProperties {

    private final Entity entity;

    public NeoForgeEntityProperties(Entity entity) {
        checkNotNull(entity);
        this.entity = entity;
    }

    @Override
    public boolean isPlayerDerived() {
        return entity instanceof Player;
    }

    @Override
    public boolean isProjectile() {
        return entity instanceof Projectile;
    }

    @Override
    public boolean isItem() {
        return entity instanceof ItemEntity;
    }

    @Override
    public boolean isFallingBlock() {
        return entity instanceof FallingBlockEntity;
    }

    @Override
    public boolean isPainting() {
        return entity instanceof Painting;
    }

    @Override
    public boolean isItemFrame() {
        return entity instanceof ItemFrame;
    }

    @Override
    public boolean isBoat() {
        return entity instanceof Boat;
    }

    @Override
    public boolean isMinecart() {
        return entity instanceof AbstractMinecart;
    }

    @Override
    public boolean isTNT() {
        return entity instanceof PrimedTnt;
    }

    @Override
    public boolean isExperienceOrb() {
        return entity instanceof ExperienceOrb;
    }

    @Override
    public boolean isLiving() {
        return entity instanceof Mob;
    }

    @Override
    public boolean isAnimal() {
        return entity instanceof Animal;
    }

    @Override
    public boolean isAmbient() {
        return entity instanceof AmbientCreature;
    }

    @Override
    public boolean isNPC() {
        return entity instanceof Npc || entity instanceof Merchant;
    }

    @Override
    public boolean isGolem() {
        return entity instanceof AbstractGolem;
    }

    @Override
    public boolean isTamed() {
        return entity instanceof TamableAnimal && ((TamableAnimal) entity).isTame();
    }

    @Override
    public boolean isTagged() {
        return entity.hasCustomName();
    }

    @Override
    public boolean isArmorStand() {
        return entity instanceof ArmorStand;
    }

    @Override
    public boolean isPasteable() {
        return !(entity instanceof ServerPlayer || entity instanceof EnderDragonPart);
    }

    @Override
    public boolean isWaterCreature() {
        return entity instanceof WaterAnimal;
    }
}
