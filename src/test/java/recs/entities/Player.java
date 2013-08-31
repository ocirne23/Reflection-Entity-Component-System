package test.java.recs.entities;

import main.java.recs.Entity;
import test.java.recs.components.Health;
import test.java.recs.components.Position;
import test.java.recs.components.Velocity;

public class Player extends Entity {
	public Position position;
	public Velocity velocity = new Velocity(2, 1);
	public Health health = new Health(10, 15);

	public Player(float x, float y) {
		position = new Position(x, y);
	}

	public Player() {}
}
