package recs.core;

/**
 * All entities should extend this class. Add component objects to the child
 * class and create the entity to let the systems process it.
 *
 * @author Enrico van Oosten
 */
public class Entity {
	int id;
	EntityDef def = null;

	/**
	 * Add an component to this entity runtime.
	 *
	 * @param component
	 *            The component.
	 */
	public void addComponent(Object... components) {
		EntityWorld.addComp(this, components);
	}

	public void removeComponent(Object... components) {
		EntityWorld.removeComp(this, components);
	}

	public int getId() {
		return id;
	}
}
