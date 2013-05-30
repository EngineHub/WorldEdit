package net.playblack.cm;

import net.canarymod.api.entity.Entity;

import com.sk89q.worldedit.LocalEntity;
import com.sk89q.worldedit.Location;

public class CanaryEntity extends LocalEntity {
    Entity entity;
    public CanaryEntity(Entity entity) {
        super(new Location(new CanaryWorld(entity.getWorld()), CanaryUtil.toVector(entity.getPosition())));
        this.entity = entity;
    }

    @Override
    public boolean spawn(Location loc) {
        return entity.spawn();
    }

}
