package recs.core;

import recs.core.utils.RECSBits;
import recs.core.utils.libgdx.RECSIntMap;
import recs.core.utils.libgdx.RECSObjectIntMap;
import recs.core.utils.libgdx.RECSObjectMap;

public final class ComponentManager {
	private EntityWorld world;

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
	/**
	 * Map which handles notification of destroyed components.
	 */
	private final RECSObjectMap<Class<?>, ComponentDestructionListener<?>> destructionListeners;

	ComponentManager(EntityWorld world) {
		this.world = world;
		destructionListeners = new RECSObjectMap<Class<?>, ComponentDestructionListener<?>>();
	}

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
		destructionListeners.clear();
	}

	void registerDestuctionListener(ComponentDestructionListener<?> listener, Class<?> componentClass) {
		destructionListeners.put(componentClass, listener);
	}

	ComponentDestructionListener<?> getDestructionListener(Class<?> componentClass) {
		return destructionListeners.get(componentClass);
	}


	void removeEntityFromMappers(Entity e) {
		RECSBits componentBits = e.data.componentBits;

		for (int i = componentBits.nextSetBit(0); i >= 0; i = componentBits.nextSetBit(i + 1)) {
			Object removedComponent = componentMappers.get(i).remove(e.id);
			if (removedComponent != null) {
				@SuppressWarnings("unchecked")
				ComponentDestructionListener<Object> listener = world.getDestructionListener(removedComponent.getClass());
				if (listener != null)
					listener.destroyed(removedComponent);
			}
		}
	}

	/**
	 * Add the given components to this entity, updating its EntityData.
	 * Does not add the entity to the systems yet.
	 */
	void addComponent(Entity e, Object... components) {
		//Copy the old componentBits.
		RECSBits newComponentBits = new RECSBits(e.data.componentBits);

		////Update componentbits.
		//For every component added
		for(Object component: components) {
			//Get its id
			int componentId = getComponentId(component.getClass());

			//Add the component to its componentMapper
			ComponentMapper<?> mapper = getComponentMapper(componentId);
			if (mapper == null)
				throw new RuntimeException("Unregistered component added: " + component.getClass().getName());
			mapper.add(e.id, component);

			//set the id in the componentBits
			newComponentBits.set(componentId);
		}

		//Retrieve a new EntityData object matching the new set of components.
		EntityData newData = world.getEntityData(newComponentBits);

		e.data = newData;
	}

	/**
	 * Remove the given components from this entity, updating its EntityData.
	 * Does not remove the entity from the systems yet.
	 */
	void removeComponent(Entity e, Object... components) {
		//Copy the old componentBits.
		RECSBits newComponentBits = new RECSBits(e.data.componentBits);

		////Update componentbits.
		//For every component added
		for(Object component: components) {
			//Get its id
			int componentId = getComponentId(component.getClass());

			//Add the component to its componentMapper
			ComponentMapper<?> mapper = getComponentMapper(componentId);
			if (mapper == null)
				throw new RuntimeException("Unregistered component added: " + component.getClass().getName());
			mapper.remove(e.id);

			//remove the id from the componentBits
			newComponentBits.clear(componentId);
		}

		//Retrieve a new EntityData object matching the new set of components.
		EntityData newData = world.getEntityData(newComponentBits);

		e.data = newData;
	}

	@SuppressWarnings("unchecked")
	<T> ComponentMapper<T> getComponentMapper(int componentId) {
		return (ComponentMapper<T>) componentMappers.get(componentId);
	}
}
