package com.sk89q.worldedit;

public class UnknownBiomeTypeException extends WorldEditException {
    private static final long serialVersionUID = -6239229394330814896L;

    private String typeName;

    public UnknownBiomeTypeException(String typeName) {
        super("Unknown " + typeName + " biome type.");
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

}
