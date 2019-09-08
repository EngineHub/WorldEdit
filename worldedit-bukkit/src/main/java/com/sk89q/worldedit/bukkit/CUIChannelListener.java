/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.bukkit;

import com.sk89q.worldedit.LocalSession;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.nio.charset.Charset;

/**
 * Handles incoming WorldEditCui init message.
 */
public class CUIChannelListener implements PluginMessageListener {

    public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");
    private final WorldEditPlugin plugin;

    public CUIChannelListener(WorldEditPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        LocalSession session = plugin.getSession(player);
        String text = new String(message, UTF_8_CHARSET);
        final BukkitPlayer actor = plugin.wrapPlayer(player);
        session.handleCUIInitializationMessage(text, actor);
        session.describeCUI(actor);
    }

}
