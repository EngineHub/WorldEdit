package com.sk89q.worldedit.spout;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.PlayerNeededException;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import org.getspout.api.ChatColor;
import org.getspout.api.Spout;
import org.getspout.api.command.CommandSource;
import org.getspout.api.player.Player;

import java.util.logging.Level;

public class SpoutCommandSender extends LocalPlayer {
    private CommandSource sender;
    private WorldEditPlugin plugin;

    public SpoutCommandSender(WorldEditPlugin plugin, ServerInterface server, CommandSource sender) {
        super(server);
        this.plugin = plugin;
        this.sender = sender;
    }

    @Override
    public String getName() {
        //return sender.getName();
        return "Console";
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
        return new String[0];
    }

    @Override
    public boolean hasPermission(String perm) {
        // TODO: Implement permissions
        // return sender.isOp():
        return true;
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
