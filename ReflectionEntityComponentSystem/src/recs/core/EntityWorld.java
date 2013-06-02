package recs.core;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;

import recs.core.utils.BlockingThreadPoolExecutor;
import recs.core.utils.RECSBits;
import recs.core.utils.RECSIntMap;
import recs.core.utils.RECSObjectMap;
import recs.core.utils.RECSObjectMap.Entry;

/**
 * The main world class containing all the logic. Add Entities with components
 * and systems to this class and call the process method.
 *
 * @author Enrico van Oosten
 */
public final class EntityWorld {
	/**
	 * Collection of ComponentManagers, managers are retrievable by using the
	 * class they represent.
	 */
	private static RECSObjectMap<Class<?>, ComponentManager<?>> componentManagers = new RECSObjectMap<Class<?>, ComponentManager<?>>();
	/**
	 * Collection of EntityManagers, managers are retrievable by using the class
	 * they represent.
	 */
	private static RECSObjectMap<Class<? extends Entity>, EntityManager> entityManagers = new RECSObjectMap<Class<? extends Entity>, EntityManager>();
	/**
	 * Map of entities, entities are retrievable using their id.
	 */
	private static RECSIntMap<Entity> entities = new RECSIntMap<Entity>();
	/**
	 * Linked list of EntitySystems for easy iteration.
	 */
	private static LinkedList<EntitySystem> systems = new LinkedList<EntitySystem>();
	/**
	 * Bitset used to know which id's are used.
	 */
	private static RECSBits entityIds = new RECSBits();
	private static int lastUsedId = 0;
	private static int numFreedIds = 0;
	/**
	 * Map of destructionListeners which get notified if a component of their
	 * type is destroyed.
	 */
	private static RECSObjectMap<Class<?>, ComponentDestructionListener> destructionListeners = new RECSObjectMap<Class<?>, ComponentDestructionListener>();
	private static EventManager eventManager = new EventManager();
	private static BlockingThreadPoolExecutor threads = new BlockingThreadPoolExecutor(2, 10);

	/**
	 * Process all the systems.
	 *
	 * @param deltaInSec
	 *            The time passed in seconds since the last update.
	 *            EntityTaskSystems are updated independantly of this delta.
	 */
	public static void process(float deltaInSec) {
		for (EntitySystem system : systems) {
			if (system.isEnabled()) {
				system.processSystem(deltaInSec);
			}
		}
	}

	protected static void postRunnable(Runnable r) {
		threads.execute(r);
	}

	protected static Class<? extends Entity> getManagerForComponents(Class<?>... components) {
		for (Entry<Class<? extends Entity>, EntityManager> o : entityManagers.entries()) {
			if (o.value.hasOnlyTheseComponents(components))
				return o.key;
		}
		return null;
	}

	protected static EntityManager getEntityManager(Class<? extends Entity> class1) {
		return entityManagers.get(class1);
	}

	/**
	 * Add an entity to the world. The fields in the Entity child class should
	 * be its components.
	 *
	 * @param entity
	 *            The entity.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void createEntity(Entity entity) {
		Class<? extends Entity> entityClass = entity.getClass();
		int id = entity.id;

		// If this is the first time this class has been added, create the
		// entity definition.
		if (!entityManagers.containsKey(entityClass)) {
			addNewEntityManager(entityClass);
		}
		EntityManager entityManager = entityManagers.get(entityClass);
		// For every component field in this entity. (read from the
		// entityManager).
		for (Class<?> componentClass : entityManager.componentFields.keySet()) {
			Field field = entityManager.componentFields.get(componentClass);
			ComponentManager componentManager = componentManagers.get(componentClass);

			Object contents = null;
			try {
				// Set contents with the value of the field of the entity.
				contents = field.get(entity);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
			// Add the contents of the field to the right manager.
			componentManager.add(id, contents);
		}
		addEntityToSystems(entityManager, entity);
		entities.put(id, entity);
	}

	/**
	 * Remove an entity from the world.
	 *
	 * @param entity
	 *            The entity.
	 */
	public static void removeEntity(Entity entity) {
		numFreedIds++;
		int id = entity.id;
		for (EntitySystem system : systems) {
			system.removeEntity(id);
		}
		for(ComponentManager manager: componentManagers.values()) {
			manager.remove(id);
		}
		entityIds.clear(id);
	}

	/**
	 * Remove an entity from the world.
	 *
	 * @param id
	 *            The id of an entity.
	 */
	public static void removeEntity(int id) {
		Entity entity = entities.get(id);
		removeEntity(entity);
	}

	/**
	 * Add a list of systems to the world
	 *
	 * @param systems
	 */
	public static void addSystem(EntitySystem... systems) {
		for (EntitySystem s : systems)
			addSystem(s);
	}

	public static void addComponent(Entity e, Object... newComponents) {
		LinkedList<Class<?>> components = new LinkedList<Class<?>>();
		for (ComponentManager<?> componentManager : componentManagers.values()) {
			Object o = componentManager.get(e.id);
			if (o != null) {
				components.add(o.getClass());
			}
		}
		for (Object o : newComponents) {
			if (components.contains(o))
				throw new RuntimeException("Component already added to entity: " + o.getClass());
			components.add(o.getClass());
			componentManagers.get(o.getClass()).add(e.id, o);
		}

		for (EntitySystem system : systems) {
			if (system.hasEntity(e.id))
				continue;
			for (Class<?> component : system.getComponents()) {
				if (!components.contains(component))
					break;
				system.addEntity(e.id);
				// only add once.
				break;
			}
		}
	}

	public static void removeComponent(Entity e, Class<?>... removedComponents) {
		for (Class<?> componentClass : removedComponents) {
			componentManagers.get(componentClass).remove(e.id);
		}
		for (EntitySystem s : systems) {
			for (Class<?> componentClass : removedComponents) {
				if (s.hasComponent(componentClass)) {
					s.removeEntity(e.id);
					break;
				}
			}
		}
	}

	/**
	 * Add a system to the world.
	 *
	 * @param system
	 *            The system.
	 */
	@SuppressWarnings("unchecked")
	public static void addSystem(EntitySystem system) {
		if (systems.contains(system))
			throw new RuntimeException("System already added");
		if (entities.size != 0)
			throw new RuntimeException("Systems must be added before entities");

		// Make sure every component used is registered, else throw exception.
		for (Class<?> component : system.getComponents()) {
			if (!componentManagers.containsKey(component)) {
				throw new RuntimeException("EntitySystem tried to use unregistered component: " + component.getName());
			}
		}

		Class<? extends EntitySystem> class1 = system.getClass();
		do {
			for (Field field : class1.getDeclaredFields()) {
				// Check for ComponentManager declarations.
				if (field.getType() == ComponentManager.class) {
					field.setAccessible(true);
					// Read the type in the <> of componentmanager
					Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
					try {
						// Set the component manager declaration with the right
						// component manager.
						field.set(system, componentManagers.get((Class<?>) type));
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
				// check for EventListener declarations.
				if (field.getType() == EventListener.class) {
					field.setAccessible(true);
					// Read the type in the <> of eventListener.
					Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
					EventListener<?> eventListener = new EventListener<>();
					registerEventListener(eventListener, (Class<?>) type);

					try {
						// Set the event listener declaration with the right
						// field listener.
						field.set(system, eventListener);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				}
			}
			class1 = (Class<? extends EntitySystem>) class1.getSuperclass();
		} while (class1 != EntitySystem.class);
		systems.add(system);
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
		for (Class<?> component : componentClasses) {
			componentManagers.put(component, new ComponentManager<T>());
		}
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
		return class1.cast(componentManagers.get(class1).get(entityId));
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
	@SuppressWarnings("unchecked")
	public static <T> ComponentManager<T> getComponentManager(Class<T> class1) {
		return (ComponentManager<T>) componentManagers.get(class1);
	}

	/**
	 * Get an unique id for an entity, may reuse id's of removed entities.
	 *
	 * @return
	 */
	protected static int getEntityId() {
		if (numFreedIds > entityIds.numBits() * 0.2f)
			lastUsedId = 0;
		while (entityIds.get(lastUsedId))
			entityIds.set(lastUsedId);
		if (numFreedIds > 0)
			numFreedIds--;
		return lastUsedId++;
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
	 */
	@SuppressWarnings("unchecked")
	private static void addNewEntityManager(Class<? extends Entity> class1) {
		Class<? extends Entity> mainClass = class1;
		HashMap<Class<?>, Field> fieldMap = new HashMap<Class<?>, Field>();
		// Iterate all the subclasses.
		while (class1 != Entity.class) {
			// Put every field object in a map with the fields class as key.
			for (Field f : class1.getDeclaredFields()) {
				Class<?> fieldClass = f.getType();
				if (componentManagers.containsKey(fieldClass)) {
					f.setAccessible(true);
					fieldMap.put(fieldClass, f);
				}
			}
			class1 = (Class<? extends Entity>) class1.getSuperclass();
		}
		EntityManager entityManager = new EntityManager(fieldMap);
		entityManagers.put(mainClass, entityManager);
		addUsableSystems(entityManager);
	}

	/**
	 * Adds an entity to the systems it can be processed.
	 *
	 * @param entityManager
	 *            The EntityManager of the same type as the entity.
	 * @param entity
	 *            The entity.
	 */
	private static void addEntityToSystems(EntityManager entityManager, Entity entity) {
		for (EntitySystem system : entityManager.usableSystems) {
			system.addEntity(entity.id);
		}
	}

	/**
	 * Add all the system the entity class can use to the manager.
	 *
	 * @param entityManager
	 *            The entity manager representing an entity class.
	 */
	private static void addUsableSystems(EntityManager entityManager) {
		for (EntitySystem system : systems) {
			for (Class<?> component : system.getComponents()) {
				// If an entity does not have all the components of a system,
				// don't add the system.
				if (!entityManager.hasComponent(component))
					break;
				entityManager.addUsableSystem(system);
				// only add once.
				break;
			}
		}
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

	public static void registerDestuctionListener(ComponentDestructionListener listener, Class<?> componentClass) {
		destructionListeners.put(componentClass, listener);
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
	private static void registerEventListener(EventListener<?> listener, Class<?> eventType) {
		eventManager.registerListener(listener, eventType);
	}

	/**
	 * Use this to clear everything in the EntityWorld. Use with care.
	 */
	public static void reset() {
		componentManagers.clear();
		entityManagers.clear();
		systems.clear();
		entities.clear();
		entityIds.clear();
		eventManager.clear();
		lastUsedId = 0;
		numFreedIds = 0;
		System.gc();
	}
}
