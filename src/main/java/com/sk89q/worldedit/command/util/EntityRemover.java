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

package com.sk89q.worldedit.command.util;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.entity.metadata.EntityType;
import com.sk89q.worldedit.function.EntityFunction;
import com.sk89q.worldedit.world.registry.EntityRegistry;

import javax.annotation.Nullable;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The implementation of /remove.
 */
public class EntityRemover {

    public enum Type {
        ALL("all") {
            @Override
            boolean matches(EntityType type) {
                for (Type value : values()) {
                    if (value != this && value.matches(type)) {
                        return true;
                    }
                }
                return false;
            }
        },
        PROJECTILES("projectiles?|arrows?") {
            @Override
            boolean matches(EntityType type) {
                return type.isProjectile();
            }
        },
        ITEMS("items?|drops?") {
            @Override
            boolean matches(EntityType type) {
                return type.isItem();
            }
        },
        FALLING_BLOCKS("falling(blocks?|sand|gravel)") {
            @Override
            boolean matches(EntityType type) {
                return type.isFallingBlock();
            }
        },
        PAINTINGS("paintings?|art") {
            @Override
            boolean matches(EntityType type) {
                return type.isPainting();
            }
        },
        ITEM_FRAMES("(item)frames?") {
            @Override
            boolean matches(EntityType type) {
                return type.isItemFrame();
            }
        },
        BOATS("boats?") {
            @Override
            boolean matches(EntityType type) {
                return type.isBoat();
            }
        },
        MINECARTS("(mine)?carts?") {
            @Override
            boolean matches(EntityType type) {
                return type.isMinecart();
            }
        },
        TNT("tnt") {
            @Override
            boolean matches(EntityType type) {
                return type.isTNT();
            }
        },
        XP_ORBS("xp") {
            @Override
            boolean matches(EntityType type) {
                return type.isExperienceOrb();
            }
        };

        private final Pattern pattern;

        Type(String pattern) {
            this.pattern = Pattern.compile(pattern);
        }

        public boolean matches(String str) {
            return pattern.matcher(str).matches();
        }

        abstract boolean matches(EntityType type);

        @Nullable
        public static Type findByPattern(String str) {
            for (Type type : values()) {
                if (type.matches(str)) {
                    return type;
                }
            }

            return null;
        }
    }

    private Type type;

    public void fromString(String str) throws CommandException {
        Type type = Type.findByPattern(str);
        if (type != null) {
            this.type = type;
        } else {
            throw new CommandException("Acceptable types: projectiles, items, paintings, itemframes, boats, minecarts, tnt, xp, or all");
        }
    }

    public EntityFunction createFunction(final EntityRegistry entityRegistry) {
        final Type type = this.type;
        checkNotNull("type can't be null", type);
        return new EntityFunction() {
            @Override
            public boolean apply(Entity entity) throws WorldEditException {
                EntityType registryType = entity.getFacet(EntityType.class);
                if (registryType != null) {
                    if (type.matches(registryType)) {
                        entity.remove();
                        return true;
                    }
                }

                return false;
            }
        };
    }

}
