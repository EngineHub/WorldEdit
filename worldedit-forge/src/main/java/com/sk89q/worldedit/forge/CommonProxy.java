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

package com.sk89q.worldedit.forge;


import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.Block;

import com.sk89q.jnbt.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.event.platform.SchematicEvent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.forge.gui.GuiHandler;
import com.sk89q.worldedit.util.eventbus.Subscribe;

import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;

public class CommonProxy {

    public void registerHandlers() {
        NetworkRegistry.INSTANCE.registerGuiHandler(ForgeWorldEdit.inst, new GuiHandler());
        WorldEdit.getInstance().getEventBus().register(this);
    }

    /**
     * An event called when a schematic is saved.
     * @param event - The event instance.
     */
    @Subscribe
    public void onSchematicSave(SchematicEvent.Write event) {
        Map<String, Tag> idMap = new HashMap<String, Tag>();
        Clipboard clipboard = event.getClipboard();

        for (Vector point : clipboard.getRegion()) {
            BaseBlock block = clipboard.getBlock(point);
            String alias = GameRegistry.findUniqueIdentifierFor(Block.getBlockById(block.getId())).toString();
            if (!mappingExists(alias, idMap)) idMap.put(alias, new ShortTag((short) block.getId()));
        }

        event.getSchematicMap().put("Mapping", new CompoundTag(idMap));
    }

    /**
     * Used to check if a block alias exists in the current id map.
     * 
     * @param alias - The alias being searched for.
     * @param idMap - The id map that is being checked.
     * @return Return true if the mapping exists in the id map.
     */
    public boolean mappingExists(String alias, Map<String, Tag> idMap) {
        return alias != null && idMap != null && idMap.containsKey(alias);
    }
}
