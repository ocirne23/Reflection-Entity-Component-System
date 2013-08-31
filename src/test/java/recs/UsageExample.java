package test.java.recs;

import main.java.recs.EntityWorld;
import main.java.recs.utils.libgdx.RECSMathUtils;
import test.java.recs.entities.Player;
import test.java.recs.entities.PlayerWithAttack;
import test.java.recs.entities.Zombie;
import test.java.recs.systems.HealthSystem;
import test.java.recs.systems.MovementSystem;

public class UsageExample {
	//Register all component classes here.

	public static void main(String[] args) {
		EntityWorld world = new EntityWorld();
		//world.registerComponents(COMPONENTS);

		world.addSystem(new HealthSystem());
		world.addSystem(new MovementSystem());

		world.addEntity(new Player(4, 6));
		world.addEntity(new PlayerWithAttack(12, 9));
		world.addEntity(new Zombie(1, 2));
		world.addEntity(new Player(1,2));

		float totalTime = 0f;
		//game loop
		float accumulator = 0f;
		float timeStep = 1/60f;
		long currentTime = System.nanoTime();
		while (true) {
			long start = System.nanoTime();
			long deltaNano = start - currentTime;
			currentTime = start;
			float deltaSec = deltaNano * RECSMathUtils.nanoToSec;

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
