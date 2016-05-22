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

package com.sk89q.worldedit.sponge;

import com.sk89q.worldedit.entity.metadata.EntityType;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.ExperienceOrb;
import org.spongepowered.api.entity.FallingBlock;
import org.spongepowered.api.entity.Item;
import org.spongepowered.api.entity.explosive.PrimedTNT;
import org.spongepowered.api.entity.hanging.ItemFrame;
import org.spongepowered.api.entity.hanging.Painting;
import org.spongepowered.api.entity.living.*;
import org.spongepowered.api.entity.living.animal.Animal;
import org.spongepowered.api.entity.living.golem.Golem;
import org.spongepowered.api.entity.projectile.Projectile;
import org.spongepowered.api.entity.vehicle.Boat;
import org.spongepowered.api.entity.vehicle.minecart.Minecart;
import org.spongepowered.api.text.Text;

import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpongeEntityType implements EntityType {

    private final Entity entity;

    public SpongeEntityType(Entity entity) {
        checkNotNull(entity);
        this.entity = entity;
    }

    @Override
    public boolean isPlayerDerived() {
        return entity instanceof Humanoid;
    }

    @Override
    public boolean isProjectile() {
        return entity instanceof Projectile;
    }

    @Override
    public boolean isItem() {
        return entity instanceof Item;
    }

    @Override
    public boolean isFallingBlock() {
        return entity instanceof FallingBlock;
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
        return entity instanceof Minecart;
    }

    @Override
    public boolean isTNT() {
        return entity instanceof PrimedTNT;
    }

    @Override
    public boolean isExperienceOrb() {
        return entity instanceof ExperienceOrb;
    }

    @Override
    public boolean isLiving() {
        return entity instanceof Living;
    }

    @Override
    public boolean isAnimal() {
        return entity instanceof Animal;
    }

    @Override
    public boolean isAmbient() {
        return entity instanceof Ambient;
    }

    @Override
    public boolean isNPC() {
        return entity instanceof Villager;
    }

    @Override
    public boolean isGolem() {
        return entity instanceof Golem;
    }

    @Override
    public boolean isTamed() {
        return entity.get(Keys.TAMED_OWNER).orElse(Optional.<UUID>empty()).isPresent();
    }

    @Override
    public boolean isTagged() {
        return !entity.get(Keys.DISPLAY_NAME).orElse(Text.EMPTY).isEmpty();
   }

    @Override
    public boolean isArmorStand() {
        return entity instanceof ArmorStand;
    }
}
