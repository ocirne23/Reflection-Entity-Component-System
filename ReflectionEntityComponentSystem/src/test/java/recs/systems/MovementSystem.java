package test.java.recs.systems;

import main.java.recs.ComponentMapper;
import main.java.recs.EntitySystem;
import test.java.recs.components.Position;
import test.java.recs.components.Velocity;

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