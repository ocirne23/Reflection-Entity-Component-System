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
	private static RECSObjectMap<Entity, LinkedList<Object>> scheduledAdds = new RECSObjectMap<Entity, LinkedList<Object>>();
	private static RECSObjectMap<Entity, LinkedList<Object>> scheduledRemoves = new RECSObjectMap<Entity, LinkedList<Object>>();

	/**
	 * Contains all the entities so they can be retrieved with getEntity
	 */
	private final RECSIntMap<Entity> addedEntities;

	/**
	 * Managers sepparate logic.
	 */
	private final EntitySystemManager systemManager;
	private final ComponentManager componentManager;
	private final EntityDefManager defManager;
	private final EventManager eventManager;

	private final RECSObjectMap<Class<?>, ComponentDestructionListener> destructionListeners;

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

	/**
	 * Add an entitiy to the world so it can be processed by the systems.
	 */
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
			componentManager.addComp(entity, scheduleAddList.toArray());
		LinkedList<Object> scheduledRemovesList = scheduledRemoves.remove(entity);
		if (scheduledRemovesList != null)
			componentManager.removeComp(entity, scheduledRemovesList.toArray());
		systemManager.addEntityToNewSystems(entity, null, entity.def.systemBits);
	}

	public Entity removeEntity(int entityId) {
		numFreedIds++;
		Entity e = getEntity(entityId);
		if(e == null)
			throw new RuntimeException("Entity was not added to this world: " + entityId);
		systemManager.removeEntityFromRemovedSystems(e, e.def.systemBits, null);
		componentManager.removeEntityFromMappers(e);

		entityIds.clear(entityId);
		return addedEntities.remove(entityId);
	}

	public Entity getEntity(int entityId) {
		return addedEntities.get(entityId);
	}

	public void addSystem(EntitySystem system) {
		systemManager.addSystem(system);

		if(addedEntities.size != 0) {
			for(Entity e: addedEntities.values()) {
				RECSBits newSystemBits = systemManager.getSystemBits(e.def.componentBits);
				systemManager.addEntityToNewSystems(e, e.def.systemBits, newSystemBits);
				e.def.systemBits.copy(newSystemBits);
			}
		}
	}

	public void removeSystem(EntitySystem system) {
		Items i = system.entitiyIds.items();
		while (i.hasNext)
			system.removeEntity(i.next());
		defManager.removeSystem(system.id);
		systemManager.removeSystem(system);
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
		EntityDef def = e.def;
		if (def == null) {
			LinkedList<Object> scheduled = scheduledAdds.get(e);
			if (scheduled == null) {
				scheduled = new LinkedList<Object>();
				scheduledAdds.put(e, scheduled);
			}
			for (Object o : components)
				scheduled.add(o);
		} else
			def.world.componentManager.addComp(e, components);
	}

	static void removeComponent(Entity e, Object... components) {
		EntityDef def = e.def;
		if (def == null) {
			LinkedList<Object> scheduled = scheduledRemoves.get(e);
			if (scheduled == null) {
				scheduled = new LinkedList<Object>();
				scheduledRemoves.put(e, scheduled);
			}
			for (Object o : components)
				scheduled.add(o);
		} else
			def.world.componentManager.removeComp(e, components);
	}

	RECSBits getComponentBits(Class<?>[] components) {
		RECSBits bits = new RECSBits();
		for (Class<?> c : components)
			bits.set(getComponentId(c));
		return bits;
	}

	RECSBits getSystemBits(RECSBits componentBits) {
		return systemManager.getSystemBits(componentBits);
	}

	int getSystemId() {
		return systemManager.getNewSystemId();
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

	void addToSystems(Entity entity, RECSBits existingSystemBits, RECSBits newSystemBits) {
		systemManager.addEntityToNewSystems(entity, existingSystemBits, newSystemBits);
	}

	void removeFromSystems(Entity entity, RECSBits existingSystemBits, RECSBits newSystemBits) {
		systemManager.removeEntityFromRemovedSystems(entity, existingSystemBits, newSystemBits);
	}

	EntityDef getDef(RECSBits componentBits) {
		return defManager.getDef(componentBits);
	}

	void putDef(RECSBits componentBits, EntityDef def) {
		defManager.putDef(componentBits, def);
	}
}
