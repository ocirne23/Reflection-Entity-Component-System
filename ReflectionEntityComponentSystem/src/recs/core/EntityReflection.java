package recs.core;

import java.lang.reflect.Field;
import java.util.LinkedList;

import recs.core.utils.RECSIntMap;

/**
 * Representation of a single Entity class, used by the EntityWorld to remember reflection data.
 * Has a map holding the component fields in that class, and a list of usable systems.
 *
 * @author Enrico van Oosten
 */
public final class EntityReflection {
	protected EntityDef definition;
	/**
	 * Map of components, retrievable by using the class of the component.
	 */
	protected RECSIntMap<Field> componentFields;
	/**
	 * List of systems this entity class can use.
	 */
	protected LinkedList<EntitySystem> usableSystems;

	protected EntityReflection(RECSIntMap<Field> componentFields) {
		this.componentFields = componentFields;
		usableSystems = new LinkedList<EntitySystem>();
	}

	protected void addUsableSystem(EntitySystem system) {
		usableSystems.add(system);
	}

	protected boolean hasComponent(int component) {
		return componentFields.containsKey(component);
	}

	/*
	protected boolean hasOnlyTheseComponents(int... components) {
		int count = 0;
		for1: for(int component: componentFields.keys()) {
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
	*/
}
