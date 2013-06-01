package lib.core;

import lib.core.utils.RECSIntMap;

/**
 * Used to easily retrieve components from entities.
 *
 * @author Enrico van Oosten
 * @param <T>
 *            The component type this manager manages.
 */
public final class ComponentManager<T> {
	protected final RECSIntMap<T> components;

	protected ComponentManager() {
		components = new RECSIntMap<T>();
	}

	protected void add(int entityId, T object) {
		components.put(entityId, object);
	}

	protected void remove(int entityId) {
		components.remove(entityId);
	}

	public T get(int entityId) {
		return components.get(entityId);
	}
}
