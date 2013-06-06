package recs.test.systems;

import recs.core.ComponentManager;
import recs.core.EntityTaskSystem;
import recs.test.components.Position;
import recs.test.components.Velocity;

public class ThreadedMovementSystem extends EntityTaskSystem {
	private ComponentManager<Position> positionManager;
	private ComponentManager<Velocity> velocityManager;

	public ThreadedMovementSystem() {
		super(Position.class, Velocity.class);
	}

	@Override
	protected void process(int entityId, float deltaInSec) {
		Position position = positionManager.get(entityId);
		Velocity velocity = velocityManager.get(entityId);

		position.x += velocity.x * deltaInSec;
		position.y += velocity.y * deltaInSec;
	}
}
