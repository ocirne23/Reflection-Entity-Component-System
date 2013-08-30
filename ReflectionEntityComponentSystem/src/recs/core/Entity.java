package recs.core;

/**
 * Base class for all entities, either extend this class and add Components as fields to the parent,
 * or use addComponent to add components to the entity.
 *
 * Then add the entity to the world so it can be processed by the systems.
 *
 * @author Enrico van Oosten
 */
public class Entity {
	int id;
	EntityData data = null;

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

	public int getId() {
		return id;
	}
}
