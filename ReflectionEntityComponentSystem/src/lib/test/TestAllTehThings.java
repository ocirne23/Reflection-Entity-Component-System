package lib.test;

import static org.junit.Assert.assertTrue;
import lib.core.EntityWorld;
import lib.test.components.Attack;
import lib.test.components.Health;
import lib.test.components.Position;
import lib.test.components.Velocity;
import lib.test.entities.Player;
import lib.test.entities.PlayerWithAttack;
import lib.test.entities.Zombie;
import lib.test.systems.AttackSystem;
import lib.test.systems.HealthSystem;
import lib.test.systems.MovementSystem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TestAllTehThings {
	private static final Class<?>[] COMPONENTS = { Health.class, Position.class, Velocity.class, Attack.class };

	private Player player;
	private Player player2;
	private PlayerWithAttack playerWithAttack;
	private Zombie zombie;

	@Before
	public void setup() {
		EntityWorld.reset();
		EntityWorld.registerComponents(COMPONENTS);
		EntityWorld.addSystem(new HealthSystem());
		EntityWorld.addSystem(new MovementSystem());
		EntityWorld.addSystem(new AttackSystem());

		player = new Player(4, 6);
		player2 = new Player(12, 9);
		playerWithAttack = new PlayerWithAttack(6, 11);
		zombie = new Zombie(1, 2);

		EntityWorld.createEntity(player);
		EntityWorld.createEntity(player2);
		EntityWorld.createEntity(playerWithAttack);
		EntityWorld.createEntity(zombie);
	}

	@Test
	public void testGetComponent() {
		int playerId = player.id;
		Position position = EntityWorld.getComponent(playerId, Position.class);
		Velocity velocity = EntityWorld.getComponent(playerId, Velocity.class);
		Health health = EntityWorld.getComponent(playerId, Health.class);

		assertTrue(position.x == 4f && position.y == 6f);
		assertTrue(velocity.x == 2f && velocity.y == 1f);
		assertTrue(health.health == 10 && health.maxHealth == 15);

		//player2
		int player2Id = player2.id;
		Position position2 = EntityWorld.getComponent(player2Id, Position.class);
		assertTrue(position2.x == 12 && position2.y == 9);

		//zombie
		int zombieId = zombie.id;
		Health healthNull = EntityWorld.getComponent(zombieId, Health.class);
		assertTrue(healthNull == null);
	}

	@Test
	public void testMovementSystem() {
		int playerId = player.id;
		final float deltaInSec1 = 2f;
		final float deltaInSec2 = 1.5f;
		//player
		Position position = EntityWorld.getComponent(playerId, Position.class);
		Velocity velocity = EntityWorld.getComponent(playerId, Velocity.class);

		float startX = position.x;
		float startY = position.y;
		float xSpeed = velocity.x;
		float ySpeed = velocity.y;
		float expectedX = startX + xSpeed * deltaInSec1;
		float expectedY = startY + ySpeed * deltaInSec1;

		EntityWorld.process(deltaInSec1);
		System.out.println(position.x +":"+ expectedX + ":"+ position.y +":"+ expectedY);
		assertTrue(position.x == expectedX && position.y == expectedY);

		startX = expectedX;
		startY = expectedY;
		expectedX = startX + xSpeed * deltaInSec2;
		expectedY = startY + ySpeed * deltaInSec2;

		EntityWorld.process(deltaInSec2);
		assertTrue(position.x == expectedX && position.y == expectedY);
	}

	@Test
	public void testInheritance() {
		int playerWithAttackId = playerWithAttack.id;
		Attack attack = EntityWorld.getComponent(playerWithAttackId, Attack.class);
		assertTrue(attack != null);

		Position position = EntityWorld.getComponent(playerWithAttackId, Position.class);
		assertTrue(position != null);
	}

	@Test
	public void testRemove() {
		int playerId = player.id;
		Position position = EntityWorld.getComponent(playerId, Position.class);
		assertTrue(position != null);

		EntityWorld.removeEntity(playerId);

		Position position2 = EntityWorld.getComponent(playerId, Position.class);
		assertTrue(position2 == null);
	}

	@After
	public void breakDown() {
		EntityWorld.reset();
	}
}
