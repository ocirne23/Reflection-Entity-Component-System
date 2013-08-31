package recs.entities;

import recs.Entity;
import recs.components.Position_0;
import recs.components.Velocity_0;

public class Zombie_Test extends Entity {
	Position_0 position;
	Velocity_0 velocity;
	public Zombie_Test(float x, float y) {
		position = new Position_0(x, y);
		velocity = new Velocity_0(1, 2);
	}
}
