package recs.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import recs.core.ComponentDestructionListener;
import recs.core.Entity;
import recs.core.EntitySystem;
import recs.core.EntityWorld;
import recs.test.components.Attack;
import recs.test.components.Gravity;
import recs.test.components.Health;
import recs.test.components.Position;
import recs.test.components.Velocity;
import recs.test.components.extras.CopyOfAttack;
import recs.test.components.extras.CopyOfGravity;
import recs.test.components.extras.CopyOfHealth;
import recs.test.components.extras.CopyOfPosition;
import recs.test.components.extras.CopyOfVelocity;
import recs.test.components.extras.Copy_2_of_Attack;
import recs.test.components.extras.Copy_2_of_Gravity;
import recs.test.components.extras.Copy_2_of_Health;
import recs.test.components.extras.Copy_2_of_Position;
import recs.test.components.extras.Copy_2_of_Velocity;
import recs.test.components.extras.Copy_3_of_Attack;
import recs.test.components.extras.Copy_3_of_Gravity;
import recs.test.components.extras.Copy_3_of_Health;
import recs.test.components.extras.Copy_3_of_Position;
import recs.test.components.extras.Copy_3_of_Velocity;
import recs.test.components.extras.Copy_4_of_Attack;
import recs.test.components.extras.Copy_4_of_Gravity;
import recs.test.components.extras.Copy_4_of_Health;
import recs.test.components.extras.Copy_4_of_Position;
import recs.test.components.extras.Copy_4_of_Velocity;
import recs.test.components.extras.Copy_5_of_Attack;
import recs.test.components.extras.Copy_5_of_Gravity;
import recs.test.components.extras.Copy_5_of_Health;
import recs.test.components.extras.Copy_5_of_Position;
import recs.test.components.extras.Copy_5_of_Velocity;
import recs.test.components.extras.Copy_6_of_Attack;
import recs.test.components.extras.Copy_6_of_Gravity;
import recs.test.components.extras.Copy_6_of_Health;
import recs.test.components.extras.Copy_6_of_Position;
import recs.test.components.extras.Copy_6_of_Velocity;
import recs.test.components.extras.Copy_7_of_Attack;
import recs.test.components.extras.Copy_7_of_Gravity;
import recs.test.components.extras.Copy_7_of_Health;
import recs.test.components.extras.Copy_7_of_Position;
import recs.test.components.extras.Copy_7_of_Velocity;
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
	private static final Class<?>[] COMPONENTS1 = { CopyOfHealth.class, CopyOfPosition.class, CopyOfVelocity.class, CopyOfAttack.class, CopyOfGravity.class };
	private static final Class<?>[] COMPONENTS2 = { Copy_2_of_Health.class, Copy_2_of_Position.class, Copy_2_of_Velocity.class, Copy_2_of_Attack.class, Copy_2_of_Gravity.class };
	private static final Class<?>[] COMPONENTS3 = { Copy_3_of_Health.class, Copy_3_of_Position.class, Copy_3_of_Velocity.class, Copy_3_of_Attack.class, Copy_3_of_Gravity.class };
	private static final Class<?>[] COMPONENTS4 = { Copy_4_of_Health.class, Copy_4_of_Position.class, Copy_4_of_Velocity.class, Copy_4_of_Attack.class, Copy_4_of_Gravity.class };
	private static final Class<?>[] COMPONENTS5 = { Copy_5_of_Health.class, Copy_5_of_Position.class, Copy_5_of_Velocity.class, Copy_5_of_Attack.class, Copy_5_of_Gravity.class };
	private static final Class<?>[] COMPONENTS6 = { Copy_6_of_Health.class, Copy_6_of_Position.class, Copy_6_of_Velocity.class, Copy_6_of_Attack.class, Copy_6_of_Gravity.class };
	private static final Class<?>[] COMPONENTS7 = { Copy_7_of_Health.class, Copy_7_of_Position.class, Copy_7_of_Velocity.class, Copy_7_of_Attack.class, Copy_7_of_Gravity.class };

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
		System.out.println("starting test");
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
	}

	private void addEntities() {
		player = new Player(4, 6);
		player2 = new Player(12, 9);
		playerWithAttack = new PlayerWithAttack(6, 11);
		zombie = new Zombie(1, 2);
		EntityWorld.addEntity(player);
		EntityWorld.addEntity(player2);
		EntityWorld.addEntity(playerWithAttack);
		EntityWorld.addEntity(zombie);
	}

	@Test
	public void testGetComponent() {
		addEntities();
		int playerId = player.id;
		Position position = EntityWorld.getComponent(playerId, Position.class);
		Velocity velocity = EntityWorld.getComponent(playerId, Velocity.class);
		Health health = EntityWorld.getComponent(playerId, Health.class);

		assertTrue(position.x == 4f && position.y == 6f);
		assertTrue(velocity.x == 2f && velocity.y == 1f);
		assertTrue(health.health == 10 && health.maxHealth == 15);

		// player2
		int player2Id = player2.id;
		Position position2 = EntityWorld.getComponent(player2Id, Position.class);
		assertTrue(position2.x == 12 && position2.y == 9);

		// zombie
		int zombieId = zombie.id;
		Health healthNull = EntityWorld.getComponent(zombieId, Health.class);
		assertTrue(healthNull == null);
	}

	@Test
	public void testMovementSystem() {
		addEntities();
		int playerId = player.id;
		final float deltaInSec1 = 2f;
		final float deltaInSec2 = 1.5f;
		// player
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
		addEntities();
		int playerWithAttackId = playerWithAttack.id;
		Attack attack = EntityWorld.getComponent(playerWithAttackId, Attack.class);
		assertTrue(attack != null);

		Position position = EntityWorld.getComponent(playerWithAttackId, Position.class);
		assertTrue(position != null);
	}

	@Test
	public void testThreadedSystem() {
		addEntities();
		ms.setEnabled(false);
		tms.setEnabled(true);
		Position position = EntityWorld.getComponent(player.id, Position.class);
		Velocity velocity = EntityWorld.getComponent(player.id, Velocity.class);
		float startX = position.x;
		float startY = position.y;
		try {
			// First iteration does not process threaded systems.
			EntityWorld.process(0);
			Thread.sleep(100);
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
		addEntities();

		ms.setEnabled(false);
		tms.setEnabled(true);

		try {
			for (int i = 0; i < 10000; i++) {
				EntityWorld.process(0f);
			}
		} catch (Exception e) {
			e.printStackTrace();
			assertTrue(false);
		}
		assertTrue(true);
	}

	@Test
	public void testEvents() {
		addEntities();

		Health health = EntityWorld.getComponent(player.id, Health.class);
		int currentHealth = health.health;

		EntityWorld.sendEvent(new DamageEvent(player.id, 2));
		EntityWorld.process(1f);

		assertTrue(health.health == currentHealth - 2);
	}

	@Test
	public void testDyamicComponentAdd() {
		addEntities();
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
		addEntities();
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
		addEntities();
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
		addEntities();
		int playerId = player.id;
		Position position = EntityWorld.getComponent(playerId, Position.class);
		assertTrue(position != null);

		EntityWorld.removeEntity(playerId);

		Position position2 = EntityWorld.getComponent(playerId, Position.class);
		assertTrue(position2 == null);
	}

	@Test
	public void testRemoveEntityWithAddedComponent() {
		addEntities();
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
		addEntities();
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
		//addEntities();
		Entity e = new Entity();
		e.addComponent(new Position(1, 2), new Velocity(4, 0), new Health(10, 15));
		EntityWorld.addEntity(e);

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

	@Test
	public void testDynamicEntity2() {
		//addEntities();
		Entity e = new Entity();
		e.addComponent(new Position(1, 2));
		e.addComponent(new Velocity(1, 2));
		EntityWorld.addEntity(e);

		Position position = EntityWorld.getComponent(e.id, Position.class);
		assertTrue(position != null);
		assertTrue(ms.hasEntity(e.id));
	}

	@Test
	public void testLotsOfComponents() {
		addEntities();
		EntityWorld.registerComponents(COMPONENTS1);
		EntityWorld.registerComponents(COMPONENTS2);
		EntityWorld.registerComponents(COMPONENTS3);
		EntityWorld.registerComponents(COMPONENTS4);
		EntityWorld.registerComponents(COMPONENTS5);
		EntityWorld.registerComponents(COMPONENTS6);
		EntityWorld.registerComponents(COMPONENTS7);


		Copy_7_of_Gravity gravity7 = new Copy_7_of_Gravity(1f);
		Copy_5_of_Velocity velocity5 = new Copy_5_of_Velocity(4, 0);

		Entity e = new Entity();
		e.addComponent(new Position(1,2), new Velocity(4, 0), new Health(10, 15), new Attack(2), new Gravity(1f),
				new CopyOfPosition(1,2), new CopyOfVelocity(4, 0), new CopyOfHealth(10, 15), new CopyOfAttack(2), new CopyOfGravity(1f),
				new Copy_2_of_Position(1,2), new Copy_2_of_Velocity(4, 0), new Copy_2_of_Health(10, 15), new Copy_2_of_Attack(2), new Copy_2_of_Gravity(1f),
				new Copy_3_of_Position(1,2), new Copy_3_of_Velocity(4, 0), new Copy_3_of_Health(10, 15), new Copy_3_of_Attack(2), new Copy_3_of_Gravity(1f),
				new Copy_4_of_Position(1,2), new Copy_4_of_Velocity(4, 0), new Copy_4_of_Health(10, 15), new Copy_4_of_Attack(2), new Copy_4_of_Gravity(1f),
				new Copy_5_of_Position(1,2), velocity5, new Copy_5_of_Health(10, 15), new Copy_5_of_Attack(2), new Copy_5_of_Gravity(1f),
				new Copy_6_of_Position(1,2), new Copy_6_of_Velocity(4, 0), new Copy_6_of_Health(10, 15), new Copy_6_of_Attack(2), new Copy_6_of_Gravity(1f),
				new Copy_7_of_Position(1,2), new Copy_7_of_Velocity(4, 0), new Copy_7_of_Health(10, 15), new Copy_7_of_Attack(2), gravity7);
		EntityWorld.addEntity(e);

		Copy_7_of_Gravity gravity = EntityWorld.getComponent(e.id, Copy_7_of_Gravity.class);
		assertTrue(gravity != null);

		Copy_5_of_Velocity velocity = EntityWorld.getComponent(e.id, Copy_5_of_Velocity.class);
		assertTrue(velocity != null);
	}

	@Test
	public void testLotsOfComponentAddAfterCreate() {
		addEntities();
		EntityWorld.registerComponents(COMPONENTS1);
		EntityWorld.registerComponents(COMPONENTS2);
		EntityWorld.registerComponents(COMPONENTS3);
		EntityWorld.registerComponents(COMPONENTS4);
		EntityWorld.registerComponents(COMPONENTS5);
		EntityWorld.registerComponents(COMPONENTS6);
		EntityWorld.registerComponents(COMPONENTS7);


		Copy_7_of_Gravity gravity7 = new Copy_7_of_Gravity(1f);
		Copy_5_of_Velocity velocity5 = new Copy_5_of_Velocity(4, 0);

		Entity e = new Entity();
		EntityWorld.addEntity(e);

		e.addComponent(new Position(1,2), new Velocity(4, 0), new Health(10, 15), new Attack(2), new Gravity(1f),
				new CopyOfPosition(1,2), new CopyOfVelocity(4, 0), new CopyOfHealth(10, 15), new CopyOfAttack(2), new CopyOfGravity(1f),
				new Copy_2_of_Position(1,2), new Copy_2_of_Velocity(4, 0), new Copy_2_of_Health(10, 15), new Copy_2_of_Attack(2), new Copy_2_of_Gravity(1f),
				new Copy_3_of_Position(1,2), new Copy_3_of_Velocity(4, 0), new Copy_3_of_Health(10, 15), new Copy_3_of_Attack(2), new Copy_3_of_Gravity(1f),
				new Copy_4_of_Position(1,2), new Copy_4_of_Velocity(4, 0), new Copy_4_of_Health(10, 15), new Copy_4_of_Attack(2), new Copy_4_of_Gravity(1f),
				new Copy_5_of_Position(1,2), velocity5, new Copy_5_of_Health(10, 15), new Copy_5_of_Attack(2), new Copy_5_of_Gravity(1f),
				new Copy_6_of_Position(1,2), new Copy_6_of_Velocity(4, 0), new Copy_6_of_Health(10, 15), new Copy_6_of_Attack(2), new Copy_6_of_Gravity(1f),
				new Copy_7_of_Position(1,2), new Copy_7_of_Velocity(4, 0), new Copy_7_of_Health(10, 15), new Copy_7_of_Attack(2), gravity7);


		Copy_7_of_Gravity gravity = EntityWorld.getComponent(e.id, Copy_7_of_Gravity.class);
		assertTrue(gravity != null);

		Copy_5_of_Velocity velocity = EntityWorld.getComponent(e.id, Copy_5_of_Velocity.class);
		assertTrue(velocity != null);
	}

	@Test
	public void testLotsOfSystems() {
		for(int i = 0; i < 40; i++) {
			EntityWorld.addSystem(
					new EntitySystem(Health.class) {@Override protected void process(int entityId, float deltaInSec) {}}
			);
		}
		addEntities();
		assertTrue(true);
	}

	@After
	public void breakDown() {
		EntityWorld.reset();
		System.out.println("finished test");
	}
}
