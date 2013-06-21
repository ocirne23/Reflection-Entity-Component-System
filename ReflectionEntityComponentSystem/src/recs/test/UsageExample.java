package recs.test;

import recs.core.EntityWorld;
import recs.core.utils.libgdx.RECSMathUtils;
import recs.test.components.Attack;
import recs.test.components.Health;
import recs.test.components.Position;
import recs.test.components.Velocity;
import recs.test.entities.Player;
import recs.test.entities.PlayerWithAttack;
import recs.test.entities.Zombie;
import recs.test.systems.HealthSystem;
import recs.test.systems.MovementSystem;

public class UsageExample {
	//Register all component classes here.
	private static final Class<?>[] COMPONENTS = { Health.class, Position.class, Velocity.class, Attack.class };

	public static void main(String[] args) {
		EntityWorld world = new EntityWorld();
		world.registerComponents(COMPONENTS);

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
