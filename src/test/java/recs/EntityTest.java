package recs;

import org.junit.Before;
import org.junit.Test;
import recs.components.Attack0;
import recs.components.Velocity0;

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
		Attack0 attack = new Attack0(5);
		entity.addComponent(attack);
		assertTrue(entity.hasComponent(Attack0.class));
		assertEquals(attack, entity.getComponent(Attack0.class));
	}

	@Test
	public void testRemoveComponentNoWorld() {
		Attack0 attack = new Attack0(5);
		entity.addComponent(attack);
		entity.removeComponent(attack);
		assertFalse(entity.hasComponent(Attack0.class));
		assertNull(entity.getComponent(Attack0.class));
	}

	@Test
	public void testAddMultipleComponentNoWorld() {
		Attack0 attack = new Attack0(5);
		Velocity0 velocity = new Velocity0();
		entity.addComponent(attack, velocity);
		assertTrue(entity.hasComponent(Attack0.class));
		assertTrue(entity.hasComponent(Velocity0.class));
		assertEquals(attack, entity.getComponent(Attack0.class));
		assertEquals(velocity, entity.getComponent(Velocity0.class));
	}

	@Test
	public void testRemoveMultipleComponentNoWorld() {
		Attack0 attack = new Attack0(5);
		Velocity0 velocity = new Velocity0();
		entity.addComponent(attack, velocity);
		entity.removeComponent(attack, velocity);
		assertFalse(entity.hasComponent(Attack0.class));
		assertFalse(entity.hasComponent(Velocity0.class));
		assertNull(entity.getComponent(Attack0.class));
		assertNull(entity.getComponent(Velocity0.class));
	}

	@Test
	public void testRemoveOneOfMultipleComponentNoWorld() {
		Attack0 attack = new Attack0(5);
		Velocity0 velocity = new Velocity0();
		entity.addComponent(attack, velocity);
		entity.removeComponent(attack);
		assertFalse(entity.hasComponent(Attack0.class));
		assertTrue(entity.hasComponent(Velocity0.class));
		assertNull(entity.getComponent(Attack0.class));
		assertNotNull(entity.getComponent(Velocity0.class));
	}
}
