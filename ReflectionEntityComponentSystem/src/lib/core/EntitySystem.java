package lib.core;


/**
 * Extend this class and add it to the EntityWorld to create a
 *
 * @author Enrico van Oosten
 */
public abstract class EntitySystem {
	/**
	 * Array of entities this system will use.
	 */
	protected final EntityIntArray entitiyIds;
	/**
	 * Collection of the classes of the components this system will use.
	 */
	protected final Class<?>[] components;
	/**
	 * Used by EntityWorld to determine if processSystem should be called.
	 */
	protected boolean enabled = true;

	public EntitySystem(Class<?>... components) {
		this.components = components;
		entitiyIds = new EntityIntArray(false, 16);
	}

	protected void processSystem(float deltaInSec) {
		processEntities(entitiyIds, deltaInSec);
	}

	protected void processEntities(EntityIntArray entities, float deltaInSec) {
		for(int i = 0, max = entities.size; i < max; i++) process(entities.items[i], deltaInSec);
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
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
