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
	protected EntityWorld world;

	int id;
	RECSIntSet entitiyIds = new RECSIntSet(16);
	RECSBits componentBits;
	Class<?>[] components;

	private boolean enabled = true;
	private final LinkedBlockingQueue<Object> receivedEvents = new LinkedBlockingQueue<Object>();
	private final LinkedList<Object> polledEventsList = new LinkedList<Object>();

	/**
	 * Create an entitysystem that processes entities with the specified
	 * components each process.
	 */
	public EntitySystem(Class<?>... components) {
		this.components = components;
	}

	void process(float deltaInSec) {
		processSystem(deltaInSec);
	}

	/**
	 * Iterates all the entities and call process() for each. Override to
	 * only process once per loop.
	 *
	 * @param deltaInSec
	 *            The time that has passed in seconds since last update.
	 */
	protected void processSystem(float deltaInSec) {
		Items i = entitiyIds.items();
		while (i.hasNext)
			processEntity(i.next(), deltaInSec);
	}

	/**
	 * Override to process each entity individually.
	 * @param id The id of the entity.
	 * @param deltaSec Time passed in seconds since the last process.
	 */
	protected void processEntity(int id, float deltaSec) {

	}

	protected RECSIntSet getAllEntities() {
		return entitiyIds;
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

	protected void addEntity(int id) {
		if (!entitiyIds.contains(id)) {
			entitiyIds.add(id);
		}
	}

	protected void removeEntity(int id) {
		entitiyIds.remove(id);
	}

	RECSBits getComponentBits() {
		return componentBits;
	}

	void clear() {
		entitiyIds.clear();
		receivedEvents.clear();
		polledEventsList.clear();
	}
}
