package recs.test.systems;

import recs.core.ComponentMapper;
import recs.core.EntitySystem;
import recs.test.components.Position;
import recs.test.components.Velocity;

public class MovementSystem extends EntitySystem {
	private ComponentMapper<Position> positionManager;
	private ComponentMapper<Velocity> velocityManager;

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