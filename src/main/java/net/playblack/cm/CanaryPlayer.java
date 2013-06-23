package net.playblack.cm;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.inventory.Item;
import net.canarymod.api.world.World;
import net.canarymod.chat.Colors;
import net.canarymod.user.Group;

import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;

public class CanaryPlayer extends LocalPlayer {

    Player player;
    World world;

    public CanaryPlayer(ServerInterface server, Player player) {
        super(server);
        this.player = player;
        this.world = player.getWorld();
    }

    @Override
    public int getItemInHand() {
        Item i = player.getItemHeld();
        if(i != null) {
            return i.getId();
        }
        return 0;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public WorldVector getPosition() {
        return new WorldVector(getWorld(), player.getX(), player.getY(), player.getZ());
    }

    @Override
    public LocalWorld getWorld() {
        return new CanaryWorld(world);
    }

    @Override
    public double getPitch() {
        return player.getPitch();
    }

    @Override
    public double getYaw() {
        return player.getRotation();
    }

    @Override
    public void giveItem(int type, int amt) {
        player.giveItem(Canary.factory().getItemFactory().newItem(type, amt));
    }

    @Override
    public void printRaw(String msg) {
        for(String str : msg.split("\n")) {
            player.message(str);
        }
    }

    @Override
    public void printDebug(String msg) {
        for(String str : msg.split("\n")) {
            player.message(Colors.LIGHT_GRAY + str);
        }
    }

    @Override
    public void print(String msg) {
        for(String str : msg.split("\n")) {
            player.message(str);
        }
    }

    @Override
    public void printError(String msg) {
        for(String str : msg.split("\n")) {
            player.message(Colors.LIGHT_RED + str);
        }
    }

    @Override
    public void setPosition(Vector pos, float pitch, float yaw) {
        player.teleportTo(pos.getX(), pos.getY(), pos.getZ(), pitch, yaw);
    }

    @Override
    public String[] getGroups() {
        String[] groups = new String[player.getPlayerGroups().length];
        Group[] playergroups = player.getPlayerGroups();
        for(int i = 0; i < groups.length; ++i) {
            groups[i] = playergroups[i].getName();
        }
        return groups;
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return new CanaryBlockBag(player);
    }

    @Override
    public boolean hasPermission(String perm) {
        return player.hasPermission(perm);
    }

}
