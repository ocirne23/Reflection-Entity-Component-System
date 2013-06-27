package recs.core;

/**
 * A threaded EntitySystem, only use when there is no possible way concurrency
 * problems can occur.
 *
 * @author Enrico van Oosten
 */
public abstract class EntityTaskSystem extends EntitySystem implements Runnable {
	private static final float NANO_TO_SEC = 1 / 1000000000f;
	private float lastTime = 0;

	private boolean useInterval = false;
	private float timePassed = 0;
	private float intervalInSec = 0;

	public EntityTaskSystem(Class<?>... components) {
		super(components);
	}

	public EntityTaskSystem(float intervalInSec, Class<?>... components) {
		super(components);
		this.intervalInSec = intervalInSec;
		useInterval = true;
	}

	@Override
	void process(float deltaInSec) {
		if (useInterval) {
			timePassed += deltaInSec;
			while (timePassed > intervalInSec) {
				timePassed -= intervalInSec;
				EntityWorld.postRunnable(this);
			}
		} else {
			EntityWorld.postRunnable(this);
		}
	}

	@Override
	public void run() {
		if(useInterval) {
			processSystem(intervalInSec);
		} else {
			float currTime = System.nanoTime() * NANO_TO_SEC;
			float deltaInSec = lastTime != 0f ? currTime - lastTime : 0;

			processSystem(deltaInSec);
			lastTime = System.nanoTime() * NANO_TO_SEC;
		}
	}
}
