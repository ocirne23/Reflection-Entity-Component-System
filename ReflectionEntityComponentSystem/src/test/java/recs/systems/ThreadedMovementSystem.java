package test.java.recs.systems;

import main.java.recs.ComponentMapper;
import main.java.recs.EntityTaskSystem;
import test.java.recs.components.Position;
import test.java.recs.components.Velocity;

public class ThreadedMovementSystem extends EntityTaskSystem {
	private ComponentMapper<Position> positionManager;
	private ComponentMapper<Velocity> velocityManager;

	@SuppressWarnings("unchecked")
	public ThreadedMovementSystem() {
		super(Position.class, Velocity.class);
	}

	@Override
	protected void processEntity(int entityId, float deltaInSec) {
		Position position = positionManager.get(entityId);
		Velocity velocity = velocityManager.get(entityId);

		position.x += velocity.x * deltaInSec;
		position.y += velocity.y * deltaInSec;
	}
}
