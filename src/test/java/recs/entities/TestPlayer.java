package recs.entities;

import recs.Entity;
import recs.components.Health0;
import recs.components.Position0;
import recs.components.Velocity0;

public class TestPlayer extends Entity {
	public Position0 position;
	public Velocity0 velocity = new Velocity0(2, 1);
	public Health0 health = new Health0(10, 15);

	public TestPlayer(float x, float y) {
		position = new Position0(x, y);
	}

	TestPlayer() {}
}
