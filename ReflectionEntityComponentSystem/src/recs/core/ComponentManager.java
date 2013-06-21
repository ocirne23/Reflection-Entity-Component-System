package recs.core;

import recs.core.utils.RECSBits;
import recs.core.utils.libgdx.RECSIntMap;
import recs.core.utils.libgdx.RECSObjectIntMap;

public final class ComponentManager {
	private EntityWorld world;

	ComponentManager(EntityWorld world) {
		this.world = world;
	}
	/**
	 * Collection of ComponentManagers, managers are retrievable by using the
	 * class they represent.
	 */
	private RECSIntMap<ComponentMapper<?>> componentMappers = new RECSIntMap<ComponentMapper<?>>();
	/**
	 * Map which links id's to components.
	 */
	private RECSObjectIntMap<Class<?>> componentIds = new RECSObjectIntMap<Class<?>>();
	private int componentIdCounter = 0;

	int getComponentId(Class<?> component) {
		return componentIds.get(component, -1);
	}

	<T> T getComponent(int entityId, Class<T> class1) {
		int componentId = componentIds.get(class1, -1);
		if (componentId == -1)
			return null;
		return class1.cast(componentMappers.get(componentId).get(entityId));
	}

	<T> void registerComponents(Class<?>[] componentClasses) {
		for (Class<?> component : componentClasses) {
			componentIds.put(component, ++componentIdCounter);
			componentMappers.put(componentIdCounter, new ComponentMapper<T>());
		}
	}

	@SuppressWarnings("unchecked")
	<T> ComponentMapper<T> getComponentMapper(Class<?> class1) {
		return (ComponentMapper<T>) componentMappers.get(componentIds.get(class1, -1));
	}

	void clear() {
		componentIdCounter = 0;
		componentIds.clear();
		componentMappers.clear();
	}

	void removeEntityFromMappers(Entity e) {
		RECSBits componentBits = e.def.componentBits;

		for (int i = componentBits.nextSetBit(0); i >= 0; i = componentBits.nextSetBit(i + 1)) {
			Object removedComponent = componentMappers.get(i).remove(e.id);
			if (removedComponent != null) {
				ComponentDestructionListener listener = world.getDestructionListener(removedComponent.getClass());
				if (listener != null)
					listener.destroyed(removedComponent);
			}
		}
	}

	void addComp(Entity e, Object... components) {
		EntityDef def = e.def;
		RECSBits componentBits = new RECSBits();
		RECSBits systemBits = new RECSBits();
		if (def != null)
			componentBits.copy(def.componentBits);
		for (Object component : components) {
			int componentId = getComponentId(component.getClass());
			ComponentMapper<?> mapper = getComponentMapper(componentId);
			if (mapper == null)
				throw new RuntimeException("Unregistered component added: " + component.getClass().getName());
			mapper.add(e.id, component);
			componentBits.set(componentId);
		}
		EntityDef newDef = world.getDef(componentBits);
		if (newDef == null) {
			systemBits = world.getSystemBits(componentBits);
			newDef = new EntityDef(world, componentBits, systemBits);
			world.putDef(componentBits, newDef);
		}
		e.def = newDef;
		world.addToSystems(e, def.systemBits, newDef.systemBits);
	}


	void removeComp(Entity e, Object... components) {
		EntityDef def = e.def;
		RECSBits componentBits = new RECSBits();
		RECSBits systemBits = new RECSBits();
		componentBits.copy(def.componentBits);
		for (Object component : components) {
			int componentId = getComponentId(component.getClass());
			getComponentMapper(componentId).remove(e.id);
			componentBits.clear(componentId);
		}
		EntityDef newDef = world.getDef(componentBits);
		if (newDef == null) {
			systemBits = world.getSystemBits(componentBits);
			newDef = new EntityDef(world, componentBits, systemBits);
			world.putDef(componentBits, def);
		}
		e.def = def;
		world.removeFromSystems(e, def.systemBits, newDef.systemBits);
	}

	@SuppressWarnings("unchecked")
	<T> ComponentMapper<T> getComponentMapper(int componentId) {
		return (ComponentMapper<T>) componentMappers.get(componentId);
	}
}
