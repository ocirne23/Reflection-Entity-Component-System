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
	public final int id;
	/**
	 * Array of entities this system will use.
	 */
	final RECSIntSet entitiyIds;
	/**
	 * Collection of the classes of the components this system will use.
	 */
	final RECSBits componentBits;
	/**
	 * Used by EntityWorld to determine if processSystem should be called.
	 */
	private boolean enabled = true;
	private final LinkedBlockingQueue<Object> receivedEvents;
	private final LinkedList<Object> polledEventsList;

	protected float intervalInSeconds = 0f;
	private float timeAccumulator = 0f;

	/**
	 *
	 * @param components
	 */
	public EntitySystem(Class<?>... components) {
		componentBits = new RECSBits();
		for (Class<?> class1 : components) {
			componentBits.set(EntityWorld.getComponentId(class1));
		}
		id = EntityWorld.getSystemId();
		receivedEvents = new LinkedBlockingQueue<Object>();
		polledEventsList = new LinkedList<Object>();
		entitiyIds = new RECSIntSet(16);
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

	protected abstract void process(int entityId, float deltaInSec);

	protected RECSBits getComponentBits() {
		return componentBits;
	}

	protected void addEntity(int id) {
		if (!entitiyIds.contains(id)) {
			entitiyIds.add(id);
		}
	}

	protected void removeEntity(int id) {
		entitiyIds.remove(id);
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
}
