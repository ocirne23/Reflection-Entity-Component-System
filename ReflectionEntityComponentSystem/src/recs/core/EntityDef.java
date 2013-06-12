package recs.core;

import recs.core.utils.RECSBits;

public class EntityDef {
	final EntityWorld world;
	final RECSBits componentBits;
	final RECSBits systemBits;

	public EntityDef(EntityWorld world, RECSBits componentBits, RECSBits systemBits) {
		this.world = world;
		this.componentBits = componentBits;
		this.systemBits = systemBits;
	}
}
