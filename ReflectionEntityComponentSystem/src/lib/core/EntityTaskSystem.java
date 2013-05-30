package lib.core;

public abstract class EntityTaskSystem extends EntitySystem implements Runnable {
	private static final float NANO_TO_SEC = 1/1000000000f;
	private float lastTime = 0;

	public EntityTaskSystem(Class<?>... components) {
		super(components);
	}

	@Override
	protected void processSystem(float deltaInSec) {
		EntityWorld.postRunnable(this);
	}

	@Override
	public void run() {
		float currTime = System.nanoTime() * NANO_TO_SEC;
		float deltaInSec = lastTime != 0f ? currTime - lastTime: 0;
		lastTime = System.nanoTime() * NANO_TO_SEC;;

		processEntities(entitiyIds, deltaInSec);
	}
}
