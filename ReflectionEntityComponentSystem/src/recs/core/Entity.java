package recs.core;

import recs.core.utils.RECSBits;

/**
 * Base class for all entities, either extend this class and add Components as fields to the parent,
 * or use addComponent to add components to the entity.
 *
 * Then add the entity to the world so it can be processed by the systems.
 *
 * @author Enrico van Oosten
 */
public class Entity {
	int id;
	EntityData data = null;

	/**
	 * Add an component to this entity runtime.
	 *
	 * @param component
	 *            The component.
	 */
	public void addComponent(Object... components) {
		EntityWorld.addComponent(this, components);
	}

	public void removeComponent(Object... components) {
		EntityWorld.removeComponent(this, components);
	}

	public int getId() {
		return id;
	}


	public <T> boolean hasComponent(Class<T> componentClass) {
		return data.componentBits.get(data.world.getComponentId(componentClass));
	}

	public <T> T getComponent(Class<T> componentClass) {
		return data.world.getComponent(id, componentClass);
	}

	public Object getComponent(int componentId) {
		return data.world.getComponent(id, componentId);
	}

	/**
	 * Get an array of componentId's this entity has.
	 * @return
	 */
	public int[] getComponentIds() {
		RECSBits componentBits = data.componentBits;
		int[] components = new int[data.componentBits.cardinality()];

		int idx = 0;
		for (int i = componentBits.nextSetBit(0); i >= 0; i = componentBits.nextSetBit(i + 1)) {
			components[idx++] = i;
		}

		return components;
	}

	public Object[] getComponents() {
		if(data == null)
			return EntityWorld.getScheduledAdds(this);

		int[] componentIds = getComponentIds();
		Object[] components = new Object[componentIds.length];

		for (int i = 0; i < componentIds.length - 1; i++) {
			components[i] = data.world.getComponent(id, componentIds[i]);
		}

		return components;
	}
}
