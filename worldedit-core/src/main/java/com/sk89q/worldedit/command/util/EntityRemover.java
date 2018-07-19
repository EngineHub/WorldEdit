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

import static com.google.common.base.Preconditions.checkNotNull;

import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.entity.metadata.EntityProperties;
import com.sk89q.worldedit.function.EntityFunction;

import java.util.regex.Pattern;

import javax.annotation.Nullable;

/**
 * The implementation of /remove.
 */
public class EntityRemover {

    public enum Type {
        ALL("all") {
            @Override
            boolean matches(EntityProperties type) {
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
            boolean matches(EntityProperties type) {
                return type.isProjectile();
            }
        },
        ITEMS("items?|drops?") {
            @Override
            boolean matches(EntityProperties type) {
                return type.isItem();
            }
        },
        FALLING_BLOCKS("falling(blocks?|sand|gravel)") {
            @Override
            boolean matches(EntityProperties type) {
                return type.isFallingBlock();
            }
        },
        PAINTINGS("paintings?|art") {
            @Override
            boolean matches(EntityProperties type) {
                return type.isPainting();
            }
        },
        ITEM_FRAMES("(item)frames?") {
            @Override
            boolean matches(EntityProperties type) {
                return type.isItemFrame();
            }
        },
        BOATS("boats?") {
            @Override
            boolean matches(EntityProperties type) {
                return type.isBoat();
            }
        },
        MINECARTS("(mine)?carts?") {
            @Override
            boolean matches(EntityProperties type) {
                return type.isMinecart();
            }
        },
        TNT("tnt") {
            @Override
            boolean matches(EntityProperties type) {
                return type.isTNT();
            }
        },
        XP_ORBS("xp") {
            @Override
            boolean matches(EntityProperties type) {
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

        abstract boolean matches(EntityProperties type);

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

    public EntityFunction createFunction() {
        final Type type = this.type;
        checkNotNull(type, "type can't be null");
        return entity -> {
            EntityProperties registryType = entity.getFacet(EntityProperties.class);
            if (registryType != null) {
                if (type.matches(registryType)) {
                    entity.remove();
                    return true;
                }
            }

            return false;
        };
    }

}
