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

package com.sk89q.worldedit;

import com.sk89q.worldedit.command.tool.BrushTool;
import com.sk89q.worldedit.command.tool.InvalidToolBindException;
import com.sk89q.worldedit.command.tool.Tool;
import com.sk89q.worldedit.command.tool.brush.CylinderBrush;
import com.sk89q.worldedit.command.tool.brush.SphereBrush;
import com.sk89q.worldedit.world.item.ItemType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Optional;

class LocalSessionTest {

    private static LocalSession session;
    public static ItemType dummyType;
    public static ItemType dummyType2;

    @BeforeAll
    static void setup() throws InvalidToolBindException {
        session = new LocalSession();
        dummyType = ItemType.REGISTRY.register("dummy:dummy", new ItemType("dummy"));
        dummyType2 = ItemType.REGISTRY.register("dummy:dummy2", new ItemType("dummy2"));
        BrushTool brushTool = new BrushTool("");
        session.setTool(dummyType, brushTool);
        brushTool.setBrush(new SphereBrush(), "");
    }

    @Test
    void isTool() throws InvalidToolBindException {
        Assertions.assertTrue(session.isTool(dummyType));
        Assertions.assertFalse(session.isTool(dummyType2));
    }

    @Test
    void isBrushTool() throws InvalidToolBindException {
        Assertions.assertTrue(session.isBrushTool(dummyType));
        Assertions.assertFalse(session.isBrushTool(dummyType2));
    }

    @Test
    void getTool() {
        Optional<BrushTool> brushTool = session.getTool(dummyType, BrushTool.class);
        Optional<Tool> tool = session.getTool(dummyType, Tool.class);
        Assertions.assertTrue(brushTool.isPresent());
        Assertions.assertTrue(tool.isPresent());

        tool = session.getTool(dummyType2, Tool.class);
        Assertions.assertFalse(tool.isPresent());
    }

    @Test
    void getBrush() {
        Optional<SphereBrush> sphereBrush = session.getBrush(dummyType, SphereBrush.class);
        Optional<CylinderBrush> cylBrush = session.getBrush(dummyType, CylinderBrush.class);

        Assertions.assertTrue(sphereBrush.isPresent());
        Assertions.assertFalse(cylBrush.isPresent());
    }
}
