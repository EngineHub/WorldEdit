package com.sk89q.worldedit.bukkit.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import com.sk89q.worldedit.events.WorldEditBlockEvent;

public class WEBlockBreakEvent extends BlockBreakEvent implements BukkitBlockEvent {

    private WorldEditBlockEvent weEvent;

    public WEBlockBreakEvent(Block theBlock, Player player, WorldEditBlockEvent orig) {
        super(theBlock, player);
        this.weEvent = orig;
    }

    public WorldEditBlockEvent getWorldEditEvent() {
        return weEvent;
    }

}
