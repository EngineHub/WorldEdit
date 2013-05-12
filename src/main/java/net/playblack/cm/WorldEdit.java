package net.playblack.cm;

import net.canarymod.Canary;
import net.canarymod.logger.Logman;
import net.canarymod.plugin.Plugin;
import net.canarymod.tasks.TaskOwner;

public class WorldEdit extends Plugin implements TaskOwner {
    public static Logman logger;
    private static com.sk89q.worldedit.WorldEdit controller = null;
    private static WorldEdit instance = null;

    public static com.sk89q.worldedit.WorldEdit getController() {
        if(controller == null) {
            if(instance == null) {
                throw new IllegalStateException("Plugin is not loaded yet, cannot instantiante a controller!");
            }
            CanaryConfiguration cfg = new CanaryConfiguration();
            cfg.load();
            controller = new com.sk89q.worldedit.WorldEdit(new CanaryServer(instance), cfg);
        }
        return controller;
    }
    public WorldEdit() {
        instance = this;
        logger = getLogman();
        if(controller == null) {
            if(instance == null) {
                throw new IllegalStateException("Plugin is not loaded yet, cannot instantiante a controller!");
            }
            CanaryConfiguration cfg = new CanaryConfiguration();
            cfg.load();
            controller = new com.sk89q.worldedit.WorldEdit(new CanaryServer(instance), cfg);
        }
    }
    @Override
    public boolean enable() {
        Canary.hooks().registerListener(new WorldEditListener(this), this);
        return true;
    }

    @Override
    public void disable() {
        // TODO Auto-generated method stub
    }
}
