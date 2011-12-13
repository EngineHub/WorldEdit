package com.sk89q.worldedit;

import java.io.File;

public abstract class LocalCommandSender {
    /**
     * Server.
     */
    protected ServerInterface server;

    public LocalCommandSender(ServerInterface server) {
        this.server = server;
    }

    /**
     * Get the name of the player.
     *
     * @return String
     */
    public abstract String getName();

    /**
     * Print a message.
     *
     * @param msg
     */
    public abstract void printRaw(String msg);

    /**
     * Print a WorldEdit message.
     *
     * @param msg
     */
    public abstract void printDebug(String msg);

    /**
     * Print a WorldEdit message.
     *
     * @param msg
     */
    public abstract void print(String msg);

    /**
     * Print a WorldEdit error.
     *
     * @param msg
     */
    public abstract void printError(String msg);

    /**
     * Get a player's list of groups.
     *
     * @return
     */
    public abstract String[] getGroups();

    /**
     * Checks if a player has permission.
     *
     * @param perm
     * @return
     */
    public abstract boolean hasPermission(String perm);

    public void checkPermission(String permission) throws WorldEditPermissionException {
        if (!hasPermission(permission)) {
            throw new WorldEditPermissionException();
        }
    }

    /**
     * Open a file open dialog.
     *
     * @param extensions null to allow all
     * @return
     */
    public File openFileOpenDialog(String[] extensions) {
        printError("File dialogs are not supported in your environment.");
        return null;
    }

    /**
     * Open a file save dialog.
     *
     * @param extensions null to allow all
     * @return
     */
    public File openFileSaveDialog(String[] extensions) {
        printError("File dialogs are not supported in your environment.");
        return null;
    }

    /**
     * Returns true if equal.
     *
     * @param other
     * @return whether the other object is equivalent
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof LocalCommandSender)) {
            return false;
        }
        LocalCommandSender other2 = (LocalCommandSender) other;
        return other2.getName().equals(getName());
    }

    /**
     * Gets the hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    public LocalPlayer asPlayer() throws PlayerNeededException {
        throw new PlayerNeededException();
    }
}
