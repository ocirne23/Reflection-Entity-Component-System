package recs.test.systems;

import recs.core.ComponentManager;
import recs.core.EntitySystem;
import recs.test.components.Position;
import recs.test.components.Velocity;

public class MovementSystem extends EntitySystem {
	private ComponentManager<Position> positionManager;
	private ComponentManager<Velocity> velocityManager;

	public MovementSystem() {
		super(Position.class, Velocity.class);
	}

	@Override
	public void process(int entityId, float deltaInSec) {
		Position position = positionManager.get(entityId);
		Velocity velocity = velocityManager.get(entityId);
		position.x += velocity.x * deltaInSec;
		position.y += velocity.y * deltaInSec;
	}
}