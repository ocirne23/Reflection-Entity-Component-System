package recs.core;

import recs.core.utils.RECSBits;

public final class EntityDef {
	final EntityWorld world;
	final RECSBits componentBits;
	final RECSBits systemBits;

	EntityDef(EntityWorld world, RECSBits componentBits, RECSBits systemBits) {
		this.world = world;
		this.componentBits = componentBits;
		this.systemBits = systemBits;
	}
}
