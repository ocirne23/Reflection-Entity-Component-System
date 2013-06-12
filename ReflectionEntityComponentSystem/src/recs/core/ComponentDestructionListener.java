package recs.core;

public abstract class ComponentDestructionListener {
	public ComponentDestructionListener(EntityWorld world, Class<?> componentType) {
		world.registerDestuctionListener(this, componentType);
	}

	public abstract void destroyed(Object object);
}
