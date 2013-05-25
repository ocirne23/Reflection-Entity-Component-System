package lib.core;

import lib.utils.IntArray;

/**
 * Extend this class and add it to the EntityWorld to create a
 *
 * @author Enrico van Oosten
 */
public abstract class EntitySystem {
	private final IntArray entitiyIds;
	private final Class<?>[] components;

	public EntitySystem(Class<?>... components) {
		this.components = components;
		entitiyIds = new IntArray(false, 16);
	}

	protected final void process(float deltaInSec) {
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
