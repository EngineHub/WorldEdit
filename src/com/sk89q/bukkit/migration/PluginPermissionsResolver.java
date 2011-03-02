// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010, 2011 sk89q <http://www.sk89q.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.bukkit.migration;

public class PluginPermissionsResolver implements PermissionsResolver {
    
    protected PermissionsProvider resolver;
    
    public PluginPermissionsResolver(PermissionsProvider resolver) {
        this.resolver = resolver;
    }

    @Override
    public void load() {
    }

    @Override
    public boolean hasPermission(String name, String permission) {
        return resolver.hasPermission(name, permission);
    }

    @Override
    public boolean inGroup(String player, String group) {
        return resolver.inGroup(player, group);
    }

    @Override
    public String[] getGroups(String player) {
        return resolver.getGroups(player);
    }

}
