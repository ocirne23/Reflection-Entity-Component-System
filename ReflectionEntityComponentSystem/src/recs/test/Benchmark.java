package recs.test;

import recs.core.Entity;
import recs.core.EntityWorld;
import recs.core.utils.libgdx.RECSMathUtils;
import recs.test.components.Position;
import recs.test.components.Velocity;
import recs.test.entities.Zombie;
import recs.test.systems.MovementSystem;

public class Benchmark {
	private static final int NR_ZOMBIES = 50000;
	private static final boolean DYNAMIC = true;
	private static final Class<?>[] COMPONENTS = { Position.class, Velocity.class };
	private final EntityWorld world;

	public static void main(String[] args) {
		new Benchmark();
	}

	public Benchmark() {
		world = new EntityWorld();

		if (DYNAMIC) {
			testDynamicAdd();
			testDynamicAdd();
			testDynamicAdd();
			testDynamicAdd();
			testDynamicAdd();
		} else {
			testReflectionAdd();
			testReflectionAdd();
			testReflectionAdd();
			testReflectionAdd();
			testReflectionAdd();
		}
	}

	private void testReflectionAdd() {
		world.reset();
		world.registerComponents(COMPONENTS);

		world.addSystem(new MovementSystem());

		long startAdd = System.nanoTime();
		for (int i = 0; i < NR_ZOMBIES; i++)
			world.addEntity(new Zombie(10, 10));
		System.out.println("RECS: addtime: " + ((System.nanoTime() - startAdd) * RECSMathUtils.nanoToSec));

		// game loop
		int loopCount = 0;
		float totalTime = 0f;
		long currentTime = System.nanoTime();
		while (true) {
			long start = System.nanoTime();
			long deltaNano = start - currentTime;
			currentTime = start;
			float deltaSec = deltaNano * RECSMathUtils.nanoToSec;

			world.process(deltaSec);
			loopCount++;

			totalTime += deltaSec;
			if (totalTime > 1f)
				break;
		}

		System.out.println("RECS: looped: " + loopCount + " times.");
		System.gc();
		Runtime runtime = Runtime.getRuntime();
		System.out.println("RECS: used Memory: " + (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) + " mb");

		//So EntityWorld does not get GC'd.
		if(world.getComponent(0, Position.class) != null) {
			System.out.println("");
		}
	}

	private void testDynamicAdd() {
		world.reset();
		world.registerComponents(COMPONENTS);

		world.addSystem(new MovementSystem());

		long startAdd = System.nanoTime();
		for (int i = 0; i < NR_ZOMBIES; i++)
			world.addEntity(createZombie(10, 10));
		System.out.println("RECS: addtime: " + ((System.nanoTime() - startAdd) * RECSMathUtils.nanoToSec));

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

			world.process(timeStep);

			totalTime += deltaSec;
			loopCount++;

			if (totalTime > 1f)
				break;
		}
		System.out.println("RECS: looped: " + loopCount + " times.");
		System.gc();
		Runtime runtime = Runtime.getRuntime();
		System.out.println("RECS: used Memory: " + (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024) + " mb");

		//So EntityWorld does not get GC'd.
		if(world.getComponent(0, Position.class) != null) {
			System.out.println("");
		}
	}

	private Entity createZombie(int x, int y) {
		Entity e = new Entity();
		e.addComponent(new Position(x, y));
		e.addComponent(new Velocity(1, 2));
		return e;
	}
}