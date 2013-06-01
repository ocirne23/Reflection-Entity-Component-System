package lib.test.systems;

import lib.core.EntitySystem;
import lib.core.EntityWorld;
import lib.core.utils.RECSMathUtils;
import lib.test.components.Attack;
import lib.test.events.DamageEvent;

public class AttackSystem extends EntitySystem {
	public AttackSystem() {
		super(1, Attack.class);
	}

	@Override
	protected void process(int entityId, float deltaInSec) {
		//send damage message 10% chance.
		int random = RECSMathUtils.random(0, 10);
		if(random == 0) {
			EntityWorld.sendEvent(new DamageEvent(entityId, 1));
		}
	}
}
