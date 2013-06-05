package recs.core;

import recs.core.utils.RECSBits;

public class EntityDef {
	protected RECSBits componentBits;
	protected RECSBits systemBits;

	public EntityDef(int nrExistingComponents, int nrExistingSystems) {
		componentBits = new RECSBits(nrExistingComponents);
		systemBits = new RECSBits(nrExistingSystems);
	}
}
