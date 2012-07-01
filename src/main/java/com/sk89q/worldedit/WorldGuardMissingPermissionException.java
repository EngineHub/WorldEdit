package com.sk89q.worldedit;

import org.bukkit.Location;

public class WorldGuardMissingPermissionException extends WorldEditException {


	private static final long serialVersionUID = -7222415044987581773L;

	public WorldGuardMissingPermissionException() {
		// TODO Auto-generated constructor stub
	}

	public WorldGuardMissingPermissionException(Location location) {
		super("Permission denied to edit on location " + location);
		// TODO Auto-generated constructor stub
	}

}
