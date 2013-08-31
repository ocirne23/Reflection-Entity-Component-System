package test.java.recs.entities;

import main.java.recs.Entity;
import test.java.recs.components.Position;
import test.java.recs.components.Velocity;

public class Zombie extends Entity {
	Position position;
	Velocity velocity;
	public Zombie(float x, float y) {
		position = new Position(x, y);
		velocity = new Velocity(1, 2);
	}
}
