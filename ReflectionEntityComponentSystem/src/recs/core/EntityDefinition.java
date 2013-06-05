package recs.core;

public class EntityDefinition {
    public System[] usableSystems;
    public Class<?>[] components;
    
    public EntityDefinition(System[] usableSystems, Class<?>[] components) {
        this.usableSystems = usableSystems;
        this.components = components;
    }
}
