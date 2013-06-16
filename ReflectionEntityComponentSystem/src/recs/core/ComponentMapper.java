package recs.core;

import recs.core.utils.RECSIntMap;
import recs.core.utils.RECSIntMap.Keys;

/**
 * Used to easily retrieve components from entities.
 *
 * @author Enrico van Oosten
 * @param <T>
 *            The component type this manager manages.
 */
public final class ComponentMapper<T> {
	final RECSIntMap<T> components;

	protected ComponentMapper() {
		components = new RECSIntMap<T>();
	}

	protected T remove(int entityId) {
		return components.remove(entityId);
	}

	public T get(int id) {
		return components.get(id);
	}

	public Keys entities() {
		return components.keys();
	}

	@SuppressWarnings("unchecked")
	public void add(int entityId, Object o) {
		if (!components.containsKey(entityId)) {
			components.put(entityId, (T) o);
		}
	}
}
