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

package com.sk89q.minecraft.util.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class CommandContextTest {

    private static final Logger log = LoggerFactory.getLogger(CommandContextTest.class);
    private static final String firstCmdString = "herpderp -opw testers \"mani world\" 'another thing'  because something";
    CommandContext firstCommand;

    @BeforeEach
    public void setUpTest() {
        try {
            firstCommand = new CommandContext(firstCmdString, new HashSet<>(Arrays.asList('o', 'w')));
        } catch (CommandException e) {
            log.warn("Error", e);
            fail("Unexpected exception when creating CommandContext");
        }
    }

    @Test
    public void testInvalidFlags() {
        final String failingCommand = "herpderp -opw testers";
        assertThrows(CommandException.class, () -> {
            new CommandContext(failingCommand, new HashSet<>(Arrays.asList('o', 'w')));
        });
    }

    @Test
    public void testBasicArgs() {
        String command = firstCommand.getCommand();
        String argOne = firstCommand.getString(0);
        String joinedArg = firstCommand.getJoinedStrings(0);
        assertEquals("herpderp", command);
        assertEquals("another thing", argOne);
        assertEquals("'another thing'  because something", joinedArg);
    }

    @Test
    public void testFlags() {
        assertTrue(firstCommand.hasFlag('p'));
        assertTrue(firstCommand.hasFlag('o'));
        assertTrue(firstCommand.hasFlag('w'));
        assertEquals("testers", firstCommand.getFlag('o'));
        assertEquals("mani world", firstCommand.getFlag('w'));
        assertFalse(firstCommand.hasFlag('u'));
    }

    @Test
    public void testOnlyQuotedString() {
        String cmd = "r \"hello goodbye have fun\"";
        String cmd2 = "r 'hellogeedby' nnnnnee";
        try {
            new CommandContext(cmd);
            new CommandContext(cmd2);
        } catch (CommandException e) {
            log.warn("Error", e);
            fail("Error creating CommandContext");
        }
    }

    @Test
    public void testUnmatchedQuote() {
        String cmd = "r \"hello goodbye have fun";
        try {
            new CommandContext(cmd);
        } catch (CommandException e) {
            log.warn("Error", e);
            fail("Error creating CommandContext");
        }
    }

    @Test
    public void testMultipleSpaces() {
        String cmd = "r hi   self";
        try {
            new CommandContext(cmd);
        } catch (CommandException e) {
            log.warn("Error", e);
            fail("Error creating CommandContext");
        }
    }

    @Test
    public void testFlagsAnywhere() {
        try {
            CommandContext context = new CommandContext("r hello -f");
            assertTrue(context.hasFlag('f'));

            CommandContext context2 = new CommandContext("r hello -f world");
            assertTrue(context2.hasFlag('f'));
        } catch (CommandException e) {
            log.warn("Error", e);
            fail("Error creating CommandContext");
        }
    }

    @Test
    public void testExactJoinedStrings() {
        try {
            CommandContext context = new CommandContext("r -f \"hello world\"   foo   bar");
            assertTrue(context.hasFlag('f'));
            assertEquals("\"hello world\"   foo   bar", context.getJoinedStrings(0));
            assertEquals("foo   bar", context.getJoinedStrings(1));

            CommandContext context2 = new CommandContext("pm name \"hello world\"   foo   bar");
            assertEquals("\"hello world\"   foo   bar", context2.getJoinedStrings(1));
        } catch (CommandException e) {
            log.warn("Error", e);
            fail("Error creating CommandContext");
        }
    }

    @Test
    public void testSlice() {
        try {
            CommandContext context = new CommandContext("foo bar baz");
            assertArrayEquals(new String[] { "foo", "bar", "baz" }, context.getSlice(0));

        } catch (CommandException e) {
            log.warn("Error", e);
            fail("Error creating CommandContext");
        }
    }

    @Test
    public void testEmptyQuote() {
        try {
            CommandContext context = new CommandContext("region flag xmas blocked-cmds \"\"");
            assertEquals(context.argsLength(), 3);
        } catch (CommandException e) {
            log.warn("Error", e);
            fail("Error creating CommandContext");
        }
    }
}
