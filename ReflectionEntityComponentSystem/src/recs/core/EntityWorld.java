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
 * and systems to this class and call the process method.
 *
 *  //////////////////////////Public Methods////////////////////////////////
 *  /// registerComponents(Class<?>[] components) -> register the classes that serve as components.
 *  /// addSystem(EntitySystem system) -> add a system to the world.
 *  /// addEntity(Entity entity) -> add an entity to the systems
 *  /// process(float deltaInSec) -> process all the systems
 *  /// removeEntity(Entity entity) -> remove an entity from the systems
 *  /// getEntity(int id) -> retrieve an entity using it's id
 *  /// getComponent(int entityId, Class<?> componentClass) ->
 *  		get the component of a type from an entity with the given id.
 *  		The preferred method of retrieving components is by using ComponentMapper<>
 *
 * @author Enrico van Oosten
 */
public final class EntityWorld {
	private static EntitySystemManager systemManager = new EntitySystemManager();
	private static ComponentManager componentManager = new ComponentManager();
	private static EntityDefManager defManager = new EntityDefManager();
	private static EventManager eventManager = new EventManager();
	private static BlockingThreadPoolExecutor threads = new BlockingThreadPoolExecutor(2, 10);
	private static RECSObjectMap<Class<?>, ComponentDestructionListener> destructionListeners = new RECSObjectMap<Class<?>, ComponentDestructionListener>();
	private static RECSIntMap<Entity> addedEntities = new RECSIntMap<Entity>();
	private static RECSIntMap<LinkedList<Object>> scheduledAddComponents = new RECSIntMap<LinkedList<Object>>();
	private static RECSBits entityIds = new RECSBits();
	private static int lastUsedId = 0;
	private static int numFreedIds = 0;


	/**
	 * Process all the systems.
	 *
	 * @param deltaInSec
	 *            The time passed in seconds since the last update.
	 *            EntityTaskSystems are updated independantly of this delta.
	 */
	public static void process(float deltaInSec) {
		systemManager.process(deltaInSec);
	}

	/**
	 * Add an entity to the world. The fields in the Entity child class should
	 * be its components.
	 *
	 * @param entity
	 *            The entity.
	 */
	public static void addEntity(Entity entity) {
		Class<? extends Entity> entityClass = entity.getClass();
		int id = entity.id;
		addedEntities.put(id, entity);

		EntityReflection reflection = defManager.getReflection(entityClass);
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
		LinkedList<Object> scheduleAddList = scheduledAddComponents.remove(id);
		if (scheduleAddList != null) {
			addComponent(entity, scheduleAddList.toArray());
		}
		addEntityToSystems(entity);
	}

	/**
	 * Remove an entity from the world.
	 *
	 * @param entity
	 *            The entity.
	 */
	public static Entity removeEntity(int id) {
		numFreedIds++;
		removeEntityFromSystem(id);
		removeEntityFromMappers(id);

		entityIds.clear(id);
		return addedEntities.remove(id);
	}

	public static Entity getEntity(int id) {
		return addedEntities.get(id);
	}

	public static void addSystem(EntitySystem system) {
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
	public static <T> void registerComponents(Class<?>... componentClasses) {
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
	public static <T> T getComponent(int entityId, Class<T> class1) {
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
	public static <T> ComponentMapper<T> getComponentMapper(Class<T> class1) {
		return componentManager.getComponentMapper(class1);
	}

	public static <T> ComponentMapper<T> getComponentMapper(int componentId) {
		return componentManager.getComponentMapper(componentId);
	}

	public static void registerDestuctionListener(ComponentDestructionListener listener, Class<?> componentClass) {
		destructionListeners.put(componentClass, listener);
	}

	public static ComponentDestructionListener getDestructionListener(Class<?> class1) {
		return destructionListeners.get(class1);
	}

	/**
	 * Send a message to all EntitySystems that are registered to the tag of
	 * this event.
	 *
	 * @param event
	 *            The event.
	 */
	public static void sendEvent(Object event) {
		eventManager.sendEvent(event);
	}

	/**
	 * Use this to clear everything in the EntityWorld. Use with care.
	 */
	public static void reset() {
		addedEntities.clear();
		componentManager.clear();
		systemManager.clear();
		defManager.clear();
		scheduledAddComponents.clear();
		destructionListeners.clear();
		entityIds.clear();
		eventManager.clear();
		lastUsedId = 0;
		numFreedIds = 0;
		System.gc();
	}

	static int createEntity(Entity e) {
		Class<? extends Entity> entityClass = e.getClass();
		EntityReflection reflection = defManager.getReflection(entityClass);
		if (reflection == null) {
			reflection = addNewEntityReflection(entityClass);
		}
		e.def = reflection.def;
		int id = getEntityId();
		return id;
	}

	static void addComponent(Entity e, Object... components) {
		if (!addedEntities.containsKey(e.id)) {
			scheduleAddComponent(e, components);
			return;
		}
		EntityDef def = e.def;
		RECSBits componentBits = new RECSBits();
		RECSBits systemBits = new RECSBits();
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
			newDef = new EntityDef(componentBits, systemBits);
			defManager.putDef(componentBits, newDef);
		}
		e.def = newDef;
		addToSystems(e, def.systemBits, newDef.systemBits);
	}

	static void removeComponent(Entity e, Object... components) {
		if (!addedEntities.containsKey(e.id)) {
			scheduleRemoveComponent(e, components);
			return;
		}
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
			newDef = new EntityDef(componentBits, systemBits);
			defManager.putDef(componentBits, def);
		}
		e.def = def;
		removeFromSystems(e, def.systemBits, newDef.systemBits);
	}

	static RECSBits getSystemBits(RECSBits componentBits) {
		return systemManager.getSystemBits(componentBits);
	}

	static int getSystemId() {
		return systemManager.getSystemId();
	}

	/**
	 * Get an unique id for an entity, may reuse id's of removed entities.
	 *
	 * @return
	 */
	static int getEntityId() {
		if (numFreedIds > entityIds.numBits() * 0.2f) {
			lastUsedId = 0;
			numFreedIds = 0;
		}
		while (entityIds.get(++lastUsedId))
			entityIds.set(lastUsedId);
		return lastUsedId;
	}

	static int getComponentId(Class<?> component) {
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
	static void registerEventListener(EventListener<?> listener, Class<?> eventType) {
		eventManager.registerListener(listener, eventType);
	}

	static void postRunnable(Runnable r) {
		threads.execute(r);
	}


	private static void removeEntityFromMappers(int id) {
		componentManager.removeEntityFromMappers(id);
	}

	private static void scheduleAddComponent(Entity e, Object... components) {
		LinkedList<Object> scheduleList = scheduledAddComponents.get(e.id);
		if (scheduleList == null) {
			scheduleList = new LinkedList<Object>();
			scheduledAddComponents.put(e.id, scheduleList);
		}
		for (Object o : components)
			scheduleList.add(o);
	}

	private static void scheduleRemoveComponent(Entity e, Object... components) {
		LinkedList<Object> scheduleList = scheduledAddComponents.get(e.id);
		if (scheduleList == null)
			return;
		for (Object o : components)
			scheduleList.remove(o);
	}

	private static void addToSystems(Entity entity, RECSBits existingSystemBits, RECSBits newSystemBits) {
		systemManager.addToSystems(entity, existingSystemBits, newSystemBits);
	}

	private static void removeFromSystems(Entity entity, RECSBits existingSystemBits, RECSBits newSystemBits) {
		systemManager.removeFromSystems(entity, existingSystemBits, newSystemBits);
	}

	/**
	 * Create a new EntityManager representing an entity.
	 *
	 * @param class1
	 *            The entity's class.
	 * @return
	 */
	private static EntityReflection addNewEntityReflection(Class<? extends Entity> class1) {
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
	private static void addEntityToSystems(Entity entity) {
		systemManager.addEntityToSystems(entity);
	}

	private static void removeEntityFromSystem(int id) {
		systemManager.removeEntityFromSystems(id);
	}
}
