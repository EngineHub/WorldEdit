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

import com.sk89q.worldedit.forge.gui.GuiHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import org.lwjgl.input.Keyboard;

public class KeyHandler {

    private static Minecraft mc = Minecraft.getMinecraft();
    private static KeyBinding mainKey = new KeyBinding("WorldEdit Reference", Keyboard.KEY_L, "WorldEdit");

    public KeyHandler() {
        ClientRegistry.registerKeyBinding(mainKey);
    }

    @SubscribeEvent
    public void onKey(KeyInputEvent evt) {
        if (mc.player != null && mc.world != null && mainKey.isPressed()) {
            mc.player.openGui(ForgeWorldEdit.inst, GuiHandler.REFERENCE_ID, mc.world, 0, 0, 0);
        }
    }

}
