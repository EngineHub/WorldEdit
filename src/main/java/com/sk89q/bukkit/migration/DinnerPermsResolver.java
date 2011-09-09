package com.sk89q.bukkit.migration;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.ArrayList;
import java.util.List;

public class DinnerPermsResolver implements PermissionsResolver {

    private static final String GROUP_PREFIX = "group.";
    private final Server server;

    public DinnerPermsResolver(Server server) {
        this.server = server;
    }

    public void load() {
        // Permissions are already loaded
    }

    public boolean hasPermission(String name, String permission) {
        Player player = server.getPlayer(name);
        if (player == null)
            return false; // Permissions are only registered for online players
        if ( player.hasPermission("*")  || player.hasPermission(permission))
            return true;
        int dotPos = permission.lastIndexOf(".");
        while (dotPos > -1) {
            if (player.hasPermission(permission.substring(0, dotPos + 1) + "*"))
                return true;
            dotPos = permission.lastIndexOf(".", dotPos - 1);
        }
        return false;
    }

    public boolean hasPermission(String worldName, String name, String permission) {
        return hasPermission(name, permission); // no per-world ability to check permissions in dinnerperms
    }

    public boolean inGroup(String name, String group) {
        Player player = server.getPlayer(name);
        if (player == null)
            return false;
        return player.hasPermission(GROUP_PREFIX + group);
    }

    public String[] getGroups(String name) {
        Player player = server.getPlayer(name);
        if (player == null)
            return new String[0];
        List<String> groupNames = new ArrayList<String>();
        for (PermissionAttachmentInfo permAttach : player.getEffectivePermissions()) {
            String perm = permAttach.getPermission();
            if (!(perm.startsWith(GROUP_PREFIX) && permAttach.getValue()))
                continue;
            groupNames.add(perm.substring(GROUP_PREFIX.length(), perm.length()));
        }
        return groupNames.toArray(new String[0]);
    }
}
