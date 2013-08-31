package main.java.recs;

import main.java.recs.utils.RECSBits;

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
	public void addComponent(Component... components) {
		EntityWorld.addComponent(this, components);
	}

	public void removeComponent(Component... components) {
		EntityWorld.removeComponent(this, components);
	}

	public int getId() {
		return id;
	}

	public <T extends Component> boolean hasComponent(Class<T> componentClass) {
		if (data == null)
			throw new RuntimeException("Entity must be added to a world before accessing its components");
		return data.componentBits.get(data.world.getComponentId(componentClass));
	}

	public <T extends Component> T getComponent(Class<T> componentClass) {
		if (data == null)
			throw new RuntimeException("Entity must be added to a world before accessing its components");
		return data.world.getComponent(id, componentClass);
	}

	public Object getComponent(int componentId) {
		if (data == null)
			throw new RuntimeException("Entity must be added to a world before accessing its components");
		return data.world.getComponent(id, componentId);
	}

	/**
	 * Get an array of componentId's this entity has.
	 * @return
	 */
	public int[] getComponentIds() {
		if (data == null)
			throw new RuntimeException("Entity must be added to a world before accessing its components");
		RECSBits componentBits = data.componentBits;
		int[] components = new int[data.componentBits.cardinality()];

		int idx = 0;
		for (int i = componentBits.nextSetBit(0); i >= 0; i = componentBits.nextSetBit(i + 1)) {
			components[idx++] = i;
		}

		return components;
	}

	/**
	 * Get all the components this entity has, if its not added to a world yet, it returns the scheduled
	 * components. (Not including class fields).
	 */
	public Component[] getComponents() {
		if(data == null)
			throw new RuntimeException("Entity must be added to a world before accessing its components");

		int[] componentIds = getComponentIds();
		Component[] components = new Component[componentIds.length];

		for (int i = 0; i < componentIds.length; i++) {
			components[i] = data.world.getComponent(id, componentIds[i]);
		}

		return components;
	}
}
