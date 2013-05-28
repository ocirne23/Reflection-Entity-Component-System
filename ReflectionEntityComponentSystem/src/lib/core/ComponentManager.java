package lib.core;


/**
 * Used by the Entity world to remember component reflection data for an unique
 * class. Holds a map of components with the key being the entity id, and the
 * value being the component of that entity.
 *
 * @author Enrico van Oosten
 * @param <T>
 */
public final class ComponentManager<T> {
	protected final lib.core.EntityIntMap<T> components;

	protected ComponentManager() {
		components = new EntityIntMap<T>();
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
