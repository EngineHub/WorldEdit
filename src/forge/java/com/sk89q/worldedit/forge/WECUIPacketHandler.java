package com.sk89q.worldedit.forge;

import java.nio.charset.Charset;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;

import com.sk89q.worldedit.LocalSession;

import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;

public class WECUIPacketHandler implements IPacketHandler {
    public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
        LocalSession session = WorldEditMod.inst.getSession((EntityPlayerMP) player);

        if (session.hasCUISupport()) {
            return;
        }

        String text = new String(packet.data, UTF_8_CHARSET);
        session.handleCUIInitializationMessage(text);
    }
}