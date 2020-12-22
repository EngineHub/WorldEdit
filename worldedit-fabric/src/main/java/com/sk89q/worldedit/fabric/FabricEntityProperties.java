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

import com.sk89q.worldedit.entity.metadata.EntityProperties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Npc;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.village.Merchant;

import static com.google.common.base.Preconditions.checkNotNull;

public class FabricEntityProperties implements EntityProperties {

    private final Entity entity;

    public FabricEntityProperties(Entity entity) {
        checkNotNull(entity);
        this.entity = entity;
    }

    @Override
    public boolean isPlayerDerived() {
        return entity instanceof PlayerEntity;
    }

    @Override
    public boolean isProjectile() {
        return entity instanceof ProjectileEntity;
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
        return entity instanceof PaintingEntity;
    }

    @Override
    public boolean isItemFrame() {
        return entity instanceof ItemFrameEntity;
    }

    @Override
    public boolean isBoat() {
        return entity instanceof BoatEntity;
    }

    @Override
    public boolean isMinecart() {
        return entity instanceof AbstractMinecartEntity;
    }

    @Override
    public boolean isTNT() {
        return entity instanceof TntEntity;
    }

    @Override
    public boolean isExperienceOrb() {
        return entity instanceof ExperienceOrbEntity;
    }

    @Override
    public boolean isLiving() {
        return entity instanceof MobEntity;
    }

    @Override
    public boolean isAnimal() {
        return entity instanceof AnimalEntity;
    }

    @Override
    public boolean isAmbient() {
        return entity instanceof AmbientEntity;
    }

    @Override
    public boolean isNPC() {
        return entity instanceof Npc || entity instanceof Merchant;
    }

    @Override
    public boolean isGolem() {
        return entity instanceof GolemEntity;
    }

    @Override
    public boolean isTamed() {
        return entity instanceof TameableEntity && ((TameableEntity) entity).isTamed();
    }

    @Override
    public boolean isTagged() {
        return entity.hasCustomName();
    }

    @Override
    public boolean isArmorStand() {
        return entity instanceof ArmorStandEntity;
    }

    @Override
    public boolean isPasteable() {
        return !(entity instanceof ServerPlayerEntity || entity instanceof EnderDragonEntity);
    }

    @Override
    public boolean isWaterCreature() {
        return entity instanceof WaterCreatureEntity;
    }
}
