package lib.core;

import lib.utils.IntMap;

/**
 * Used by the Entity world to remember component reflection data for an unique
 * class. Holds a map of components with the key being the entity id, and the
 * value being the component of that entity.
 *
 * @author Enrico van Oosten
 * @param <T>
 */
public final class ComponentManager<T> {
	protected final int id;
	protected final IntMap<T> components;

	protected ComponentManager(int id) {
		this.id = id;
		components = new IntMap<T>();
	}

	protected void addComponent(int entityId, T object) {
		components.put(entityId, object);
	}

	protected void removeComponent(int entityId) {
		components.remove(entityId);
	}

	public T getComponent(int entityId) {
		return components.get(entityId);
	}
}
