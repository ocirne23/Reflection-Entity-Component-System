package recs.test.systems;

import recs.core.ComponentManager;
import recs.core.EntitySystem;
import recs.core.EntityWorld;
import recs.core.EventListener;
import recs.test.components.Health;
import recs.test.events.DamageEvent;

public class HealthSystem extends EntitySystem {
	public ComponentManager<Health> healthManager;

	public EventListener<DamageEvent> damageListener;

	public HealthSystem() {
		super(Health.class);
	}

	@Override
	protected void processSystem(float deltaInSec) {
		for(DamageEvent damageEvent: damageListener.pollEvents()) {
			Health health = healthManager.get(damageEvent.entityId);
			health.health -= damageEvent.damage;
		}
		super.processSystem(deltaInSec);
	}

	@Override
	protected void process(int entityId, float deltaInSec) {
		Health health = healthManager.get(entityId);
		if (health.health <= 0) {
			EntityWorld.removeEntity(entityId);
		}
	}
}