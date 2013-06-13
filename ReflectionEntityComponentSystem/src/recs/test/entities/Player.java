package recs.test.entities;

import recs.core.Entity;
import recs.test.components.Health;
import recs.test.components.Position;
import recs.test.components.Velocity;

public class Player extends Entity {
	public Position position;
	public Velocity velocity = new Velocity(2, 1);
	public Health health = new Health(10, 15);

	public Player(float x, float y) {
		position = new Position(x, y);
	}

	public Player() {}
}
