package recs.entities;

import recs.Entity;
import recs.components.Position;
import recs.components.Velocity;

public class Zombie extends Entity {
	Position position;
	Velocity velocity;
	public Zombie(float x, float y) {
		position = new Position(x, y);
		velocity = new Velocity(1, 2);
	}
}
