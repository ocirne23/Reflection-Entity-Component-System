package recs.core;

/**
 * All entities should extend this class. Add component objects to the child
 * class and create the entity to let the systems process it.
 *
 * @author Enrico van Oosten
 */
public class Entity {
	public final int id;

	public Entity() {
		id = EntityWorld.getEntityId();
	}

	/**
	 * Add an component to this entity runtime.
	 *
	 * @param component
	 *            The component.
	 */
	public void addComponent(Object... components) {
		EntityWorld.addComponent(this, components);
	}

	public void removeComponent(Object... components) {
		EntityWorld.removeComponent(this, components);
	}
}
