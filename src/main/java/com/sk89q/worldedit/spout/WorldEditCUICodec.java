/*
 * WorldEdit
 * Copyright (C) 2012 sk89q <http://www.sk89q.com> and contributors
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

package com.sk89q.worldedit.spout;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.spout.api.protocol.MessageCodec;
import org.spout.api.util.Named;

import java.nio.charset.Charset;

/**
 * @author zml2008
 */
public class WorldEditCUICodec extends MessageCodec<WorldEditCUIMessage> implements Named {
    public static final Charset UTF_8_CHARSET = Charset.forName("UTF-8");

    public WorldEditCUICodec(int opcode) {
        super(WorldEditCUIMessage.class, opcode);
    }

    @Override
    public ChannelBuffer encode(WorldEditCUIMessage message) {
        byte[] data = message.getMessage().getBytes(UTF_8_CHARSET);

        ChannelBuffer buffer = ChannelBuffers.buffer(data.length);
        buffer.writeBytes(data);
        return buffer;
    }

    @Override
    public WorldEditCUIMessage decode(ChannelBuffer buffer) {
        byte[] data = new byte[buffer.readableBytes()];
        buffer.readBytes(data);
        String message = new String(data, UTF_8_CHARSET);
        return new WorldEditCUIMessage(message);
    }

    @Override
    public String getName() {
        return "WECUI";
    }
}
