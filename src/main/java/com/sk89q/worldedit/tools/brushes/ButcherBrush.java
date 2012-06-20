package com.sk89q.worldedit.tools.brushes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.patterns.Pattern;

public class ButcherBrush implements Brush{
public ButcherBrush(){
	
}
	
	public void build(EditSession editSession, Vector pos, Pattern mat,
			double size) throws MaxChangedBlocksException {
		editSession.getWorld().killMobs(pos, size, 0x0);
	}

}
