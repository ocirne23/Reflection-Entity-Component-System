package lib.core;

/**
 * All entities should extend this class. Add component objects to the child
 * class and create the entity to let the systems process it.
 *
 * @author Enrico van Oosten
 */
public abstract class Entity {
	public final int id;
	public Entity() {
		id = EntityWorld.getEntityId();
	}
}
