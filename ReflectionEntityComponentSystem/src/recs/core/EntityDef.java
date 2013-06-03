package recs.core;

import java.util.LinkedList;

import recs.core.utils.RECSIntArray;

public class EntityDef {
	public RECSIntArray components;
	public LinkedList<EntitySystem> systems;

	public EntityDef(RECSIntArray components, LinkedList<EntitySystem> usableSystems) {
		this.components = components;
		this.systems = usableSystems;
	}
}
