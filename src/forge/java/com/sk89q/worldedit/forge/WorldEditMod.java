package com.sk89q.worldedit.forge;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.wepif.PermissionsResolverManager;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.WorldEdit;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.EventBus;

@Mod(modid = "WorldEdit", name = "WorldEdit", version = "5.5.2-forge-alpha1")
public class WorldEditMod {

    @Mod.Instance("WorldEdit")
    public static WorldEditMod inst;

    protected static Logger logger;
    public static final String CUI_PLUGIN_CHANNEL = "WECUI";
    private ForgeServerInterface server;
    private WorldEdit controller;
    private ForgeConfiguration config;
    private File workingDir;

    @SideOnly(Side.SERVER)
    @Mod.PreInit
    public void preInit(FMLPreInitializationEvent event) {
        logger = Logger.getLogger(((Mod) getClass().getAnnotation(Mod.class)).modid());
        logger.setParent(FMLLog.getLogger());
        String modVersion = ((Mod) WorldEditMod.class.getAnnotation(Mod.class)).version();
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

    @SideOnly(Side.SERVER)
    @Mod.Init
    public void init(FMLInitializationEvent event) {
        this.server = new ForgeServerInterface();
        this.controller = new WorldEdit(this.server, this.config);

        NetworkRegistry.instance().registerChannel(new WECUIPacketHandler(), "WECUI");

        MinecraftForge.EVENT_BUS.register(new WorldEditForgeListener());
    }

    @SideOnly(Side.SERVER)
    @Mod.PostInit
    public void postInit(FMLPostInitializationEvent event) {
        logger.info("WorldEdit " + WorldEdit.getVersion() + " Loaded");
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
            try {
                JarFile file = new JarFile(jar);
                ZipEntry copy = file.getEntry("defaults/" + name);
                if (copy == null)
                    throw new FileNotFoundException();
                input = file.getInputStream(copy);
            } catch (IOException e) {
                logger.severe("Unable to read default configuration: " + name);
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