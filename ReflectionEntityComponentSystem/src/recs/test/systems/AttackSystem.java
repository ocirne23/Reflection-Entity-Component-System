package recs.test.systems;

import recs.core.IntervalEntitySystem;
import recs.core.utils.libgdx.RECSMathUtils;
import recs.test.components.Attack;
import recs.test.events.DamageEvent;

public class AttackSystem extends IntervalEntitySystem {
	@SuppressWarnings("unchecked")
	public AttackSystem() {
		super(1f, Attack.class);
	}

	@Override
	protected void processEntity(int entityId, float deltaInSec) {
		//send damage message 10% chance.
		int random = RECSMathUtils.random(0, 10);
		if(random == 0) {
			world.sendEvent(new DamageEvent(entityId, 1));
		}
	}
}
