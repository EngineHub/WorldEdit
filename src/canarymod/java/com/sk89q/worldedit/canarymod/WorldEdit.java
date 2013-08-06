package com.sk89q.worldedit.canarymod;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.World;
import net.canarymod.logger.Logman;
import net.canarymod.plugin.Plugin;
import net.canarymod.tasks.TaskOwner;

import com.sk89q.worldedit.canarymod.selections.CuboidSelection;
import com.sk89q.worldedit.canarymod.selections.Polygonal2DSelection;
import com.sk89q.worldedit.canarymod.selections.Selection;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;

public class WorldEdit extends Plugin implements TaskOwner {
    public static Logman logger;
    private static com.sk89q.worldedit.WorldEdit controller = null;
    private static WorldEdit instance = null;

    /**
     * Return the WorldEdit controller
     * @return
     */
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

    /**
     * Get the WorldEdit Plugin instance
     * @return
     */
    public static WorldEdit getInstance() {
        return instance;
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

    /**
     * Gets the region selection for the player.
     *
     * @param player
     * @return the selection or null if there was none
     */
    public Selection getSelection(Player player) {
        if (player == null) {
            throw new IllegalArgumentException("Null player not allowed");
        }

        LocalSession session = controller.getSession(wrapPlayer(player));
        RegionSelector selector = session.getRegionSelector(CanaryUtil.getLocalWorld(player.getWorld()));

        try {
            Region region = selector.getRegion();
            World world = ((CanaryWorld) session.getSelectionWorld()).getHandle();

            if (region instanceof CuboidRegion) {
                return new CuboidSelection(world, selector, (CuboidRegion) region);
            } else if (region instanceof Polygonal2DRegion) {
                return new Polygonal2DSelection(world, selector, (Polygonal2DRegion) region);
            } else {
                return null;
            }
        } catch (IncompleteRegionException e) {
            return null;
        }
    }

    public void setSelection(Player player, Selection selection) {
        if (player == null) {
            throw new IllegalArgumentException("Null player not allowed");
        }

        LocalSession session = controller.getSession(wrapPlayer(player));
        RegionSelector sel = selection.getRegionSelector();
        session.setRegionSelector(CanaryUtil.getLocalWorld(player.getWorld()), sel);
        session.dispatchCUISelection(wrapPlayer(player));
    }
    public LocalPlayer wrapPlayer(Player player) {
        return new CanaryPlayer(getController().getServer(), player);
    }
}
