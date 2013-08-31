package recs;

import recs.entities.Player_Test;
import recs.entities.PlayerWithAttack_Test;
import recs.entities.Zombie_Test;
import recs.systems.HealthSystem_Test;
import recs.systems.MovementSystem_Test;

import com.badlogic.gdx.math.MathUtils;

public class UsageExample {
	public static void main(String[] args) {
		EntityWorld world = new EntityWorld();

		world.addSystem(new HealthSystem_Test());
		world.addSystem(new MovementSystem_Test());

		world.addEntity(new Player_Test(4, 6));
		world.addEntity(new PlayerWithAttack_Test(12, 9));
		world.addEntity(new Zombie_Test(1, 2));
		world.addEntity(new Player_Test(1,2));

		float totalTime = 0f;
		//game loop
		float accumulator = 0f;
		float timeStep = 1/60f;
		long currentTime = System.nanoTime();
		while (true) {
			long start = System.nanoTime();
			long deltaNano = start - currentTime;
			currentTime = start;
			float deltaSec = deltaNano * MathUtils.nanoToSec;

			accumulator += deltaSec;
			while(accumulator > timeStep) {
				accumulator -= timeStep;
				world.process(timeStep);
			}

			totalTime += deltaSec;
			if(totalTime > 3f) break;
		}
	}
}
