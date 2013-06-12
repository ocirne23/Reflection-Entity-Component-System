package recs.core;

import java.lang.reflect.Field;
import java.util.LinkedList;

import recs.core.utils.BlockingThreadPoolExecutor;
import recs.core.utils.RECSBits;
import recs.core.utils.RECSIntMap;
import recs.core.utils.RECSIntMap.Keys;
import recs.core.utils.RECSObjectMap;

/**
 * The main world class containing all the logic. Add Entities with components
 * and systems to this class and call the process method. </br>
 * //////////////////////////Public Methods////////////////////////////////</br>
 * registerComponents(Class<?>[] components) -> register the classes that serve
 * as components. </br> addSystem(EntitySystem system) -> add a system to the
 * world. </br> addEntity(Entity entity) -> add an entity to the systems. </br>
 * process(float deltaInSec) -> process all the systems. </br>
 * removeEntity(Entity entity) -> remove an entity from the systems. </br>
 * getEntity(int id) -> retrieve an entity using it's id. </br> getComponent(int
 * entityId, Class<?> componentClass) -> get the component of a type from an
 * entity with the given id. The preferred method of retrieving components is by
 * using ComponentMapper<>
 *
 * @author Enrico van Oosten
 */
public final class EntityWorld {
	private EntitySystemManager systemManager = new EntitySystemManager(this);
	private ComponentManager componentManager = new ComponentManager(this);
	private EntityDefManager defManager = new EntityDefManager(this);
	private EventManager eventManager = new EventManager();
	private RECSObjectMap<Class<?>, ComponentDestructionListener> destructionListeners = new RECSObjectMap<Class<?>, ComponentDestructionListener>();
	private RECSIntMap<Entity> addedEntities = new RECSIntMap<Entity>();
	private RECSBits entityIds = new RECSBits();
	private int lastUsedId = 0;
	private int numFreedIds = 0;

	private static BlockingThreadPoolExecutor threads = new BlockingThreadPoolExecutor(2, 10);
	private static RECSObjectMap<Entity, LinkedList<Object>> scheduledAdds = new RECSObjectMap<Entity, LinkedList<Object>>();
	private static RECSObjectMap<Entity, LinkedList<Object>> scheduledRemoves = new RECSObjectMap<Entity, LinkedList<Object>>();

	static void addComp(Entity e, Object... comps) {
		if(e.def == null) {
			LinkedList<Object> scheduled = scheduledAdds.get(e);
			if(scheduled == null) {
				scheduled = new LinkedList<Object>();
				scheduledAdds.put(e, scheduled);
			}
			for(Object o: comps)
				scheduled.add(o);
		} else {
			e.def.world.addComponent(e, comps);
		}
	}

	static void removeComp(Entity e, Object... comps) {
		if(e.def == null) {
			LinkedList<Object> scheduled = scheduledRemoves.get(e);
			if(scheduled == null) {
				scheduled = new LinkedList<Object>();
				scheduledRemoves.put(e, scheduled);
			}
			for(Object o: comps)
				scheduled.add(o);
		} else {
			e.def.world.removeComponent(e, comps);
		}
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
	 * Add an entity to the world. The fields in the Entity child class should
	 * be its components.
	 *
	 * @param entity
	 *            The entity.
	 */
	public void addEntity(Entity entity) {
		Class<? extends Entity> entityClass = entity.getClass();
		int id = getEntityId();
		entity.id = id;
		addedEntities.put(id, entity);

		EntityReflection reflection = defManager.getReflection(entityClass);
		if(reflection == null) {
			reflection = defManager.addNewEntityReflection(entity.getClass());
		}
		entity.def = reflection.def;
		Keys k = reflection.componentFields.keys();
		while (k.hasNext) {
			int next = k.next();
			Field field = reflection.componentFields.get(next);
			ComponentMapper<?> componentManager = getComponentMapper(next);
			try {
				componentManager.add(id, field.get(entity));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}

		LinkedList<Object> scheduleAddList = scheduledAdds.remove(entity);
		if (scheduleAddList != null) {
			addComponent(entity, scheduleAddList.toArray());
		}
		LinkedList<Object> scheduledRemovesList = scheduledRemoves.remove(entity);
		if (scheduledRemovesList != null) {
			removeComponent(entity, scheduledRemovesList.toArray());
		}
		addEntityToSystems(entity);
	}

	/**
	 * Remove an entity from the world.
	 *
	 * @param entity
	 *            The entity.
	 */
	public Entity removeEntity(int id) {
		numFreedIds++;
		removeEntityFromSystem(id);
		removeEntityFromMappers(id);

		entityIds.clear(id);
		return addedEntities.remove(id);
	}

	public Entity getEntity(int id) {
		return addedEntities.get(id);
	}

	public void addSystem(EntitySystem system) {
		systemManager.addSystem(system);
	}

	/**
	 * Register all the component classes that are being used here. {
	 * Health.class, Position.class } etc.
	 *
	 * @param <T>
	 *
	 * @param componentClasses
	 *            A list of component classes.
	 */
	public <T> void registerComponents(Class<?>... componentClasses) {
		componentManager.registerComponents(componentClasses);
	}

	/**
	 * Get a component of an entity. Always use a ComponentMapper<T> instead of
	 * this to retrieve components.
	 *
	 * @param entityId
	 *            The id of the entity.
	 * @param class1
	 *            The type of that component
	 * @return The component
	 */
	public <T> T getComponent(int entityId, Class<T> class1) {
		return componentManager.getComponent(entityId, class1);
	}

	/**
	 * Get a manager for a type of component so you can more easily retrieve
	 * this type of components from entities.
	 *
	 * @param class1
	 *            The component's class.
	 * @return The component manager, or null if none exists (component not
	 *         registered).
	 */
	public <T> ComponentMapper<T> getComponentMapper(Class<T> class1) {
		return componentManager.getComponentMapper(class1);
	}

	public <T> ComponentMapper<T> getComponentMapper(int componentId) {
		return componentManager.getComponentMapper(componentId);
	}

	public void registerDestuctionListener(ComponentDestructionListener listener, Class<?> componentClass) {
		destructionListeners.put(componentClass, listener);
	}

	public ComponentDestructionListener getDestructionListener(Class<?> class1) {
		return destructionListeners.get(class1);
	}

	/**
	 * Send a message to all EntitySystems that are registered to the tag of
	 * this event.
	 *
	 * @param event
	 *            The event.
	 */
	public void sendEvent(Object event) {
		eventManager.sendEvent(event);
	}

	/**
	 * Use this to clear everything in the EntityWorld. Use with care.
	 */
	public void reset() {
		addedEntities.clear();
		componentManager.clear();
		systemManager.clear();
		defManager.clear();
		destructionListeners.clear();
		scheduledAdds.clear();
		scheduledRemoves.clear();
		entityIds.clear();
		eventManager.clear();
		lastUsedId = 0;
		numFreedIds = 0;
		System.gc();
	}

	void createEntity(Entity e) {
		Class<? extends Entity> entityClass = e.getClass();
		EntityReflection reflection = defManager.getReflection(entityClass);
		if (reflection == null) {
			reflection = addNewEntityReflection(entityClass);
		}
		e.def = reflection.def;
	}

	void addComponent(Entity e, Object... components) {
		EntityDef def = e.def;
		RECSBits componentBits = new RECSBits();
		RECSBits systemBits = new RECSBits();
		if(def != null)
			componentBits.copy(def.componentBits);
		for (Object component : components) {
			int componentId = getComponentId(component.getClass());
			ComponentMapper<?> mapper = getComponentMapper(componentId);
			if (mapper == null)
				throw new RuntimeException("Unregistered component added: " + component.getClass().getName());
			mapper.add(e.id, component);
			componentBits.set(componentId);
		}
		EntityDef newDef = defManager.getDef(componentBits);
		if (newDef == null) {
			systemBits = getSystemBits(componentBits);
			newDef = new EntityDef(this, componentBits, systemBits);
			defManager.putDef(componentBits, newDef);
		}
		e.def = newDef;
		addToSystems(e, def.systemBits, newDef.systemBits);
	}

	void removeComponent(Entity e, Object... components) {
		EntityDef def = e.def;
		RECSBits componentBits = new RECSBits();
		RECSBits systemBits = new RECSBits();
		componentBits.copy(def.componentBits);
		for (Object component : components) {
			int componentId = getComponentId(component.getClass());
			getComponentMapper(componentId).remove(e.id);
			componentBits.clear(componentId);
		}
		EntityDef newDef = defManager.getDef(componentBits);
		if (newDef == null) {
			systemBits = getSystemBits(componentBits);
			newDef = new EntityDef(this, componentBits, systemBits);
			defManager.putDef(componentBits, def);
		}
		e.def = def;
		removeFromSystems(e, def.systemBits, newDef.systemBits);
	}

	RECSBits getSystemBits(RECSBits componentBits) {
		return systemManager.getSystemBits(componentBits);
	}

	int getSystemId() {
		return systemManager.getSystemId();
	}

	/**
	 * Get an unique id for an entity, may reuse id's of removed entities.
	 *
	 * @return
	 */
	int getEntityId() {
		if (numFreedIds > entityIds.numBits() * 0.2f) {
			lastUsedId = 0;
			numFreedIds = 0;
		}
		while (entityIds.get(++lastUsedId))
			entityIds.set(lastUsedId);
		return lastUsedId;
	}

	int getComponentId(Class<?> component) {
		return componentManager.getComponentId(component);
	}

	/**
	 * Register a system so it can receive events with the specified
	 * messageTags.
	 *
	 * @param system
	 *            The entitySystem.
	 * @param messageTags
	 *            The tags listened to.
	 */
	void registerEventListener(EventListener<?> listener, Class<?> eventType) {
		eventManager.registerListener(listener, eventType);
	}

	static void postRunnable(Runnable r) {
		threads.execute(r);
	}

	private void removeEntityFromMappers(int id) {
		componentManager.removeEntityFromMappers(id);
	}

	private void addToSystems(Entity entity, RECSBits existingSystemBits, RECSBits newSystemBits) {
		systemManager.addToSystems(entity, existingSystemBits, newSystemBits);
	}

	private void removeFromSystems(Entity entity, RECSBits existingSystemBits, RECSBits newSystemBits) {
		systemManager.removeFromSystems(entity, existingSystemBits, newSystemBits);
	}

	/**
	 * Create a new EntityManager representing an entity.
	 *
	 * @param class1
	 *            The entity's class.
	 * @return
	 */
	private EntityReflection addNewEntityReflection(Class<? extends Entity> class1) {
		return defManager.addNewEntityReflection(class1);
	}

	/**
	 * Adds an entity to the systems it can be processed.
	 *
	 * @param entityManager
	 *            The EntityManager of the same type as the entity.
	 * @param entity
	 *            The entity.
	 */
	private void addEntityToSystems(Entity entity) {
		systemManager.addEntityToSystems(entity);
	}

	private void removeEntityFromSystem(int id) {
		systemManager.removeEntityFromSystems(id);
	}

	public RECSBits getComponentBits(Class<?>[] components) {
		RECSBits bits = new RECSBits();
		for(Class<?> c: components) {
			bits.set(getComponentId(c));
		}
		return bits;
	}
}
