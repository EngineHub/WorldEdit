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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.worldedit.entity.metadata.EntityProperties;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.MultiPartEntityPart;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;

public class ForgeEntityProperties implements EntityProperties {

    private final Entity entity;

    public ForgeEntityProperties(Entity entity) {
        checkNotNull(entity);
        this.entity = entity;
    }

    @Override
    public boolean isPlayerDerived() {
        return entity instanceof EntityPlayer;
    }

    @Override
    public boolean isProjectile() {
        return entity instanceof EntityEnderEye || entity instanceof IProjectile;
    }

    @Override
    public boolean isItem() {
        return entity instanceof EntityItem;
    }

    @Override
    public boolean isFallingBlock() {
        return entity instanceof EntityFallingBlock;
    }

    @Override
    public boolean isPainting() {
        return entity instanceof EntityPainting;
    }

    @Override
    public boolean isItemFrame() {
        return entity instanceof EntityItemFrame;
    }

    @Override
    public boolean isBoat() {
        return entity instanceof EntityBoat;
    }

    @Override
    public boolean isMinecart() {
        return entity instanceof EntityMinecart;
    }

    @Override
    public boolean isTNT() {
        return entity instanceof EntityTNTPrimed;
    }

    @Override
    public boolean isExperienceOrb() {
        return entity instanceof EntityXPOrb;
    }

    @Override
    public boolean isLiving() {
        return entity instanceof EntityLiving;
    }

    @Override
    public boolean isAnimal() {
        return entity instanceof EntityAnimal;
    }

    @Override
    public boolean isAmbient() {
        return entity instanceof EntityAmbientCreature;
    }

    @Override
    public boolean isNPC() {
        return entity instanceof INpc || entity instanceof IMerchant;
    }

    @Override
    public boolean isGolem() {
        return entity instanceof EntityGolem;
    }

    @Override
    public boolean isTamed() {
        return entity instanceof EntityTameable && ((EntityTameable) entity).isTamed();
    }

    @Override
    public boolean isTagged() {
        return entity.hasCustomName();
    }

    @Override
    public boolean isArmorStand() {
        return entity instanceof EntityArmorStand;
    }

    @Override
    public boolean isPasteable() {
        return !(entity instanceof EntityPlayerMP || entity instanceof MultiPartEntityPart);
    }
}
