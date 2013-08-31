package recs;

import recs.utils.libgdx.RECSIntMap;
import recs.utils.libgdx.RECSIntMap.Keys;

/**
 * Used to easily retrieve components from entities.
 *
 * @author Enrico van Oosten
 * @param <T>
 *            The component type this manager manages.
 */
public final class ComponentMapper<T extends Component> {
	final RECSIntMap<T> components;

	protected ComponentMapper() {
		components = new RECSIntMap<T>();
	}

	protected Component remove(int entityId) {
		return components.remove(entityId);
	}

	/**
	 * Retrieve a component using an entityId, returns null if the component does
	 * not exist.
	 */
	public T get(int id) {
		return components.get(id);
	}

	public Keys entities() {
		return components.keys();
	}

	@SuppressWarnings("unchecked")
	void add(int entityId, Component o) {
		if (!components.containsKey(entityId)) {
			components.put(entityId, (T) o);
		}
	}
}
