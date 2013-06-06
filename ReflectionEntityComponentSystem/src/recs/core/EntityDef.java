package recs.core;

import recs.core.utils.RECSBits;

public class EntityDef {
	protected RECSBits componentBits;
	protected RECSBits systemBits;

	public EntityDef(RECSBits componentBits, RECSBits systemBits) {
		this.componentBits = componentBits;
		this.systemBits = systemBits;
	}
}
