package lib.test.entities;

import lib.core.Entity;
import lib.test.components.Health;
import lib.test.components.Position;
import lib.test.components.Velocity;

public class Player extends Entity {
	Position position;
	Velocity velocity = new Velocity(2, 1);
	Health health = new Health(10, 15);

	public Player(float x, float y) {
		position = new Position(x, y);
	}
}
