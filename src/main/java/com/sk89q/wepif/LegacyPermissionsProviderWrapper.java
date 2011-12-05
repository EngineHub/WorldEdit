package com.sk89q.wepif;

import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("deprecation")
public class LegacyPermissionsProviderWrapper implements PermissionsProvider {
    private final com.sk89q.bukkit.migration.PermissionsProvider legacyPermissionsProvider;

    static PermissionsProvider wrap(Plugin plugin) {
        if (!(plugin instanceof com.sk89q.bukkit.migration.PermissionsProvider)) {
            return null;
        }

        final com.sk89q.bukkit.migration.PermissionsProvider legacyPermissionsProvider = (com.sk89q.bukkit.migration.PermissionsProvider) plugin;
        return new LegacyPermissionsProviderWrapper(legacyPermissionsProvider);
    }

    private LegacyPermissionsProviderWrapper(com.sk89q.bukkit.migration.PermissionsProvider legacyPermissionsProvider) {
        this.legacyPermissionsProvider = legacyPermissionsProvider;
    }

    @Override
    public boolean hasPermission(String name, String permission) {
        return legacyPermissionsProvider.hasPermission(name, permission);
    }

    @Override
    public boolean hasPermission(String worldName, String name, String permission) {
        return legacyPermissionsProvider.hasPermission(worldName, name, permission);
    }

    @Override
    public boolean inGroup(String player, String group) {
        return legacyPermissionsProvider.inGroup(player, group);
    }

    @Override
    public String[] getGroups(String player) {
        return legacyPermissionsProvider.getGroups(player);
    }

    @Override
    public boolean hasPermission(OfflinePlayer player, String permission) {
        return legacyPermissionsProvider.hasPermission(player.getName(), permission);
    }

    @Override
    public boolean hasPermission(String worldName, OfflinePlayer player, String permission) {
        return legacyPermissionsProvider.hasPermission(worldName, player.getName(), permission);
    }

    @Override
    public boolean inGroup(OfflinePlayer player, String group) {
        return legacyPermissionsProvider.inGroup(player.getName(), group);
    }

    @Override
    public String[] getGroups(OfflinePlayer player) {
        return legacyPermissionsProvider.getGroups(player.getName());
    }
}
