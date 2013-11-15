package recs;

import recs.utils.RECSBits;

/**
 * Class used to store which components an entity has, and what systems it belongs to.
 *
 * Is shared between entities with the same set of components.
 *
 * @author Enrico
 */
public final class EntityFamily {
	final EntityWorld world;
	final RECSBits componentBits;
	final RECSBits systemBits;

	EntityFamily(EntityWorld world, RECSBits componentBits, RECSBits systemBits) {
		this.world = world;
		this.componentBits = componentBits;
		this.systemBits = systemBits;
	}
}
