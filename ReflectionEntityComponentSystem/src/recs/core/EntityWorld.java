package recs.core;

import java.lang.reflect.Field;
import java.util.LinkedList;

import recs.core.utils.BlockingThreadPoolExecutor;
import recs.core.utils.RECSBits;
import recs.core.utils.RECSIntMap;
import recs.core.utils.RECSIntMap.Keys;
import recs.core.utils.RECSIntSet;
import recs.core.utils.RECSObjectMap;

/**
 * The main world class containing all the logic. Add Entities with components
 * and systems to this class and call the process method.
 *
 * @author Enrico van Oosten
 */
public final class EntityWorld {
	private static EntitySystemManager systemManager = new EntitySystemManager();
	private static ComponentManager componentManager = new ComponentManager();
	private static EntityDefManager defManager = new EntityDefManager();
	private static EventManager eventManager = new EventManager();

	/**
	 * Collection of EntityManagers, managers are retrievable by using the class
	 * they represent.
	 */
	private static RECSIntSet addedEntities = new RECSIntSet();
	/**
	 * Bitset used to know which id's are used.
	 */
	private static RECSBits entityIds = new RECSBits();
	private static int lastUsedId = 0;
	private static int numFreedIds = 0;

	private static RECSIntMap<LinkedList<Object>> scheduledAddComponents = new RECSIntMap<LinkedList<Object>>();

	public static int createEntity(Entity e) {
		Class<? extends Entity> entityClass = e.getClass();
		EntityReflection reflection = defManager.getReflection(entityClass);
		if (reflection == null) {
			reflection = addNewEntityReflection(entityClass);
		}
		e.def = reflection.def;
		return getEntityId();
	}

	public static void addComponent(Entity e, Object... components) {
		if (!addedEntities.contains(e.id)) {
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
			defManager.putDef(componentBits, def);
		}
		e.def = newDef;
		addToSystems(e, def.systemBits, newDef.systemBits);
	}

	public static void removeComponent(Entity e, Object... components) {
		if (!addedEntities.contains(e.id)) {
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
		System.out.println("after: " + componentBits.binaryString());
		EntityDef newDef = defManager.getDef(componentBits);
		if (newDef == null) {
			systemBits = getSystemBits(componentBits);
			newDef = new EntityDef(componentBits, systemBits);
			defManager.putDef(componentBits, def);
		}
		e.def = def;
		removeFromSystems(e, def.systemBits, newDef.systemBits);
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

	static RECSBits getSystemBits(RECSBits componentBits) {
		return systemManager.getSystemBits(componentBits);
	}

	public static int getSystemId() {
		return systemManager.getSystemId();
	}

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

	protected static EntityReflection getEntityReflection(Class<? extends Entity> class1) {
		return defManager.getReflection(class1);
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
		addedEntities.add(id);

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
		LinkedList<Object> scheduleAddList = scheduledAddComponents.get(id);
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
	public static void removeEntity(int id) {
		numFreedIds++;
		addedEntities.remove(id);
		removeEntityFromSystem(id);
		removeEntityFromMappers(id);

		entityIds.clear(id);
	}

	public static void removeEntityFromMappers(int id) {
		componentManager.removeEntityFromMappers(id);
	}

	/**
	 * Get an unique id for an entity, may reuse id's of removed entities.
	 *
	 * @return
	 */
	protected static int getEntityId() {
		if (numFreedIds > entityIds.numBits() * 0.2f) {
			lastUsedId = 0;
			numFreedIds = 0;
		}
		while (entityIds.get(++lastUsedId))
			entityIds.set(lastUsedId);
		return lastUsedId;
	}

	protected static void returnEntityId(int id) {
		numFreedIds++;
		entityIds.clear(id);
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

	public static void addSystem(EntitySystem system) {
		systemManager.addSystem(system);
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

	protected static int getComponentId(Class<?> component) {
		return componentManager.getComponentId(component);
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

	/**
	 * Map of destructionListeners which get notified if a component of their
	 * type is destroyed.
	 */
	private static RECSObjectMap<Class<?>, ComponentDestructionListener> destructionListeners = new RECSObjectMap<Class<?>, ComponentDestructionListener>();
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
	 * Register a system so it can receive events with the specified
	 * messageTags.
	 *
	 * @param system
	 *            The entitySystem.
	 * @param messageTags
	 *            The tags listened to.
	 */
	protected static void registerEventListener(EventListener<?> listener, Class<?> eventType) {
		eventManager.registerListener(listener, eventType);
	}

	private static BlockingThreadPoolExecutor threads = new BlockingThreadPoolExecutor(2, 10);

	protected static void postRunnable(Runnable r) {
		threads.execute(r);
	}

	/**
	 * Use this to clear everything in the EntityWorld. Use with care.
	 */
	public static void reset() {
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
}
