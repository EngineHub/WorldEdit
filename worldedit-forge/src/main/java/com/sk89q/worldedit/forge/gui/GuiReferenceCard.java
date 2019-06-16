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

package com.sk89q.worldedit.forge.gui;

import com.sk89q.worldedit.forge.ForgeWorldEdit;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

@OnlyIn(Dist.CLIENT)
public class GuiReferenceCard extends Screen {

    private Button closeButton;
    private int backgroundWidth = 256;
    private int backgroundHeight = 256;

    public GuiReferenceCard(ITextComponent title) {
        super(title);
    }

    @Override
    public void init() {
        this.addButton(closeButton = new Button(
                (this.width - this.backgroundWidth + 56) / 2, (this.height + this.backgroundHeight) / 2,
                200, 20, "Close",
                button -> this.minecraft.player.closeScreen()));
    }

    @Override
    public void render(int mouseX, int mouseY, float par3) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2 - this.closeButton.getHeight();

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.textureManager.bindTexture(new ResourceLocation(ForgeWorldEdit.MOD_ID, "textures/gui/reference.png"));
        this.blit(x, y, 0, 0, this.backgroundWidth, this.backgroundHeight);
        super.render(mouseX, mouseY, par3);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

}
