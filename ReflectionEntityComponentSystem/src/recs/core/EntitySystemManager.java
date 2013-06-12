package recs.core;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;

import recs.core.utils.RECSBits;
import recs.core.utils.RECSIntMap;

public final class EntitySystemManager {
	/**
	 * Linked list of EntitySystems for easy iteration.
	 */
	private LinkedList<EntitySystem> systems = new LinkedList<EntitySystem>();
	private RECSIntMap<EntitySystem> systemMap = new RECSIntMap<EntitySystem>();
	private EntityWorld world;
	private int systemIdCounter = 0;

	EntitySystemManager(EntityWorld world) {
		this.world = world;
	}

	/**
	 * Add a list of systems to the world
	 *
	 * @param systems
	 */
	void addSystem(EntitySystem... systems) {
		for (EntitySystem s : systems)
			addSystem(s);
	}

	/**
	 * Adds an entity to the systems it can be processed.
	 *
	 * @param entityManager
	 *            The EntityManager of the same type as the entity.
	 * @param entity
	 *            The entity.
	 */
	void addEntityToSystems(Entity entity) {
		RECSBits systemBits = entity.def.systemBits;
		for (int i = systemBits.nextSetBit(0); i >= 0; i = systemBits.nextSetBit(i + 1)) {
			systemMap.get(i).addEntity(entity.id);
		}
	}

	void removeEntityFromSystems(int id) {
		for (EntitySystem system : systems) {
			system.removeEntity(id);
		}
	}

	/**
	 * Add a system to the world.
	 *
	 * @param system
	 *            The system.
	 */
	@SuppressWarnings("unchecked")
	void addSystem(EntitySystem system) {
		if (systems.contains(system))
			throw new RuntimeException("System already added");
		system.id = getSystemId();
		system.componentBits = world.getComponentBits(system.components);

		Class<? extends EntitySystem> class1 = system.getClass();
		do {
			for (Field field : class1.getDeclaredFields()) {
				// Check for ComponentManager declarations.
				if (field.getType() == ComponentMapper.class) {
					field.setAccessible(true);
					// Read the type in the <> of componentmanager
					Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
					try {
						// Set the component manager declaration with the right
						// component manager.
						field.set(system, world.getComponentMapper((Class<?>) type));
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
					world.registerEventListener(eventListener, (Class<?>) type);

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
		systemMap.put(system.id, system);
	}

	void process(float deltaInSec) {
		for (EntitySystem system : systems) {
			if (system.isEnabled()) {
				system.processSystem(deltaInSec);
			}
		}
	}

	RECSBits getSystemBits(RECSBits componentBits) {
		RECSBits systemBits = new RECSBits();
		for (EntitySystem s : systems) {
			if (s.getComponentBits().contains(componentBits)) {
				systemBits.set(s.id);
			}
		}
		return systemBits;
	}

	void removeFromSystems(Entity entity, RECSBits existingSystemBits, RECSBits newSystemBits) {
		RECSBits removedSystemBits = existingSystemBits.getRemovedBits(newSystemBits);
		for (int i = removedSystemBits.nextSetBit(0); i >= 0; i = removedSystemBits.nextSetBit(i + 1)) {
			systemMap.get(i).removeEntity(entity.id);
		}
	}

	void addToSystems(Entity entity, RECSBits existingSystemBits, RECSBits newSystemBits) {
		RECSBits addedSystemBits = existingSystemBits.getAddedBits(newSystemBits);
		for (int i = addedSystemBits.nextSetBit(0); i >= 0; i = addedSystemBits.nextSetBit(i + 1)) {
			systemMap.get(i).addEntity(entity.id);
		}
	}

	int getSystemId() {
		return ++systemIdCounter;
	}

	void clear() {
		systems.clear();
		systemMap.clear();
		systemIdCounter = 0;
	}
}
