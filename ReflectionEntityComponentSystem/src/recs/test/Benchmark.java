package recs.test;

import recs.core.Entity;
import recs.core.EntityWorld;
import recs.core.utils.RECSMathUtils;
import recs.test.components.Position;
import recs.test.components.Velocity;
import recs.test.entities.Zombie;
import recs.test.systems.MovementSystem;

public class Benchmark {
	private static final Class<?>[] COMPONENTS = { Position.class, Velocity.class };

	public static void main(String[] args) {
		Benchmark b = new Benchmark();

	//	b.testReflectionAdd();
	//	b.testReflectionAdd();
	//	b.testReflectionAdd();
	//	b.testReflectionAdd();
	//	b.testReflectionAdd();

		b.testDynamicAdd();
		b.testDynamicAdd();
		b.testDynamicAdd();
		b.testDynamicAdd();
		b.testDynamicAdd();
	}

	private void testReflectionAdd() {
		EntityWorld.reset();
		EntityWorld.registerComponents(COMPONENTS);

		EntityWorld.addSystem(new MovementSystem());

		long startAdd = System.nanoTime();
		for (int i = 0; i < 1000000; i++)
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

	private void testDynamicAdd() {
		EntityWorld.reset();
		EntityWorld.registerComponents(COMPONENTS);

		EntityWorld.addSystem(new MovementSystem());

		long startAdd = System.nanoTime();
		for (int i = 0; i < 1000000; i++)
			EntityWorld.addEntity(createZombie(10, 10));
		System.out.println("addtime: " + ((System.nanoTime() - startAdd) * RECSMathUtils.nanoToSec));

		// game loop
		int loopCount = 0;
		float totalTime = 0f;
		float timeStep = 1 / 60f;
		long currentTime = System.nanoTime();
		while (true) {
			long start = System.nanoTime();
			long deltaNano = start - currentTime;
			currentTime = start;
			float deltaSec = deltaNano * RECSMathUtils.nanoToSec;

			EntityWorld.process(timeStep);

			totalTime += deltaSec;
			loopCount++;

			if (totalTime > 1f)
				break;
		}
		System.out.println("looped: " + loopCount + " times.");
	}

	private Entity createZombie(int x, int y) {
		Entity e = new Entity();
		e.addComponent(new Position(x, y));
		e.addComponent(new Velocity(1, 2));
		return e;
	}
}