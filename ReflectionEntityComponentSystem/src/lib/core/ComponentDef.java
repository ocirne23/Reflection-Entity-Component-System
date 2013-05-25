package lib.core;

import lib.utils.IntMap;

public final class ComponentDef {
	protected final int id;
	protected final IntMap<Object> components;
	protected ComponentDef(int id) {
		this.id = id;
		components = new IntMap<Object>();
	}

	protected void addComponent(int entityId, Object object) {
		components.put(entityId, object);
	}

	protected void removeComponent(int entityId) {
		components.remove(entityId);
	}

	protected Object getComponent(int entityId) {
		return components.get(entityId);
	}
}
