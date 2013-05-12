package net.playblack.cm;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.canarymod.Canary;
import net.canarymod.Translator;
import net.canarymod.api.entity.EntityType;
import net.canarymod.api.inventory.ItemType;
import net.canarymod.api.world.World;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.commandsys.CanaryCommand;
import net.canarymod.tasks.ServerTask;
import net.canarymod.tasks.ServerTaskManager;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import com.sk89q.minecraft.util.commands.CommandsManager;
import com.sk89q.worldedit.BiomeTypes;
import com.sk89q.worldedit.LocalPlayer;
import com.sk89q.worldedit.LocalWorld;
import com.sk89q.worldedit.ServerInterface;

public class CanaryServer extends ServerInterface {

    private WorldEdit plugin;
    private int taskid = 0;
    public CanaryServer(WorldEdit plugin) {
        this.plugin = plugin;
    }

    @Override
    public int resolveItem(String name) {
        ItemType t = ItemType.fromString(name);
        if(t != null) {
            return t.getId();
        }
        return 0;
    }

    @Override
    public boolean isValidMobType(String type) {
        try {
            EntityType.valueOf(type);
        }
        catch(Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void reload() {
        // TODO Auto-generated method stub
    }

    @Override
    public BiomeTypes getBiomes() {
        return new CanaryBiomeTypes();
    }

    @Override
    public List<LocalWorld> getWorlds() {
        List<LocalWorld> toRet = new ArrayList<LocalWorld>();
        for(World w : Canary.getServer().getWorldManager().getAllWorlds()) {
            toRet.add(new CanaryWorld(w));
        }
        return toRet;
    }

    @Override
    public void onCommandRegistration(List<Command> commands, CommandsManager<LocalPlayer> manager) {
        for (final Command command : commands) {
            final Method cmdMethod = manager.getMethods().get(null).get(command.aliases()[0]);

            if (cmdMethod != null && cmdMethod.isAnnotationPresent(CommandPermissions.class)) {
                final String[] permissions = cmdMethod.getAnnotation(CommandPermissions.class).value();
                net.canarymod.commandsys.Command cmd = new net.canarymod.commandsys.Command() {

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return getClass();
                    }

                    @Override
                    public String toolTip() {
                        return command.usage();
                    }

                    @Override
                    public String[] searchTerms() {
                        return new String[0];
                    }

                    @Override
                    public String[] permissions() {
                        return permissions;
                    }

                    @Override
                    public String parent() {
                        return "";
                    }

                    @Override
                    public int min() {
                        return command.min();
                    }

                    @Override
                    public int max() {
                        return command.max();
                    }

                    @Override
                    public String helpLookup() {
                        return "";
                    }

                    @Override
                    public String description() {
                        return command.desc();
                    }

                    @Override
                    public String[] aliases() {
                        return command.aliases();
                    }
                };
                CanaryCommand canaryCmd = new CanaryCommand(cmd, plugin, Translator.getInstance()) {

                    @Override
                    protected void execute(MessageReceiver arg0, String[] arg1) {
                        //Do nothing
                    }
                };

                Canary.help().registerCommand(plugin, canaryCmd);
            }
        }
    }

    @Override
    public int schedule(long delay, long period, final Runnable task) {
        if(ServerTaskManager.addTask(new ServerTask(plugin, period, period > 0) {
            Runnable r = task;
            @Override
            public void run() {
                r.run();
            }
        })) {
            taskid++;
            return taskid;
        }
        return -1;
    }
}
