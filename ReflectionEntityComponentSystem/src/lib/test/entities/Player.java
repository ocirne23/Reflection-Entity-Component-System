package lib.test.entities;

import lib.core.Entity;
import lib.test.components.Health;
import lib.test.components.Position;
import lib.test.components.Velocity;

public class Player extends Entity {
	Health health;
	Position position;
	Velocity velocity = new Velocity(2, 1);

	public Player(float x, float y) {
		position = new Position(x, y);
		health = new Health(10, 15);
	}
}
