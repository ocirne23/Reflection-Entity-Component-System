package recs;

import recs.utils.RECSBits;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectIntMap;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Manages component retrieval, addition and removal for a world.
 * @author Enrico van Oosten
 */
public final class ComponentManager {
	private EntityWorld world;

	/**
	 * Collection of ComponentManagers, managers are retrievable by using the
	 * class id they represent.
	 */
	private IntMap<ComponentMapper<?>> componentMappers = new IntMap<ComponentMapper<?>>();
	/**
	 * Map which links id's to component classes.
	 */
	private ObjectIntMap<Class<? extends Component>> componentIds = new ObjectIntMap<Class<? extends Component>>();
	private int componentIdCounter = 0;
	/**
	 * Map which handles notification of destroyed components.
	 */
	private final ObjectMap<Class<? extends Component>, ComponentDestructionListener<?>> destructionListeners;

	ComponentManager(EntityWorld world) {
		this.world = world;
		destructionListeners = new ObjectMap<Class<? extends Component>, ComponentDestructionListener<? extends Component>>();
	}

	/** Retrieve the id representing the <b>class</b> of a component */
	int getComponentId(Class<? extends Component> component) {
		int id = componentIds.get(component, -1);
		if(id == -1)
			id = registerComponent(component);
		return id;
	}

	/** Get a component with the componentId from the entity with entityId. */
	Component getComponent(int entityId, int componentId) {
		return componentMappers.get(componentId).get(entityId);
	}

	/** Get a component with the given class from the entity with the entityId */
	<T extends Component> T getComponent(int entityId, Class<T> class1) {
		int componentId = componentIds.get(class1, -1);
		if (componentId == -1)
			return null;
		return class1.cast(componentMappers.get(componentId).get(entityId));
	}

	/**
	 * Register a new component
	 * @param componentClass The class of the component
	 * @return The id of the new class
	 */
	<T extends Component> int registerComponent(Class<? extends Component> componentClass) {
		componentIds.put(componentClass, ++componentIdCounter);
		componentMappers.put(componentIdCounter, new ComponentMapper<T>());
		return componentIdCounter;
	}

	/** Retrieve a component mapper for the given component class */
	@SuppressWarnings("unchecked")
	<T extends Component> ComponentMapper<T> getComponentMapper(Class<? extends Component> class1) {
		return (ComponentMapper<T>) componentMappers.get(getComponentId(class1));
	}

	/** Wipe all the data */
	void clear() {
		componentIdCounter = 0;
		componentIds.clear();
		componentMappers.clear();
		destructionListeners.clear();
	}

	/** Register a listener that gets notified when components of the given class are removed from the world */
	void registerDestuctionListener(ComponentDestructionListener<? extends Component> listener, Class<? extends Component> componentClass) {
		destructionListeners.put(componentClass, listener);
	}

	/** Retrieve a listener that gets notified when components of the given class are removed from the world */
	ComponentDestructionListener<?> getDestructionListener(Class<? extends Component> componentClass) {
		return destructionListeners.get(componentClass);
	}

	/**
	 * Removes all the components of an entity from all the component mappers.
	 */
	void removeEntityFromMappers(Entity e) {
		RECSBits componentBits = e.family.componentBits;

		for (int i = componentBits.nextSetBit(0); i >= 0; i = componentBits.nextSetBit(i + 1)) {
			Component removedComponent = componentMappers.get(i).remove(e.id);
			if (removedComponent != null) {
				@SuppressWarnings("unchecked")
				ComponentDestructionListener<Component> listener = world.getDestructionListener(removedComponent.getClass());
				if (listener != null)
					listener.destroyed(removedComponent);
			}
		}
	}

	/**
	 * Add the given components to this entity, updating its Family.
	 * Does not add the entity to the systems yet.
	 */
	void addComponent(Entity e, Component... components) {
		//Copy the old componentBits.
		RECSBits newComponentBits = new RECSBits(e.family.componentBits);

		////Update componentbits.
		//For every component added
		for(Component component: components) {
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
		EntityFamily newData = world.getEntityFamily(newComponentBits);

		e.family = newData;
	}

	/**
	 * Remove the given components from this entity, updating its EntityData.
	 * Does not remove the entity from the systems yet.
	 */
	void removeComponent(Entity e, Component... components) {
		//Copy the old componentBits.
		RECSBits newComponentBits = new RECSBits(e.family.componentBits);

		////Update componentbits.
		//For every component added
		for(Component component: components) {
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
		EntityFamily newData = world.getEntityFamily(newComponentBits);

		e.family = newData;
	}

	@SuppressWarnings("unchecked")
	<T extends Component> ComponentMapper<T> getComponentMapper(int componentId) {
		return (ComponentMapper<T>) componentMappers.get(componentId);
	}
}
