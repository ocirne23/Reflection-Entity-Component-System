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
 *
 * @author Enrico van Oosten
 */
public final class EntityWorld {
	private final EntitySystemManager systemManager;
	private final ComponentManager componentManager;
	private final EntityDefManager defManager;
	private final EventManager eventManager;
	private final RECSObjectMap<Class<?>, ComponentDestructionListener> destructionListeners;
	private final RECSIntMap<Entity> addedEntities;
	private final RECSBits entityIds;
	private int lastUsedId = 0;
	private int numFreedIds = 0;

	private static BlockingThreadPoolExecutor threads = new BlockingThreadPoolExecutor(2, 10);
	private static RECSObjectMap<Entity, LinkedList<Object>> scheduledAdds = new RECSObjectMap<Entity, LinkedList<Object>>();
	private static RECSObjectMap<Entity, LinkedList<Object>> scheduledRemoves = new RECSObjectMap<Entity, LinkedList<Object>>();

	public EntityWorld() {
		systemManager = new EntitySystemManager(this);
		componentManager = new ComponentManager(this);
		defManager = new EntityDefManager(this);
		eventManager = new EventManager();

		destructionListeners = new RECSObjectMap<Class<?>, ComponentDestructionListener>();
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

	public void addEntity(Entity entity) {
		Class<? extends Entity> entityClass = entity.getClass();
		int id = getEntityId();
		entity.id = id;
		addedEntities.put(id, entity);

		EntityReflection reflection = defManager.getReflection(entityClass);
		if (reflection == null)
			reflection = defManager.addNewEntityReflection(entity.getClass());
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
		if (scheduleAddList != null)
			addComp(entity, scheduleAddList.toArray());
		LinkedList<Object> scheduledRemovesList = scheduledRemoves.remove(entity);
		if (scheduledRemovesList != null)
			removeComp(entity, scheduledRemovesList.toArray());
		addEntityToSystems(entity);
	}

	public Entity removeEntity(int entityId) {
		numFreedIds++;
		removeEntityFromSystem(entityId);
		removeEntityFromMappers(entityId);

		entityIds.clear(entityId);
		return addedEntities.remove(entityId);
	}

	public Entity getEntity(int entityId) {
		return addedEntities.get(entityId);
	}

	public void addSystem(EntitySystem system) {
		systemManager.addSystem(system);
	}

	/**
	 * Register all the component classes that are being used here. {
	 * Health.class, Position.class } etc.
	 *
	 * @param componentClasses
	 *            A list of component classes.
	 */
	public void registerComponents(Class<?>... componentClasses) {
		componentManager.registerComponents(componentClasses);
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
	public <T> T getComponent(int entityId, Class<T> componentClass) {
		return componentManager.getComponent(entityId, componentClass);
	}

	public <T> ComponentMapper<T> getComponentMapper(Class<T> componentClass) {
		return componentManager.getComponentMapper(componentClass);
	}

	public <T> ComponentMapper<T> getComponentMapper(int componentId) {
		return componentManager.getComponentMapper(componentId);
	}

	public void registerDestuctionListener(ComponentDestructionListener listener, Class<?> componentClass) {
		destructionListeners.put(componentClass, listener);
	}

	public ComponentDestructionListener getDestructionListener(Class<?> componentClass) {
		return destructionListeners.get(componentClass);
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
		defManager.clear();
		destructionListeners.clear();
		entityIds.clear();
		eventManager.clear();
		lastUsedId = 0;
		numFreedIds = 0;
		System.gc();
	}

	static void addComponent(Entity e, Object... components) {
		if (e.def == null) {
			LinkedList<Object> scheduled = scheduledAdds.get(e);
			if (scheduled == null) {
				scheduled = new LinkedList<Object>();
				scheduledAdds.put(e, scheduled);
			}
			for (Object o : components)
				scheduled.add(o);
		} else
			e.def.world.addComp(e, components);
	}

	static void removeComponent(Entity e, Object... components) {
		if (e.def == null) {
			LinkedList<Object> scheduled = scheduledRemoves.get(e);
			if (scheduled == null) {
				scheduled = new LinkedList<Object>();
				scheduledRemoves.put(e, scheduled);
			}
			for (Object o : components)
				scheduled.add(o);
		} else
			e.def.world.removeComp(e, components);
	}

	RECSBits getComponentBits(Class<?>[] components) {
		RECSBits bits = new RECSBits();
		for (Class<?> c : components)
			bits.set(getComponentId(c));
		return bits;
	}

	void addComp(Entity e, Object... components) {
		EntityDef def = e.def;
		RECSBits componentBits = new RECSBits();
		RECSBits systemBits = new RECSBits();
		if (def != null)
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

	void removeComp(Entity e, Object... components) {
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

	private void removeEntityFromMappers(int id) {
		componentManager.removeEntityFromMappers(id);
	}

	private void addToSystems(Entity entity, RECSBits existingSystemBits, RECSBits newSystemBits) {
		systemManager.addToSystems(entity, existingSystemBits, newSystemBits);
	}

	private void removeFromSystems(Entity entity, RECSBits existingSystemBits, RECSBits newSystemBits) {
		systemManager.removeFromSystems(entity, existingSystemBits, newSystemBits);
	}

	private void addEntityToSystems(Entity entity) {
		systemManager.addEntityToSystems(entity);
	}

	private void removeEntityFromSystem(int id) {
		systemManager.removeEntityFromSystems(id);
	}
}
