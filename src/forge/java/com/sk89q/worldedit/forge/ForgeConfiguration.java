package com.sk89q.worldedit.forge;

import com.sk89q.worldedit.util.PropertiesConfiguration;
import java.io.File;

public class ForgeConfiguration extends PropertiesConfiguration {

    public ForgeConfiguration(WorldEditMod mod) {
        super(new File(mod.getWorkingDir() + File.separator + "worldedit.properties"));
    }

    public void load() {
        super.load();
        showFirstUseVersion = false;
    }

    public File getWorkingDirectory() {
        return WorldEditMod.inst.getWorkingDir();
    }
}