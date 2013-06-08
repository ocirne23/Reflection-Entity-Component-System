package recs.test.systems;

import recs.core.ComponentMapper;
import recs.core.EntitySystem;
import recs.test.components.Position;
import recs.test.components.Velocity;

public class MovementSystem extends EntitySystem {
	private ComponentMapper<Position> positionMapper;
	private ComponentMapper<Velocity> velocityMapper;

	public MovementSystem() {
		super(Position.class, Velocity.class);
	}

	@Override
	public void process(int entityId, float deltaInSec) {
		Position position = positionMapper.get(entityId);
		Velocity velocity = velocityMapper.get(entityId);
		position.x += velocity.x * deltaInSec;
		position.y += velocity.y * deltaInSec;
	}
}