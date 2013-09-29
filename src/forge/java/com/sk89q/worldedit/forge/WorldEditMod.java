package com.sk89q.worldedit.forge;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.WorldEdit;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(modid = "WorldEdit", name = "WorldEdit", version = "%VERSION%")
@NetworkMod(channels="WECUI", packetHandler=WECUIPacketHandler.class)
public class WorldEditMod {

    @Instance("WorldEdit")
    public static WorldEditMod inst;

    protected static Logger logger;
    public static final String CUI_PLUGIN_CHANNEL = "WECUI";
    private ForgeServerInterface server;
    private WorldEdit controller;
    private ForgeConfiguration config;
    private File workingDir;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = Logger.getLogger(getClass().getAnnotation(Mod.class).modid());
        logger.setParent(FMLLog.getLogger());
        String modVersion = WorldEditMod.class.getAnnotation(Mod.class).version();
        String manifestVersion = WorldEdit.getVersion();
        if (!manifestVersion.equalsIgnoreCase(modVersion)) {
            WorldEdit.setVersion(manifestVersion + " (" + modVersion + ")");
        }

        this.workingDir = new File(event.getModConfigurationDirectory() + File.separator + "WorldEdit");
        this.workingDir.mkdir();

        createDefaultConfiguration(event.getSourceFile(), "worldedit.properties");
        config = new ForgeConfiguration(this);
        config.load();
        // PermissionsResolverManager.initialize(this, this.workingDir);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new WorldEditForgeListener());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        logger.info("WorldEdit " + WorldEdit.getVersion() + " Loaded");
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        this.server = new ForgeServerInterface();
        this.controller = new WorldEdit(this.server, this.config);
    }

    public ForgeConfiguration getConfig() {
        return this.config;
    }

    /*public PermissionsResolverManager getPermissionsResolver() {
        return PermissionsResolverManager.getInstance();
    }*/

    public LocalSession getSession(EntityPlayerMP player) {
        return this.controller.getSession(wrapPlayer(player));
    }

    public LocalWorld getWorld(World world) {
        return new ForgeWorld(world);
    }

    public ForgePlayer wrapPlayer(EntityPlayerMP player) {
        return new ForgePlayer(player);
    }

    public WorldEdit getWorldEdit() {
        return this.controller;
    }

    public ServerInterface getServerInterface() {
        return this.server;
    }

    public File getWorkingDir() {
        return this.workingDir;
    }

    private void createDefaultConfiguration(File jar, String name) {
        File actual = new File(getWorkingDir(), name);
        if (!actual.exists()) {
            InputStream input = null;
            JarFile file = null;
            try {
                file = new JarFile(jar);
                ZipEntry copy = file.getEntry("defaults/" + name);
                if (copy == null)
                    throw new FileNotFoundException();
                input = file.getInputStream(copy);
            } catch (IOException e) {
                logger.severe("Unable to read default configuration: " + name);
            } finally {
                try {
                    file.close();
                } catch (Exception e) {}
            }
            if (input != null) {
                FileOutputStream output = null;
                try {
                    output = new FileOutputStream(actual);
                    byte[] buf = new byte[8192];
                    int length = 0;
                    while ((length = input.read(buf)) > 0) {
                        output.write(buf, 0, length);
                    }

                    logger.info("Default configuration file written: " + name);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (input != null)
                            input.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (output != null)
                            output.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }
}
