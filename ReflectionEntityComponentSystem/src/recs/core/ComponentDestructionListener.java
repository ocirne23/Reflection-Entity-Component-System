package recs.core;

public abstract class ComponentDestructionListener {
	public ComponentDestructionListener(Class<?> componentType) {
		EntityWorld.registerDestuctionListener(this, componentType);
	}

	public abstract void destroyed(Object object);
}
