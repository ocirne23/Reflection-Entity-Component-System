package recs.core;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import recs.core.utils.RECSIntArray;

/**
 * Extend this class and add it to the EntityWorld to create a new EntitySystem.
 *
 * @author Enrico van Oosten
 */
public abstract class EntitySystem {
	/**
	 * Array of entities this system will use.
	 */
	protected final RECSIntArray entitiyIds;
	/**
	 * Collection of the classes of the components this system will use.
	 */
	protected final Class<?>[] components;
	/**
	 * Used by EntityWorld to determine if processSystem should be called.
	 */
	private boolean enabled = true;
	private final LinkedBlockingQueue<Object> receivedEvents;
	private final LinkedList<Object> polledEventsList;
	private float intervalInSeconds = 0f;
	private float timeAccumulator = 0f;

	/**
	 *
	 * @param components
	 */
	public EntitySystem(Class<?>... components) {
		this.components = components;
		receivedEvents = new LinkedBlockingQueue<Object>();
		polledEventsList = new LinkedList<Object>();
		entitiyIds = new RECSIntArray(false, 16);
	}

	/**
	 * Create an entitysystem that processes with the given interval. (1/60f) is
	 * 60 times a second.
	 *
	 * @param intervalInSeconds
	 *            The interval in seconds.
	 * @param components
	 *            The components this system uses.
	 */
	public EntitySystem(float intervalInSeconds, Class<?>... components) {
		this(components);
		this.intervalInSeconds = intervalInSeconds;
	}

	/**
	 * Iterate all the entities and call process() for each.
	 *
	 * @param deltaInSec
	 *            The time that has passed in seconds since last update.
	 */
	protected void processSystem(float deltaInSec) {
		// if set interval process with delta.
		if (intervalInSeconds == 0f) {
			for (int i = 0, max = entitiyIds.size; i < max; i++)
				process(entitiyIds.items[i], deltaInSec);
			// else fixed timestep.
		} else {
			timeAccumulator += deltaInSec;
			if (timeAccumulator >= intervalInSeconds) {
				timeAccumulator -= intervalInSeconds;

				for (int i = 0, max = entitiyIds.size; i < max; i++)
					process(entitiyIds.items[i], intervalInSeconds);
			}
		}
	}

	protected abstract void process(int entityId, float deltaInSec);

	protected Class<?>[] getComponents() {
		return components;
	}

	protected void addEntity(int id) {
		entitiyIds.add(id);
	}

	protected void removeEntity(int id) {
		entitiyIds.removeValue(id);
	}

	/**
	 * Set if this system should be processed by the world.
	 *
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean hasEntity(int entityId) {
		return entitiyIds.contains(entityId);
	}

	protected LinkedList<Object> pollEvents() {
		polledEventsList.clear();
		receivedEvents.drainTo(polledEventsList);
		return polledEventsList;
	}

	public void sendMessage(Object message) {
		receivedEvents.add(message);
	}

	public boolean hasComponent(Class<?> component) {
		for (Class<?> c : components) {
			if (c == component)
				return true;
		}
		return false;
	}
}
