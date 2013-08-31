package test.java.recs.systems;

import main.java.recs.ComponentMapper;
import main.java.recs.EntitySystem;
import main.java.recs.EventListener;
import test.java.recs.components.Health;
import test.java.recs.events.DamageEvent;

public class HealthSystem extends EntitySystem {
	public ComponentMapper<Health> healthManager;

	public EventListener<DamageEvent> damageListener;

	@SuppressWarnings("unchecked")
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