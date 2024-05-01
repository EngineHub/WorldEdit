/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.neoforge;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.AbstractCommandBlockActor;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.WorldEditText;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.LogicalSidedProvider;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class NeoForgeBlockCommandSender extends AbstractCommandBlockActor {
    private final BaseCommandBlock sender;
    private final UUID uuid;

    public NeoForgeBlockCommandSender(BaseCommandBlock sender) {
        super(new Location(NeoForgeAdapter.adapt(checkNotNull(sender).getLevel()), NeoForgeAdapter.adapt(sender.getPosition())));

        this.sender = sender;
        this.uuid = UUID.nameUUIDFromBytes((UUID_PREFIX + sender.getName()).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getName() {
        return sender.getName().getString();
    }

    @Override
    @Deprecated
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            sendMessage(net.minecraft.network.chat.Component.literal(part));
        }
    }

    @Override
    @Deprecated
    public void printDebug(String msg) {
        sendColorized(msg, ChatFormatting.GRAY);
    }

    @Override
    @Deprecated
    public void print(String msg) {
        sendColorized(msg, ChatFormatting.LIGHT_PURPLE);
    }

    @Override
    @Deprecated
    public void printError(String msg) {
        sendColorized(msg, ChatFormatting.RED);
    }

    @Override
    public void print(Component component) {
        sendMessage(net.minecraft.network.chat.Component.Serializer.fromJson(
            GsonComponentSerializer.INSTANCE.serialize(WorldEditText.format(component, getLocale())),
            sender.getLevel().registryAccess()
        ));
    }

    private void sendColorized(String msg, ChatFormatting formatting) {
        for (String part : msg.split("\n")) {
            var component = net.minecraft.network.chat.Component.literal(part);
            component.withStyle(formatting);
            sendMessage(component);
        }
    }

    private void sendMessage(net.minecraft.network.chat.Component textComponent) {
        this.sender.sendSystemMessage(textComponent);
    }

    @Override
    public Locale getLocale() {
        return WorldEdit.getInstance().getConfiguration().defaultLocale;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public String[] getGroups() {
        return new String[0];
    }

    @Override
    public void checkPermission(String permission) throws AuthorizationException {
        if (!hasPermission(permission)) {
            throw new AuthorizationException();
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    public BaseCommandBlock getSender() {
        return this.sender;
    }

    @Override
    public SessionKey getSessionKey() {
        return new SessionKey() {

            private volatile boolean active = true;

            private void updateActive() {
                BlockPos pos = new BlockPos((int) sender.getPosition().x, (int) sender.getPosition().y, (int) sender.getPosition().z);
                int chunkX = SectionPos.blockToSectionCoord(pos.getX());
                int chunkZ = SectionPos.blockToSectionCoord(pos.getZ());
                if (!sender.getLevel().getChunkSource().hasChunk(chunkX, chunkZ)) {
                    active = false;
                    return;
                }
                Block type = sender.getLevel().getBlockState(pos).getBlock();
                active = type == Blocks.COMMAND_BLOCK
                    || type == Blocks.CHAIN_COMMAND_BLOCK
                    || type == Blocks.REPEATING_COMMAND_BLOCK;
            }

            @Override
            public String getName() {
                return sender.getName().getString();
            }

            @Override
            public boolean isActive() {
                LogicalSidedProvider.WORKQUEUE.get(LogicalSide.SERVER).submitAsync(this::updateActive);
                return active;
            }

            @Override
            public boolean isPersistent() {
                return true;
            }

            @Override
            public UUID getUniqueId() {
                return uuid;
            }
        };
    }
}
