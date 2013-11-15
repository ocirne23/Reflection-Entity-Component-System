package recs;

public class IntervalEntitySystem extends EntitySystem {
	private float timePassed = 0;
	private float intervalInSec = 0;

	/**
	 * EntitySystem that gets processed once per interval, world.process() still needs to be called.
	 * @param intervalInSec The deltaInSec that need to pass before this system is processed.
	 * Resets after every process
	 * @param components The components an entity requires to let it be processed by this system.
	 */
	public IntervalEntitySystem(float intervalInSec, Class<? extends Component>... components) {
		super(components);
		this.intervalInSec = intervalInSec;
	}

	@Override
	void process(float deltaInSec) {
		if (intervalInSec == 0f) {
			processSystem(deltaInSec);
		} else {
			timePassed += deltaInSec;
			while(timePassed > intervalInSec) {
				timePassed -= intervalInSec;
				processSystem(intervalInSec);
			}
		}
	}

	public void setInterval(float intervalInSec) {
		this.intervalInSec = intervalInSec;
	}
}
