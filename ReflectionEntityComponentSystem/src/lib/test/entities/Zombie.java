package lib.test.entities;

import lib.core.Entity;
import lib.test.components.Position;
import lib.test.components.Velocity;

public class Zombie extends Entity {
	Position position;
	Velocity velocity;
	public Zombie(float x, float y) {
		position = new Position(x, y);
		velocity = new Velocity(1, 2);
	}
}
