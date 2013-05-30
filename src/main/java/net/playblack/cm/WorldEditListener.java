package net.playblack.cm;

import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.command.PlayerCommandHook;
import net.canarymod.hook.player.BlockDestroyHook;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.hook.player.DisconnectionHook;
import net.canarymod.hook.player.ItemUseHook;
import net.canarymod.hook.player.PlayerLeftClickHook;
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
    public void onLeftClick(PlayerLeftClickHook hook) {
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
        //This is dirty shit right now, there's probably a nicer method
        if(WorldEdit.getController().handleCommand(wrapPlayer(hook.getPlayer()), hook.getCommand())) {
            hook.setCanceled();
        }
    }
    private LocalPlayer wrapPlayer(Player player) {
        return new CanaryPlayer(server, player);
    }
}
