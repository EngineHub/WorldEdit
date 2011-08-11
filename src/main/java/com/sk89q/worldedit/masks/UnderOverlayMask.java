
package com.sk89q.worldedit.masks;

import java.util.HashSet;
import java.util.Set;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;

/**
 *
 * @author 1337
 */
public class UnderOverlayMask implements Mask{
    boolean overlay;
    boolean wildcard;
    Set<Integer> ids = new HashSet<Integer>();
    public UnderOverlayMask(Set<Integer> ids,boolean overlay,boolean wildcard){
        addAll(ids);
        this.overlay = overlay;
        this.wildcard = wildcard;
        
    }
    public void addAll(Set<Integer> ids){
        this.ids.addAll(ids);
    }
    public boolean matches(EditSession editSession, Vector pos) {
        if(!wildcard){
            int id = editSession.getBlock(pos.setY(pos.getBlockY() + (overlay ? -1 :1))).getType();  
        if(overlay){
return ids.contains(id);
              }
        else if(!overlay){ 
        return ids.contains(id);
        }
        }
        else{
        return (overlay ? editSession.getBlock(pos.setY(pos.getBlockY() + 1)).getType() != 0: editSession.getBlock(pos.setY(pos.getBlockY() - 1)).getType() != 0);
        }
            return false;
    }
   
    
    }
    

