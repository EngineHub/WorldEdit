package com.sk89q.worldedit.canarymod;

import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.command.PlayerCommandHook;
import net.canarymod.hook.player.BlockDestroyHook;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.hook.player.DisconnectionHook;
import net.canarymod.hook.player.ItemUseHook;
import net.canarymod.hook.player.PlayerArmSwingHook;
import net.canarymod.plugin.PluginListener;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.WorldVector;

public class WorldEditListener implements PluginListener {
    private CanaryServer server;

    public WorldEditListener(WorldEdit plugin) {
        server = new CanaryServer(plugin);
    }

    @HookHandler
    public void onDisconnect(DisconnectionHook hook) {
        WorldEdit.getController().markExpire(wrapPlayer(hook.getPlayer()));
    }

    @HookHandler
    public void onLeftClick(PlayerArmSwingHook hook) {
        WorldEdit.getController().handleArmSwing(wrapPlayer(hook.getPlayer()));
    }

    @HookHandler
    public void onBlockRightClick(BlockRightClickHook hook) {
        Block blockClicked = hook.getBlockClicked();
        WorldVector pos = new WorldVector(new CanaryWorld(hook.getPlayer().getWorld()), blockClicked.getX(),
                blockClicked.getY(), blockClicked.getZ());
        if(WorldEdit.getController().handleBlockRightClick(wrapPlayer(hook.getPlayer()), pos)) {
            hook.setCanceled();
        }
    }

    @HookHandler
    public void ItemUse(ItemUseHook hook) {
        WorldEdit.getController().handleRightClick(wrapPlayer(hook.getPlayer()));
    }

    @HookHandler
    public void onBlockDestroy(BlockDestroyHook hook) {
        Block blockClicked = hook.getBlock();
        WorldVector pos = new WorldVector(new CanaryWorld(hook.getPlayer().getWorld()), blockClicked.getX(),
                blockClicked.getY(), blockClicked.getZ());
        if(WorldEdit.getController().handleBlockLeftClick(wrapPlayer(hook.getPlayer()), pos)) {
            hook.setCanceled();
        }
    }

    @HookHandler
    public void onPlayerCommand(PlayerCommandHook hook) {
        //This is a little dirty right now, as it is circumventing
        //the command system. However, the help is registered anyway so it's not too bad.
        if(WorldEdit.getController().handleCommand(wrapPlayer(hook.getPlayer()), hook.getCommand())) {
            hook.setCanceled();
        }
    }
    private LocalPlayer wrapPlayer(Player player) {
        return new CanaryPlayer(server, player);
    }
}
