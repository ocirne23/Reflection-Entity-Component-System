package recs.systems;

import recs.IntervalEntitySystem;
import com.badlogic.gdx.math.MathUtils;
import recs.components.Attack_0;
import recs.events.DamageEvent_Test;

public class AttackSystem_Test extends IntervalEntitySystem {
	@SuppressWarnings("unchecked")
	public AttackSystem_Test() {
		super(1f, Attack_0.class);
	}

	@Override
	protected void processEntity(int entityId, float deltaInSec) {
		//send damage message 10% chance.
		int random = MathUtils.random(0, 10);
		if(random == 0) {
			world.sendEvent(new DamageEvent_Test(entityId, 1));
		}
	}
}
