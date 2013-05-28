package lib.test.systems;

import lib.core.EntityIntArray;
import lib.core.EntitySystem;
import lib.core.EntityWorld;
import lib.test.components.Position;
import lib.test.components.Velocity;

public class MovementSystem extends EntitySystem {
	public MovementSystem() {
		super(Position.class, Velocity.class);
	}

	@Override
	protected void processEntities(EntityIntArray entities, float deltaInSec) {
		//System.out.println("processing MovementSystem, nr entities; " + entities.size);
		for(int i = 0; i < entities.size; i++) {
			processEntity(entities.items[i], deltaInSec);
		}
	}

	private void processEntity(int entityId, float deltaInSec) {
		Position position = EntityWorld.getComponent(entityId, Position.class);
		Velocity velocity = EntityWorld.getComponent(entityId, Velocity.class);
		position.x += velocity.x * deltaInSec;
		position.y += velocity.y * deltaInSec;

		System.out.println("processed movement, entity: " + entityId + " position: " + position.x + ":" + position.y);
	}
}
