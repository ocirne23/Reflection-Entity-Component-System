package recs;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Keys;
import com.badlogic.gdx.utils.IntMap.Values;

/**
 * Used to easily retrieve components from entities.
 *
 * @author Enrico van Oosten
 * @param <T>
 *            The component type this manager manages.
 */
public final class ComponentMapper<T extends Component> {
	final IntMap<T> components;

	protected ComponentMapper() {
		components = new IntMap<T>();
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

	public Keys getIds() {
		return components.keys();
	}

	public Values<T> getComponents() {
		return components.values();
	}

	@SuppressWarnings("unchecked")
	void add(int entityId, Component o) {
		components.put(entityId, (T) o);
	}
}
