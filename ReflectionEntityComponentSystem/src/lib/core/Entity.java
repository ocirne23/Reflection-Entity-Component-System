package lib.core;

public abstract class Entity {
	public final int id;
	public Entity() {
		id = EntityWorld.getEntityId();
	}
}
