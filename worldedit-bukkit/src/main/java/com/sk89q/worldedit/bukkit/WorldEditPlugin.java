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

package com.sk89q.worldedit.bukkit;

import com.google.common.base.Joiner;
import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.adapter.AdapterLoadException;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplAdapter;
import com.sk89q.worldedit.bukkit.adapter.BukkitImplLoader;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.event.platform.CommandSuggestionEvent;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Capability;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.internal.command.CommandUtil;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.FuzzyBlockState;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.gamemode.GameModes;
import com.sk89q.worldedit.world.item.ItemCategory;
import com.sk89q.worldedit.world.item.ItemType;
import com.sk89q.worldedit.world.weather.WeatherTypes;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.zip.ZipEntry;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Plugin for Bukkit.
 */
public class WorldEditPlugin extends JavaPlugin implements TabCompleter {

    private static final Logger log = LoggerFactory.getLogger(WorldEditPlugin.class);
    public static final String CUI_PLUGIN_CHANNEL = "worldedit:cui";
    private static WorldEditPlugin INSTANCE;

    private BukkitImplAdapter bukkitAdapter;
    private BukkitServerInterface server;
    private BukkitConfiguration config;

    @Override
    public void onLoad() {
        INSTANCE = this;

        //noinspection ResultOfMethodCallIgnored
        getDataFolder().mkdirs();

        WorldEdit worldEdit = WorldEdit.getInstance();

        // Setup platform
        server = new BukkitServerInterface(this, getServer());
        worldEdit.getPlatformManager().register(server);
        loadAdapter(); // Need an adapter to work with special blocks with NBT data
        setupRegistries();
        worldEdit.loadMappings();

        loadConfig(); // Load configuration
    }

    /**
     * Called on plugin enable.
     */
    @Override
    public void onEnable() {
        setupTags(); // these have to be done post-world since they rely on MC registries. the other ones just use Bukkit enums

        PermissionsResolverManager.initialize(this); // Setup permission resolver

        // Register CUI
        getServer().getMessenger().registerIncomingPluginChannel(this, CUI_PLUGIN_CHANNEL, new CUIChannelListener(this));
        getServer().getMessenger().registerOutgoingPluginChannel(this, CUI_PLUGIN_CHANNEL);

        // Now we can register events
        getServer().getPluginManager().registerEvents(new WorldEditListener(this), this);

        // If we are on MCPC+/Cauldron, then Forge will have already loaded
        // Forge WorldEdit and there's (probably) not going to be any other
        // platforms to be worried about... at the current time of writing
        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent());

        // Enable metrics
        new Metrics(this);
    }

    public void setupRegistries() {
        // Biome
        for (Biome biome : Biome.values()) {
            String lowerCaseBiomeName = biome.name().toLowerCase(Locale.ROOT);
            BiomeType.REGISTRY.register("minecraft:" + lowerCaseBiomeName, new BiomeType("minecraft:" + lowerCaseBiomeName));
        }
        // Block & Item
        for (Material material : Material.values()) {
            if (material.isBlock() && !material.isLegacy()) {
                BlockType.REGISTRY.register(material.getKey().toString(), new BlockType(material.getKey().toString(), blockState -> {
                    // TODO Use something way less hacky than this.
                    ParserContext context = new ParserContext();
                    context.setPreferringWildcard(true);
                    context.setTryLegacy(false);
                    context.setRestricted(false);
                    try {
                        FuzzyBlockState state = (FuzzyBlockState) WorldEdit.getInstance().getBlockFactory().parseFromInput(
                                BukkitAdapter.adapt(blockState.getBlockType()).createBlockData().getAsString(), context
                        ).toImmutableState();
                        BlockState defaultState = blockState.getBlockType().getAllStates().get(0);
                        for (Map.Entry<Property<?>, Object> propertyObjectEntry : state.getStates().entrySet()) {
                            defaultState = defaultState.with((Property) propertyObjectEntry.getKey(), propertyObjectEntry.getValue());
                        }
                        return defaultState;
                    } catch (InputParseException e) {
                        e.printStackTrace();
                        return blockState;
                    }
                }));
            }
            if (material.isItem() && !material.isLegacy()) {
                ItemType.REGISTRY.register(material.getKey().toString(), new ItemType(material.getKey().toString()));
            }
        }
        // Entity
        for (org.bukkit.entity.EntityType entityType : org.bukkit.entity.EntityType.values()) {
            String mcid = entityType.getName();
            if (mcid != null) {
                String lowerCaseMcId = mcid.toLowerCase(Locale.ROOT);
                EntityType.REGISTRY.register("minecraft:" + lowerCaseMcId, new EntityType("minecraft:" + lowerCaseMcId));
            }
        }
        // ... :|
        GameModes.get("");
        WeatherTypes.get("");
    }

    private void setupTags() {
        // Tags
        try {
            for (Tag<Material> blockTag : Bukkit.getTags(Tag.REGISTRY_BLOCKS, Material.class)) {
                BlockCategory.REGISTRY.register(blockTag.getKey().toString(), new BlockCategory(blockTag.getKey().toString()));
            }
            for (Tag<Material> itemTag : Bukkit.getTags(Tag.REGISTRY_ITEMS, Material.class)) {
                ItemCategory.REGISTRY.register(itemTag.getKey().toString(), new ItemCategory(itemTag.getKey().toString()));
            }
        } catch (NoSuchMethodError e) {
            getLogger().warning("The version of Spigot/Paper you are using doesn't support Tags. The usage of tags with WorldEdit will not work until you update.");
        }
    }

    private void loadConfig() {
        createDefaultConfiguration("config.yml"); // Create the default configuration file

        config = new BukkitConfiguration(new YAMLProcessor(new File(getDataFolder(), "config.yml"), true), this);
        config.load();
    }

    private void loadAdapter() {
        WorldEdit worldEdit = WorldEdit.getInstance();

        // Attempt to load a Bukkit adapter
        BukkitImplLoader adapterLoader = new BukkitImplLoader();

        try {
            adapterLoader.addFromPath(getClass().getClassLoader());
        } catch (IOException e) {
            log.warn("Failed to search path for Bukkit adapters");
        }

        try {
            adapterLoader.addFromJar(getFile());
        } catch (IOException e) {
            log.warn("Failed to search " + getFile() + " for Bukkit adapters", e);
        }
        try {
            bukkitAdapter = adapterLoader.loadAdapter();
            log.info("Using " + bukkitAdapter.getClass().getCanonicalName() + " as the Bukkit adapter");
        } catch (AdapterLoadException e) {
            Platform platform = worldEdit.getPlatformManager().queryCapability(Capability.WORLD_EDITING);
            if (platform instanceof BukkitServerInterface) {
                log.warn(e.getMessage());
            } else {
                log.info("WorldEdit could not find a Bukkit adapter for this MC version, " +
                        "but it seems that you have another implementation of WorldEdit installed (" + platform.getPlatformName() + ") " +
                        "that handles the world editing.");
            }
        }
    }

    /**
     * Called on plugin disable.
     */
    @Override
    public void onDisable() {
        WorldEdit worldEdit = WorldEdit.getInstance();
        worldEdit.getSessionManager().unload();
        worldEdit.getPlatformManager().unregister(server);
        if (config != null) {
            config.unload();
        }
        if (server != null) {
            server.unregisterCommands();
        }
        this.getServer().getScheduler().cancelTasks(this);
    }

    /**
     * Loads and reloads all configuration.
     */
    protected void loadConfiguration() {
        config.unload();
        config.load();
        getPermissionsResolver().load();
    }

    /**
     * Create a default configuration file from the .jar.
     *
     * @param name the filename
     */
    protected void createDefaultConfiguration(String name) {
        File actual = new File(getDataFolder(), name);
        if (!actual.exists()) {
            try (JarFile file = new JarFile(getFile())) {
                ZipEntry copy = file.getEntry("defaults/" + name);
                if (copy == null) throw new FileNotFoundException();
                copyDefaultConfig(file.getInputStream(copy), actual, name);
            } catch (IOException e) {
                getLogger().severe("Unable to read default configuration: " + name);
            }
        }
    }

    private void copyDefaultConfig(InputStream input, File actual, String name) {
        try (FileOutputStream output = new FileOutputStream(actual)) {
            byte[] buf = new byte[8192];
            int length;
            while ((length = input.read(buf)) > 0) {
                output.write(buf, 0, length);
            }

            getLogger().info("Default configuration file written: " + name);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Failed to write default config file", e);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        // Add the command to the array because the underlying command handling
        // code of WorldEdit expects it
        String[] split = new String[args.length + 1];
        System.arraycopy(args, 0, split, 1, args.length);
        split[0] = "/" + cmd.getName();

        CommandEvent event = new CommandEvent(wrapCommandSender(sender), Joiner.on(" ").join(split));
        getWorldEdit().getEventBus().post(event);

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String commandLabel, String[] args) {
        // Add the command to the array because the underlying command handling
        // code of WorldEdit expects it
        String[] split = new String[args.length + 1];
        System.arraycopy(args, 0, split, 1, args.length);
        split[0] = "/" + cmd.getName();

        String arguments = Joiner.on(" ").join(split);
        CommandSuggestionEvent event = new CommandSuggestionEvent(wrapCommandSender(sender), arguments);
        getWorldEdit().getEventBus().post(event);
        return CommandUtil.fixSuggestions(arguments, event.getSuggestions());
    }

    /**
     * Gets the session for the player.
     *
     * @param player a player
     * @return a session
     */
    public LocalSession getSession(Player player) {
        return WorldEdit.getInstance().getSessionManager().get(wrapPlayer(player));
    }

    /**
     * Gets the session for the player.
     *
     * @param player a player
     * @return a session
     */
    public EditSession createEditSession(Player player) {
        com.sk89q.worldedit.entity.Player wePlayer = wrapPlayer(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);
        BlockBag blockBag = session.getBlockBag(wePlayer);

        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory()
                .getEditSession(wePlayer.getWorld(), session.getBlockChangeLimit(), blockBag, wePlayer);
        editSession.enableStandardMode();

        return editSession;
    }

    /**
     * Remember an edit session.
     *
     * @param player a player
     * @param editSession an edit session
     */
    public void remember(Player player, EditSession editSession) {
        com.sk89q.worldedit.entity.Player wePlayer = wrapPlayer(player);
        LocalSession session = WorldEdit.getInstance().getSessionManager().get(wePlayer);

        session.remember(editSession);
        editSession.flushSession();

        WorldEdit.getInstance().flushBlockBag(wePlayer, editSession);
    }

    /**
     * Returns the configuration used by WorldEdit.
     *
     * @return the configuration
     */
    public BukkitConfiguration getLocalConfiguration() {
        return config;
    }

    /**
     * Get the permissions resolver in use.
     *
     * @return the permissions resolver
     */
    public PermissionsResolverManager getPermissionsResolver() {
        return PermissionsResolverManager.getInstance();
    }

    /**
     * Used to wrap a Bukkit Player as a WorldEdit Player.
     *
     * @param player a player
     * @return a wrapped player
     */
    public BukkitPlayer wrapPlayer(Player player) {
        return new BukkitPlayer(this, player);
    }

    public Actor wrapCommandSender(CommandSender sender) {
        if (sender instanceof Player) {
            return wrapPlayer((Player) sender);
        }

        return new BukkitCommandSender(this, sender);
    }

    BukkitServerInterface getInternalPlatform() {
        return server;
    }

    /**
     * Get WorldEdit.
     *
     * @return an instance
     */
    public WorldEdit getWorldEdit() {
        return WorldEdit.getInstance();
    }

    /**
     * Gets the instance of this plugin.
     *
     * @return an instance of the plugin
     * @throws NullPointerException if the plugin hasn't been enabled
     */
    static WorldEditPlugin getInstance() {
        return checkNotNull(INSTANCE);
    }

    /**
     * Get the Bukkit implementation adapter.
     *
     * @return the adapter
     */
    @Nullable
    BukkitImplAdapter getBukkitImplAdapter() {
        return bukkitAdapter;
    }

}
