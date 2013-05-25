package lib.test.systems;

import lib.core.EntitySystem;
import lib.core.EntityWorld;
import lib.test.components.Health;
import lib.utils.IntArray;
import lib.utils.MathUtils;

public class HealthSystem extends EntitySystem {
	public HealthSystem() {
		super(Health.class);
	}

	@Override
	protected void processEntities(IntArray entities, float deltaInSec) {
		for(int i = 0; i < entities.size; i++) {
			processEntity(entities.items[i], deltaInSec);
		}
	}
	private void processEntity(int entityId, float deltaInSec) {
		Health health = EntityWorld.getComponent(entityId, Health.class);
		if(MathUtils.random() <= deltaInSec * 3f) health.health--;
		System.out.println("processed health, entity: " + entityId + " health: " + health.health);
		if(health.health <= 0) {
			EntityWorld.removeEntity(entityId);
			System.out.println("removing entity: " + entityId);
		}
	}
}

