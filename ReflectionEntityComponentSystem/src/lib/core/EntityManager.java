package lib.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Used by the EntityWorld to remember reflection data of an unique class of an
 * entity. Has a map holding the component fields in that class, and a list of
 * usable systems.
 *
 * @author Enrico van Oosten
 */
public final class EntityManager {
	protected HashMap<Class<?>, Field> componentFields;
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
}
