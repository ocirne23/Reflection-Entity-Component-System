package recs.systems;

import recs.ComponentMapper;
import recs.EntityTaskSystem;
import recs.components.Position0;
import recs.components.Velocity0;

public class TestThreadedMovementSystem extends EntityTaskSystem {
	private ComponentMapper<Position0> positionManager;
	private ComponentMapper<Velocity0> velocityManager;

	@SuppressWarnings("unchecked")
	public TestThreadedMovementSystem() {
		super(Position0.class, Velocity0.class);
	}

	@Override
	protected void processEntity(int entityId, float deltaInSec) {
		Position0 position = positionManager.get(entityId);
		Velocity0 velocity = velocityManager.get(entityId);

		position.x += velocity.x * deltaInSec;
		position.y += velocity.y * deltaInSec;
	}
}
