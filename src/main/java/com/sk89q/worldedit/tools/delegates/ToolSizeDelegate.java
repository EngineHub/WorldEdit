package com.sk89q.worldedit.tools.delegates;

import com.sk89q.worldedit.tools.delegates.interfaces.ToolSize;

public class ToolSizeDelegate implements ToolSize {
    protected long x,y,z;
    protected boolean lockX,lockY,lockZ;
    
    public ToolSizeDelegate(boolean lockX, boolean lockY, boolean lockZ) {
        x = 0;
        y = 0;
        z = 0;
        this.lockX = lockX;
        this.lockY = lockZ;
        this.lockY = lockZ;
    }
    
    public void setX(long x) {
        this.x = x;
        if(lockX) {
            if(lockY) {
                this.y = x;
            }
            if(lockZ) {
                this.z = x;
            }             
        }
    }
    
    public void setY(long y) {
        this.y = y;
        if(lockY) {
            if(lockX) {
                this.x = y;
            }
            if(lockZ) {
                this.z = x;
            }             
        }
    }
    
    public void setZ(long z) {
        this.z = z;
        if(lockZ) {
            if(lockX) {
                this.x = z;
            }
            if(lockY) {
                this.y = z;
            }             
        }
    }
    
    public long getX() {
        return x;
    }
    
    public long getY() {
        return y;
    }
    
    public long getZ() {
        return z;
    }
}