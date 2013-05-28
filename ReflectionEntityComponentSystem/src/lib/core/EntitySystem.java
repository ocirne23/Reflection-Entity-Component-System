package lib.core;

import lib.utils.IntArray;

/**
 * Extend this class and add it to the EntityWorld to create a
 *
 * @author Enrico van Oosten
 */
public abstract class EntitySystem {
	/**
	 * Array of entities this system will use.
	 */
	private final IntArray entitiyIds;
	/**
	 * Collection of the classes of the components this system will use.
	 */
	private final Class<?>[] components;

	public EntitySystem(Class<?>... components) {
		this.components = components;
		entitiyIds = new IntArray(false, 16);
	}

	protected final void processSystem(float deltaInSec) {
		processEntities(entitiyIds, deltaInSec);
	}

	protected abstract void processEntities(IntArray entities, float deltaInSec);

	protected Class<?>[] getComponents() {
		return components;
	}

	protected void addEntity(int id) {
		entitiyIds.add(id);
	}

	protected void removeEntity(int id) {
		entitiyIds.removeValue(id);
	}
}
