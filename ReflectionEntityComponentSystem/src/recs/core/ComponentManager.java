package recs.core;

import recs.core.utils.RECSIntMap;
import recs.core.utils.RECSObjectIntMap;

public class ComponentManager {
	/**
	 * Collection of ComponentManagers, managers are retrievable by using the
	 * class they represent.
	 */
	private static RECSIntMap<ComponentMapper<?>> componentMappers = new RECSIntMap<ComponentMapper<?>>();
	/**
	 * Map with Id's for components.
	 */
	private static RECSObjectIntMap<Class<?>> componentIds = new RECSObjectIntMap<Class<?>>();
	private static int componentIdCounter = 0;

	public int getComponentId(Class<?> component) {
		return componentIds.get(component, -1);
	}

	public <T> T getComponent(int entityId, Class<T> class1) {
		int componentId = componentIds.get(class1, -1);
		if (componentId == -1)
			return null;
		return class1.cast(componentMappers.get(componentId).get(entityId));
	}

	public <T> void registerComponents(Class<?>[] componentClasses) {
		for (Class<?> component : componentClasses) {
			componentIds.put(component, ++componentIdCounter);
			componentMappers.put(componentIdCounter, new ComponentMapper<T>());
		}
	}

	@SuppressWarnings("unchecked")
	public <T> ComponentMapper<T> getComponentMapper(Class<?> class1) {
		return (ComponentMapper<T>) componentMappers.get(componentIds.get(class1, -1));
	}

	public void clear() {
		componentIdCounter = 0;
		componentIds.clear();
		componentMappers.clear();
	}

	public void removeEntityFromMappers(int id) {
		for (ComponentMapper<?> manager : componentMappers.values()) {
			Object removedComponent = manager.remove(id);
			if (removedComponent != null) {
				ComponentDestructionListener listener = EntityWorld.getDestructionListener(removedComponent.getClass());
				if (listener != null)
					listener.destroyed(removedComponent);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public <T> ComponentMapper<T> getComponentMapper(int componentId) {
		return (ComponentMapper<T>) componentMappers.get(componentId);
	}
}
