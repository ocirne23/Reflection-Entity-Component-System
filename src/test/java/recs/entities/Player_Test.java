package recs.entities;

import recs.Entity;
import recs.components.Health_0;
import recs.components.Position_0;
import recs.components.Velocity_0;

public class Player_Test extends Entity {
	public Position_0 position;
	public Velocity_0 velocity = new Velocity_0(2, 1);
	public Health_0 health = new Health_0(10, 15);

	public Player_Test(float x, float y) {
		position = new Position_0(x, y);
	}

	Player_Test() {}
}
