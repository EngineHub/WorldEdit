package com.sk89q.worldedit;

/**
 * @author zml2008
 */
public class WorldEditPermissionException extends WorldEditException {
    public WorldEditPermissionException() {
        super("You don't have permission to do this.");
    }
}
