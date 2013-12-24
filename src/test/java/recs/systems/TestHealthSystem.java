package recs.systems;

import recs.ComponentMapper;
import recs.EntitySystem;
import recs.EventListener;
import recs.components.Health0;
import recs.events.TestDamageEvent;

public class TestHealthSystem extends EntitySystem {
	public ComponentMapper<Health0> healthManager;

	public EventListener<TestDamageEvent> damageListener;

	@SuppressWarnings("unchecked")
	public TestHealthSystem() {
		super(Health0.class);
	}

	@Override
	protected void processSystem(float deltaInSec) {
		for(TestDamageEvent damageEvent: damageListener.pollEvents()) {
			Health0 health = healthManager.get(damageEvent.entityId);
			health.amount -= damageEvent.damage;
		}
		super.processSystem(deltaInSec);
	}

	@Override
	protected void processEntity(int entityId, float deltaInSec) {
		Health0 health = healthManager.get(entityId);
		if (health.amount <= 0) {
			world.removeEntity(entityId);
		}
	}
}