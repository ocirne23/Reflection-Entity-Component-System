package recs.systems;

import recs.ComponentMapper;
import recs.EntitySystem;
import recs.components.Position_0;
import recs.components.Velocity_0;

public class MovementSystem_Test extends EntitySystem {
	private ComponentMapper<Position_0> positionMapper;
	private ComponentMapper<Velocity_0> velocityMapper;

	@SuppressWarnings("unchecked")
	public MovementSystem_Test() {
		super(Position_0.class, Velocity_0.class);
	}

	@Override
	public void processEntity(int entityId, float deltaInSec) {
		Position_0 position = positionMapper.get(entityId);
		Velocity_0 velocity = velocityMapper.get(entityId);

		position.x += velocity.x * deltaInSec;
		position.y += velocity.y * deltaInSec;

	}
}