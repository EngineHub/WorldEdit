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

import com.sk89q.worldedit.forge.gui.GuiReferenceCard;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;

public class CommonProxy {

    public static ResourceLocation REFERENCE_GUI = new ResourceLocation("worldedit", "resource_gui");

    public void registerHandlers() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.GUIFACTORY, () -> openContainer -> {
            if (openContainer.getId().equals(REFERENCE_GUI)) {
                return new GuiReferenceCard();
            }
            return null;
        });
    }

}
