package lib.test.systems;

import lib.core.EntityMathUtils;
import lib.core.EntitySystem;
import lib.core.EntityWorld;
import lib.test.components.Health;

public class HealthSystem extends EntitySystem {
	public HealthSystem() {
		super(Health.class);
	}

	@Override
	protected void process(int entityId, float deltaInSec) {
		Health health = EntityWorld.getComponent(entityId, Health.class);
		if (EntityMathUtils.random() <= deltaInSec * 3f)
			health.health--;
		System.out.println("processed health, entity: " + entityId + " health: " + health.health);
		if (health.health <= 0) {
			EntityWorld.removeEntity(entityId);
			System.out.println("removing entity: " + entityId);
		}
	}
}