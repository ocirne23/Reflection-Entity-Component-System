package recs.systems;

import recs.ComponentMapper;
import recs.EntitySystem;
import recs.EventListener;
import recs.components.Health_0;
import recs.events.DamageEvent_Test;

public class HealthSystem_Test extends EntitySystem {
	public ComponentMapper<Health_0> healthManager;

	public EventListener<DamageEvent_Test> damageListener;

	@SuppressWarnings("unchecked")
	public HealthSystem_Test() {
		super(Health_0.class);
	}

	@Override
	protected void processSystem(float deltaInSec) {
		for(DamageEvent_Test damageEvent: damageListener.pollEvents()) {
			Health_0 health = healthManager.get(damageEvent.entityId);
			health.amount -= damageEvent.damage;
		}
		super.processSystem(deltaInSec);
	}

	@Override
	protected void processEntity(int entityId, float deltaInSec) {
		Health_0 health = healthManager.get(entityId);
		if (health.amount <= 0) {
			world.removeEntity(entityId);
		}
	}
}