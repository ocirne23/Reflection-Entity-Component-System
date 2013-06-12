package recs.core;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import recs.core.utils.RECSBits;
import recs.core.utils.RECSIntSet;
import recs.core.utils.RECSIntSet.Items;

/**
 * Extend this class and add it to the EntityWorld to create a new EntitySystem.
 *
 * @author Enrico van Oosten
 */
public abstract class EntitySystem {
	int id;
	RECSIntSet entitiyIds = new RECSIntSet(16);
	RECSBits componentBits;
	Class<?>[] components;

	private boolean enabled = true;
	private final LinkedBlockingQueue<Object> receivedEvents = new LinkedBlockingQueue<Object>();
	private final LinkedList<Object> polledEventsList = new LinkedList<Object>();
	private float timeAccumulator = 0f;

	protected float intervalInSeconds = 0f;
	protected EntityWorld world;

	/**
	 * Create an entitysystem that processes entities with the specified
	 * components each process.
	 */
	public EntitySystem(Class<?>... components) {
		this.components = components;
	}

	/**
	 * Create an entitysystem that processes with the given interval in seconds.
	 * (1/60f is 60 times a second.)
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
		Items i = entitiyIds.items();
		// if set interval process with delta.
		if (intervalInSeconds == 0f) {
			while (i.hasNext)
				process(i.next(), deltaInSec);
		} else {
			// else fixed timestep.
			timeAccumulator += deltaInSec;
			if (timeAccumulator >= intervalInSeconds) {
				timeAccumulator -= intervalInSeconds;
				while (i.hasNext)
					process(i.next(), intervalInSeconds);
			}
		}
	}

	protected abstract void process(int id, float deltaSec);

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

	public int getId() {
		return id;
	}

	public void sendMessage(Object message) {
		receivedEvents.add(message);
	}

	protected LinkedList<Object> pollEvents() {
		polledEventsList.clear();
		receivedEvents.drainTo(polledEventsList);
		return polledEventsList;
	}

	RECSBits getComponentBits() {
		return componentBits;
	}

	void addEntity(int id) {
		if (!entitiyIds.contains(id)) {
			entitiyIds.add(id);
		}
	}

	void removeEntity(int id) {
		entitiyIds.remove(id);
	}
}
