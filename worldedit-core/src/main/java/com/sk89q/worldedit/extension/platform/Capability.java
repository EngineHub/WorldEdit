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

package com.sk89q.worldedit.extension.platform;

import com.sk89q.worldedit.internal.block.BlockStateIdAccess;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.registry.BlockRegistry;

/**
 * A collection of capabilities that a {@link Platform} may support.
 */
public enum Capability {

    /**
     * The capability of registering game hooks to catch events such as
     * a player clicking a block.
     */
    GAME_HOOKS {
        @Override
        void initialize(PlatformManager platformManager, Platform platform) {
            platform.registerGameHooks();
        }

        @Override
        void unload(PlatformManager platformManager, Platform platform) {
        }
    },

    /**
     * The capability of providing configuration.
     */
    CONFIGURATION,

    /**
     * The capability of handling user commands entered in chat or console.
     */
    USER_COMMANDS {
        @Override
        void initialize(PlatformManager platformManager, Platform platform) {
            platformManager.getPlatformCommandManager().registerCommandsWith(platform);
        }

        @Override
        void unload(PlatformManager platformManager, Platform platform) {
            platformManager.getPlatformCommandManager().removeCommands();
        }
    },

    /**
     * The capability of a platform to assess whether a given
     * {@link Actor} has sufficient authorization to perform a task.
     */
    PERMISSIONS,

    /**
     * The capability of a platform to dispatch WorldEditCUI events.
     */
    WORLDEDIT_CUI,

    /**
     * The capability of a platform to perform modifications to a world.
     */
    WORLD_EDITING {
        @Override
        void initialize(PlatformManager platformManager, Platform platform) {
            BlockRegistry blockRegistry = platform.getRegistries().getBlockRegistry();
            for (BlockType type : BlockType.REGISTRY) {
                for (BlockState state : type.getAllStates()) {
                    BlockStateIdAccess.register(state, blockRegistry.getInternalBlockStateId(state));
                }
            }
        }

        @Override
        void unload(PlatformManager platformManager, Platform platform) {
            BlockStateIdAccess.clear();
        }
    };

    void initialize(PlatformManager platformManager, Platform platform) {

    }

    void unload(PlatformManager platformManager, Platform platform) {

    }

}
