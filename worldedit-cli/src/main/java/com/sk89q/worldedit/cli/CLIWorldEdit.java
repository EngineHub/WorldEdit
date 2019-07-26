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

package com.sk89q.worldedit.cli;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.cli.data.FileRegistries;
import com.sk89q.worldedit.cli.schematic.ClipboardWorld;
import com.sk89q.worldedit.event.platform.CommandEvent;
import com.sk89q.worldedit.event.platform.PlatformReadyEvent;
import com.sk89q.worldedit.extension.input.InputParseException;
import com.sk89q.worldedit.extension.input.ParserContext;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.registry.state.Property;
import com.sk89q.worldedit.util.FileDialogUtil;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.BlockCategory;
import com.sk89q.worldedit.world.block.BlockState;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.FuzzyBlockState;
import com.sk89q.worldedit.world.entity.EntityType;
import com.sk89q.worldedit.world.item.ItemCategory;
import com.sk89q.worldedit.world.item.ItemType;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Scanner;
import java.util.jar.Manifest;

/**
 * The CLI implementation of WorldEdit.
 */
public class CLIWorldEdit {

    private static final Logger LOGGER = LoggerFactory.getLogger(CLIWorldEdit.class);

    public static CLIWorldEdit inst;

    private CLIPlatform platform;
    private CLIConfiguration config;
    private Path workingDir;
    private String version;

    private Actor commandSender;

    private FileRegistries fileRegistries;

    public CLIWorldEdit() {
        inst = this;
    }

    private void setupPlatform() {
        this.fileRegistries = new FileRegistries(this);
        this.fileRegistries.loadDataFiles();

        WorldEdit.getInstance().getPlatformManager().register(platform);
    }

    public void setupRegistries() {
        // Blocks
        for (Map.Entry<String, FileRegistries.BlockManifest> manifestEntry : fileRegistries.getDataFile().blocks.entrySet()) {
            if (BlockType.REGISTRY.get(manifestEntry.getKey()) == null) {
                BlockType.REGISTRY.register(manifestEntry.getKey(), new BlockType(manifestEntry.getKey(), input -> {
                            ParserContext context = new ParserContext();
                            context.setPreferringWildcard(true);
                            context.setTryLegacy(false);
                            context.setRestricted(false);
                            try {
                                FuzzyBlockState state = (FuzzyBlockState) WorldEdit.getInstance().getBlockFactory().parseFromInput(
                                        manifestEntry.getValue().defaultstate,
                                        context
                                ).toImmutableState();
                                BlockState defaultState = input.getBlockType().getAllStates().get(0);
                                for (Map.Entry<Property<?>, Object> propertyObjectEntry : state.getStates().entrySet()) {
                                    @SuppressWarnings("unchecked")
                                    Property<Object> prop = (Property<Object>) propertyObjectEntry.getKey();
                                    defaultState = defaultState.with(prop, propertyObjectEntry.getValue());
                                }
                                return defaultState;
                            } catch (InputParseException e) {
                                LOGGER.warn("Error loading block state for " + manifestEntry.getKey(), e);
                                return input;
                            }
                        }));
            }
        }
        // Items
        for (String name : fileRegistries.getDataFile().items) {
            if (ItemType.REGISTRY.get(name) == null) {
                ItemType.REGISTRY.register(name, new ItemType(name));
            }
        }
        // Entities
        for (String name : fileRegistries.getDataFile().entities) {
            if (EntityType.REGISTRY.get(name) == null) {
                EntityType.REGISTRY.register(name, new EntityType(name));
            }
        }
        // Biomes
        for (String name : fileRegistries.getDataFile().biomes) {
            if (BiomeType.REGISTRY.get(name) == null) {
                BiomeType.REGISTRY.register(name, new BiomeType(name));
            }
        }
        // Tags
        for (String name : fileRegistries.getDataFile().blocktags.keySet()) {
            if (BlockCategory.REGISTRY.get(name) == null) {
                BlockCategory.REGISTRY.register(name, new BlockCategory(name));
            }
        }
        for (String name : fileRegistries.getDataFile().itemtags.keySet()) {
            if (ItemCategory.REGISTRY.get(name) == null) {
                ItemCategory.REGISTRY.register(name, new ItemCategory(name));
            }
        }
    }

    public void onInitialized() {
        // Setup working directory
        workingDir = new File("worldedit").toPath();
        if (!Files.exists(workingDir)) {
            try {
                Files.createDirectory(workingDir);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        this.platform = new CLIPlatform(this);
        LOGGER.info("WorldEdit CLI (version " + getInternalVersion() + ") is loaded");
    }

    public void onStarted() {
        setupPlatform();

        setupRegistries();
        WorldEdit.getInstance().loadMappings();

        config = new CLIConfiguration(this);
        config.load();
        commandSender = new CLICommandSender(this, LOGGER);

        WorldEdit.getInstance().getEventBus().post(new PlatformReadyEvent());
    }

    public void onStopped() {
        WorldEdit worldEdit = WorldEdit.getInstance();
        worldEdit.getSessionManager().unload();
        worldEdit.getPlatformManager().unregister(platform);
    }

    public FileRegistries getFileRegistries() {
        return this.fileRegistries;
    }

    /**
     * Get the configuration.
     *
     * @return the CLI configuration
     */
    CLIConfiguration getConfig() {
        return this.config;
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
     * Get the version of the WorldEdit-CLI implementation.
     *
     * @return a version string
     */
    String getInternalVersion() {
        if (version == null) {
            version = getClass().getPackage().getImplementationVersion();
        }
        return version;
    }

    public void run(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream);
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.equalsIgnoreCase("stop")) {
                commandSender.print("Stopping!");
                break;
            }
            if (line.startsWith("save")) {
                String[] bits = line.split(" ");
                if (bits.length == 0) {
                    commandSender.print("Usage: save <filename>");
                    continue;
                }
                World world = platform.getWorlds().get(0);
                if (world instanceof ClipboardWorld) {
                    File file;
                    if (bits.length >= 2) {
                        file = new File(bits[1]);
                    } else {
                        file = FileDialogUtil.showSaveDialog(new String[]{"schem"});
                    }
                    if (file == null) {
                        commandSender.printError("Please choose a file.");
                        continue;
                    }
                    try(ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(file))) {
                        writer.write((Clipboard) world);
                        commandSender.print("Saved to file");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                continue;
            }
            WorldEdit.getInstance().getEventBus().post(new CommandEvent(
                    commandSender,
                    line
            ));
        }
        scanner.close();
    }

    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("f", "file", true, "The file to load in. Either a schematic, or a level.dat in a world folder.");
        options.addOption("s", "script", true, "A file containing a list of commands to run. Newline separated.");
        int exitCode = 0;

        CLIWorldEdit app = new CLIWorldEdit();
        app.onInitialized();

        InputStream inputStream = System.in;

        try {
            CommandLine cmd = new DefaultParser().parse(options, args);

            String fileArg = cmd.getOptionValue('f');
            File file;
            if (fileArg == null) {
                file = FileDialogUtil.showOpenDialog(new String[]{"schem", "dat"});
            } else {
                file = new File(fileArg);
            }
            if (file == null) {
                throw new IllegalArgumentException("A file must be provided!");
            }
            if (file.getName().endsWith("level.dat")) {
                throw new IllegalArgumentException("level.dat file support is unfinished.");
            } else if (file.getName().endsWith(".schem")) {
                ClipboardFormat format = BuiltInClipboardFormat.SPONGE_SCHEMATIC;
                ClipboardReader dataVersionReader = format
                        .getReader(Files.newInputStream(file.toPath(), StandardOpenOption.READ));
                int dataVersion = dataVersionReader.getDataVersion()
                        .orElseThrow(() -> new IllegalArgumentException("Failed to obtain data version from schematic."));
                dataVersionReader.close();
                app.platform.setDataVersion(dataVersion);
                app.onStarted();
                try (ClipboardReader clipboardReader = format.getReader(Files.newInputStream(file.toPath(), StandardOpenOption.READ))) {
                    ClipboardWorld world = new ClipboardWorld(
                            clipboardReader.read(),
                            file.getName()
                    );
                    app.platform.addWorld(world);
                    WorldEdit.getInstance().getSessionManager().get(app.commandSender).setWorldOverride(world);
                }
            } else {
                throw new IllegalArgumentException("Unknown file provided!");
            }

            String scriptFile = cmd.getOptionValue('s');
            if (scriptFile != null) {
                File scriptFileHandle = new File(scriptFile);
                if (!scriptFileHandle.exists()) {
                    throw new IllegalArgumentException("Could not find given script file.");
                }
                inputStream = new SequenceInputStream(Files.newInputStream(scriptFileHandle.toPath(), StandardOpenOption.READ), inputStream);
            }

            app.run(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            exitCode = 1;
        } finally {
            app.onStopped();
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.exit(exitCode);
    }
}
