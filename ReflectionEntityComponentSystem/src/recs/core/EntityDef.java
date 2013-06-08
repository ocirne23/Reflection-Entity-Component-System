package recs.core;

import recs.core.utils.RECSBits;

public class EntityDef {
	final RECSBits componentBits;
	final RECSBits systemBits;

	public EntityDef(RECSBits componentBits, RECSBits systemBits) {
		this.componentBits = componentBits;
		this.systemBits = systemBits;
	}
}
