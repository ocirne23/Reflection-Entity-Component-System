package recs.systems;

import recs.ComponentMapper;
import recs.EntitySystem;
import recs.components.Position0;
import recs.components.Velocity0;

public class TestMovementSystem extends EntitySystem {
	private ComponentMapper<Position0> positionMapper;
	private ComponentMapper<Velocity0> velocityMapper;

	@SuppressWarnings("unchecked")
	public TestMovementSystem() {
		super(Position0.class, Velocity0.class);
	}

	@Override
	public void processEntity(int entityId, float deltaInSec) {
		Position0 position = positionMapper.get(entityId);
		Velocity0 velocity = velocityMapper.get(entityId);

		position.x += velocity.x * deltaInSec;
		position.y += velocity.y * deltaInSec;

	}
}