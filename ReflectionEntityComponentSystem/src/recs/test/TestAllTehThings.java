package recs.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import recs.core.ComponentDestructionListener;
import recs.core.Entity;
import recs.core.EntityWorld;
import recs.test.components.Attack;
import recs.test.components.Gravity;
import recs.test.components.Health;
import recs.test.components.Position;
import recs.test.components.Velocity;
import recs.test.entities.Player;
import recs.test.entities.PlayerWithAttack;
import recs.test.entities.Zombie;
import recs.test.events.DamageEvent;
import recs.test.systems.AttackSystem;
import recs.test.systems.HealthSystem;
import recs.test.systems.MovementSystem;
import recs.test.systems.ThreadedMovementSystem;


public class TestAllTehThings {
	static {
		EntityWorld.reset();
	}
	private static final Class<?>[] COMPONENTS = { Health.class, Position.class, Velocity.class, Attack.class, Gravity.class };

	private Player player;
	private Player player2;
	private PlayerWithAttack playerWithAttack;
	private Zombie zombie;

	private MovementSystem ms;
	private ThreadedMovementSystem tms;
	private HealthSystem hs;
	private AttackSystem as;

	@Before
	public void setup() {
		EntityWorld.registerComponents(COMPONENTS);

		ms = new MovementSystem();
		EntityWorld.addSystem(ms);

		tms = new ThreadedMovementSystem();
		tms.setEnabled(false);
		EntityWorld.addSystem(tms);

		hs = new HealthSystem();
		EntityWorld.addSystem(hs);

		as = new AttackSystem();
		EntityWorld.addSystem(as);

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
	public void testThreadedSystem() {
		ms.setEnabled(false);
		tms.setEnabled(true);
		Position position = EntityWorld.getComponent(player.id, Position.class);
		Velocity velocity = EntityWorld.getComponent(player.id, Velocity.class);
		float startX = position.x;
		float startY = position.y;
		//First iteration does not process threaded systems.
		try {
			EntityWorld.process(0);
			Thread.sleep(1000);
			EntityWorld.process(0);
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		assertTrue(velocity.x != 0 || velocity.y != 0);
		assertTrue(startX != position.x || startY != position.y);
	}

	@Test
	public void testThreadPool() {
		ms.setEnabled(false);
		tms.setEnabled(true);

		try {
		for(int i = 0; i < 10000; i++) {
			EntityWorld.process(0f);
		}
		} catch(Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue(true);
	}

	@Test
	public void testEvents() {
		Health health = EntityWorld.getComponent(player.id, Health.class);
		int currentHealth = health.health;

		EntityWorld.sendEvent(new DamageEvent(player.id, 2));
		EntityWorld.process(1f);

		assertTrue(health.health == currentHealth - 2);
	}

	@Test
	public void testDyamicComponentAdd() {
		int player2Id = player2.id;
		Attack attack = EntityWorld.getComponent(player2Id, Attack.class);
		assertTrue(attack == null);
		assertFalse(as.hasEntity(player2Id));

		player2.addComponent(new Attack(2), new Gravity(1));
		Attack attack2 = EntityWorld.getComponent(player2Id, Attack.class);
		assertTrue(attack2 != null);
		assertTrue(as.hasEntity(player2Id));

		Gravity gravity = EntityWorld.getComponent(player2Id, Gravity.class);
		assertTrue(gravity != null);

		player.addComponent(new Gravity(2), new Attack(3));
	}

	@Test
	public void testDynamicComponentRemove() {
		int player2Id = player2.id;
		Position position = EntityWorld.getComponent(player2Id, Position.class);
		assertTrue(position != null);
		assertTrue(ms.hasEntity(player2Id));

		player2.removeComponent(position);
		Position position2 = EntityWorld.getComponent(player2Id, Position.class);
		assertTrue(position2 == null);
		assertFalse(ms.hasEntity(player2Id));
	}

	@Test
	public void testComponentAddRemove() {
		int player2Id = player2.id;

		Attack attack = EntityWorld.getComponent(player2Id, Attack.class);
		assertTrue(attack == null);
		assertFalse(as.hasEntity(player2Id));

		player2.addComponent(new Attack(2), new Gravity(1));
		Attack attack2 = EntityWorld.getComponent(player2Id, Attack.class);
		assertTrue(attack2 != null);
		assertTrue(as.hasEntity(player2Id));

		player2.removeComponent(attack2);
		Attack attack3 = EntityWorld.getComponent(player2Id, Attack.class);
		assertTrue(attack3 == null);
		assertFalse(as.hasEntity(player2Id));
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

	@Test
	public void testRemoveEntityWithAddedComponent() {
		int playerId = player.id;

		player.addComponent(new Attack(2));
		Attack attack = EntityWorld.getComponent(playerId, Attack.class);
		assertTrue(attack != null);
		assertTrue(as.hasEntity(playerId));

		EntityWorld.removeEntity(playerId);

		Attack attack2 = EntityWorld.getComponent(playerId, Attack.class);
		assertTrue(attack2 == null);
		assertFalse(as.hasEntity(playerId));
	}

	private class MyDestructionListener extends ComponentDestructionListener {
		public boolean destroyed = false;
		public MyDestructionListener() {
			super(Position.class);
		}

		@Override
		public void destroyed(Object object) {
			destroyed = true;
		}

	}

	@Test
	public void testDestructionListener() {
		int playerId = player.id;
		Position position = EntityWorld.getComponent(playerId, Position.class);
		assertTrue(position != null);

		MyDestructionListener dl = new MyDestructionListener();

		EntityWorld.removeEntity(playerId);

		assertTrue(dl.destroyed == true);

		Position position2 = EntityWorld.getComponent(playerId, Position.class);
		assertTrue(position2 == null);
	}

	@Test
	public void testDynamicEntity() {
		Entity e = new Entity();
		e.addComponent(new Position(1,2), new Velocity(4, 0), new Health(10, 15));
		EntityWorld.createEntity(e);

		Position position = EntityWorld.getComponent(e.id, Position.class);
		assertTrue(position != null);

		assertTrue(ms.hasEntity(e.id));
		assertTrue(hs.hasEntity(e.id));

		Health health = EntityWorld.getComponent(e.id, Health.class);
		e.removeComponent(health);

		Health health2 = EntityWorld.getComponent(e.id, Health.class);
		assertTrue(health2 == null);
		assertFalse(hs.hasEntity(e.id));
	}

	@After
	public void breakDown() {
		EntityWorld.reset();
	}
}
