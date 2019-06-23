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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.fabric.FabricAdapter.adaptPlayer;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.item.ItemCategory;
import com.sk89q.worldedit.world.item.ItemType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.fabricmc.fabric.api.event.server.ServerTickCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The Fabric implementation of WorldEdit.
 */
public class FabricWorldEdit implements ModInitializer, ServerStartCallback, ServerStopCallback {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "worldedit";
    public static final String CUI_PLUGIN_CHANNEL = "cui";

    private FabricPermissionsProvider provider;

    public static FabricWorldEdit inst;

    private FabricPlatform platform;
    private FabricConfiguration config;
    private Path workingDir;

    private ModContainer container;

    public FabricWorldEdit() {
        inst = this;
    }

    @Override
    public void onInitialize() {
        this.container = FabricLoader.getInstance().getModContainer("worldedit").orElseThrow(
                () -> new IllegalStateException("WorldEdit mod missing in Fabric")
        );

        // Setup working directory
        workingDir = new File(FabricLoader.getInstance().getConfigDirectory(), "worldedit").toPath();
        if (!Files.exists(workingDir)) {
            try {
                Files.createDirectory(workingDir);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

//        WECUIPacketHandler.init();
//        InternalPacketHandler.init();

        ServerTickCallback.EVENT.register(ThreadSafeCache.getInstance());
        ServerStartCallback.EVENT.register(this);
        ServerStopCallback.EVENT.register(this);
        LOGGER.info("WorldEdit for Fabric (version " + getInternalVersion() + ") is loaded");
    }

    private void setupPlatform(MinecraftServer server) {
        this.platform = new FabricPlatform(this, server);

        WorldEdit.getInstance().getPlatformManager().register(platform);

        this.provider = new FabricPermissionsProvider.VanillaPermissionsProvider(platform);
    }

    private void setupRegistries() {
        // Blocks
        for (Identifier name : Registry.BLOCK.getIds()) {
            if (BlockType.REGISTRY.get(name.toString()) == null) {
                BlockType.REGISTRY.register(name.toString(), new BlockType(name.toString(),
                    input -> FabricAdapter.adapt(FabricAdapter.adapt(input.getBlockType()).getDefaultState())));
            }
        }
        // Items
        for (Identifier name : Registry.ITEM.getIds()) {
            if (ItemType.REGISTRY.get(name.toString()) == null) {
                ItemType.REGISTRY.register(name.toString(), new ItemType(name.toString()));
            }
        }
        // Entities
        for (Identifier name : Registry.ENTITY_TYPE.getIds()) {
            if (EntityType.REGISTRY.get(name.toString()) == null) {
                EntityType.REGISTRY.register(name.toString(), new EntityType(name.toString()));
            }
        }
        // Biomes
        for (Identifier name : Registry.BIOME.getIds()) {
            if (BiomeType.REGISTRY.get(name.toString()) == null) {
                BiomeType.REGISTRY.register(name.toString(), new BiomeType(name.toString()));
            }
        }
        // Tags
        for (Identifier name : BlockTags.getContainer().getKeys()) {
            if (BlockCategory.REGISTRY.get(name.toString()) == null) {
                BlockCategory.REGISTRY.register(name.toString(), new BlockCategory(name.toString()));
            }
        }
        for (Identifier name : ItemTags.getContainer().getKeys()) {
            if (ItemCategory.REGISTRY.get(name.toString()) == null) {
                ItemCategory.REGISTRY.register(name.toString(), new ItemCategory(name.toString()));
            }
        }
    }

    @Override
    public void onStartServer(MinecraftServer minecraftServer) {
        setupPlatform(minecraftServer);
        setupRegistries();

        config = new FabricConfiguration(this);
        config.load();
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent());
    }

    @Override
    public void onStopServer(MinecraftServer minecraftServer) {
        WorldEdit worldEdit = WorldEdit.getInstance();
        worldEdit.getSessionManager().unload();
        worldEdit.getPlatformManager().unregister(platform);
    }

//    @SubscribeEvent
//    public void onPlayerInteract(PlayerInteractEvent event) {
//        if (platform == null) {
//            return;
//        }
//
//        if (!platform.isHookingEvents())
//            return; // We have to be told to catch these events
//
//        if (event.getWorld().isRemote && event instanceof LeftClickEmpty) {
//            // catch LCE, pass it to server
//            InternalPacketHandler.getHandler().sendToServer(new LeftClickAirEventMessage());
//            return;
//        }
//
//        boolean isLeftDeny = event instanceof PlayerInteractEvent.LeftClickBlock
//                && ((PlayerInteractEvent.LeftClickBlock) event)
//                        .getUseItem() == Event.Result.DENY;
//        boolean isRightDeny =
//                event instanceof PlayerInteractEvent.RightClickBlock
//                        && ((PlayerInteractEvent.RightClickBlock) event)
//                                .getUseItem() == Event.Result.DENY;
//        if (isLeftDeny || isRightDeny || event.getEntity().world.isRemote || event.getHand() == Hand.OFF_HAND) {
//            return;
//        }
//
//        WorldEdit we = WorldEdit.getInstance();
//        FabricPlayer player = adaptPlayer((ServerPlayerEntity) event.getEntityPlayer());
//        FabricWorld world = getWorld(event.getEntityPlayer().world);
//
//        if (event instanceof PlayerInteractEvent.LeftClickEmpty) {
//            we.handleArmSwing(player); // this event cannot be canceled
//        } else if (event instanceof PlayerInteractEvent.LeftClickBlock) {
//            Location pos = new Location(world, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
//
//            if (we.handleBlockLeftClick(player, pos)) {
//                event.setCanceled(true);
//            }
//
//            if (we.handleArmSwing(player)) {
//                event.setCanceled(true);
//            }
//        } else if (event instanceof PlayerInteractEvent.RightClickBlock) {
//            Location pos = new Location(world, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
//
//            if (we.handleBlockRightClick(player, pos)) {
//                event.setCanceled(true);
//            }
//
//            if (we.handleRightClick(player)) {
//                event.setCanceled(true);
//            }
//        } else if (event instanceof PlayerInteractEvent.RightClickItem) {
//            if (we.handleRightClick(player)) {
//                event.setCanceled(true);
//            }
//        }
//    }

    /**
     * Get the configuration.
     *
     * @return the Forge configuration
     */
    FabricConfiguration getConfig() {
        return this.config;
    }

    /**
     * Get the session for a player.
     *
     * @param player the player
     * @return the session
     */
    public LocalSession getSession(ServerPlayerEntity player) {
        checkNotNull(player);
        return WorldEdit.getInstance().getSessionManager().get(adaptPlayer(player));
    }

    /**
     * Get the WorldEdit proxy for the given world.
     *
     * @param world the world
     * @return the WorldEdit world
     */
    public FabricWorld getWorld(World world) {
        checkNotNull(world);
        return new FabricWorld(world);
    }

    /**
     * Get the WorldEdit proxy for the platform.
     *
     * @return the WorldEdit platform
     */
    public Platform getPlatform() {
        return this.platform;
    }

    /**
     * Get the working directory where WorldEdit's files are stored.
     *
     * @return the working directory
     */
    public File getWorkingDir() {
        return this.workingDir.toFile();
    }

    /**
     * Get the version of the WorldEdit-for-Forge implementation.
     *
     * @return a version string
     */
    String getInternalVersion() {
        return container.getMetadata().getVersion().getFriendlyString();
    }

    public void setPermissionsProvider(FabricPermissionsProvider provider) {
        this.provider = provider;
    }

    public FabricPermissionsProvider getPermissionsProvider() {
        return provider;
    }
}
