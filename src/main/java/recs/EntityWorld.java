package recs;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import recs.utils.BlockingThreadPoolExecutor;
import recs.utils.RECSBits;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Keys;
import com.badlogic.gdx.utils.IntSet.IntSetIterator;
import com.badlogic.gdx.utils.ObjectMap;


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
	private static ObjectMap<Entity, List<Component>> scheduledAdds = new ObjectMap<Entity, List<Component>>();
	private static ObjectMap<Entity, List<Component>> scheduledRemoves = new ObjectMap<Entity, List<Component>>();

	/**
	 * Contains all the entities so they can be retrieved with getEntity
	 */
	private final IntMap<Entity> addedEntities;

	/**
	 * Managers separate logic.
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

		addedEntities = new IntMap<Entity>();
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
	 *
	 * @return
	 * 		returns the id assigned to the entity.
	 */
	public int addEntity(Entity entity) {
		int id = getNewEntityId();
		entity.id = id;
		addedEntities.put(id, entity);

		Class<? extends Entity> entityClass = entity.getClass();
		// Read reflection data and use it to add all the components that were
		// declared as fields.
		EntityReflectionCache reflection = entitydataManager.getReflection(entityClass);
		entity.family = reflection.family;

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
		List<Component> scheduleAddList = scheduledAdds.remove(entity);
		if (scheduleAddList != null) {
			componentManager.addComponent(entity, scheduleAddList.toArray(new Component[0]));
		}
		// Remove all the components that were removed from the entity before
		// the entity was added to the world.
		List<Component> scheduledRemovesList = scheduledRemoves.remove(entity);
		if (scheduledRemovesList != null) {
			componentManager.removeComponent(entity, scheduledRemovesList.toArray(new Component[0]));
		}

		//Add the entity to the systems.
		systemManager.addEntityToSystems(entity, entity.family.systemBits);

		return id;
	}

	public Entity removeEntity(int entityId) {
		numFreedIds++;
		Entity e = getEntity(entityId);
		if (e == null)
			throw new RuntimeException("Entity was not added to this world: " + entityId);
		//remove the entity from all its systems.
		systemManager.removeEntityFromSystems(e, e.family.systemBits);
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

	public IntMap<Entity> getAddedEntities() {
		return addedEntities;
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
			RECSBits newSystemBits = systemManager.getSystemBits(e.family.componentBits);
			RECSBits oldSystemBits = e.family.systemBits;

			//Compare the new systembits with the old systembits and add the entity to all
			//the systems that were added for the entity.
			RECSBits addedSystemBits = oldSystemBits.getAddedBits(newSystemBits);
			systemManager.addEntityToSystems(e, addedSystemBits);

			//Set the old systembits to the new one.
			e.family.systemBits.copy(newSystemBits);
		}
	}

	/**
	 * Remove an EntitySystem from the world
	 */
	public void removeSystem(EntitySystem system) {
		IntSetIterator i = system.entityIds.iterator();
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
	public Component getComponent(int entityId, int componentId) {
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

	/** Return a list of components that were added to an entity before it was added to a world */
	static List<Component> getScheduledAdds(Entity e) {
		List<Component> scheduledAdds = EntityWorld.scheduledAdds.get(e);
		if (scheduledAdds == null) {
			return Collections.unmodifiableList(new ArrayList<Component>());
		} else {
			return Collections.unmodifiableList(scheduledAdds);
		}
	}

	/** Return a list of components that were removed from an entity before it was added to a world */
	static List<Component> getScheduledRemoves(Entity e) {
		List<Component> scheduledRemoves = EntityWorld.scheduledRemoves.get(e);
		if (scheduledRemoves == null) {
			return Collections.unmodifiableList(new ArrayList<Component>());
		} else {
			return Collections.unmodifiableList(scheduledRemoves);
		}
	}

	/**
	 * Add components to an entity, updating its Family and adding it to the new systems.
	 */
	static void addComponent(Entity e, Component... components) {
		EntityFamily oldData = e.family;

		//If an entity is not yet added to a world, its EntityData is null
		if (oldData == null) {
			//Schedule the component for adding so its added once the entity is added to a world.
			List<Component> scheduled = scheduledAdds.get(e);
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
			EntityFamily newData = e.family;
			RECSBits newSystemBits = newData.systemBits;
			RECSBits addedSystemBits = oldSystemBits.getAddedBits(newSystemBits);

			world.systemManager.addEntityToSystems(e, addedSystemBits);
		}
	}

	/**
	 * Remove components to an entity, updating its EntityData and removing it from the old systems.
	 */
	static void removeComponent(Entity e, Component... components) {
		EntityFamily oldData = e.family;

		//If an entity is not yet added to a world, its EntityData is null
		if (oldData == null) {
			//Schedule the component for removal so its removed once the entity is added to a world.
			List<Component> scheduled = scheduledRemoves.get(e);
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
			EntityFamily newData = e.family;
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
		do {
			lastUsedId++;
		} while (entityIds.get(lastUsedId));
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
	 *  See {@link EntityDataManager#getEntityFamily(RECSBits) EntityDataManager.getEntityFamily(RECSBits componentBits)}
	 */
	EntityFamily getEntityFamily(RECSBits componentBits) {
		return entitydataManager.getEntityFamily(componentBits);
	}
}
