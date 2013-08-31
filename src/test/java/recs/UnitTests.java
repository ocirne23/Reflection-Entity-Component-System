package recs;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import recs.components.Attack_0;
import recs.components.Gravity_0;
import recs.components.Health_0;
import recs.components.Position_0;
import recs.components.Velocity_0;
import recs.entities.Player_Test;
import recs.entities.PlayerWithAttack_Test;
import recs.entities.Zombie_Test;
import recs.events.DamageEvent_Test;
import recs.systems.AttackSystem_Test;
import recs.systems.HealthSystem_Test;
import recs.systems.MovementSystem_Test;
import recs.systems.ThreadedMovementSystem_Test;
import recs.utils.Saver;

public class UnitTests {

	private Player_Test player;
	private Player_Test player2;
	private PlayerWithAttack_Test playerWithAttack;
	private Zombie_Test zombie;

	private MovementSystem_Test ms;
	private ThreadedMovementSystem_Test tms;
	private HealthSystem_Test hs;
	private AttackSystem_Test as;

	private EntityWorld world;

	@Before
	public void setup() {
		world = new EntityWorld();
		System.out.println("starting test");
		//world.registerComponents(COMPONENTS);

		ms = new MovementSystem_Test();
		world.addSystem(ms);

		tms = new ThreadedMovementSystem_Test();
		tms.setEnabled(false);
		world.addSystem(tms);

		hs = new HealthSystem_Test();
		world.addSystem(hs);

		as = new AttackSystem_Test();
		world.addSystem(as);
		as.setEnabled(false);
	}

	private void addEntities() {
		player = new Player_Test(4, 6);
		player2 = new Player_Test(12, 9);
		playerWithAttack = new PlayerWithAttack_Test(6, 11);
		zombie = new Zombie_Test(1, 2);
		world.addEntity(player);
		world.addEntity(player2);
		world.addEntity(playerWithAttack);
		world.addEntity(zombie);
	}

	@Test
	public void testId() {
		addEntities();
		assertTrue(player.getId() == 1);
		assertTrue(player2.getId() == 2);
		assertTrue(playerWithAttack.getId() == 3);
		assertTrue(zombie.getId() == 4);
	}

	@Test
	public void testGetComponent() {
		addEntities();
		int playerId = player.getId();
		Position_0 position = world.getComponent(playerId, Position_0.class);
		Velocity_0 velocity = world.getComponent(playerId, Velocity_0.class);
		Health_0 health = world.getComponent(playerId, Health_0.class);

		assertTrue(position.x == 4f && position.y == 6f);
		assertTrue(velocity.x == 2f && velocity.y == 1f);
		assertTrue(health.amount == 10 && health.max == 15);

		// player2
		int player2Id = player2.getId();
		Position_0 position2 = world.getComponent(player2Id, Position_0.class);
		assertTrue(position2.x == 12 && position2.y == 9);

		// zombie
		int zombieId = zombie.getId();
		Health_0 healthNull = world.getComponent(zombieId, Health_0.class);
		assertTrue(healthNull == null);
	}

	@Test
	public void testMovementSystem() {
		addEntities();
		int playerId = player.getId();
		final float deltaInSec1 = 2f;
		final float deltaInSec2 = 1.5f;
		// player
		Position_0 position = world.getComponent(playerId, Position_0.class);
		Velocity_0 velocity = world.getComponent(playerId, Velocity_0.class);

		assertTrue(position != null);
		assertTrue(velocity != null);
		assertTrue(ms.hasEntity(playerId));

		float startX = position.x;
		float startY = position.y;
		float xSpeed = velocity.x;
		float ySpeed = velocity.y;
		float expectedX = startX + xSpeed * deltaInSec1;
		float expectedY = startY + ySpeed * deltaInSec1;

		world.process(deltaInSec1);
		assertTrue(position.x == expectedX && position.y == expectedY);

		startX = expectedX;
		startY = expectedY;
		expectedX = startX + xSpeed * deltaInSec2;
		expectedY = startY + ySpeed * deltaInSec2;

		world.process(deltaInSec2);
		assertTrue(position.x == expectedX && position.y == expectedY);
	}

	@Test
	public void testInheritance() {
		addEntities();
		int playerWithAttackId = playerWithAttack.getId();
		Attack_0 attack = world.getComponent(playerWithAttackId, Attack_0.class);
		assertTrue(attack != null);

		Position_0 position = world.getComponent(playerWithAttackId, Position_0.class);
		assertTrue(position != null);
	}

	@Test
	public void testThreadedSystem() {
		addEntities();
		ms.setEnabled(false);
		tms.setEnabled(true);
		Position_0 position = world.getComponent(player.getId(), Position_0.class);
		Velocity_0 velocity = world.getComponent(player.getId(), Velocity_0.class);

		assertTrue(tms.hasEntity(player.getId()));

		float startX = position.x;
		float startY = position.y;

		try {
			world.process(50);
			Thread.sleep(500);	//so threads can finish up.
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		position = world.getComponent(player.getId(), Position_0.class);

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
				world.process(0f);
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

		Health_0 health = world.getComponent(player.getId(), Health_0.class);
		int currentHealth = health.amount;

		world.sendEvent(new DamageEvent_Test(player.getId(), 2));
		world.process(1f);

		assertTrue(health.amount == currentHealth - 2);
	}

	@Test
	public void testDyamicComponentAdd() {
		addEntities();
		int player2Id = player2.getId();
		Attack_0 attack = world.getComponent(player2Id, Attack_0.class);
		assertTrue(attack == null);
		assertFalse(as.hasEntity(player2Id));

		player2.addComponent(new Attack_0(2), new Gravity_0(1));
		Attack_0 attack2 = world.getComponent(player2Id, Attack_0.class);
		assertTrue(attack2 != null);
		assertTrue(as.hasEntity(player2Id));

		Gravity_0 gravity = world.getComponent(player2Id, Gravity_0.class);
		assertTrue(gravity != null);

		player.addComponent(new Gravity_0(2), new Attack_0(3));
	}

	@Test
	public void testComponentOverwrite() {
		addEntities();
		player.addComponent(new Attack_0(2));
		Attack_0 a1 = world.getComponent(player.getId(), Attack_0.class);
		assertTrue(a1 != null);
		assertTrue(a1.attack == 2);

		player.addComponent(new Attack_0(3));
		Attack_0 a2 = world.getComponent(player.getId(), Attack_0.class);
		assertTrue(a2 != null);
		assertTrue(a2.attack == 3);
	}

	@Test
	public void testDynamicComponentRemove() {
		addEntities();
		int player2Id = player2.getId();
		Position_0 position = world.getComponent(player2Id, Position_0.class);
		assertTrue(position != null);
		assertTrue(ms.hasEntity(player2Id));

		player2.removeComponent(position);
		Position_0 position2 = world.getComponent(player2Id, Position_0.class);
		assertTrue(position2 == null);
		assertFalse(ms.hasEntity(player2Id));
	}

	@Test
	public void testComponentAddRemove() {
		addEntities();
		int player2Id = player2.getId();

		Attack_0 attack = world.getComponent(player2Id, Attack_0.class);
		assertTrue(attack == null);
		assertFalse(as.hasEntity(player2Id));

		player2.addComponent(new Attack_0(2), new Gravity_0(1));
		Attack_0 attack2 = world.getComponent(player2Id, Attack_0.class);
		assertTrue(attack2 != null);
		assertTrue(as.hasEntity(player2Id));

		player2.removeComponent(attack2);
		Attack_0 attack3 = world.getComponent(player2Id, Attack_0.class);
		assertTrue(attack3 == null);
		assertFalse(as.hasEntity(player2Id));
	}

	@Test
	public void testRemove() {
		addEntities();
		int playerId = player.getId();
		Position_0 position = world.getComponent(playerId, Position_0.class);
		assertTrue(position != null);

		world.removeEntity(playerId);

		Position_0 position2 = world.getComponent(playerId, Position_0.class);
		assertTrue(position2 == null);
	}

	@Test
	public void testRemoveEntityWithAddedComponent() {
		addEntities();
		int playerId = player.getId();

		player.addComponent(new Attack_0(2));
		Attack_0 attack = world.getComponent(playerId, Attack_0.class);
		assertTrue(attack != null);
		assertTrue(as.hasEntity(playerId));

		world.removeEntity(playerId);

		Attack_0 attack2 = world.getComponent(playerId, Attack_0.class);
		assertTrue(attack2 == null);
		assertFalse(as.hasEntity(playerId));
	}

	private class MyDestructionListener extends ComponentDestructionListener<Position_0> {

		public MyDestructionListener(EntityWorld world) {
			super(world);
		}

		private boolean destroyed = false;

		@Override
		public void destroyed(Position_0 component) {
			destroyed = true;
		}
	}

	@Test
	public void testDestructionListener() {
		addEntities();
		int playerId = player.getId();
		Position_0 position = world.getComponent(playerId, Position_0.class);
		assertTrue(position != null);

		MyDestructionListener dl = new MyDestructionListener(world);

		world.removeEntity(playerId);

		assertTrue(dl.destroyed == true);

		Position_0 position2 = world.getComponent(playerId, Position_0.class);
		assertTrue(position2 == null);
	}

	@Test
	public void testDynamicEntity() {
		// addEntities();
		Entity e = new Entity();
		e.addComponent(new Position_0(1, 2), new Velocity_0(4, 0), new Health_0(10, 15));
		world.addEntity(e);

		Position_0 position = world.getComponent(e.getId(), Position_0.class);
		assertTrue(position != null);

		assertTrue(ms.hasEntity(e.getId()));
		assertTrue(hs.hasEntity(e.getId()));

		Health_0 health = world.getComponent(e.getId(), Health_0.class);
		e.removeComponent(health);

		Health_0 health2 = world.getComponent(e.getId(), Health_0.class);
		assertTrue(health2 == null);
		assertFalse(hs.hasEntity(e.getId()));
	}

	@Test
	public void testDynamicEntity2() {
		// addEntities();
		Entity e = new Entity();
		e.addComponent(new Position_0(1, 2));
		e.addComponent(new Velocity_0(1, 2));
		world.addEntity(e);

		Position_0 position = world.getComponent(e.getId(), Position_0.class);
		assertTrue(position != null);
		assertTrue(ms.hasEntity(e.getId()));
	}
/*
	@Test
	public void testLotsOfComponents() {
		addEntities();
		world.registerComponents(COMPONENTS1);
		world.registerComponents(COMPONENTS2);
		world.registerComponents(COMPONENTS3);
		world.registerComponents(COMPONENTS4);
		world.registerComponents(COMPONENTS5);
		world.registerComponents(COMPONENTS6);
		world.registerComponents(COMPONENTS7);

		Copy_7_of_Gravity gravity7 = new Copy_7_of_Gravity(1f);
		Copy_5_of_Velocity velocity5 = new Copy_5_of_Velocity(4, 0);

		Entity e = new Entity();
		e.addComponent(new Position(1, 2), new Velocity(4, 0), new Health(10, 15), new Attack(2), new Gravity(1f), new CopyOfPosition(1, 2), new CopyOfVelocity(4, 0), new CopyOfHealth(10, 15), new CopyOfAttack(2), new CopyOfGravity(1f), new Copy_2_of_Position(1, 2), new Copy_2_of_Velocity(4, 0), new Copy_2_of_Health(10, 15), new Copy_2_of_Attack(2), new Copy_2_of_Gravity(1f), new Copy_3_of_Position(1, 2), new Copy_3_of_Velocity(4, 0), new Copy_3_of_Health(10, 15), new Copy_3_of_Attack(2), new Copy_3_of_Gravity(1f), new Copy_4_of_Position(1, 2), new Copy_4_of_Velocity(4, 0), new Copy_4_of_Health(10, 15), new Copy_4_of_Attack(2), new Copy_4_of_Gravity(1f), new Copy_5_of_Position(1, 2), velocity5, new Copy_5_of_Health(10, 15), new Copy_5_of_Attack(2), new Copy_5_of_Gravity(1f), new Copy_6_of_Position(1, 2), new Copy_6_of_Velocity(4, 0), new Copy_6_of_Health(10, 15), new Copy_6_of_Attack(2), new Copy_6_of_Gravity(1f), new Copy_7_of_Position(1, 2), new Copy_7_of_Velocity(4, 0), new Copy_7_of_Health(10, 15), new Copy_7_of_Attack(2), gravity7);
		world.addEntity(e);

		Copy_7_of_Gravity gravity = world.getComponent(e.getId(), Copy_7_of_Gravity.class);
		assertTrue(gravity != null);

		Copy_5_of_Velocity velocity = world.getComponent(e.getId(), Copy_5_of_Velocity.class);
		assertTrue(velocity != null);
	}

	@Test
	public void testLotsOfComponentAddAfterCreate() {
		addEntities();
		world.registerComponents(COMPONENTS1);
		world.registerComponents(COMPONENTS2);
		world.registerComponents(COMPONENTS3);
		world.registerComponents(COMPONENTS4);
		world.registerComponents(COMPONENTS5);
		world.registerComponents(COMPONENTS6);
		world.registerComponents(COMPONENTS7);

		Copy_7_of_Gravity gravity7 = new Copy_7_of_Gravity(1f);
		Copy_5_of_Velocity velocity5 = new Copy_5_of_Velocity(4, 0);

		Entity e = new Entity();
		world.addEntity(e);

		e.addComponent(new Position(1, 2), new Velocity(4, 0), new Health(10, 15), new Attack(2), new Gravity(1f), new CopyOfPosition(1, 2), new CopyOfVelocity(4, 0), new CopyOfHealth(10, 15), new CopyOfAttack(2), new CopyOfGravity(1f), new Copy_2_of_Position(1, 2), new Copy_2_of_Velocity(4, 0), new Copy_2_of_Health(10, 15), new Copy_2_of_Attack(2), new Copy_2_of_Gravity(1f), new Copy_3_of_Position(1, 2), new Copy_3_of_Velocity(4, 0), new Copy_3_of_Health(10, 15), new Copy_3_of_Attack(2), new Copy_3_of_Gravity(1f), new Copy_4_of_Position(1, 2), new Copy_4_of_Velocity(4, 0), new Copy_4_of_Health(10, 15), new Copy_4_of_Attack(2), new Copy_4_of_Gravity(1f), new Copy_5_of_Position(1, 2), velocity5, new Copy_5_of_Health(10, 15), new Copy_5_of_Attack(2), new Copy_5_of_Gravity(1f), new Copy_6_of_Position(1, 2), new Copy_6_of_Velocity(4, 0), new Copy_6_of_Health(10, 15), new Copy_6_of_Attack(2), new Copy_6_of_Gravity(1f), new Copy_7_of_Position(1, 2), new Copy_7_of_Velocity(4, 0), new Copy_7_of_Health(10, 15), new Copy_7_of_Attack(2), gravity7);

		Copy_7_of_Gravity gravity = world.getComponent(e.getId(), Copy_7_of_Gravity.class);
		assertTrue(gravity != null);

		Copy_5_of_Velocity velocity = world.getComponent(e.getId(), Copy_5_of_Velocity.class);
		assertTrue(velocity != null);
	}
*/
	@SuppressWarnings("unchecked")
	@Test
	public void testLotsOfSystems() {
		for (int i = 0; i < 40; i++) {
			world.addSystem(new EntitySystem(Health_0.class) {

			});
		}
		addEntities();
		assertTrue(true);
	}

	@Test
	public void testSaveEntity() {
		Player_Test player = new Player_Test(3, 5);
		float x = player.position.x;
		float y = player.position.y;
		int health = player.health.amount -= 5;

		File playerFile = Saver.saveObject(player, new File("player"));

		Player_Test player2 = Saver.readObject(new Player_Test(), playerFile);

		assertTrue(player2.health.amount == health);
		assertTrue(player2.position.x == x);
		assertTrue(player2.position.y == y);
	}

	@Test
	public void testSaveLotsEntities() {
		final int amount = 500;
		Player_Test[] entities = new Player_Test[amount];
		for (int i = 0; i < amount; i++) {
			entities[i] = new Player_Test(i, i / 2f);
		}

		entities[0] = null;
		entities[2] = new Player_Test(40, 50);
		entities[5] = null;
		entities[6] = new Player_Test(60, 70);
		entities[12] = null;

		File entitiesFile = Saver.saveObject(new PlayerWrapper(entities), new File("entities"));
		Player_Test[] entities2 = Saver.readObject(new PlayerWrapper(null), entitiesFile).entities;

		assertTrue(entities2[2].position.x == 40);
		assertTrue(entities2[6].position.x == 60);
	}

	private static class PlayerWrapper {
		Player_Test[] entities;
		public PlayerWrapper(Player_Test[] entities) {
			this.entities = entities;
		}
	}

	@After
	public void breakDown() {
		world.reset();
		System.out.println("finished test");
	}
}
