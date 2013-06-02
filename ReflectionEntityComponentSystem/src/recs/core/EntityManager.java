package recs.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Representation of a single Entity class, used by the EntityWorld to remember reflection data.
 * Has a map holding the component fields in that class, and a list of usable systems.
 *
 * @author Enrico van Oosten
 */
public final class EntityManager {
	/**
	 * Map of components, retrievable by using the class of the component.
	 */
	protected HashMap<Class<?>, Field> componentFields;
	/**
	 * List of systems this entity class can use.
	 */
	protected LinkedList<EntitySystem> usableSystems;

	protected EntityManager(HashMap<Class<?>, Field> componentFields) {
		this.componentFields = componentFields;
		usableSystems = new LinkedList<EntitySystem>();
	}

	protected void addUsableSystem(EntitySystem system) {
		usableSystems.add(system);
	}

	protected boolean hasComponent(Class<?> component) {
		return componentFields.containsKey(component);
	}

	protected boolean hasOnlyTheseComponents(Class<?>... components) {
		int count = 0;
		for1: for(Class<?> component: componentFields.keySet()) {
			for(Class<?> thisComponent: components) {
				if(component.getName().equals(thisComponent.getName())) {
					count++;
					continue for1;
				}
			}
			return false;
		}
		if(count == components.length) return true;
		return false;
	}
}
