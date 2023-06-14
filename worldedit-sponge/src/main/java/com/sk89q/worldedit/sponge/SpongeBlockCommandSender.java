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

package com.sk89q.worldedit.sponge;

import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.extension.platform.AbstractCommandBlockActor;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.auth.AuthorizationException;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.util.formatting.text.TextComponent;
import com.sk89q.worldedit.util.formatting.text.format.TextColor;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.block.entity.CommandBlock;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.util.Ticks;
import org.spongepowered.math.vector.Vector3d;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class SpongeBlockCommandSender extends AbstractCommandBlockActor {
    private final SpongeWorldEdit worldEdit;
    private final CommandBlock sender;
    private final UUID uuid;

    public SpongeBlockCommandSender(SpongeWorldEdit worldEdit, CommandBlock sender) {
        super(SpongeAdapter.adapt(checkNotNull(sender).serverLocation(), Vector3d.ZERO));
        checkNotNull(worldEdit);

        this.worldEdit = worldEdit;
        this.sender = sender;
        this.uuid = UUID.nameUUIDFromBytes((UUID_PREFIX + sender.name()).getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getName() {
        return sender.name();
    }

    @Override
    @Deprecated
    public void printRaw(String msg) {
        for (String part : msg.split("\n")) {
            sendMessage(net.kyori.adventure.text.Component.text(part));
        }
    }

    @Override
    @Deprecated
    public void print(String msg) {
        for (String part : msg.split("\n")) {
            print(TextComponent.of(part, TextColor.LIGHT_PURPLE));
        }
    }

    @Override
    @Deprecated
    public void printDebug(String msg) {
        for (String part : msg.split("\n")) {
            print(TextComponent.of(part, TextColor.GRAY));
        }
    }

    @Override
    @Deprecated
    public void printError(String msg) {
        for (String part : msg.split("\n")) {
            print(TextComponent.of(part, TextColor.RED));
        }
    }

    @Override
    public void print(Component component) {
        sendMessage(SpongeTextAdapter.convert(component, getLocale()));
    }

    private void sendMessage(net.kyori.adventure.text.Component textComponent) {
        this.sender.offer(Keys.LAST_COMMAND_OUTPUT, textComponent);
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
        return sender.hasPermission(permission);
    }

    public CommandBlock getSender() {
        return this.sender;
    }

    @Override
    public SessionKey getSessionKey() {
        return new SessionKey() {

            private volatile boolean active = true;

            private void updateActive() {
                BlockState block = sender.block();
                if (!sender.serverLocation().world().isChunkLoadedAtBlock(sender.blockPosition(), false)) {
                    active = false;
                    return;
                }
                BlockType type = block.type();
                active = type == BlockTypes.COMMAND_BLOCK.get()
                    || type == BlockTypes.CHAIN_COMMAND_BLOCK.get()
                    || type == BlockTypes.REPEATING_COMMAND_BLOCK.get();
            }

            @Override
            public String getName() {
                return sender.name();
            }

            @Override
            public boolean isActive() {
                if (Sponge.server().onMainThread()) {
                    // we can update eagerly
                    updateActive();
                } else {
                    // we should update it eventually
                    Task task = Task.builder().delay(Ticks.zero()).plugin(worldEdit.getPluginContainer()).execute(this::updateActive).build();
                    Sponge.server().scheduler().submit(task);
                }
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
