package lib.test;

import static org.junit.Assert.assertTrue;
import lib.core.EntityWorld;
import lib.test.components.Health;
import lib.test.components.Position;
import lib.test.components.Velocity;
import lib.test.entities.Player;
import lib.test.entities.Zombie;
import lib.test.systems.HealthSystem;
import lib.test.systems.MovementSystem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TestAllTehThings {
	private static final Class<?>[] COMPONENTS = { Health.class, Position.class, Velocity.class };

	private Player player;
	private Player player2;
	private Zombie zombie;

	@Before
	public void setup() {
		EntityWorld.registerComponents(COMPONENTS);
		EntityWorld.addSystem(new HealthSystem());
		EntityWorld.addSystem(new MovementSystem());

		player = new Player(4, 6);
		player2 = new Player(12, 9);
		zombie = new Zombie(1, 2);

		EntityWorld.createEntity(player);
		EntityWorld.createEntity(player2);
		EntityWorld.createEntity(zombie);
	}

	@Test
	public void testGetComponent() {
		//player
		Position position = EntityWorld.getComponent(0, Position.class);
		Velocity velocity = EntityWorld.getComponent(0, Velocity.class);
		Health health = EntityWorld.getComponent(0, Health.class);

		assertTrue(position.x == 4f && position.y == 6f);
		assertTrue(velocity.x == 2f && velocity.y == 1f);
		assertTrue(health.health == 10 && health.maxHealth == 15);

		//player2
		Position position2 = EntityWorld.getComponent(1, Position.class);
		assertTrue(position2.x == 12 && position2.y == 9);

		//zombie
		Health healthNull = EntityWorld.getComponent(2, Health.class);
		assertTrue(healthNull == null);
	}

	@Test
	public void testMovementSystem() {
		final float deltaInSec1 = 2f;
		final float deltaInSec2 = 1.5f;
		//player
		Position position = EntityWorld.getComponent(0, Position.class);
		Velocity velocity = EntityWorld.getComponent(0, Velocity.class);

		float startX = position.x;
		float startY = position.y;
		float xSpeed = velocity.x;
		float ySpeed = velocity.y;
		float expectedX = startX + xSpeed * deltaInSec1;
		float expectedY = startY + ySpeed * deltaInSec1;

		EntityWorld.process(deltaInSec1);
		assertTrue(position.x == expectedX && position.y == expectedY);

		startX = expectedX;
		startY = expectedY;
		expectedX = startX + xSpeed * deltaInSec2;
		expectedY = startY + ySpeed * deltaInSec2;

		EntityWorld.process(deltaInSec2);
		assertTrue(position.x == expectedX && position.y == expectedY);
	}

	@Test
	public void testRemove() {
		Position position = EntityWorld.getComponent(0, Position.class);
		assertTrue(position != null);

		EntityWorld.removeEntity(player);

		Position position2 = EntityWorld.getComponent(0, Position.class);
		assertTrue(position2 == null);
	}

	@After
	public void breakDown() {
		EntityWorld.reset(true);
	}
}
