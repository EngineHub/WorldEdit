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

package com.sk89q.worldedit.fabric.internal;

import com.sk89q.worldedit.coremc.CoreMcPermissionsProvider;
import com.sk89q.worldedit.coremc.internal.CoreMcMod;
import com.sk89q.worldedit.coremc.internal.CoreMcPlatform;
import com.sk89q.worldedit.coremc.internal.ThreadSafeCache;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;
import net.minecraft.world.InteractionResult;
import org.apache.logging.log4j.Logger;
import org.enginehub.worldeditcui.protocol.CUIPacketHandler;

import java.util.Optional;

/**
 * The Fabric implementation of WorldEdit.
 */
public class FabricWorldEdit extends CoreMcMod implements ModInitializer {

    private static final Logger LOGGER = LogManagerCompat.getLogger();

    public static FabricWorldEdit inst;

    private static volatile CoreMcPlatform PLATFORM;

    public static CoreMcPlatform getPlatform() {
        CoreMcPlatform platform = PLATFORM;
        if (platform == null) {
            throw new IllegalStateException("FabricWorldEdit is not initialized");
        }
        return platform;
    }

    private ModContainer container;

    public FabricWorldEdit() {
        inst = this;
    }

    @Override
    public void onInitialize() {
        this.container = FabricLoader.getInstance().getModContainer("worldedit").orElseThrow(
            () -> new IllegalStateException("WorldEdit mod missing in Fabric")
        );

        PLATFORM = new FabricPlatform(this);
        init(PLATFORM, FabricLoader.getInstance().getConfigDir());

        CUIPacketHandler.instance().registerServerboundHandler(this::onCuiPacket);

        ServerTickEvents.END_SERVER_TICK.register(server -> ThreadSafeCache.getInstance().onEndTick(server));
        CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> registerCommands(dispatcher));
        ServerLifecycleEvents.SERVER_STARTING.register(_ -> serverAboutToStart());
        ServerLifecycleEvents.SERVER_STARTED.register(this::serverStarted);
        ServerLifecycleEvents.SERVER_STOPPING.register(_ -> serverStopping());
        ServerPlayConnectionEvents.DISCONNECT.register((handler, _) -> onPlayerDisconnect(handler.player));
        AttackBlockCallback.EVENT.register(
            (playerEntity, _, hand, blockPos, direction) ->
                onLeftClickBlock(playerEntity, hand, blockPos, direction)
                    ? InteractionResult.SUCCESS : InteractionResult.PASS
        );
        UseBlockCallback.EVENT.register(
            (playerEntity, _, hand, blockHitResult) ->
                onRightClickBlock(playerEntity, hand, blockHitResult.getBlockPos(), blockHitResult.getDirection())
                    ? InteractionResult.SUCCESS : InteractionResult.PASS
        );
        UseItemCallback.EVENT.register((playerEntity, _, hand) -> {
            Optional<Boolean> result = onRightClickItem(playerEntity, hand);
            if (result.isPresent()) {
                return result.get() ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
            return InteractionResult.PASS;
        });
        LOGGER.info("WorldEdit for Fabric (version " + getInternalVersion() + ") is loaded");
    }

    @Override
    protected String getInternalVersion() {
        return container.getMetadata().getVersion().getFriendlyString();
    }

    @Override
    protected CoreMcPermissionsProvider createPermissionsProvider(CoreMcPlatform platform) {
        try {
            Class.forName("me.lucko.fabric.api.permissions.v0.Permissions", false, getClass().getClassLoader());
            Optional<Version> version = FabricLoader.getInstance().getModContainer("fabric-permissions-api-v0")
                    .map(ModContainer::getMetadata)
                    .map(ModMetadata::getVersion);

            if (version.isPresent() && !VersionPredicate.parse(">=0.7.0").test(version.get())) {
                throw new RuntimeException("Fabric permissions version " + version.get() + " is not supported. Please update Fabric Permissions API");
            }

            return new LuckoFabricPermissionsProvider(platform);
        } catch (ClassNotFoundException ignored) {
            // fallback to vanilla
        } catch (Exception e) {
            // catch any exception to prevent crashing the server, but still print a warning
            LOGGER.warn("Failed to load Fabric permissions provider. Falling back to Minecraft", e);
        }

        return super.createPermissionsProvider(platform);
    }
}
