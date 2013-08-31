package recs.systems;

import recs.ComponentMapper;
import recs.EntitySystem;
import recs.components.Position;
import recs.components.Velocity;

public class MovementSystem extends EntitySystem {
	private ComponentMapper<Position> positionMapper;
	private ComponentMapper<Velocity> velocityMapper;

	@SuppressWarnings("unchecked")
	public MovementSystem() {
		super(Position.class, Velocity.class);
	}

	@Override
	public void processEntity(int entityId, float deltaInSec) {
		Position position = positionMapper.get(entityId);
		Velocity velocity = velocityMapper.get(entityId);
		position.x += velocity.x * deltaInSec;
		position.y += velocity.y * deltaInSec;
	}
}