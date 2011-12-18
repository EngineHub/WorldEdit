package com.sk89q.worldedit;

/**
 * Thrown when an operation is run that needs an actual player, not a console.
 *
 */
public class PlayerNeededException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public PlayerNeededException() {
        super("This command cannot be run on the console."); 
    }
}
