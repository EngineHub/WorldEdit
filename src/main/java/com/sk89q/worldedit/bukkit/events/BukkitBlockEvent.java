package com.sk89q.worldedit.bukkit.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import com.sk89q.worldedit.events.WorldEditBlockEvent;

public interface BukkitBlockEvent {

    public WorldEditBlockEvent getWorldEditEvent();

    public Block getBlock();

    public Player getPlayer();
}
