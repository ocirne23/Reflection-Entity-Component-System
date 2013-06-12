package recs.test.systems;

import recs.core.EntitySystem;
import recs.core.utils.RECSMathUtils;
import recs.test.components.Attack;
import recs.test.events.DamageEvent;

public class AttackSystem extends EntitySystem {
	public AttackSystem() {
		super(1, Attack.class);
	}

	@Override
	protected void process(int entityId, float deltaInSec) {
		//send damage message 10% chance.
		int random = RECSMathUtils.random(0, 10);
		if(random == 0) {
			world.sendEvent(new DamageEvent(entityId, 1));
		}
	}
}
