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

package com.sk89q.worldedit.neoforge.internal;

import com.sk89q.worldedit.coremc.internal.CoreMcMod;
import com.sk89q.worldedit.coremc.internal.CoreMcPlatform;
import com.sk89q.worldedit.coremc.internal.ThreadSafeCache;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Logger;
import org.enginehub.worldeditcui.protocol.CUIPacketHandler;

import java.util.Optional;

/**
 * The NeoForge implementation of WorldEdit.
 */
@Mod(NeoForgeWorldEdit.MOD_ID)
public class NeoForgeWorldEdit extends CoreMcMod {

    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final String MOD_ID = "worldedit";

    private static volatile CoreMcPlatform PLATFORM;

    public static CoreMcPlatform getPlatform() {
        CoreMcPlatform platform = PLATFORM;
        if (platform == null) {
            throw new IllegalStateException("NeoForgeWorldEdit is not initialized");
        }
        return platform;
    }

    private ModContainer container;

    public NeoForgeWorldEdit(IEventBus modBus) {
        modBus.addListener(this::onFMLCommonSetup);

        NeoForge.EVENT_BUS.register(new EventHandler(this));
    }

    private void onFMLCommonSetup(FMLCommonSetupEvent event) {
        this.container = ModLoadingContext.get().getActiveContainer();

        PLATFORM = new NeoForgePlatform(this);
        init(PLATFORM, FMLPaths.CONFIGDIR.get());

        CUIPacketHandler.instance().registerServerboundHandler(this::onCuiPacket);

        LOGGER.info("WorldEdit for NeoForge (version {}) is loaded", getInternalVersion());
    }

    @Override
    protected String getInternalVersion() {
        return container.getModInfo().getVersion().toString();
    }

    // Proxy all events through a separate class to avoid loading all of CoreMcMod's methods immediately
    // It causes some weird classloader issue
    public static final class EventHandler {
        private final NeoForgeWorldEdit mod;

        public EventHandler(NeoForgeWorldEdit mod) {
            this.mod = mod;
        }

        @SubscribeEvent
        public void registerCommands(RegisterCommandsEvent event) {
            mod.registerCommands(event.getDispatcher());
        }

        @SubscribeEvent
        public void serverAboutToStart(ServerAboutToStartEvent event) {
            mod.serverAboutToStart();
        }

        @SubscribeEvent
        public void serverStarted(ServerStartedEvent event) {
            mod.serverStarted(event.getServer());
        }

        @SubscribeEvent
        public void serverStopping(ServerStoppingEvent event) {
            mod.serverStopping();
        }

        @SubscribeEvent
        public void onServerTick(ServerTickEvent.Post event) {
            ThreadSafeCache.getInstance().onEndTick(ServerLifecycleHooks.getCurrentServer());
        }

        @SubscribeEvent
        public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
            if (event.getUseItem().isFalse()) {
                return;
            }
            if (mod.onLeftClickBlock(event.getEntity(), event.getHand(), event.getPos(), event.getFace())) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            if (event.getUseItem().isFalse()) {
                return;
            }
            if (mod.onRightClickBlock(event.getEntity(), event.getHand(), event.getPos(), event.getFace())) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            Optional<Boolean> result = mod.onRightClickItem(event.getEntity(), event.getHand());
            if (result.isPresent() && result.get()) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
            mod.onPlayerDisconnect(event.getEntity());
        }
    }
}
