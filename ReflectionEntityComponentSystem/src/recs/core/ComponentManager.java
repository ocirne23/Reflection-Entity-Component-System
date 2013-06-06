package recs.core;

import recs.core.utils.RECSIntMap;

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

	protected T remove(int entityId) {
		return components.remove(entityId);
	}

	public T get(int entityId) {
		return components.get(entityId);
	}

	@SuppressWarnings("unchecked")
	public void add(int entityId, Object o) {
		if(!components.containsKey(entityId)) {
			components.put(entityId, (T) o);
		}
	}
}
