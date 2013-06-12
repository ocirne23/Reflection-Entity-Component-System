package recs.core;

import recs.core.utils.RECSIntMap;
import recs.core.utils.RECSObjectIntMap;

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

	void removeEntityFromMappers(int id) {
		for (ComponentMapper<?> manager : componentMappers.values()) {
			Object removedComponent = manager.remove(id);
			if (removedComponent != null) {
				ComponentDestructionListener listener = world.getDestructionListener(removedComponent.getClass());
				if (listener != null)
					listener.destroyed(removedComponent);
			}
		}
	}

	@SuppressWarnings("unchecked")
	<T> ComponentMapper<T> getComponentMapper(int componentId) {
		return (ComponentMapper<T>) componentMappers.get(componentId);
	}
}
