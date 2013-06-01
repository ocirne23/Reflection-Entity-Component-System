package lib.test.systems;

import lib.core.ComponentManager;
import lib.core.EntitySystem;
import lib.test.components.Position;
import lib.test.components.Velocity;

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