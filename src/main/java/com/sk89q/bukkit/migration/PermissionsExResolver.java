package com.sk89q.bukkit.migration;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import ru.tehkode.permissions.PermissionManager;
import ru.tehkode.permissions.PermissionUser;

import com.sk89q.bukkit.migration.PermissionsResolverManager.MissingPluginException;

public class PermissionsExResolver implements PermissionsResolver {
    private final PermissionManager manager;
    private final Server server;
    
    public PermissionsExResolver(Server server) throws MissingPluginException {
        this.server = server;
        manager = server.getServicesManager().load(PermissionManager.class);
        if (manager == null)
            throw new MissingPluginException();
    }
    
    public void load() {
        
    }
    
    public boolean hasPermission(String name, String permission) {
        Player player = server.getPlayer(name);
        return manager.has(name, permission, player == null ? null : player.getWorld().getName());
    }
    
    public boolean hasPermission(String worldName, String name, String permission) {
        return manager.has(name, permission, worldName);
    }
    
    public boolean inGroup(String player, String group) {
        PermissionUser user = manager.getUser(player);
        if (user == null) {
            return false;
        }
        return user.inGroup(group);
    }
    
    public String[] getGroups(String player) {
        PermissionUser user = manager.getUser(player);
        if (user == null) {
            return new String[0];
        }
        return user.getGroupsNames();
    }

}
