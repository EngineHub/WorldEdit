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

package com.sk89q.wepif;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class TestOfflinePermissible implements OfflinePlayer, Permissible {
    private boolean op;
    private UUID randomUuid = UUID.randomUUID();
    
    private final Map<String, Boolean> assignedPermissions = new HashMap<String, Boolean>();

    public boolean isOp() {
        return op;
    }

    public void setOp(boolean b) {
        this.op = b;
    }

    public boolean isPermissionSet(String s) {
        return assignedPermissions.containsKey(s.toLowerCase());
    }

    public boolean isPermissionSet(Permission permission) {
        return isPermissionSet(permission.getName());
    }

    public boolean hasPermission(String s) {
        if (isPermissionSet(s)) {
            return assignedPermissions.get(s.toLowerCase());
        }
        return false;
    }

    public boolean hasPermission(Permission permission) {
        return hasPermission(permission.getName());
    }

    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PermissionAttachment addAttachment(Plugin plugin) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public PermissionAttachment addAttachment(Plugin plugin, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeAttachment(PermissionAttachment permissionAttachment) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void recalculatePermissions() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        Set<PermissionAttachmentInfo> ret = new HashSet<PermissionAttachmentInfo>();
        for (Map.Entry<String, Boolean> entry : assignedPermissions.entrySet()) {
            ret.add(new PermissionAttachmentInfo(this, entry.getKey(), null, entry.getValue()));
        }
        return ret;
    }
    
    public void setPermission(String permission, boolean value) {
        assignedPermissions.put(permission.toLowerCase(), value);
    }
    
    public void unsetPermission(String permission) {
        assignedPermissions.remove(permission.toLowerCase());
    }

    public void clearPermissions() {
        assignedPermissions.clear();
    }
    
    // -- Unneeded OfflinePlayer methods

    public boolean isOnline() {
        return false;
    }

    public String getName() {
        return "Tester";
    }

    public UUID getUniqueId() {
        return randomUuid;
    }

    public boolean isBanned() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setBanned(boolean b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isWhitelisted() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setWhitelisted(boolean b) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Player getPlayer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getFirstPlayed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getLastPlayed() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasPlayedBefore() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Location getBedSpawnLocation() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Map<String, Object> serialize() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
