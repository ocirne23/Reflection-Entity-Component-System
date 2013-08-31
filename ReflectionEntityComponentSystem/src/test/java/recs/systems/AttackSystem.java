package test.java.recs.systems;

import main.java.recs.IntervalEntitySystem;
import main.java.recs.utils.libgdx.RECSMathUtils;
import test.java.recs.components.Attack;
import test.java.recs.events.DamageEvent;

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
