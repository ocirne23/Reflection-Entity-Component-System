package recs.test;

import recs.core.EntityWorld;
import recs.core.utils.RECSMathUtils;
import recs.test.components.Attack;
import recs.test.components.Gravity;
import recs.test.components.Health;
import recs.test.components.Position;
import recs.test.components.Velocity;
import recs.test.entities.Player;
import recs.test.entities.Zombie;
import recs.test.systems.MovementSystem;

@SuppressWarnings("unused")
public class Benchmark {
	private static final Class<?>[] COMPONENTS = { Health.class, Position.class, Velocity.class, Attack.class, Gravity.class };

	public static void main(String[] args) {
		Benchmark b = new Benchmark();

		//b.testFixedTimestep();
		b.testUnlimitedTimeStep();
		b.testUnlimitedTimeStep();
		b.testUnlimitedTimeStep();
		b.testUnlimitedTimeStep();
		b.testUnlimitedTimeStep();
	}

	private void testUnlimitedTimeStep() {
		EntityWorld.reset();
		EntityWorld.registerComponents(COMPONENTS);

		EntityWorld.addSystem(new MovementSystem());

		long startAdd = System.nanoTime();
		for(int i = 0; i < 1000000; i++)
			EntityWorld.addEntity(new Zombie(10, 10));
		System.out.println("addtime: " + ((System.nanoTime() - startAdd) * RECSMathUtils.nanoToSec));

		// game loop
		int loopCount = 0;
		float totalTime = 0f;
		long currentTime = System.nanoTime();
		while (true) {
			long start = System.nanoTime();
			long deltaNano = start - currentTime;
			currentTime = start;
			float deltaSec = deltaNano * RECSMathUtils.nanoToSec;

			EntityWorld.process(deltaSec);
			loopCount++;

			totalTime += deltaSec;
			if (totalTime > 1f)
				break;
		}
		System.out.println("looped: " + loopCount + " times.");
	}

	private void testFixedTimestep() {
		EntityWorld.reset();
		EntityWorld.registerComponents(COMPONENTS);

		EntityWorld.addSystem(new MovementSystem());
		EntityWorld.addEntity(new Player(10, 10));

		// game loop
		float totalTime = 0f;
		float accumulator = 0f;
		float timeStep = 1 / 60f;
		long currentTime = System.nanoTime();
		while (true) {
			long start = System.nanoTime();
			long deltaNano = start - currentTime;
			currentTime = start;
			float deltaSec = deltaNano * RECSMathUtils.nanoToSec;

			accumulator += deltaSec;
			while (accumulator > timeStep) {
				accumulator -= timeStep;
				EntityWorld.process(timeStep);
			}

			totalTime += deltaSec;
			if (totalTime > 3f)
				break;
		}
	}
}
