package recs;

import org.junit.Before;
import org.junit.Test;
import recs.components.Attack_0;
import recs.components.Velocity_0;

import static org.junit.Assert.*;

public class EntityTest {

	EntityWorld world;
	Entity entity;

	@Before
	public void setup() {
		world = new EntityWorld();
		entity = new Entity();
	}

	@Test
	public void testEntityDefaultsNoWorld() {
		assertNotNull(entity.getComponents());
		assertEquals(0, entity.getComponents().length);
		assertEquals(0, entity.getId());
		assertTrue(throwsIllegalStateExceptionForGetComponentIds());
		assertNull(entity.getComponent(Component.class));
	}

	private boolean throwsIllegalStateExceptionForGetComponentIds() {
		try {
			entity.getComponentIds();
		} catch (IllegalStateException e) {
			return true;
		}
		return false;
	}

	@Test
	public void testAddComponentNoWorld() {
		Attack_0 attack = new Attack_0(5);
		entity.addComponent(attack);
		assertTrue(entity.hasComponent(Attack_0.class));
		assertEquals(attack, entity.getComponent(Attack_0.class));
	}

	@Test
	public void testRemoveComponentNoWorld() {
		Attack_0 attack = new Attack_0(5);
		entity.addComponent(attack);
		entity.removeComponent(attack);
		assertFalse(entity.hasComponent(Attack_0.class));
		assertNull(entity.getComponent(Attack_0.class));
	}

	@Test
	public void testAddMultipleComponentNoWorld() {
		Attack_0 attack = new Attack_0(5);
		Velocity_0 velocity = new Velocity_0();
		entity.addComponent(attack, velocity);
		assertTrue(entity.hasComponent(Attack_0.class));
		assertTrue(entity.hasComponent(Velocity_0.class));
		assertEquals(attack, entity.getComponent(Attack_0.class));
		assertEquals(velocity, entity.getComponent(Velocity_0.class));
	}

	@Test
	public void testRemoveMultipleComponentNoWorld() {
		Attack_0 attack = new Attack_0(5);
		Velocity_0 velocity = new Velocity_0();
		entity.addComponent(attack, velocity);
		entity.removeComponent(attack, velocity);
		assertFalse(entity.hasComponent(Attack_0.class));
		assertFalse(entity.hasComponent(Velocity_0.class));
		assertNull(entity.getComponent(Attack_0.class));
		assertNull(entity.getComponent(Velocity_0.class));
	}

	@Test
	public void testRemoveOneOfMultipleComponentNoWorld() {
		Attack_0 attack = new Attack_0(5);
		Velocity_0 velocity = new Velocity_0();
		entity.addComponent(attack, velocity);
		entity.removeComponent(attack);
		assertFalse(entity.hasComponent(Attack_0.class));
		assertTrue(entity.hasComponent(Velocity_0.class));
		assertNull(entity.getComponent(Attack_0.class));
		assertNotNull(entity.getComponent(Velocity_0.class));
	}
}
