package com.sk89q.worldedit.bukkit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.CommandSender;
import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;

public class WorldEditCommand extends org.bukkit.command.Command {

    public WorldEditCommand(Command command, CommandPermissions commandPermissions) {
        super(command.aliases()[0]);
        this.description = command.desc();
        this.usageMessage = "/"+getName()+" "+command.usage();

        List<String> aliases = new ArrayList<String>(Arrays.asList(command.aliases()));
        aliases.remove(0);
        this.setAliases(aliases);
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        // This method is never called. 
        return true;
    }
}
