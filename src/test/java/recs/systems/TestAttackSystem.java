package recs.systems;

import recs.IntervalEntitySystem;
import com.badlogic.gdx.math.MathUtils;
import recs.components.Attack0;
import recs.events.TestDamageEvent;

public class TestAttackSystem extends IntervalEntitySystem {
	@SuppressWarnings("unchecked")
	public TestAttackSystem() {
		super(1f, Attack0.class);
	}

	@Override
	protected void processEntity(int entityId, float deltaInSec) {
		//send damage message 10% chance.
		int random = MathUtils.random(0, 10);
		if(random == 0) {
			world.sendEvent(new TestDamageEvent(entityId, 1));
		}
	}
}
