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

package com.sk89q.worldedit.forge;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.event.platform.SessionIdleEvent;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.forge.net.handler.InternalPacketHandler;
import com.sk89q.worldedit.forge.net.handler.WECUIPacketHandler;
import com.sk89q.worldedit.forge.net.packet.LeftClickAirEventMessage;
import com.sk89q.worldedit.internal.anvil.ChunkDeleter;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.item.ItemCategory;
import com.sk89q.worldedit.world.item.ItemType;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickEmpty;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.forge.ForgeAdapter.adaptPlayer;
import static com.sk89q.worldedit.internal.anvil.ChunkDeleter.DELCHUNKS_FILE_NAME;

/**
 * The Forge implementation of WorldEdit.
 */
@Mod(ForgeWorldEdit.MOD_ID)
public class ForgeWorldEdit {

    private static final Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "worldedit";
    public static final String CUI_PLUGIN_CHANNEL = "cui";

    private ForgePermissionsProvider provider;

    public static ForgeWorldEdit inst;

    private ForgePlatform platform;
    private ForgeConfiguration config;
    private Path workingDir;

    private ModContainer container;

    public ForgeWorldEdit() {
        inst = this;

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(this::init);

        MinecraftForge.EVENT_BUS.register(ThreadSafeCache.getInstance());
        MinecraftForge.EVENT_BUS.register(this);

        // Mark WorldEdit as only required on the server
        ModLoadingContext.get().registerExtensionPoint(
            ExtensionPoint.DISPLAYTEST,
            () -> Pair.of(
                () -> FMLNetworkConstants.IGNORESERVERONLY,
                (a, b) -> true
            )
        );
    }

    private void init(FMLCommonSetupEvent event) {
        this.container = ModLoadingContext.get().getActiveContainer();

        // Setup working directory
        workingDir = FMLPaths.CONFIGDIR.get().resolve("worldedit");
        if (!Files.exists(workingDir)) {
            try {
                Files.createDirectory(workingDir);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        WECUIPacketHandler.init();
        InternalPacketHandler.init();

        LOGGER.info("WorldEdit for Forge (version " + getInternalVersion() + ") is loaded");
    }

    private void setupPlatform() {
        this.platform = new ForgePlatform(this);

        WorldEdit.getInstance().getPlatformManager().register(platform);

        //  TODO      if (ModList.get().isLoaded("sponge")) {
        //            this.provider = new ForgePermissionsProvider.SpongePermissionsProvider();
        //        } else {
        this.provider = new ForgePermissionsProvider.VanillaPermissionsProvider(platform);
        //        }
    }

    private void setupRegistries(MinecraftServer server) {
        // Blocks
        for (ResourceLocation name : ForgeRegistries.BLOCKS.getKeys()) {
            if (BlockType.REGISTRY.get(name.toString()) == null) {
                BlockType.REGISTRY.register(name.toString(), new BlockType(name.toString(),
                    input -> ForgeAdapter.adapt(ForgeAdapter.adapt(input.getBlockType()).getDefaultState())));
            }
        }
        // Items
        for (ResourceLocation name : ForgeRegistries.ITEMS.getKeys()) {
            if (ItemType.REGISTRY.get(name.toString()) == null) {
                ItemType.REGISTRY.register(name.toString(), new ItemType(name.toString()));
            }
        }
        // Entities
        for (ResourceLocation name : ForgeRegistries.ENTITIES.getKeys()) {
            if (EntityType.REGISTRY.get(name.toString()) == null) {
                EntityType.REGISTRY.register(name.toString(), new EntityType(name.toString()));
            }
        }
        // Biomes
        for (ResourceLocation name : server.func_244267_aX().func_243612_b(Registry.field_239720_u_).keySet()) {
            if (BiomeType.REGISTRY.get(name.toString()) == null) {
                BiomeType.REGISTRY.register(name.toString(), new BiomeType(name.toString()));
            }
        }
        // Tags
        for (ResourceLocation name : BlockTags.getCollection().getRegisteredTags()) {
            if (BlockCategory.REGISTRY.get(name.toString()) == null) {
                BlockCategory.REGISTRY.register(name.toString(), new BlockCategory(name.toString()));
            }
        }
        for (ResourceLocation name : ItemTags.getCollection().getRegisteredTags()) {
            if (ItemCategory.REGISTRY.get(name.toString()) == null) {
                ItemCategory.REGISTRY.register(name.toString(), new ItemCategory(name.toString()));
            }
        }
    }

    @SubscribeEvent
    public void serverAboutToStart(FMLServerAboutToStartEvent event) {
        final Path delChunks = workingDir.resolve(DELCHUNKS_FILE_NAME);
        if (Files.exists(delChunks)) {
            ChunkDeleter.runFromFile(delChunks, true);
        }
    }

    @SubscribeEvent
    public void serverStopping(FMLServerStoppingEvent event) {
        WorldEdit worldEdit = WorldEdit.getInstance();
        worldEdit.getSessionManager().unload();
        worldEdit.getPlatformManager().unregister(platform);
    }

    @SubscribeEvent
    public void serverStarted(FMLServerStartedEvent event) {
        setupPlatform();
        setupRegistries(event.getServer());

        config = new ForgeConfiguration(this);
        config.load();
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent());
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (platform == null) {
            return;
        }

        if (!platform.isHookingEvents()) {
            return; // We have to be told to catch these events
        }

        if (event.getWorld().isRemote && event instanceof LeftClickEmpty) {
            // catch LCE, pass it to server
            InternalPacketHandler.getHandler().sendToServer(LeftClickAirEventMessage.INSTANCE);
            return;
        }

        boolean isLeftDeny = event instanceof PlayerInteractEvent.LeftClickBlock
                && ((PlayerInteractEvent.LeftClickBlock) event)
                        .getUseItem() == Event.Result.DENY;
        boolean isRightDeny =
                event instanceof PlayerInteractEvent.RightClickBlock
                        && ((PlayerInteractEvent.RightClickBlock) event)
                                .getUseItem() == Event.Result.DENY;
        if (isLeftDeny || isRightDeny || event.getEntity().world.isRemote || event.getHand() == Hand.OFF_HAND) {
            return;
        }

        WorldEdit we = WorldEdit.getInstance();
        ForgePlayer player = adaptPlayer((ServerPlayerEntity) event.getPlayer());
        ForgeWorld world = getWorld(event.getPlayer().world);
        Direction direction = ForgeAdapter.adaptEnumFacing(event.getFace());

        if (event instanceof PlayerInteractEvent.LeftClickEmpty) {
            we.handleArmSwing(player); // this event cannot be canceled
        } else if (event instanceof PlayerInteractEvent.LeftClickBlock) {
            Location pos = new Location(world, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());

            if (we.handleBlockLeftClick(player, pos, direction)) {
                event.setCanceled(true);
            }

            if (we.handleArmSwing(player)) {
                event.setCanceled(true);
            }
        } else if (event instanceof PlayerInteractEvent.RightClickBlock) {
            Location pos = new Location(world, event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());

            if (we.handleBlockRightClick(player, pos, direction)) {
                event.setCanceled(true);
            }

            if (we.handleRightClick(player)) {
                event.setCanceled(true);
            }
        } else if (event instanceof PlayerInteractEvent.RightClickItem) {
            if (we.handleRightClick(player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onCommandEvent(CommandEvent event) throws CommandSyntaxException {
        ParseResults<CommandSource> parseResults = event.getParseResults();
        if (!(parseResults.getContext().getSource().getEntity() instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity player = parseResults.getContext().getSource().asPlayer();
        if (player.world.isRemote()) {
            return;
        }
        if (parseResults.getContext().getCommand() != CommandWrapper.FAKE_COMMAND) {
            return;
        }
        event.setCanceled(true);
        WorldEdit.getInstance().getEventBus().post(new com.sk89q.worldedit.event.platform.CommandEvent(
            adaptPlayer(parseResults.getContext().getSource().asPlayer()),
            parseResults.getReader().getString()
        ));
    }

    @SubscribeEvent
    public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() instanceof ServerPlayerEntity) {
            WorldEdit.getInstance().getEventBus()
                    .post(new SessionIdleEvent(new ForgePlayer.SessionKeyImpl((ServerPlayerEntity) event.getPlayer())));
        }
    }

    /**
     * Get the configuration.
     *
     * @return the Forge configuration
     */
    ForgeConfiguration getConfig() {
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
    public ForgeWorld getWorld(World world) {
        checkNotNull(world);
        return new ForgeWorld(world);
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
    public Path getWorkingDir() {
        return this.workingDir;
    }

    /**
     * Get the version of the WorldEdit-for-Forge implementation.
     *
     * @return a version string
     */
    String getInternalVersion() {
        return container.getModInfo().getVersion().toString();
    }

    public void setPermissionsProvider(ForgePermissionsProvider provider) {
        this.provider = provider;
    }

    public ForgePermissionsProvider getPermissionsProvider() {
        return provider;
    }

}
