package recs.core;

public abstract class ComponentDestructionListener {
	private EntityWorld world;
	public ComponentDestructionListener(EntityWorld world) {
		this.world = world;
	}
	public ComponentDestructionListener(Class<?> componentType) {
		world.registerDestuctionListener(this, componentType);
	}

	public abstract void destroyed(Object object);
}
