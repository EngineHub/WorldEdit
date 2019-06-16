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
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

public class KeyHandler {

    private static Minecraft mc = Minecraft.getInstance();
    private static KeyBinding mainKey = new KeyBinding("WorldEdit Reference", GLFW.GLFW_KEY_L, "WorldEdit");

    public KeyHandler() {
        ClientRegistry.registerKeyBinding(mainKey);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent evt) {
        if (mc.player != null && mc.world != null && mainKey.isPressed()) {
            mc.displayGuiScreen(new GuiReferenceCard(new StringTextComponent("WorldEdit Reference")));
        }
    }

}
