package lib.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;

public final class EntityDef {
	protected HashMap<Class<?>, Field> componentFields;
	protected LinkedList<EntitySystem> usableSystems;

	protected EntityDef(HashMap<Class<?>, Field> componentFields) {
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
