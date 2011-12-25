package com.sk89q.bukkit.util;

import org.bukkit.command.CommandMap;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;

/**
* @author zml2008
*/
public class FallbackRegistrationListener extends PlayerListener {

    private final CommandMap commandRegistration;

    public FallbackRegistrationListener(CommandMap commandRegistration) {
        this.commandRegistration = commandRegistration;
    }

    @Override
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (commandRegistration.dispatch(event.getPlayer(), event.getMessage())) {
            event.setCancelled(true);
        }
    }
}
