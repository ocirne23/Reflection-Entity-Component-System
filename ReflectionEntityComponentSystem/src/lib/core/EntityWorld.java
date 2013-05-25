package lib.core;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;

import lib.utils.Bits;
import lib.utils.IntMap;

public final class EntityWorld {
	private static HashMap<Class<?>, ComponentDef> componentDefs = new HashMap<Class<?>, ComponentDef>();
	private static HashMap<Class<? extends Entity>, EntityDef> entityDefs = new HashMap<Class<? extends Entity>, EntityDef>();
	private static LinkedList<EntitySystem> systems = new LinkedList<EntitySystem>();
	private static IntMap<Entity> entities = new IntMap<Entity>();
	private static Bits entityIds = new Bits();
	private static int componentIdCount = 0;

	public static void process(float deltaInSec) {
		for (EntitySystem system : systems) {
			system.process(deltaInSec);
		}
	}

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

	public static void removeEntity(Entity entity) {
		int id = entity.id;
		EntityDef entityDef = entityDefs.get(entity.getClass());
		for (EntitySystem system : entityDef.usableSystems) {
			system.removeEntity(id);
		}
		for(Class<?> component : entityDef.componentFields.keySet()) {
			componentDefs.get(component).removeComponent(id);
		}
		entityIds.clear(id);
	}

	public static void removeEntity(int id) {
		Entity entity = entities.get(id);
		EntityDef entityDef = entityDefs.get(entity.getClass());
		for (EntitySystem system : entityDef.usableSystems) {
			system.removeEntity(id);
		}
		for(Class<?> component : entityDef.componentFields.keySet()) {
			componentDefs.get(component).removeComponent(id);
		}
		entityIds.clear(id);
	}

	public static void addSystem(EntitySystem system) {
		if (systems.contains(system))
			throw new RuntimeException("System already added");
		if (entities.size != 0)
			throw new RuntimeException("Systems must be added before entities");
		systems.add(system);
	}

	public static void registerComponents(Class<?>... componentClasses) {
		for (Class<?> component : componentClasses) {
			componentDefs.put(component, new ComponentDef(getComponentId()));
		}
	}

	public static <T> T getComponent(int entityId, Class<T> class1) {
		return class1.cast(componentDefs.get(class1).getComponent(entityId));
	}

	protected static int getEntityId() {
		int i = 0;
		while (entityIds.get(i))
			i++;
		entityIds.set(i);
		return i;
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
		for(EntitySystem system: entityDef.usableSystems) {
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

	public static void reset(boolean areYouSure) {
		if(!areYouSure) return;

		componentDefs.clear();
		entityDefs.clear();
		systems.clear();
		entities.clear();
		entityIds.clear();
		componentIdCount = 0;
	}
}
