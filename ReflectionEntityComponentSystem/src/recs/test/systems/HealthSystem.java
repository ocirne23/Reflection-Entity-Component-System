package recs.test.systems;

import recs.core.ComponentMapper;
import recs.core.EntitySystem;
import recs.core.EventListener;
import recs.test.components.Health;
import recs.test.events.DamageEvent;

public class HealthSystem extends EntitySystem {
	public ComponentMapper<Health> healthManager;

	public EventListener<DamageEvent> damageListener;

	public HealthSystem() {
		super(Health.class);
	}

	@Override
	protected void processSystem(float deltaInSec) {
		for(DamageEvent damageEvent: damageListener.pollEvents()) {
			Health health = healthManager.get(damageEvent.entityId);
			health.amount -= damageEvent.damage;
		}
		super.processSystem(deltaInSec);
	}

	@Override
	protected void processEntity(int entityId, float deltaInSec) {
		Health health = healthManager.get(entityId);
		if (health.amount <= 0) {
			world.removeEntity(entityId);
		}
	}
}