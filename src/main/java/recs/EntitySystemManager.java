package recs;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;

import recs.utils.RECSBits;

import com.badlogic.gdx.utils.IntMap;


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
	private IntMap<EntitySystem> systemMap = new IntMap<EntitySystem>();
	private EntityWorld world;
	private RECSBits freeSystemBits = new RECSBits();

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
	 * Add the entity to all the systems in the given systembits;
	 */
	void addEntityToSystems(Entity e, RECSBits systemBits) {
		for (int i = systemBits.nextSetBit(0); i >= 0; i = systemBits.nextSetBit(i + 1)) {
			systemMap.get(i).addEntity(e.id);
		}
	}

	/**
	 * Remove the entity from all the systems in the given systembits;
	 */
	void removeEntityFromSystems(Entity e, RECSBits systemBits) {
		for (int i = systemBits.nextSetBit(0); i >= 0; i = systemBits.nextSetBit(i + 1)) {
			systemMap.get(i).removeEntity(e.id);
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
						field.set(system, world.getComponentMapper((Class<? extends Component>) type));
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
					@SuppressWarnings("rawtypes")
					EventListener<? extends Event> eventListener = new EventListener();
					world.registerEventListener(eventListener, (Class<? extends Event>) type);

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
		system.world = world;
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
		int i = 0;
		for (; i < freeSystemBits.numBits(); i++) {
			if (!freeSystemBits.get(i)) {
				freeSystemBits.set(i);
				return i;
			}
		}
		freeSystemBits.set(i + 1);
		return i + 1;
	}

	void clear() {
		systems.clear();
		systemMap.clear();
		freeSystemBits.clear();
	}

	public void removeSystem(EntitySystem system) {
		freeSystemBits.clear(system.id);
		systems.remove(system);
		systemMap.remove(system.id);
		system.clear();
	}
}
