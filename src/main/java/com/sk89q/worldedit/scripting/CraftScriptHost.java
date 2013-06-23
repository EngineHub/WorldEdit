// $Id$
/*
 * This file is a part of WorldEdit.
 * Copyright (c) sk89q <http://www.sk89q.com>
 * Copyright (c) the WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software 
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
*/

package com.sk89q.worldedit.scripting;

import com.sk89q.worldedit.*;

import javax.script.ScriptException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the execution of scripts.
 */
public class CraftScriptHost {

    private final WorldEdit worldEdit;

    /**
     * Create a new instance.
     *
     * @param worldEdit an instance of WorldEdit
     */
    public CraftScriptHost(WorldEdit worldEdit) {
        this.worldEdit = worldEdit;
    }

    /**
     * Executes a WorldEdit script.
     *
     * @param player the player
     * @param scriptFile the script file
     * @param args the arguments
     * @throws WorldEditException
     */
    public void execute(LocalPlayer player, File scriptFile, String[] args)
            throws WorldEditException {
        String filename = scriptFile.getPath();
        int index = filename.lastIndexOf(".");
        String ext = filename.substring(index + 1, filename.length());

        if (!ext.equalsIgnoreCase("js")) {
            player.printError("Only .js scripts are currently supported");
            return;
        }

        String script;
        InputStream is = null;

        try {
            if (!scriptFile.exists()) {
                is = WorldEdit.class.getResourceAsStream("craftscripts/" + filename);

                if (is == null) {
                    player.printError("Script does not exist: " + filename);
                    return;
                }
            } else {
                is = new FileInputStream(scriptFile);
            }

            DataInputStream in = new DataInputStream(is);
            byte[] data = new byte[in.available()];
            in.readFully(data);
            in.close();
            script = new String(data, 0, data.length, "utf-8");
        } catch (IOException e) {
            player.printError("Script read error: " + e.getMessage());
            return;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }

        LocalSession session = worldEdit.getSessions().get(player);
        CraftScriptContext scriptContext =
                new CraftScriptContext(worldEdit, worldEdit.getServer(),
                        worldEdit.getConfiguration(), session, player, args);

        CraftScriptEngine engine = null;

        try {
            engine = new RhinoCraftScriptEngine();
        } catch (NoClassDefFoundError e) {
            player.printError("Failed to find an installed script engine.");
            player.printError("Please see http://wiki.sk89q.com/wiki/WorldEdit/Installation");
            return;
        }

        engine.setTimeLimit(worldEdit.getConfiguration().scriptTimeout);

        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("argv", args);
        vars.put("context", scriptContext);
        vars.put("player", player);

        try {
            engine.evaluate(script, filename, vars);
        } catch (ScriptException e) {
            player.printError("Failed to execute:");
            player.printRaw(e.getMessage());
            e.printStackTrace();
        } catch (NumberFormatException e) {
            throw e;
        } catch (WorldEditException e) {
            throw e;
        } catch (Throwable e) {
            player.printError("Failed to execute (see console):");
            player.printRaw(e.getClass().getCanonicalName());
            e.printStackTrace();
        } finally {
            for (EditSession editSession : scriptContext.getEditSessions()) {
                editSession.flushQueue();
                session.remember(editSession);
            }
        }
    }

}
