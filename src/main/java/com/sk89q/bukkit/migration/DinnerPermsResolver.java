package com.sk89q.bukkit.migration;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public class DinnerPermsResolver implements PermissionsResolver {
    private final Server server;

    public DinnerPermsResolver(Server server) {
        this.server = server;
    }
    @Override
    public void load() {
        // Permissions are already loaded
    }

    @Override
    public boolean hasPermission(String name, String permission) {
        Player player = server.getPlayer(name);
        if (player == null)
            return false; // Permissions are only registered for online players
        if ( player.hasPermission("*")  || player.hasPermission(permission))
            return true;

        for (int i = 0; i > -1; i++) {
            int dotPos = permission.indexOf(".", i);
            if (dotPos > -1) {
                if (player.hasPermission(permission.substring(0, dotPos) + ".*"))
                    return true;
            } else {
                break;
            }
        }
        return false;
    }

    @Override
    public boolean hasPermission(String worldName, String name, String permission) {
        return hasPermission(name, permission); // no per-world ability to check permissions in dinnerperms
    }

    @Override
    public boolean inGroup(String player, String group) {
        return false; // No group support
    }

    @Override
    public String[] getGroups(String player) {
        return new String[0]; // There are no groups in dinnerperms
    }
}
