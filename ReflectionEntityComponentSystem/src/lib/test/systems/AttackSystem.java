package lib.test.systems;

import lib.core.EntitySystem;
import lib.test.components.Attack;
import lib.utils.IntArray;

public class AttackSystem extends EntitySystem {
	public AttackSystem() {
		super(Attack.class);
	}

	@Override
	protected void processEntities(IntArray entities, float deltaInSec) {

	}

}
