package recs.core;

import java.lang.reflect.Field;
import java.util.LinkedList;

import recs.core.utils.BlockingThreadPoolExecutor;
import recs.core.utils.RECSBits;
import recs.core.utils.RECSIntSet.Items;
import recs.core.utils.libgdx.RECSIntMap;
import recs.core.utils.libgdx.RECSIntMap.Keys;
import recs.core.utils.libgdx.RECSObjectMap;

/**
 * The main world class connecting all the logic. Add Entities with components
 * and systems to this class and call the process method.
 *
 * @author Enrico van Oosten
 */
public final class EntityWorld {
	/**
	 * Global thread pool used by TaskSystems
	 */
	private static BlockingThreadPoolExecutor threads = new BlockingThreadPoolExecutor(2, 10);
	/**
	 * Maps used to temporarily store added/removed components from entities
	 * that are not yet added to the EntityWorld.
	 */
	private static RECSObjectMap<Entity, LinkedList<Component>> scheduledAdds = new RECSObjectMap<Entity, LinkedList<Component>>();
	private static RECSObjectMap<Entity, LinkedList<Component>> scheduledRemoves = new RECSObjectMap<Entity, LinkedList<Component>>();

	/**
	 * Contains all the entities so they can be retrieved with getEntity
	 */
	private final RECSIntMap<Entity> addedEntities;

	/**
	 * Managers sepparate logic.
	 */
	private final EntitySystemManager systemManager;
	private final ComponentManager componentManager;
	private final EntityDataManager entitydataManager;
	private final EventManager eventManager;

	/**
	 * Values used to give entities an unique id.
	 */
	private final RECSBits entityIds;
	private int lastUsedId = 0;
	private int numFreedIds = 0;

	/**
	 * Add Entities with components and systems to this class and call the
	 * process method.
	 */
	public EntityWorld() {
		systemManager = new EntitySystemManager(this);
		componentManager = new ComponentManager(this);
		entitydataManager = new EntityDataManager(this);
		eventManager = new EventManager();

		addedEntities = new RECSIntMap<Entity>();
		entityIds = new RECSBits();
	}

	/**
	 * Process all the systems.
	 *
	 * @param deltaInSec
	 *            The time passed in seconds since the last update.
	 *            EntityTaskSystems are updated independantly of this delta.
	 */
	public void process(float deltaInSec) {
		systemManager.process(deltaInSec);
	}

	/**
	 * Add an entitiy to the world so it can be processed by the systems.
	 */
	public void addEntity(Entity entity) {
		int id = getNewEntityId();
		entity.id = id;
		addedEntities.put(id, entity);

		Class<? extends Entity> entityClass = entity.getClass();
		// Read reflection data and use it to add all the components that were
		// declared as fields.
		EntityReflection reflection = entitydataManager.getReflection(entityClass);
		entity.data = reflection.data;

		if (entity.getClass() != Entity.class) {
			Keys k = reflection.componentFields.keys();
			while (k.hasNext) {
				int next = k.next();
				Field field = reflection.componentFields.get(next);
				ComponentMapper<? extends Component> mapper = componentManager.getComponentMapper(next);
				try {
					mapper.add(id, (Component) field.get(entity));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		// Add all the components that were added to the entity before the
		// entity was added to the world.
		LinkedList<Component> scheduleAddList = scheduledAdds.remove(entity);
		if (scheduleAddList != null) {
			componentManager.addComponent(entity, scheduleAddList.toArray(new Component[0]));
		}
		// Remove all the components that were removed from the entity before
		// the entity was added to the world.
		LinkedList<Component> scheduledRemovesList = scheduledRemoves.remove(entity);
		if (scheduledRemovesList != null) {
			componentManager.removeComponent(entity, scheduledRemovesList.toArray(new Component[0]));
		}

		//Add the entity to the systems.
		systemManager.addEntityToSystems(entity, entity.data.systemBits);
	}

	public Entity removeEntity(int entityId) {
		numFreedIds++;
		Entity e = getEntity(entityId);
		if (e == null)
			throw new RuntimeException("Entity was not added to this world: " + entityId);
		//remove the entity from all its systems.
		systemManager.removeEntityFromSystems(e, e.data.systemBits);
		//Remove all the entities components from the componentmappers.
		componentManager.removeEntityFromMappers(e);

		//Free the entities id for reuse
		entityIds.clear(entityId);
		return addedEntities.remove(entityId);
	}

	/**
	 * Retrieve an entity using its id.
	 */
	public Entity getEntity(int entityId) {
		return addedEntities.get(entityId);
	}

	/**
	 * Add an EntitySystem(s) to the world.
	 */
	public void addSystem(EntitySystem... systems) {
		//register the systems
		for(EntitySystem system: systems)
			systemManager.addSystem(system);

		if (addedEntities.size == 0)
			return;

		//for every entity in the world
		for (Entity e : addedEntities.values()) {
			//Get the updated system bits which include the bits for the new systems
			RECSBits newSystemBits = systemManager.getSystemBits(e.data.componentBits);
			RECSBits oldSystemBits = e.data.systemBits;

			//Compare the new systembits with the old systembits and add the entity to all
			//the systems that were added for the entity.
			RECSBits addedSystemBits = oldSystemBits.getAddedBits(newSystemBits);
			systemManager.addEntityToSystems(e, addedSystemBits);

			//Set the old systembits to the new one.
			e.data.systemBits.copy(newSystemBits);
		}
	}

	/**
	 * Remove an EntitySystem from the world
	 */
	public void removeSystem(EntitySystem system) {
		Items i = system.entityIds.items();
		while (i.hasNext)
			system.removeEntity(i.next());
		entitydataManager.removeSystem(system.id);
		systemManager.removeSystem(system);
	}

	/**
	 * Get a component of an entity. Always use a ComponentMapper<T> instead of
	 * this to retrieve components.
	 *
	 * @param entityId
	 *            The id of the entity.
	 * @param componentClass
	 *            The type of that component
	 * @return The component
	 */
	public <T extends Component> T getComponent(int entityId, Class<T> componentClass) {
		return componentManager.getComponent(entityId, componentClass);
	}

	/**
	 * Get a componentMapper so you can efficiently retrieve components from Entities using
	 * the class of a component.
	 */
	public <T extends Component> ComponentMapper<T> getComponentMapper(Class<T> componentClass) {
		return componentManager.getComponentMapper(componentClass);
	}

	/**
	 * Get a component with the componentId from the entity with entityId.
	 */
	public Object getComponent(int entityId, int componentId) {
		return componentManager.getComponent(entityId, componentId);
	}

	/**
	 * Get a DestructionListener that is notified whenever a component of the
	 * given class is destroyed.
	 */
	@SuppressWarnings("rawtypes")
	public ComponentDestructionListener getDestructionListener(Class<? extends Component> componentClass) {
		return componentManager.getDestructionListener(componentClass);
	}

	/**
	 * Send a message to all EntitySystems that are registered to the tag of
	 * this event.
	 *
	 * @param event
	 *            The event.
	 */
	public void sendEvent(Event event) {
		eventManager.sendEvent(event);
	}

	public static void postRunnable(Runnable task) {
		threads.execute(task);
	}

	/**
	 * Use this to clear everything in the EntityWorld. Use with care.
	 */
	public void reset() {
		addedEntities.clear();
		componentManager.clear();
		systemManager.clear();
		entitydataManager.clear();
		entityIds.clear();
		eventManager.clear();
		lastUsedId = 0;
		numFreedIds = 0;
		System.gc();
	}

	static Object[] getScheduledAdds(Entity e) {
		return scheduledAdds.get(e).toArray();
	}

	/**
	 * Add components to an entity, updating its EntityData and adding it to the new systems.
	 */
	static void addComponent(Entity e, Component... components) {
		EntityData oldData = e.data;

		//If an entity is not yet added to a world, its EntityData is null
		if (oldData == null) {
			//Schedule the component for adding so its added once the entity is added to a world.
			LinkedList<Component> scheduled = scheduledAdds.get(e);
			if (scheduled == null) {
				scheduled = new LinkedList<Component>();
				scheduledAdds.put(e, scheduled);
			}
			for (Component o : components)
				scheduled.add(o);
		//Entity is added to a world.
		} else {
			EntityWorld world = oldData.world;
			//Save the old systemBits;
			RECSBits oldSystemBits = oldData.systemBits;
			world.componentManager.addComponent(e, components);
			//addComponent updates the entities EntityData
			EntityData newData = e.data;
			RECSBits newSystemBits = newData.systemBits;
			RECSBits addedSystemBits = oldSystemBits.getAddedBits(newSystemBits);

			world.systemManager.addEntityToSystems(e, addedSystemBits);
		}
	}

	/**
	 * Remove components to an entity, updating its EntityData and removing it from the old systems.
	 */
	static void removeComponent(Entity e, Component... components) {
		EntityData oldData = e.data;

		//If an entity is not yet added to a world, its EntityData is null
		if (oldData == null) {
			//Schedule the component for removal so its removed once the entity is added to a world.
			LinkedList<Component> scheduled = scheduledRemoves.get(e);
			if (scheduled == null) {
				scheduled = new LinkedList<Component>();
				scheduledRemoves.put(e, scheduled);
			}
			for (Component o : components)
				scheduled.add(o);
		//Entity is added to a world.
		} else {
			EntityWorld world = oldData.world;
			//Save the old systemBits;
			RECSBits oldSystemBits = oldData.systemBits;
			world.componentManager.removeComponent(e, components);
			//removeComponent updates the entities EntityData
			EntityData newData = e.data;
			RECSBits newSystemBits = newData.systemBits;
			RECSBits removedSystemBits = oldSystemBits.getRemovedBits(newSystemBits);

			world.systemManager.removeEntityFromSystems(e, removedSystemBits);
		}
	}

	/**
	 * Get a RECSBits object matching an array of component classes.
	 */
	RECSBits getComponentBits(Class<? extends Component>[] components) {
		RECSBits bits = new RECSBits();
		for (Class<? extends Component> c : components)
			bits.set(getComponentId(c));
		return bits;
	}

	/**
	 * Register a DestructionListener to the world so it receives notifications when a component
	 * of the given class is destroyed.
	 */
	void registerDestuctionListener(ComponentDestructionListener<?> listener, Class<? extends Component> componentClass) {
		componentManager.registerDestuctionListener(listener, componentClass);
	}

	/**
	 * Get the systembits matching the given componentbits.
	 */
	RECSBits getSystemBits(RECSBits componentBits) {
		return systemManager.getSystemBits(componentBits);
	}

	/**
	 * Get the id of a system.
	 */
	int getSystemId() {
		return systemManager.getNewSystemId();
	}

	/**
	 * Get a new EntityId.
	 */
	int getNewEntityId() {
		// if the amount of freed ids is more than 20% of the amount of id's, recycle ids.
		if (numFreedIds > entityIds.numBits() * 0.2f) {
			lastUsedId = 0;
			numFreedIds = 0;
		}
		//Search for a free id.
		while (entityIds.get(++lastUsedId))
			entityIds.set(lastUsedId);
		return lastUsedId;
	}

	/**
	 * Get the id of a component class.
	 */
	int getComponentId(Class<? extends Component> component) {
		return componentManager.getComponentId(component);
	}

	/**
	 * Register a system so it can receive events with the specified type.
	 */
	void registerEventListener(EventListener<? extends Event> listener, Class<? extends Event> eventType) {
		eventManager.registerListener(listener, eventType);
	}

	/**
	 * Retrieve the EntityData matching the given componentbits, creating a new instance
	 * if no match is found.
	 */
	EntityData getEntityData(RECSBits componentBits) {
		EntityData data = entitydataManager.getEntityData(componentBits);

		//If no match for bits is found, create a new instance.
		if (data == null) {
			//Get the systembits matching the componentbits
			RECSBits systemBits = systemManager.getSystemBits(componentBits);
			//Create a new EntityData for the component/system bits.
			data = new EntityData(this, componentBits, systemBits);
			//Add the new EntityData to the datamanager
			entitydataManager.putEntityData(data);
		}

		return data;
	}
}
