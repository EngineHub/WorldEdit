// $Id$
/*
 * WorldEdit
 * Copyright (C) 2010 sk89q <http://www.sk89q.com>
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

package com.sk89q.minecraft.util.commands;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;

public class CommandContextTest {
    private static final String firstCmdString = "herpderp -opw testers \"mani world\" 'another thing'  because something";
    CommandContext firstCommand;

    @Before
    public void setUpTest(){
        try {
            firstCommand = new CommandContext(firstCmdString, new HashSet<Character>(Arrays.asList('o', 'w')));
        } catch (CommandException e) {
            e.printStackTrace();
            fail("Unexpected exception when creating CommandContext");
        }
    }

    @Test(expected = CommandException.class)
    public void testInvalidFlags() throws CommandException {
        final String failingCommand = "herpderp -opw testers";
        new CommandContext(failingCommand, new HashSet<Character>(Arrays.asList('o', 'w')));
    }

    @Test
    public void testBasicArgs() {
        String command = firstCommand.getCommand();
        String argOne = firstCommand.getString(0);
        String joinedArg = firstCommand.getJoinedStrings(0);
        assertEquals(command, "herpderp");
        assertEquals(argOne, "another thing");
        assertEquals(joinedArg, "another thing because something");
    }

    @Test
    public void testFlags() {
        assertTrue(firstCommand.hasFlag('p'));
        assertTrue(firstCommand.hasFlag('o'));
        assertTrue(firstCommand.hasFlag('w'));
        assertEquals(firstCommand.getFlag('o'), "testers");
        assertEquals(firstCommand.getFlag('w'), "mani world");
        assertNull(firstCommand.getFlag('u'));
    }

    @Test
    public void testOnlyQuotedString() {
        String cmd = "r \"hello goodbye have fun\"";
        String cmd2 = "r 'hellogeedby' nnnnnee";
        try {
            CommandContext context = new CommandContext(cmd);
            CommandContext context2 = new CommandContext(cmd2);
        } catch (CommandException e) {
            e.printStackTrace();
            fail("Error creating CommandContext");
        }
    }

    @Test
    public void testHangingQuote() {
        String cmd = "r \"hello goodbye have fun";
        try {
            CommandContext context = new CommandContext(cmd);
        } catch (CommandException e) {
            e.printStackTrace();
            fail("Error creating CommandContext");
        }
    }

}
