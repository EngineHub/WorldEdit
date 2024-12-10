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

package com.sk89q.worldedit.fabric;

import com.mojang.brigadier.CommandDispatcher;
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
import com.sk89q.worldedit.fabric.net.handler.WECUIPacketHandler;
import com.sk89q.worldedit.internal.anvil.ChunkDeleter;
import com.sk89q.worldedit.internal.event.InteractionDebouncer;
import com.sk89q.worldedit.internal.util.LogManagerCompat;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.lifecycle.Lifecycled;
import com.sk89q.worldedit.util.lifecycle.SimpleLifecycled;
import com.sk89q.worldedit.world.biome.BiomeCategory;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.generation.ConfiguredFeatureType;
import com.sk89q.worldedit.world.generation.StructureType;
import com.sk89q.worldedit.world.item.ItemCategory;
import com.sk89q.worldedit.world.item.ItemType;
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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.BlockHitResult;
import org.apache.logging.log4j.Logger;
import org.enginehub.piston.Command;

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
import static com.sk89q.worldedit.fabric.FabricAdapter.adaptPlayer;
import static com.sk89q.worldedit.internal.anvil.ChunkDeleter.DELCHUNKS_FILE_NAME;

/**
 * The Fabric implementation of WorldEdit.
 */
public class FabricWorldEdit implements ModInitializer {

    private static final Logger LOGGER = LogManagerCompat.getLogger();
    public static final String MOD_ID = "worldedit";
    public static final String CUI_PLUGIN_CHANNEL = "cui";

    public static final Lifecycled<MinecraftServer> LIFECYCLED_SERVER;

    static {
        SimpleLifecycled<MinecraftServer> lifecycledServer = SimpleLifecycled.invalid();
        ServerLifecycleEvents.SERVER_STARTED.register(lifecycledServer::newValue);
        ServerLifecycleEvents.SERVER_STOPPING.register(__ -> lifecycledServer.invalidate());
        LIFECYCLED_SERVER = lifecycledServer;
    }

    /**
     * {@return current server's registry access} Not for long-term storage.
     */
    public static RegistryAccess registryAccess() {
        return LIFECYCLED_SERVER.valueOrThrow().registryAccess();
    }

    /**
     * {@return current server's registry} Not for long-term storage.
     *
     * @param key the registry key
     */
    public static <T> Registry<T> getRegistry(ResourceKey<Registry<T>> key) {
        return LIFECYCLED_SERVER.valueOrThrow().registryAccess().lookupOrThrow(key);
    }

    private FabricPermissionsProvider provider;

    public static FabricWorldEdit inst;

    private InteractionDebouncer debouncer;
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
        workingDir = FabricLoader.getInstance().getConfigDir().resolve("worldedit");
        if (!Files.exists(workingDir)) {
            try {
                Files.createDirectory(workingDir);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        this.platform = new FabricPlatform(this);
        debouncer = new InteractionDebouncer(platform);

        WorldEdit.getInstance().getPlatformManager().register(platform);

        config = new FabricConfiguration(this);
        this.provider = getInitialPermissionsProvider();

        WECUIPacketHandler.init();

        ServerTickEvents.END_SERVER_TICK.register(ThreadSafeCache.getInstance());
        CommandRegistrationCallback.EVENT.register(this::registerCommands);
        ServerLifecycleEvents.SERVER_STARTING.register(this::onStartingServer);
        ServerLifecycleEvents.SERVER_STARTED.register(this::onStartServer);
        ServerLifecycleEvents.SERVER_STOPPING.register(this::onStopServer);
        ServerPlayConnectionEvents.DISCONNECT.register(this::onPlayerDisconnect);
        AttackBlockCallback.EVENT.register(this::onLeftClickBlock);
        UseBlockCallback.EVENT.register(this::onRightClickBlock);
        UseItemCallback.EVENT.register(this::onRightClickItem);
        LOGGER.info("WorldEdit for Fabric (version " + getInternalVersion() + ") is loaded");
    }

    private void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
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
            CommandWrapper.register(dispatcher, command);
            Set<String> perms = command.getCondition().as(PermissionCondition.class)
                .map(PermissionCondition::getPermissions)
                .orElseGet(Collections::emptySet);
            if (!perms.isEmpty()) {
                perms.forEach(getPermissionsProvider()::registerPermission);
            }
        }
    }

    private FabricPermissionsProvider getInitialPermissionsProvider() {
        try {
            Class.forName("me.lucko.fabric.api.permissions.v0.Permissions", false, getClass().getClassLoader());
            return new FabricPermissionsProvider.LuckoFabricPermissionsProvider(platform);
        } catch (ClassNotFoundException ignored) {
            // fallback to vanilla
        }
        return new FabricPermissionsProvider.VanillaPermissionsProvider(platform);
    }

    private void setupRegistries(MinecraftServer server) {
        // Blocks
        for (ResourceLocation name : server.registryAccess().lookupOrThrow(Registries.BLOCK).keySet()) {
            String key = name.toString();
            if (BlockType.REGISTRY.get(key) == null) {
                BlockType.REGISTRY.register(key, new BlockType(key,
                    input -> FabricAdapter.adapt(FabricAdapter.adapt(input.getBlockType()).defaultBlockState())));
            }
        }
        // Items
        for (ResourceLocation name : server.registryAccess().lookupOrThrow(Registries.ITEM).keySet()) {
            String key = name.toString();
            if (ItemType.REGISTRY.get(key) == null) {
                ItemType.REGISTRY.register(key, new ItemType(key));
            }
        }
        // Entities
        for (ResourceLocation name : server.registryAccess().lookupOrThrow(Registries.ENTITY_TYPE).keySet()) {
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
                        .map(FabricAdapter::adapt)
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

    private void onStartingServer(MinecraftServer minecraftServer) {
        final Path delChunks = workingDir.resolve(DELCHUNKS_FILE_NAME);
        if (Files.exists(delChunks)) {
            ChunkDeleter.runFromFile(delChunks, true);
        }
    }

    private void onStartServer(MinecraftServer minecraftServer) {
        setupRegistries(minecraftServer);

        config.load();
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent(platform));
    }

    private void onStopServer(MinecraftServer minecraftServer) {
        WorldEdit worldEdit = WorldEdit.getInstance();
        worldEdit.getSessionManager().unload();
        WorldEdit.getInstance().getEventBus().post(new PlatformUnreadyEvent(platform));
    }

    private boolean skipEvents() {
        return platform == null || !platform.isHookingEvents();
    }

    private boolean skipInteractionEvent(Player player, InteractionHand hand) {
        return skipEvents() || hand != InteractionHand.MAIN_HAND || player.level().isClientSide || !(player instanceof ServerPlayer);
    }

    private InteractionResult onLeftClickBlock(Player playerEntity, Level world, InteractionHand hand, BlockPos blockPos, Direction direction) {
        if (skipInteractionEvent(playerEntity, hand)) {
            return InteractionResult.PASS;
        }

        WorldEdit we = WorldEdit.getInstance();
        FabricPlayer player = adaptPlayer((ServerPlayer) playerEntity);
        FabricWorld localWorld = getWorld(world);
        Location pos = new Location(localWorld,
            blockPos.getX(),
            blockPos.getY(),
            blockPos.getZ()
        );
        com.sk89q.worldedit.util.Direction weDirection = FabricAdapter.adaptEnumFacing(direction);

        boolean result = we.handleBlockLeftClick(player, pos, weDirection) || we.handleArmSwing(player);
        debouncer.setLastInteraction(player, result);

        return result ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private InteractionResult onRightClickBlock(Player playerEntity, Level world, InteractionHand hand, BlockHitResult blockHitResult) {
        if (skipInteractionEvent(playerEntity, hand)) {
            return InteractionResult.PASS;
        }

        WorldEdit we = WorldEdit.getInstance();
        FabricPlayer player = adaptPlayer((ServerPlayer) playerEntity);
        FabricWorld localWorld = getWorld(world);
        Location pos = new Location(localWorld,
            blockHitResult.getBlockPos().getX(),
            blockHitResult.getBlockPos().getY(),
            blockHitResult.getBlockPos().getZ()
        );
        com.sk89q.worldedit.util.Direction direction = FabricAdapter.adaptEnumFacing(blockHitResult.getDirection());

        boolean result = we.handleBlockRightClick(player, pos, direction) || we.handleRightClick(player);
        debouncer.setLastInteraction(player, result);

        return result ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    public void onLeftClickAir(ServerPlayer playerEntity, InteractionHand hand) {
        if (skipInteractionEvent(playerEntity, hand)) {
            return;
        }

        WorldEdit we = WorldEdit.getInstance();
        FabricPlayer player = adaptPlayer(playerEntity);

        Optional<Boolean> previousResult = debouncer.getDuplicateInteractionResult(player);
        if (previousResult.isPresent()) {
            return;
        }

        boolean result = we.handleArmSwing(player);
        debouncer.setLastInteraction(player, result);
    }

    private InteractionResult onRightClickItem(Player playerEntity, Level world, InteractionHand hand) {
        if (skipInteractionEvent(playerEntity, hand)) {
            return InteractionResult.PASS;
        }

        WorldEdit we = WorldEdit.getInstance();
        FabricPlayer player = adaptPlayer((ServerPlayer) playerEntity);

        Optional<Boolean> previousResult = debouncer.getDuplicateInteractionResult(player);
        if (previousResult.isPresent()) {
            return previousResult.get() ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        boolean result = we.handleRightClick(player);
        debouncer.setLastInteraction(player, result);

        return result ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    private void onPlayerDisconnect(ServerGamePacketListenerImpl handler, MinecraftServer server) {
        debouncer.clearInteraction(adaptPlayer(handler.player));

        WorldEdit.getInstance().getEventBus()
            .post(new SessionIdleEvent(new FabricPlayer.SessionKeyImpl(handler.player)));
    }

    /**
     * Get the configuration.
     *
     * @return the Fabric configuration
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
    public FabricWorld getWorld(Level world) {
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
    public Path getWorkingDir() {
        return this.workingDir;
    }

    /**
     * Get the version of the WorldEdit-Fabric implementation.
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
