package com.sk89q.worldedit.forge;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ChatMessageComponent;

import com.sk89q.util.StringUtil;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.bags.BlockBag;
import com.sk89q.worldedit.cui.CUIEvent;

public class ForgePlayer extends LocalPlayer {
    private EntityPlayerMP player;

    protected ForgePlayer(EntityPlayerMP player) {
        super(WorldEditMod.inst.getServerInterface());
        this.player = player;
    }

    public int getItemInHand() {
        ItemStack is = this.player.getCurrentEquippedItem();
        return is == null ? 0 : is.itemID;
    }

    public String getName() {
        return this.player.username;
    }

    public WorldVector getPosition() {
        return new WorldVector(WorldEditMod.inst.getWorld(this.player.worldObj), this.player.posX, this.player.posY, this.player.posZ);
    }

    public LocalWorld getWorld() {
        return WorldEditMod.inst.getWorld(this.player.worldObj);
    }

    public double getPitch() {
        return this.player.rotationPitch;
    }

    public double getYaw() {
        return this.player.rotationYaw;
    }

    public void giveItem(int type, int amt) {
        this.player.inventory.addItemStackToInventory(new ItemStack(type, amt, 0));
    }

    public void dispatchCUIEvent(CUIEvent event) {
        String[] params = event.getParameters();
        String send = event.getTypeId();
        if (params.length > 0) {
            send = send + "|" + StringUtil.joinString(params, "|");
        }
        Packet250CustomPayload packet = new Packet250CustomPayload(WorldEditMod.CUI_PLUGIN_CHANNEL, send.getBytes(WECUIPacketHandler.UTF_8_CHARSET));
        this.player.playerNetServerHandler.sendPacketToPlayer(packet);
    }

    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendChatToPlayer(ChatMessageComponent.createFromText(part));
        }
    }

    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendChatToPlayer(ChatMessageComponent.createFromText("\u00a77" + part));
        }
    }

    public void print(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendChatToPlayer(ChatMessageComponent.createFromText("\u00a7d" + part));
        }
    }

    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            this.player.sendChatToPlayer(ChatMessageComponent.createFromText("\u00a7c" + part));
        }
    }

    public void setPosition(Vector pos, float pitch, float yaw) {
        this.player.playerNetServerHandler.setPlayerLocation(pos.getX(), pos.getY(), pos.getZ(), pitch, yaw);
    }

    public String[] getGroups() {
        return new String[]{}; // WorldEditMod.inst.getPermissionsResolver().getGroups(this.player.username);
    }

    public BlockBag getInventoryBlockBag() {
        return null;
    }

    public boolean hasPermission(String perm) {
        return ForgeUtil.hasPermission(this.player, perm);
    }
}