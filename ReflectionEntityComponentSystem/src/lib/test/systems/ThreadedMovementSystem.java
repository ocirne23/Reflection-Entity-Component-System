package lib.test.systems;

import lib.core.ComponentManager;
import lib.core.EntityTaskSystem;
import lib.test.components.Position;
import lib.test.components.Velocity;

public class ThreadedMovementSystem extends EntityTaskSystem {
	private ComponentManager<Position> positionManager;
	private ComponentManager<Velocity> velocityManager;

	public ThreadedMovementSystem() {
		super(Position.class, Velocity.class);
	}

	@Override
	protected void process(int entityId, float deltaInSec) {
		System.out.println("processing delta: " + deltaInSec);
		Position position = positionManager.get(entityId);
		Velocity velocity = velocityManager.get(entityId);

		position.x += velocity.x * deltaInSec;
		position.y += velocity.y * deltaInSec;

		System.out.println("processed threaded movement, entity: " + entityId + " position: " + position.x + ":" + position.y);
	}
}
