package com.sk89q.minecraft.util.commands;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;

public class CommandContextTest {
    final String firstCmdString = "herpderp -opw testers \"mani world\" 'another thing'  because something";
    CommandContext firstCommand;
    CommandContext secondCommand;

    @Before
    public void setUpTest(){
        try {
            firstCommand = new CommandContext(firstCmdString, new HashSet<Character>(Arrays.asList('o', 'w')));
        } catch (CommandException e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test(expected = CommandException.class)
    public void testInvalidFlags() throws CommandException {
        String failingCommand = "herpderp -opw testers";
        new CommandContext(failingCommand, new HashSet<Character>(Arrays.asList('o', 'w')));
    }

}
