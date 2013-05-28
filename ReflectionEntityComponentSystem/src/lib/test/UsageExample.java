package lib.test;

import lib.core.EntityWorld;
import lib.test.components.Attack;
import lib.test.components.Health;
import lib.test.components.Position;
import lib.test.components.Velocity;
import lib.test.entities.Player;
import lib.test.entities.PlayerWithAttack;
import lib.test.entities.Zombie;
import lib.test.systems.HealthSystem;
import lib.test.systems.MovementSystem;
import lib.utils.MathUtils;

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

			float deltaSec = deltaNano * MathUtils.nanoToSec;
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
