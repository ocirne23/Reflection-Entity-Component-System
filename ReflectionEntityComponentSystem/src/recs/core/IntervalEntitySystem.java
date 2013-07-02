package recs.core;

public class IntervalEntitySystem extends EntitySystem {
	private float timePassed = 0;
	private float intervalInSec = 0;

	public IntervalEntitySystem(float intervalInSec, Class<?>... components) {
		super(components);
		this.intervalInSec = intervalInSec;
	}

	@Override
	void process(float deltaInSec) {
		timePassed += deltaInSec;
		while(timePassed > intervalInSec) {
			timePassed -= intervalInSec;
			processSystem(intervalInSec);
		}
	}
}