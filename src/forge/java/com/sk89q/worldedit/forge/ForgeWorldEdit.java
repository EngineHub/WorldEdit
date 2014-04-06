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

package com.sk89q.worldedit.forge;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.extension.platform.Platform;
import com.sk89q.worldedit.extension.platform.PlatformRejectionException;
import com.sk89q.worldedit.internal.LocalWorldAdapter;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.*;
import cpw.mods.fml.common.network.NetworkMod;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

/**
 * The Forge implementation of WorldEdit.
 */
@Mod(modid = "WorldEdit", name = "WorldEdit", version = "%VERSION%")
@NetworkMod(channels="WECUI", packetHandler=WECUIPacketHandler.class)
public class ForgeWorldEdit {

    private static final Logger logger = Logger.getLogger(ForgeWorldEdit.class.getCanonicalName());
    public static final String CUI_PLUGIN_CHANNEL = "WECUI";

    @Instance("WorldEdit")
    public static ForgeWorldEdit inst;

    private ForgePlatform platform;
    private ForgeConfiguration config;
    private File workingDir;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Redirect all loggers under com.sk89q to FML's logger
        Logger.getLogger("com.sk89q").setParent(FMLLog.getLogger());

        // Setup working directory
        workingDir = new File(event.getModConfigurationDirectory() + File.separator + "worldedit");
        workingDir.mkdir();

        // Create default configuration
        createDefaultConfiguration(event.getSourceFile(), "worldedit.properties");

        config = new ForgeConfiguration(this);
        config.load();
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        logger.info("WorldEdit for Forge (version " + getInternalVersion() + ") is loaded");
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        if (this.platform != null) {
            logger.warning("FMLServerStartingEvent occurred when FMLServerStoppingEvent hasn't");
            WorldEdit.getInstance().getPlatformManager().unregister(platform);
        }

        this.platform = new ForgePlatform(this);
        try {
            WorldEdit.getInstance().getPlatformManager().register(platform);
        } catch (PlatformRejectionException e) {
            throw new RuntimeException("Failed to register with WorldEdit", e);
        }
    }

    @EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
        WorldEdit.getInstance().getPlatformManager().unregister(platform);
    }

    @ForgeSubscribe
    public void onCommandEvent(CommandEvent event) {
        if ((event.sender instanceof EntityPlayerMP)) {
            if (((EntityPlayerMP) event.sender).worldObj.isRemote) return;
            String[] split = new String[event.parameters.length + 1];
            System.arraycopy(event.parameters, 0, split, 1, event.parameters.length);
            split[0] = ("/" + event.command.getCommandName());
            WorldEdit.getInstance().handleCommand(wrap((EntityPlayerMP) event.sender), split);
        }
    }

    @ForgeSubscribe
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.useItem == Result.DENY || event.entity.worldObj.isRemote) return;

        WorldEdit we = WorldEdit.getInstance();
        ForgePlayer player = wrap((EntityPlayerMP) event.entityPlayer);
        ForgeWorld world = getWorld(event.entityPlayer.worldObj);

        Action action = event.action;
        switch (action) {
            case LEFT_CLICK_BLOCK: {
                WorldVector pos = new WorldVector(LocalWorldAdapter.wrap(world), event.x, event.y, event.z);

                if (we.handleBlockLeftClick(player, pos)) {
                    event.setCanceled(true);
                }

                if (we.handleArmSwing(player)) {
                    event.setCanceled(true);
                }
            }
            case RIGHT_CLICK_BLOCK: {
                WorldVector pos = new WorldVector(LocalWorldAdapter.wrap(world), event.x, event.y, event.z);

                if (we.handleBlockRightClick(player, pos)) {
                    event.setCanceled(true);
                }

                if (we.handleRightClick(player)) {
                    event.setCanceled(true);
                }
            }
            case RIGHT_CLICK_AIR: {
                if (we.handleRightClick(player)) {
                    event.setCanceled(true);
                }
            }
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
     * Get the WorldEdit proxy for the given player.
     *
     * @param player the player
     * @return the WorldEdit player
     */
    public ForgePlayer wrap(EntityPlayerMP player) {
        checkNotNull(player);
        return new ForgePlayer(player);
    }

    /**
     * Get the session for a player.
     *
     * @param player the player
     * @return the session
     */
    public LocalSession getSession(EntityPlayerMP player) {
        checkNotNull(player);
        return WorldEdit.getInstance().getSessionManager().get(wrap(player));
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
    public File getWorkingDir() {
        return this.workingDir;
    }

    /**
     * Create the default configuration.
     *
     * @param jar the jar
     * @param name the name
     */
    private void createDefaultConfiguration(File jar, String name) {
        checkNotNull(jar);
        checkNotNull(name);

        String path = "defaults/" + name;
        File targetFile = new File(getWorkingDir(), name);
        Closer closer = Closer.create();

        try {
            @Nullable InputStream inputStream = getClass().getResourceAsStream(path);
            if (inputStream == null) {
                throw new IOException("Failed to get resource '" + path + "' from .class");
            }
            closer.register(inputStream);
            FileOutputStream outputStream = new FileOutputStream(targetFile);
            ByteStreams.copy(inputStream, outputStream);
            logger.info("Default configuration file written: " + name);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to extract defaults", e);
        } finally {
            try {
                closer.close();
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Get the version of the WorldEdit-for-Forge implementation.
     *
     * @return a version string
     */
    String getInternalVersion() {
        return ForgeWorldEdit.class.getAnnotation(Mod.class).version();
    }

}
