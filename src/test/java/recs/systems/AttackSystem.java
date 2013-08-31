package recs.systems;

import recs.IntervalEntitySystem;
import com.badlogic.gdx.math.MathUtils;
import recs.components.Attack;
import recs.events.DamageEvent;

public class AttackSystem extends IntervalEntitySystem {
	@SuppressWarnings("unchecked")
	public AttackSystem() {
		super(1f, Attack.class);
	}

	@Override
	protected void processEntity(int entityId, float deltaInSec) {
		//send damage message 10% chance.
		int random = MathUtils.random(0, 10);
		if(random == 0) {
			world.sendEvent(new DamageEvent(entityId, 1));
		}
	}
}
