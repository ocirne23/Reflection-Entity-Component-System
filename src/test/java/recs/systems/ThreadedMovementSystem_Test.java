package recs.systems;

import recs.ComponentMapper;
import recs.EntityTaskSystem;
import recs.components.Position_0;
import recs.components.Velocity_0;

public class ThreadedMovementSystem_Test extends EntityTaskSystem {
	private ComponentMapper<Position_0> positionManager;
	private ComponentMapper<Velocity_0> velocityManager;

	@SuppressWarnings("unchecked")
	public ThreadedMovementSystem_Test() {
		super(Position_0.class, Velocity_0.class);
	}

	@Override
	protected void processEntity(int entityId, float deltaInSec) {
		Position_0 position = positionManager.get(entityId);
		Velocity_0 velocity = velocityManager.get(entityId);

		position.x += velocity.x * deltaInSec;
		position.y += velocity.y * deltaInSec;
		System.out.println("pos: " + position.x +":"+ position.y);
	}
}
