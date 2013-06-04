package recs.core;

import java.util.LinkedList;

import recs.core.utils.RECSIntArray;

public class EntityDef {
	protected RECSIntArray components;
	protected LinkedList<EntitySystem> systems;

	public EntityDef(RECSIntArray components, LinkedList<EntitySystem> usableSystems) {
		this.components = components;
		this.systems = usableSystems;
	}

	public EntityDef() {
		components = new RECSIntArray();
		systems = new LinkedList<EntitySystem>();
	}
}
