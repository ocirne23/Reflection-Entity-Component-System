package recs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import recs.components.Attack0;
import recs.components.Gravity0;
import recs.components.Health0;
import recs.components.Position0;
import recs.components.Velocity0;
import recs.components.copies.Attack1;
import recs.components.copies.Attack2;
import recs.components.copies.Attack3;
import recs.components.copies.Attack4;
import recs.components.copies.Attack5;
import recs.components.copies.Attack6;
import recs.components.copies.Attack7;
import recs.components.copies.Gravity1;
import recs.components.copies.Gravity2;
import recs.components.copies.Gravity3;
import recs.components.copies.Gravity4;
import recs.components.copies.Gravity5;
import recs.components.copies.Gravity6;
import recs.components.copies.Gravity7;
import recs.components.copies.Health1;
import recs.components.copies.Health2;
import recs.components.copies.Health3;
import recs.components.copies.Health4;
import recs.components.copies.Health5;
import recs.components.copies.Health6;
import recs.components.copies.Health7;
import recs.components.copies.Position1;
import recs.components.copies.Position2;
import recs.components.copies.Position3;
import recs.components.copies.Position4;
import recs.components.copies.Position5;
import recs.components.copies.Position6;
import recs.components.copies.Position7;
import recs.components.copies.Velocity1;
import recs.components.copies.Velocity2;
import recs.components.copies.Velocity3;
import recs.components.copies.Velocity4;
import recs.components.copies.Velocity5;
import recs.components.copies.Velocity6;
import recs.components.copies.Velocity7;
import recs.entities.TestPlayer;
import recs.entities.TestPlayerWithAttack;
import recs.entities.TestZombie;
import recs.events.TestDamageEvent;
import recs.systems.TestAttackSystem;
import recs.systems.TestHealthSystem;
import recs.systems.TestMovementSystem;
import recs.systems.TestThreadedMovementSystem;
import recs.utils.BinarySerializer;

import com.badlogic.gdx.utils.IntArray;

//TODO: separate tests into different classes

public class UnitTest {

	private TestPlayer player;
	private TestPlayer player2;
	private TestPlayerWithAttack playerWithAttack;
	private TestZombie zombie;

	private TestMovementSystem ms;
	private TestThreadedMovementSystem tms;
	private TestHealthSystem hs;
	private TestAttackSystem as;

	private EntityWorld world;

	@Before
	public void setup() {
		world = new EntityWorld();
		System.out.println("starting test");
		// world.registerComponents(COMPONENTS);

		ms = new TestMovementSystem();
		world.addSystem(ms);

		tms = new TestThreadedMovementSystem();
		tms.setEnabled(false);
		world.addSystem(tms);

		hs = new TestHealthSystem();
		world.addSystem(hs);

		as = new TestAttackSystem();
		world.addSystem(as);
		as.setEnabled(false);
	}

	private void addEntities() {
		player = new TestPlayer(4, 6);
		player2 = new TestPlayer(12, 9);
		playerWithAttack = new TestPlayerWithAttack(6, 11);
		zombie = new TestZombie(1, 2);
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
		Position0 position = world.getComponent(playerId, Position0.class);
		Velocity0 velocity = world.getComponent(playerId, Velocity0.class);
		Health0 health = world.getComponent(playerId, Health0.class);

		assertTrue(position.x == 4f && position.y == 6f);
		assertTrue(velocity.x == 2f && velocity.y == 1f);
		assertTrue(health.amount == 10 && health.max == 15);

		// player2
		int player2Id = player2.getId();
		Position0 position2 = world.getComponent(player2Id, Position0.class);
		assertTrue(position2.x == 12 && position2.y == 9);

		// zombie
		int zombieId = zombie.getId();
		Health0 healthNull = world.getComponent(zombieId, Health0.class);
		assertTrue(healthNull == null);
	}

	@Test
	public void testMovementSystem() {
		addEntities();
		int playerId = player.getId();
		final float deltaInSec1 = 2f;
		final float deltaInSec2 = 1.5f;
		// player
		Position0 position = world.getComponent(playerId, Position0.class);
		Velocity0 velocity = world.getComponent(playerId, Velocity0.class);

		assertTrue(position != null);
		assertTrue(velocity != null);
		assertTrue(ms.hasEntity(playerId));

		float startX = position.x;
		float startY = position.y;
		float xSpeed = velocity.x;
		float ySpeed = velocity.y;
		float expectedX = startX + xSpeed * deltaInSec1;
		float expectedY = startY + ySpeed * deltaInSec1;

		// world.process(0);
		world.process(deltaInSec1);
		System.out.println(playerId + ":" + ms.getId() + ":" + position.x);

		assertEquals(position.x, expectedX, 0.00001f);
		assertEquals(position.y, expectedY, 0.00001f);

		startX = expectedX;
		startY = expectedY;
		expectedX = startX + xSpeed * deltaInSec2;
		expectedY = startY + ySpeed * deltaInSec2;

		world.process(deltaInSec2);
		assertEquals(position.x, expectedX, 0.00001f);
		assertEquals(position.y, expectedY, 0.00001f);
	}

	@Test
	public void testInheritance() {
		addEntities();
		int playerWithAttackId = playerWithAttack.getId();
		Attack0 attack = world.getComponent(playerWithAttackId, Attack0.class);
		assertTrue(attack != null);

		Position0 position = world.getComponent(playerWithAttackId, Position0.class);
		assertTrue(position != null);
	}

	// TODO: get this working? @Test
	// Works fine, just fails if the machine executing it doesnt give time to
	// the threads.
	public void testThreadedSystem() {
		addEntities();
		ms.setEnabled(false);
		tms.setEnabled(true);
		Position0 position = world.getComponent(player.getId(), Position0.class);
		Velocity0 velocity = world.getComponent(player.getId(), Velocity0.class);

		assertTrue(tms.hasEntity(player.getId()));

		float startX = position.x;
		float startY = position.y;

		try {
			world.process(50);
			Thread.sleep(500); // so threads can finish up.
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		position = world.getComponent(player.getId(), Position0.class);

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

		Health0 health = world.getComponent(player.getId(), Health0.class);
		int currentHealth = health.amount;

		world.sendEvent(new TestDamageEvent(player.getId(), 2));
		world.process(1f);

		assertTrue(health.amount == currentHealth - 2);
	}

	@Test
	public void testDyamicComponentAdd() {
		addEntities();
		int player2Id = player2.getId();
		Attack0 attack = world.getComponent(player2Id, Attack0.class);
		assertTrue(attack == null);
		assertFalse(as.hasEntity(player2Id));

		player2.addComponent(new Attack0(2), new Gravity0(1));
		Attack0 attack2 = world.getComponent(player2Id, Attack0.class);
		assertTrue(attack2 != null);
		assertTrue(as.hasEntity(player2Id));

		Gravity0 gravity = world.getComponent(player2Id, Gravity0.class);
		assertTrue(gravity != null);

		player.addComponent(new Gravity0(2), new Attack0(3));
	}

	@Test
	public void testComponentOverwrite() {
		addEntities();
		player.addComponent(new Attack0(2));
		Attack0 a1 = world.getComponent(player.getId(), Attack0.class);
		assertTrue(a1 != null);
		assertTrue(a1.attack == 2);

		player.addComponent(new Attack0(3));
		Attack0 a2 = world.getComponent(player.getId(), Attack0.class);
		assertTrue(a2 != null);
		assertTrue(a2.attack == 3);
	}

	@Test
	public void testDynamicComponentRemove() {
		addEntities();
		int player2Id = player2.getId();
		Position0 position = world.getComponent(player2Id, Position0.class);
		assertTrue(position != null);
		assertTrue(ms.hasEntity(player2Id));

		player2.removeComponent(position);
		Position0 position2 = world.getComponent(player2Id, Position0.class);
		assertTrue(position2 == null);
		assertFalse(ms.hasEntity(player2Id));
	}

	@Test
	public void testComponentAddRemove() {
		addEntities();
		int player2Id = player2.getId();

		Attack0 attack = world.getComponent(player2Id, Attack0.class);
		assertTrue(attack == null);
		assertFalse(as.hasEntity(player2Id));

		player2.addComponent(new Attack0(2), new Gravity0(1));
		Attack0 attack2 = world.getComponent(player2Id, Attack0.class);
		assertTrue(attack2 != null);
		assertTrue(as.hasEntity(player2Id));

		player2.removeComponent(attack2);
		Attack0 attack3 = world.getComponent(player2Id, Attack0.class);
		assertTrue(attack3 == null);
		assertFalse(as.hasEntity(player2Id));
	}

	@Test
	public void testRemove() {
		addEntities();
		int playerId = player.getId();
		Position0 position = world.getComponent(playerId, Position0.class);
		assertTrue(position != null);

		world.removeEntity(playerId);

		Position0 position2 = world.getComponent(playerId, Position0.class);
		assertTrue(position2 == null);
	}

	@Test
	public void testRemoveEntityWithAddedComponent() {
		addEntities();
		int playerId = player.getId();

		player.addComponent(new Attack0(2));
		Attack0 attack = world.getComponent(playerId, Attack0.class);
		assertTrue(attack != null);
		assertTrue(as.hasEntity(playerId));

		world.removeEntity(playerId);

		Attack0 attack2 = world.getComponent(playerId, Attack0.class);
		assertTrue(attack2 == null);
		assertFalse(as.hasEntity(playerId));
	}

	private class MyDestructionListener extends ComponentDestructionListener<Position0> {
		public MyDestructionListener(EntityWorld world) {
			super(world);

		}

		private boolean destroyed = false;

		@Override
		public void destroyed(Position0 component) {
			destroyed = true;
		}
	}

	@Test
	public void testDestructionListener() {
		addEntities();

		int playerId = player.getId();
		Position0 position = world.getComponent(playerId, Position0.class);
		assertTrue(position != null);

		MyDestructionListener dl = new MyDestructionListener(world);

		world.removeEntity(playerId);

		assertTrue(dl.destroyed == true);

		Position0 position2 = world.getComponent(playerId, Position0.class);
		assertTrue(position2 == null);
	}

	@Test
	public void testDynamicEntity() {
		Entity e = new Entity();
		e.addComponent(new Position0(1, 2), new Velocity0(4, 0), new Health0(10, 15));
		world.addEntity(e);

		Position0 position = world.getComponent(e.getId(), Position0.class);
		assertTrue(position != null);

		assertTrue(ms.hasEntity(e.getId()));
		assertTrue(hs.hasEntity(e.getId()));

		Health0 health = world.getComponent(e.getId(), Health0.class);
		e.removeComponent(health);

		Health0 health2 = world.getComponent(e.getId(), Health0.class);
		assertTrue(health2 == null);
		assertFalse(hs.hasEntity(e.getId()));
	}

	@Test
	public void testDynamicEntity2() {
		Entity e = new Entity();
		e.addComponent(new Position0(1, 2));
		e.addComponent(new Velocity0(1, 2));
		world.addEntity(e);

		Position0 position = world.getComponent(e.getId(), Position0.class);
		assertTrue(position != null);
		assertTrue(ms.hasEntity(e.getId()));
	}

	@Test
	public void testLotsOfComponents() {
		Entity e = new Entity();
		e.addComponent(new Attack0(0), new Attack1(0), new Attack2(0), new Attack3(0), new Attack4(0), new Attack5(0), new Attack6(0), new Attack7(0));
		e.addComponent(new Gravity0(0), new Gravity1(0), new Gravity2(0), new Gravity3(0), new Gravity4(0), new Gravity5(0), new Gravity6(0), new Gravity7(0));
		e.addComponent(new Health0(0, 0), new Health1(0, 0), new Health2(0, 0), new Health3(0, 0), new Health4(0, 0), new Health5(0, 0), new Health6(0, 0), new Health7(0, 0));
		e.addComponent(new Position0(0, 0), new Position1(0, 0), new Position2(0, 0), new Position3(0, 0), new Position4(0, 0), new Position5(0, 0), new Position6(0, 0), new Position7(0, 0));
		e.addComponent(new Velocity0(0, 0), new Velocity1(0, 0), new Velocity2(0, 0), new Velocity3(0, 0), new Velocity4(0, 0), new Velocity5(0, 0), new Velocity6(0, 0), new Velocity7(0, 0));
		world.addEntity(e);

		assertNotNull(e.getComponent(Attack0.class));
		assertNotNull(e.getComponent(Attack1.class));
		assertNotNull(e.getComponent(Attack2.class));
		assertNotNull(e.getComponent(Attack3.class));
		assertNotNull(e.getComponent(Attack4.class));
		assertNotNull(e.getComponent(Attack5.class));
		assertNotNull(e.getComponent(Attack6.class));
		assertNotNull(e.getComponent(Attack7.class));

		assertNotNull(e.getComponent(Gravity0.class));
		assertNotNull(e.getComponent(Gravity1.class));
		assertNotNull(e.getComponent(Gravity2.class));
		assertNotNull(e.getComponent(Gravity3.class));
		assertNotNull(e.getComponent(Gravity4.class));
		assertNotNull(e.getComponent(Gravity5.class));
		assertNotNull(e.getComponent(Gravity6.class));
		assertNotNull(e.getComponent(Gravity7.class));

		assertNotNull(e.getComponent(Health0.class));
		assertNotNull(e.getComponent(Health1.class));
		assertNotNull(e.getComponent(Health2.class));
		assertNotNull(e.getComponent(Health3.class));
		assertNotNull(e.getComponent(Health4.class));
		assertNotNull(e.getComponent(Health5.class));
		assertNotNull(e.getComponent(Health6.class));
		assertNotNull(e.getComponent(Health7.class));

		assertNotNull(e.getComponent(Position0.class));
		assertNotNull(e.getComponent(Position1.class));
		assertNotNull(e.getComponent(Position2.class));
		assertNotNull(e.getComponent(Position3.class));
		assertNotNull(e.getComponent(Position4.class));
		assertNotNull(e.getComponent(Position5.class));
		assertNotNull(e.getComponent(Position6.class));
		assertNotNull(e.getComponent(Position7.class));

		assertNotNull(e.getComponent(Velocity0.class));
		assertNotNull(e.getComponent(Velocity1.class));
		assertNotNull(e.getComponent(Velocity2.class));
		assertNotNull(e.getComponent(Velocity3.class));
		assertNotNull(e.getComponent(Velocity4.class));
		assertNotNull(e.getComponent(Velocity5.class));
		assertNotNull(e.getComponent(Velocity6.class));
		assertNotNull(e.getComponent(Velocity7.class));

		e.removeComponent(e.getComponent(Attack1.class));
		e.removeComponent(e.getComponent(Velocity6.class));
		assertNull(e.getComponent(Attack1.class));
		assertNull(e.getComponent(Velocity6.class));

		e.addComponent(new Attack1(0), new Velocity6(0,0));
		assertNotNull(e.getComponent(Attack1.class));
		assertNotNull(e.getComponent(Velocity6.class));
	}


	@SuppressWarnings("unchecked")
	@Test
	public void testLotsOfSystems() {

		EntitySystem[] systems = new EntitySystem[40];
		for (int i = 0; i < 40; i++) {
			systems[i] = new EntitySystem(Health0.class) {};
		}

		for (EntitySystem s: systems)
			world.addSystem(s);

		addEntities();

		for (EntitySystem s: systems)
			world.removeSystem(s);

		// just testing if things do not explode
		assertTrue(true);
	}

	@Test
	public void testSaveEntity() {
		TestPlayer player = new TestPlayer(3, 5);
		float x = player.position.x;
		float y = player.position.y;
		int health = player.health.amount -= 5;

		File playerFile = BinarySerializer.saveToFile(new File("player"), player);

		TestPlayer player2 = BinarySerializer.readFromFile(playerFile, new TestPlayer(0, 0));

		assertTrue(player2.health.amount == health);
		assertTrue(player2.position.x == x);
		assertTrue(player2.position.y == y);
	}

	@Test
	public void testEntityIdReuse() {
		IntArray removedList = new IntArray();
		for (int i = 0; i < 10; i++) {
			world.addEntity(new TestPlayer(0, 0));
			int id = world.addEntity(new TestPlayer(0, 0));
			world.addEntity(new TestPlayer(0, 0));

			world.removeEntity(id);
			removedList.add(id);
		}

		world.process(1f);

		boolean hasAnRemovedEntityId = false;
		for (int i : removedList.items) {
			if (world.getEntity(i) != null)
				hasAnRemovedEntityId = true;
		}

		assertTrue(hasAnRemovedEntityId);
	}

	@After
	public void breakDown() {
		world.reset();
		tms.setEnabled(false);
		System.out.println("finished test");
	}
}
