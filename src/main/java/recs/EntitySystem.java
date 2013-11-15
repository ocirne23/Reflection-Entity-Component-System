package recs;

import recs.utils.RECSBits;

import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.IntSet.IntSetIterator;

/**
 * Extend this class and add it to the EntityWorld to create a new EntitySystem.
 *
 * Override processEntity to process entities one by one, or override processSystem
 * to do an action once per iteration of the system. (call super on processSystem to do both).
 *
 * @author Enrico van Oosten
 */
public abstract class EntitySystem {
	/**
	 * The world this system belongs to.
	 */
	protected EntityWorld world;

	/**
	 * The id of this system.
	 */
	int id;
	/**
	 * A set of the entityIds of the entities that are being processed by this system.
	 */
	IntSet entityIds;
	/**
	 * A bitset of the components required for an entity to be processed by this system.
	 */
	RECSBits componentBits;
	/**
	 * An array of the classes of the components this system requires.
	 */
	Class<? extends Component>[] components;

	/**
	 * Indicates if this system will be processed by the world or not.
	 */
	private boolean enabled = true;
	private IntSetIterator iterator;

	/**
	 * Create an entitysystem that processes entities with the specified
	 * components each process.
	 */
	public EntitySystem(Class<? extends Component>... components) {
		this.components = components;
		entityIds = new IntSet(16);
		iterator = new IntSetIterator(entityIds);
	}

	/**
	 * Calls processSystem. Interval/task systems override this so they can cleanly
	 * do this part with a runnable and/or delay the process.
	 */
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
		iterator.reset();

		while (iterator.hasNext)
			processEntity(iterator.next(), deltaInSec);
	}

	/**
	 * Override to process each entity individually.
	 * @param id The id of the entity.
	 * @param deltaSec Time passed in seconds since the last process.
	 */
	protected void processEntity(int id, float deltaSec) {

	}

	/**
	 * Return a set of all the ids of all the entities that are being processed by this system.
	 */
	public IntSet getAllEntities() {
		return entityIds;
	}

	/**
	 * Set if this system should be processed by the world.
	 *
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Return true if this system should be processed by the world.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Returns if an entity is being processed by this system.
	 */
	public boolean hasEntity(int entityId) {
		return entityIds.contains(entityId);
	}

	/**
	 * Get the id of this system.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Add an entity to this system so it can be processed.
	 */
	protected void addEntity(int id) {
		if (!entityIds.contains(id)) {
			entityIds.add(id);
		} else {
			throw new RuntimeException("Entity: " + id + " is being added twice, this should not happen");
		}
	}

	/**
	 * Remove an entity from the system.
	 */
	protected void removeEntity(int id) {
		entityIds.remove(id);
	}

	/**
	 * Get the componentbits that this system requires.
	 */
	RECSBits getComponentBits() {
		return componentBits;
	}

	/**
	 * Remove all the entities from this system.
	 */
	void clear() {
		entityIds.clear();
	}
}
