package lib.test.systems;

import lib.core.EntityIntArray;
import lib.core.EntitySystem;
import lib.test.components.Attack;

public class AttackSystem extends EntitySystem {
	public AttackSystem() {
		super(Attack.class);
	}

	@Override
	protected void processEntities(EntityIntArray entities, float deltaInSec) {

	}

}
