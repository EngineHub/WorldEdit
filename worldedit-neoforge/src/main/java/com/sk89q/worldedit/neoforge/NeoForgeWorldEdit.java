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

package com.sk89q.worldedit.neoforge;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.command.util.PermissionCondition;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.event.platform.PlatformUnreadyEvent;
import com.sk89q.worldedit.event.platform.PlatformsRegisteredEvent;
import com.sk89q.worldedit.event.platform.SessionIdleEvent;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformManager;
import com.sk89q.worldedit.internal.anvil.ChunkDeleter;
import com.sk89q.worldedit.internal.event.InteractionDebouncer;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeCategory;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.item.ItemCategory;
import com.sk89q.worldedit.world.item.ItemType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.CommandEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.apache.logging.log4j.Logger;
import org.enginehub.piston.Command;
import org.enginehub.worldeditcui.protocol.CUIPacket;
import org.enginehub.worldeditcui.protocol.CUIPacketHandler;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.sk89q.worldedit.internal.anvil.ChunkDeleter.DELCHUNKS_FILE_NAME;
import static com.sk89q.worldedit.neoforge.NeoForgeAdapter.adaptCommandSource;
import static com.sk89q.worldedit.neoforge.NeoForgeAdapter.adaptPlayer;

/**
 * The Forge implementation of WorldEdit.
 */
@Mod(NeoForgeWorldEdit.MOD_ID)
public class NeoForgeWorldEdit {

    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final String MOD_ID = "worldedit";

    private NeoForgePermissionsProvider provider;

    public static NeoForgeWorldEdit inst;

    private InteractionDebouncer debouncer;
    private NeoForgePlatform platform;
    private NeoForgeConfiguration config;
    private Path workingDir;

    private ModContainer container;

    public NeoForgeWorldEdit(IEventBus modBus) {
        inst = this;

        modBus.addListener(this::init);

        NeoForge.EVENT_BUS.register(ThreadSafeCache.getInstance());
        NeoForge.EVENT_BUS.register(this);
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

        CUIPacketHandler.instance().registerServerboundHandler(this::onCuiPacket);

        setupPlatform();

        LOGGER.info("WorldEdit for NeoForge (version {}) is loaded", getInternalVersion());
    }

    private void setupPlatform() {
        this.platform = new NeoForgePlatform(this);
        debouncer = new InteractionDebouncer(platform);

        WorldEdit.getInstance().getPlatformManager().register(platform);

        config = new NeoForgeConfiguration(this);

        this.provider = new NeoForgePermissionsProvider.VanillaPermissionsProvider(platform);
    }

    private void setupRegistries(MinecraftServer server) {
        // Blocks
        for (ResourceLocation name : BuiltInRegistries.BLOCK.keySet()) {
            String key = name.toString();
            if (BlockType.REGISTRY.get(key) == null) {
                BlockType.REGISTRY.register(key, new BlockType(key,
                    input -> NeoForgeAdapter.adapt(NeoForgeAdapter.adapt(input.getBlockType()).defaultBlockState())));
            }
        }
        // Items
        for (ResourceLocation name : BuiltInRegistries.ITEM.keySet()) {
            String key = name.toString();
            if (ItemType.REGISTRY.get(key) == null) {
                ItemType.REGISTRY.register(key, new ItemType(key));
            }
        }
        // Entities
        for (ResourceLocation name : BuiltInRegistries.ENTITY_TYPE.keySet()) {
            String key = name.toString();
            if (EntityType.REGISTRY.get(key) == null) {
                EntityType.REGISTRY.register(key, new EntityType(key));
            }
        }
        // Biomes
        for (ResourceLocation name : server.registryAccess().lookupOrThrow(Registries.BIOME).keySet()) {
            String key = name.toString();
            if (BiomeType.REGISTRY.get(key) == null) {
                BiomeType.REGISTRY.register(key, new BiomeType(key));
            }
        }
        // Tags
        server.registryAccess().lookupOrThrow(Registries.BLOCK).getTags().map(t -> t.key().location()).forEach(name -> {
            String key = name.toString();
            if (BlockCategory.REGISTRY.get(key) == null) {
                BlockCategory.REGISTRY.register(key, new BlockCategory(key));
            }
        });
        server.registryAccess().lookupOrThrow(Registries.ITEM).getTags().map(t -> t.key().location()).forEach(name -> {
            String key = name.toString();
            if (ItemCategory.REGISTRY.get(key) == null) {
                ItemCategory.REGISTRY.register(key, new ItemCategory(key));
            }
        });
        Registry<Biome> biomeRegistry = server.registryAccess().lookupOrThrow(Registries.BIOME);
        biomeRegistry.getTags().forEach(tag -> {
            String key = tag.key().location().toString();
            if (BiomeCategory.REGISTRY.get(key) == null) {
                BiomeCategory.REGISTRY.register(key, new BiomeCategory(
                    key,
                    () -> biomeRegistry.get(tag.key())
                        .stream()
                        .flatMap(HolderSet.Named::stream)
                        .map(Holder::value)
                        .map(NeoForgeAdapter::adapt)
                        .collect(Collectors.toSet()))
                );
            }
        });
        // Features
        for (ResourceLocation name : server.registryAccess().lookupOrThrow(Registries.CONFIGURED_FEATURE).keySet()) {
            String key = name.toString();
            if (ConfiguredFeatureType.REGISTRY.get(key) == null) {
                ConfiguredFeatureType.REGISTRY.register(key, new ConfiguredFeatureType(key));
            }
        }
        // Structures
        for (ResourceLocation name : server.registryAccess().lookupOrThrow(Registries.STRUCTURE).keySet()) {
            String key = name.toString();
            if (StructureType.REGISTRY.get(key) == null) {
                StructureType.REGISTRY.register(key, new StructureType(key));
            }
        }
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        WorldEdit.getInstance().getEventBus().post(new PlatformsRegisteredEvent());

        PlatformManager manager = WorldEdit.getInstance().getPlatformManager();
        Platform commandsPlatform = manager.queryCapability(Capability.USER_COMMANDS);
        if (commandsPlatform != platform || !platform.isHookingEvents()) {
            // We're not in control of commands/events -- do not register.
            return;
        }

        List<Command> commands = manager.getPlatformCommandManager().getCommandManager()
            .getAllCommands().toList();
        for (Command command : commands) {
            CommandWrapper.register(event.getDispatcher(), command);
            Set<String> perms = command.getCondition().as(PermissionCondition.class)
                .map(PermissionCondition::getPermissions)
                .orElseGet(Collections::emptySet);
            if (!perms.isEmpty()) {
                perms.forEach(getPermissionsProvider()::registerPermission);
            }
        }
    }

    @SubscribeEvent
    public void serverAboutToStart(ServerAboutToStartEvent event) {
        final Path delChunks = workingDir.resolve(DELCHUNKS_FILE_NAME);
        if (Files.exists(delChunks)) {
            ChunkDeleter.runFromFile(delChunks, true);
        }
    }

    @SubscribeEvent
    public void serverStopping(ServerStoppingEvent event) {
        WorldEdit worldEdit = WorldEdit.getInstance();
        worldEdit.getSessionManager().unload();
        WorldEdit.getInstance().getEventBus().post(new PlatformUnreadyEvent(platform));
    }

    @SubscribeEvent
    public void serverStarted(ServerStartedEvent event) {
        setupRegistries(event.getServer());

        config.load();
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent(platform));
    }

    private boolean skipEvents() {
        return platform == null || !platform.isHookingEvents();
    }

    private boolean skipInteractionEvent(Player player, InteractionHand hand) {
        return skipEvents() || hand != InteractionHand.MAIN_HAND || player.level().isClientSide || !(player instanceof ServerPlayer);
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (skipInteractionEvent(event.getEntity(), event.getHand()) || event.getUseItem().isFalse()) {
            return;
        }

        ServerPlayer playerEntity = (ServerPlayer) event.getEntity();
        WorldEdit we = WorldEdit.getInstance();
        NeoForgePlayer player = adaptPlayer(playerEntity);
        NeoForgeWorld world = getWorld(playerEntity.level());
        Direction direction = NeoForgeAdapter.adaptEnumFacing(event.getFace());

        BlockPos blockPos = event.getPos();
        Location pos = new Location(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());

        boolean result = we.handleBlockLeftClick(player, pos, direction) || we.handleArmSwing(player);
        debouncer.setLastInteraction(player, result);

        if (result) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (skipInteractionEvent(event.getEntity(), event.getHand()) || event.getUseItem().isFalse()) {
            return;
        }

        ServerPlayer playerEntity = (ServerPlayer) event.getEntity();
        WorldEdit we = WorldEdit.getInstance();
        NeoForgePlayer player = adaptPlayer(playerEntity);
        NeoForgeWorld world = getWorld(playerEntity.level());
        Direction direction = NeoForgeAdapter.adaptEnumFacing(event.getFace());

        BlockPos blockPos = event.getPos();
        Location pos = new Location(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());

        boolean result = we.handleBlockRightClick(player, pos, direction) || we.handleRightClick(player);
        debouncer.setLastInteraction(player, result);

        if (result) {
            event.setCanceled(true);
        }
    }

    public void onLeftClickAir(ServerPlayer playerEntity, InteractionHand hand) {
        if (skipInteractionEvent(playerEntity, hand)) {
            return;
        }

        WorldEdit we = WorldEdit.getInstance();
        NeoForgePlayer player = adaptPlayer(playerEntity);

        Optional<Boolean> previousResult = debouncer.getDuplicateInteractionResult(player);
        if (previousResult.isPresent()) {
            return;
        }

        boolean result = we.handleArmSwing(player);
        debouncer.setLastInteraction(player, result);
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (skipInteractionEvent(event.getEntity(), event.getHand())) {
            return;
        }

        ServerPlayer playerEntity = (ServerPlayer) event.getEntity();
        WorldEdit we = WorldEdit.getInstance();
        NeoForgePlayer player = adaptPlayer(playerEntity);

        Optional<Boolean> previousResult = debouncer.getDuplicateInteractionResult(player);
        if (previousResult.isPresent()) {
            if (previousResult.get()) {
                event.setCanceled(true);
            }
            return;
        }

        boolean result = we.handleRightClick(player);
        debouncer.setLastInteraction(player, result);

        if (result) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onCommandEvent(CommandEvent event) throws CommandSyntaxException {
        ParseResults<CommandSourceStack> parseResults = event.getParseResults();
        if (parseResults.getContext().getSource().getEntity() instanceof ServerPlayer player && player.level().isClientSide) {
            return;
        }
        if (parseResults.getContext().getCommand() != CommandWrapper.FAKE_COMMAND) {
            return;
        }
        event.setCanceled(true);
        WorldEdit.getInstance().getEventBus().post(new com.sk89q.worldedit.event.platform.CommandEvent(
            adaptCommandSource(parseResults.getContext().getSource()),
            "/" + parseResults.getReader().getString()
        ));
    }

    @SubscribeEvent
    public void onPlayerLogOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            debouncer.clearInteraction(adaptPlayer(player));

            WorldEdit.getInstance().getEventBus()
                .post(new SessionIdleEvent(new NeoForgePlayer.SessionKeyImpl(player)));
        }
    }

    private void onCuiPacket(CUIPacket payload, CUIPacketHandler.PacketContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            // Ignore - this is not a server-bound packet
            return;
        }
        NeoForgePlayer actor = NeoForgeAdapter.adaptPlayer(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(actor);
        session.handleCUIInitializationMessage(payload.eventType(), payload.args(), actor);
    }

    /**
     * Get the configuration.
     *
     * @return the Forge configuration
     */
    NeoForgeConfiguration getConfig() {
        return this.config;
    }

    /**
     * Get the session for a player.
     *
     * @param player the player
     * @return the session
     */
    public LocalSession getSession(ServerPlayer player) {
        checkNotNull(player);
        return WorldEdit.getInstance().getSessionManager().get(adaptPlayer(player));
    }

    /**
     * Get the WorldEdit proxy for the given world.
     *
     * @param world the world
     * @return the WorldEdit world
     */
    public NeoForgeWorld getWorld(ServerLevel world) {
        checkNotNull(world);
        return new NeoForgeWorld(world);
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

    public void setPermissionsProvider(NeoForgePermissionsProvider provider) {
        this.provider = provider;
    }

    public NeoForgePermissionsProvider getPermissionsProvider() {
        return provider;
    }

}
