package com.sk89q.minecraft.util.commands;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;

public class CommandContextTest {
    final String firstCmdString = "herpderp -opw testers \"mani world\" 'another thing'  because something";
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
        String failingCommand = "herpderp -opw testers";
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
    public void testHagingQuoted() {
        String cmd = "r \"hello goodbye have fun";
        try {
            CommandContext context = new CommandContext(cmd);
        } catch (CommandException e) {
            e.printStackTrace();
            fail("Error creating CommandContext");
        }
    }

}
