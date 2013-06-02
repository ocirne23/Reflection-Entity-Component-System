package recs.test;

import recs.core.EntityWorld;
import recs.core.utils.RECSMathUtils;
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
		EntityWorld.registerComponents(COMPONENTS);

		EntityWorld.addSystem(new HealthSystem());
		EntityWorld.addSystem(new MovementSystem());

		EntityWorld.createEntity(new Player(4, 6));
		EntityWorld.createEntity(new PlayerWithAttack(12, 9));
		EntityWorld.createEntity(new Zombie(1, 2));

		//game loop
		float totalTime = 0f;
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
				EntityWorld.process(timeStep);
			}

			totalTime += deltaSec;
			if(totalTime > 3f) break;
		}
	}
}
