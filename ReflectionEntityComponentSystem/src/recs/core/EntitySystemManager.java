package recs.core;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;

import recs.core.utils.RECSBits;
import recs.core.utils.libgdx.RECSIntMap;

/**
 * Manages Systems and adding and removing entities from systems.
 *
 * @author Enrico van Oosten
 */
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
	 * Remove the entity from the systems using the systembits that are removed
	 * from the newSystemBits compared to the existingSystemBits.
	 *
	 * Give null to newSystemBits to remove the entity from all its systems.
	 */
	void removeEntityFromRemovedSystems(Entity entity, RECSBits existingSystemBits, RECSBits newSystemBits) {
		RECSBits removedSystemBits;
		if (newSystemBits == null)
			removedSystemBits = existingSystemBits;
		else
			removedSystemBits = existingSystemBits.getRemovedBits(newSystemBits);

		for (int i = removedSystemBits.nextSetBit(0); i >= 0; i = removedSystemBits.nextSetBit(i + 1)) {
			systemMap.get(i).removeEntity(entity.id);
		}
	}

	/**
	 * Add an entity to the systems so it can be processed.
	 *
	 * Adds to the systems in the newSystemBits but not in the
	 * existingSystemBits. Give null to existingSystemBits to add the entity to
	 * every system in newSystemBits.
	 */
	void addEntityToNewSystems(Entity entity, RECSBits existingSystemBits, RECSBits newSystemBits) {
		RECSBits addedSystemBits;
		if (existingSystemBits != null)
			addedSystemBits = existingSystemBits.getAddedBits(newSystemBits);
		else
			addedSystemBits = newSystemBits;

		for (int i = addedSystemBits.nextSetBit(0); i >= 0; i = addedSystemBits.nextSetBit(i + 1)) {
			systemMap.get(i).addEntity(entity.id);
		}
	}

	/**
	 * Add a system to the world.
	 */
	@SuppressWarnings("unchecked")
	void addSystem(EntitySystem system) {
		if (systems.contains(system))
			throw new RuntimeException("System already added");
		system.id = getNewSystemId();
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

	/**
	 * Process all the systems with the given delta.
	 */
	void process(float deltaInSec) {
		for (EntitySystem system : systems) {
			if (system.isEnabled()) {
				system.processSystem(deltaInSec);
			}
		}
	}

	/**
	 * Get the system bits matching the given componentbits.
	 */
	RECSBits getSystemBits(RECSBits componentBits) {
		RECSBits systemBits = new RECSBits();
		for (EntitySystem s : systems) {
			if (s.getComponentBits().contains(componentBits)) {
				systemBits.set(s.id);
			}
		}
		return systemBits;
	}

	int getNewSystemId() {
		return ++systemIdCounter;
	}

	void clear() {
		systems.clear();
		systemMap.clear();
		systemIdCounter = 0;
	}
}
