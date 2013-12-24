package recs;

import java.util.ArrayList;
import java.util.List;

import recs.utils.RECSBits;

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
	EntityFamily family = null;

	/**
	 * Add an component to this entity.
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
		if (family == null) {	// not yet added to a world
			for (Component component : getComponents()) {
				if (componentClass.equals(component.getClass())) {
					return true;
				}
			}
			return false;
		}
		return family.componentBits.get(family.world.getComponentId(componentClass));
	}

	/**
	 * Get the component with the given class, returns null if not found.
	 */
	public <T extends Component> T getComponent(Class<T> componentClass) {
		if (family != null)
			return family.world.getComponent(id, componentClass);

		//null family,
		for (Component component : getComponents())
			if (componentClass.equals(component.getClass()))
				return componentClass.cast(component);

		return null;
	}

	public Component getComponent(int componentId) {
		if (family == null)
			throw new IllegalStateException(
					"Components of entity will not have IDs until the entity is added to a world");
		return family.world.getComponent(id, componentId);
	}

	/**
	 * Get an array of componentId's this entity has.
	 */
	public int[] getComponentIds() {
		if (family == null)
			throw new IllegalStateException(
					"Components of entity will not have IDs until the entity is added to a world");

		RECSBits componentBits = family.componentBits;
		int[] components = new int[componentBits.cardinality()];

		for (int i = componentBits.nextSetBit(0), idx = 0; i >= 0; i = componentBits.nextSetBit(i + 1))
			components[idx++] = i;

		return components;
	}

	/**
	 * Get all the components this entity has, if its not added to a world yet, it returns the scheduled
	 * components.
	 */
	public Component[] getComponents() {
		if(family == null) { // not yet added to a world, use the scheduled add/remove data.
			List<Component> results = new ArrayList<Component>(EntityWorld.getScheduledAdds(this));
			results.removeAll(EntityWorld.getScheduledRemoves(this));
			return results.toArray(new Component[results.size()]);
		}

		int[] componentIds = getComponentIds();
		Component[] components = new Component[componentIds.length];

		for (int i = 0; i < componentIds.length; i++)
			components[i] = family.world.getComponent(id, componentIds[i]);

		return components;
	}
}
