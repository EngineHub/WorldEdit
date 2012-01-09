package com.sk89q.worldedit.spout;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.PlayerNeededException;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import org.spout.api.ChatColor;
import org.spout.api.command.CommandSource;
import org.spout.api.player.Player;

public class SpoutCommandSender extends LocalPlayer {
    private CommandSource sender;
    @SuppressWarnings("unused")
    private WorldEditPlugin plugin;

    public SpoutCommandSender(WorldEditPlugin plugin, ServerInterface server, CommandSource sender) {
        super(server);
        this.plugin = plugin;
        this.sender = sender;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public void printRaw(String msg) {
        sender.sendRawMessage(msg);
    }

    @Override
    public void printDebug(String msg) {
        sender.sendMessage(ChatColor.GRAY + msg);

    }

    @Override
    public void print(String msg) {
        sender.sendMessage(ChatColor.PURPLE + msg);
    }

    @Override
    public void printError(String msg) {
        sender.sendMessage(ChatColor.RED + msg);
    }

    @Override
    public String[] getGroups() {
        return sender.getGroups();
    }

    @Override
    public boolean hasPermission(String perm) {
        return sender.hasPermission(perm);
    }

    @Override
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    @Override
    public int getItemInHand() {
        throw new PlayerNeededException();
    }

    @Override
    public WorldVector getPosition() {
        throw new PlayerNeededException();
    }

    @Override
    public LocalWorld getWorld() {
        throw new PlayerNeededException();
    }

    @Override
    public double getPitch() {
        throw new PlayerNeededException();
    }

    @Override
    public double getYaw() {
        throw new PlayerNeededException();
    }

    @Override
    public void giveItem(int type, int amt) {
        throw new PlayerNeededException();
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        throw new PlayerNeededException();
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        throw new PlayerNeededException();
    }
}
