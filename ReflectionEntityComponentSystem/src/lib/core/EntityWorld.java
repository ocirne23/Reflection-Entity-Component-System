package lib.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;

import lib.utils.Bits;
import lib.utils.IntMap;

/**
 * The main world class containing all the logic. Add Entities with components
 * and systems to this class and call the process method.
 *
 * @author Enrico van Oosten
 */
public final class EntityWorld {
	private static HashMap<Class<?>, ComponentDef> componentDefs = new HashMap<Class<?>, ComponentDef>();
	private static HashMap<Class<? extends Entity>, EntityDef> entityDefs = new HashMap<Class<? extends Entity>, EntityDef>();
	private static LinkedList<EntitySystem> systems = new LinkedList<EntitySystem>();
	private static IntMap<Entity> entities = new IntMap<Entity>();
	private static Bits entityIds = new Bits();
	private static int lastUsedId = 0;
	private static int numFreedIds = 0;
	private static int componentIdCount = 0;

	/**
	 * Process all the systems.
	 *
	 * @param deltaInSec
	 *            The time passed in seconds since the last update.
	 */
	public static void process(float deltaInSec) {
		for (EntitySystem system : systems) {
			system.process(deltaInSec);
		}
	}

	/**
	 * Add an entity to the world. The fields in the Entity child class should
	 * be its components.
	 *
	 * @param entity
	 *            The entity.
	 */
	public static void createEntity(Entity entity) {
		Class<? extends Entity> entityClass = entity.getClass();

		if (!entityDefs.containsKey(entityClass)) {
			addNewEntityDef(entityClass);
			addUsableSystems(entityDefs.get(entityClass));
		}

		int id = entity.id;
		EntityDef entityDef = entityDefs.get(entityClass);
		for (Class<?> componentClass : entityDef.componentFields.keySet()) {
			Field field = entityDef.componentFields.get(componentClass);
			ComponentDef def = componentDefs.get(componentClass);

			Object contents = null;
			try {
				contents = field.get(entity);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
			def.addComponent(id, contents);
		}
		addEntityToSystems(entityDef, entity);
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
		EntityDef entityDef = entityDefs.get(entity.getClass());
		for (EntitySystem system : entityDef.usableSystems) {
			system.removeEntity(id);
		}
		for (Class<?> component : entityDef.componentFields.keySet()) {
			componentDefs.get(component).removeComponent(id);
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
	 * Add a system to the world.
	 *
	 * @param system
	 *            The system.
	 */
	public static void addSystem(EntitySystem system) {
		if (systems.contains(system))
			throw new RuntimeException("System already added");
		if (entities.size != 0)
			throw new RuntimeException("Systems must be added before entities");
		systems.add(system);
	}

	/**
	 * Register all the component classes that are being used here.
	 * { Health.class, Position.class } etc.
	 *
	 * @param componentClasses
	 *            A list of component classes.
	 */
	public static void registerComponents(Class<?>... componentClasses) {
		for (Class<?> component : componentClasses) {
			componentDefs.put(component, new ComponentDef(getComponentId()));
		}
	}

	/**
	 * Get a component of an entity.
	 *
	 * @param entityId
	 *            The id of the entity.
	 * @param class1
	 *            The type of that component
	 * @return The component
	 */
	public static <T> T getComponent(int entityId, Class<T> class1) {
		return class1.cast(componentDefs.get(class1).getComponent(entityId));
	}

	protected static int getEntityId() {
		if(numFreedIds > entityIds.numBits() * 0.2f)
			lastUsedId = 0;
		while (entityIds.get(lastUsedId))
		entityIds.set(lastUsedId);
		if(numFreedIds > 0) numFreedIds--;

		return lastUsedId++;
	}

	protected static int getComponentId() {
		return componentIdCount++;
	}

	private static void addNewEntityDef(Class<? extends Entity> class1) {
		HashMap<Class<?>, Field> fieldMap = new HashMap<Class<?>, Field>();
		for (Field f : class1.getDeclaredFields()) {
			Class<?> fieldClass = f.getType();
			if (componentDefs.containsKey(fieldClass)) {
				f.setAccessible(true);
				fieldMap.put(fieldClass, f);
			}
		}
		entityDefs.put(class1, new EntityDef(fieldMap));
	}

	private static void addEntityToSystems(EntityDef entityDef, Entity entity) {
		for (EntitySystem system : entityDef.usableSystems) {
			system.addEntity(entity.id);
		}
	}

	private static void addUsableSystems(EntityDef entityDef) {
		for (EntitySystem system : systems) {
			boolean canProcess = true;
			for (Class<?> component : system.getComponents()) {
				if (!entityDef.hasComponent(component)) {
					canProcess = false;
					break;
				}
			}
			if (canProcess) {
				entityDef.addUsableSystem(system);
			}
		}
	}

	/**
	 * Use this to clear everything in the EntityWorld. Use with care.
	 */
	public static void reset() {
		componentDefs.clear();
		entityDefs.clear();
		systems.clear();
		entities.clear();
		entityIds.clear();
		componentIdCount = 0;
		lastUsedId = 0;
		numFreedIds = 0;
	}
}
