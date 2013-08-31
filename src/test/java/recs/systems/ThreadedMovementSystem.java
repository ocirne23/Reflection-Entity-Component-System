package recs.systems;

import recs.ComponentMapper;
import recs.EntityTaskSystem;
import recs.components.Position;
import recs.components.Velocity;

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
		System.out.println("pos: " + position.x +":"+ position.y);
	}
}
