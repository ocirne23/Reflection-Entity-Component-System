package recs.test.entities;

import recs.core.Entity;
import recs.test.components.Position;
import recs.test.components.Velocity;

public class Zombie extends Entity {
	Position position;
	Velocity velocity;
	public Zombie(float x, float y) {
		position = new Position(x, y);
		velocity = new Velocity(1, 2);
	}
}
