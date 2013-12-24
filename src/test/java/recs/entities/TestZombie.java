package recs.entities;

import recs.Entity;
import recs.components.Position0;
import recs.components.Velocity0;

public class TestZombie extends Entity {
	Position0 position;
	Velocity0 velocity;
	public TestZombie(float x, float y) {
		position = new Position0(x, y);
		velocity = new Velocity0(1, 2);
	}
}
